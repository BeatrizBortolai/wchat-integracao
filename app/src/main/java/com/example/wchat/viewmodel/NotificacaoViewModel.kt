package com.example.wchat.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.BackendCatalogRepository
import com.example.wchat.data.repository.BackendNotificacaoRepository
import com.example.wchat.data.repository.UsuarioApiRepository
import com.example.wchat.model.Grupo
import com.example.wchat.model.Notificacao
import com.example.wchat.model.Segmento
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class CriarNotificacaoFormState(
    val titulo: String = "",
    val descricao: String = "",
    val nomeCampanha: String = "",
    val linkEvento: String = "",
    val urlSaberMais: String = "",
    val urlInscrever: String = ""
)

data class SelecionarDestinatariosState(
    val grupos: List<Grupo> = emptyList(),
    val segmentos: List<Segmento> = emptyList(),
    val usuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = true,
    val isEnviando: Boolean = false,
    val envioConcluido: Boolean = false,
    val error: String? = null,
    val textoBusca: String = "",
    val destinatariosSelecionados: Set<Any> = emptySet()
)

sealed class NotificacaoEvento {
    data class NavegarParaDestinatarios(val rota: String) : NotificacaoEvento()
    data class Erro(val mensagem: String) : NotificacaoEvento()
    data class SucessoEnvio(val mensagem: String) : NotificacaoEvento()
}

class NotificacaoViewModel(application: Application) : AndroidViewModel(application) {
    private val catalogRepository = BackendCatalogRepository(application.applicationContext)
    private val usuarioRepository = UsuarioApiRepository(application.applicationContext)
    private val notificacaoRepository = BackendNotificacaoRepository(application.applicationContext)

    var formState by mutableStateOf(CriarNotificacaoFormState())
        private set

    private val _destinatariosState = MutableStateFlow(SelecionarDestinatariosState())
    val destinatariosState = _destinatariosState.asStateFlow()

    private val _evento = MutableSharedFlow<NotificacaoEvento>()
    val evento = _evento.asSharedFlow()

    fun onTituloChange(novoTitulo: String) { formState = formState.copy(titulo = novoTitulo) }
    fun onDescricaoChange(novaDescricao: String) { formState = formState.copy(descricao = novaDescricao) }
    fun onNomeCampanhaChange(novoNome: String) { formState = formState.copy(nomeCampanha = novoNome) }
    fun onLinkEventoChange(novoLink: String) { formState = formState.copy(linkEvento = novoLink) }
    fun onUrlSaberMaisChange(novaUrl: String) { formState = formState.copy(urlSaberMais = novaUrl) }
    fun onUrlInscreverChange(novaUrl: String) { formState = formState.copy(urlInscrever = novaUrl) }

    fun onAvancarParaDestinatarios() {
        viewModelScope.launch {
            if (formState.titulo.isBlank() || formState.nomeCampanha.isBlank()) {
                _evento.emit(NotificacaoEvento.Erro("Título e Nome da Campanha são obrigatórios."))
                return@launch
            }

            val tituloNav = encode(formState.titulo)
            val campanhaNav = encode(formState.nomeCampanha)
            val descricaoNav = encode(formState.descricao)
            val linkEventoNav = encode(formState.linkEvento)
            val urlSaberMaisNav = encode(formState.urlSaberMais)
            val urlInscreverNav = encode(formState.urlInscrever)

            val rotaFinal = "selecionarDestinatarios/$tituloNav/$campanhaNav" +
                    "?descricao=$descricaoNav" +
                    "&linkEvento=$linkEventoNav" +
                    "&urlSaberMais=$urlSaberMaisNav" +
                    "&urlInscrever=$urlInscreverNav"

            _evento.emit(NotificacaoEvento.NavegarParaDestinatarios(rotaFinal))
        }
    }

    private fun encode(valor: String): String {
        return URLEncoder.encode(valor.trim().ifEmpty { " " }, StandardCharsets.UTF_8.name())
    }

    fun onBuscaChange(novoTexto: String) {
        _destinatariosState.value = _destinatariosState.value.copy(textoBusca = novoTexto)
    }

    fun onDestinatarioToggle(destinatario: Any) {
        val selecionadosAtuais = _destinatariosState.value.destinatariosSelecionados
        val novaSelecao = if (selecionadosAtuais.contains(destinatario)) {
            selecionadosAtuais - destinatario
        } else {
            selecionadosAtuais + destinatario
        }
        _destinatariosState.value = _destinatariosState.value.copy(destinatariosSelecionados = novaSelecao)
    }

    fun carregarDestinatarios() {
        viewModelScope.launch {
            _destinatariosState.value = _destinatariosState.value.copy(isLoading = true, error = null)

            val gruposResult = catalogRepository.listarGrupos()
            val segmentosResult = catalogRepository.listarSegmentos()
            val usuariosResult = usuarioRepository.listarUsuarios()

            val erro = listOf(gruposResult, segmentosResult, usuariosResult)
                .firstOrNull { it.isFailure }
                ?.exceptionOrNull()

            if (erro != null) {
                _destinatariosState.value = _destinatariosState.value.copy(
                    isLoading = false,
                    error = "Erro ao carregar dados do backend: ${erro.message}"
                )
                return@launch
            }

            _destinatariosState.value = _destinatariosState.value.copy(
                grupos = gruposResult.getOrDefault(emptyList()),
                segmentos = segmentosResult.getOrDefault(emptyList()),
                usuarios = usuariosResult.getOrDefault(emptyList()).filter { it.tipo == TipoUsuario.CLIENTE },
                isLoading = false
            )
        }
    }

    fun enviarNotificacaoComoMensagem(notificacao: Notificacao) {
        viewModelScope.launch {
            val destinatariosParaEnviar = _destinatariosState.value.destinatariosSelecionados.toList()
            if (destinatariosParaEnviar.isEmpty()) {
                _evento.emit(NotificacaoEvento.Erro("Selecione ao menos um destinatário."))
                return@launch
            }

            val remetenteId = Firebase.auth.currentUser?.uid
            if (remetenteId.isNullOrBlank()) {
                _evento.emit(NotificacaoEvento.Erro("Usuário autenticado não encontrado."))
                return@launch
            }

            _destinatariosState.value = _destinatariosState.value.copy(isEnviando = true, error = null)

            val falhas = mutableListOf<String>()
            var enviosComSucesso = 0

            suspend fun enviarERegistrarErro(
                descricaoDestino: String,
                bloco: suspend () -> Result<*>
            ) {
                bloco()
                    .onSuccess { enviosComSucesso++ }
                    .onFailure { erro ->
                        falhas += "$descricaoDestino: ${erro.message ?: "erro desconhecido"}"
                    }
            }

            val usuariosSelecionados = destinatariosParaEnviar
                .filterIsInstance<Usuario>()
                .map { it.id }
                .filter { it.isNotBlank() }
                .distinct()

            if (usuariosSelecionados.isNotEmpty()) {
                enviarERegistrarErro("usuários selecionados") {
                    notificacaoRepository.enviarCampanha(
                        titulo = notificacao.titulo,
                        descricao = notificacao.descricao,
                        nomeCampanha = notificacao.nomeCampanha,
                        remetenteId = remetenteId,
                        tipoDestinatario = "USUARIO",
                        destinatariosIds = usuariosSelecionados,
                        linkEvento = notificacao.linkEvento.ifBlank { null },
                        urlSaberMais = notificacao.urlSaberMais.ifBlank { null },
                        urlInscrever = notificacao.urlInscrever.ifBlank { null }
                    )
                }
            }

            destinatariosParaEnviar.filterIsInstance<Grupo>().forEach { grupo ->
                val grupoId = (grupo.tipo?.name ?: grupo.id).trim().uppercase()
                if (grupoId.isBlank()) {
                    falhas += "grupo inválido: identificador vazio"
                    return@forEach
                }

                enviarERegistrarErro("grupo $grupoId") {
                    notificacaoRepository.enviarCampanha(
                        titulo = notificacao.titulo,
                        descricao = notificacao.descricao,
                        nomeCampanha = notificacao.nomeCampanha,
                        remetenteId = remetenteId,
                        tipoDestinatario = "GRUPO",
                        grupo = grupoId,
                        linkEvento = notificacao.linkEvento.ifBlank { null },
                        urlSaberMais = notificacao.urlSaberMais.ifBlank { null },
                        urlInscrever = notificacao.urlInscrever.ifBlank { null }
                    )
                }
            }

            destinatariosParaEnviar.filterIsInstance<Segmento>().forEach { segmento ->
                val segmentoId = (segmento.tipo?.name ?: segmento.id).trim().uppercase()
                if (segmentoId.isBlank()) {
                    falhas += "segmento inválido: identificador vazio"
                    return@forEach
                }

                enviarERegistrarErro("segmento $segmentoId") {
                    notificacaoRepository.enviarCampanha(
                        titulo = notificacao.titulo,
                        descricao = notificacao.descricao,
                        nomeCampanha = notificacao.nomeCampanha,
                        remetenteId = remetenteId,
                        tipoDestinatario = "SEGMENTO",
                        segmento = segmentoId,
                        linkEvento = notificacao.linkEvento.ifBlank { null },
                        urlSaberMais = notificacao.urlSaberMais.ifBlank { null },
                        urlInscrever = notificacao.urlInscrever.ifBlank { null }
                    )
                }
            }

            if (enviosComSucesso > 0) {
                _destinatariosState.value = _destinatariosState.value.copy(
                    isEnviando = false,
                    envioConcluido = true,
                    error = null
                )

                val mensagem = if (falhas.isEmpty()) {
                    "Campanha enviada com sucesso!"
                } else {
                    "Campanha enviada parcialmente. Falhas: ${falhas.joinToString("; ")}"
                }

                _evento.emit(NotificacaoEvento.SucessoEnvio(mensagem))
            } else {
                val mensagemErro = "Falha ao enviar campanha: ${falhas.joinToString("; ").ifBlank { "nenhum envio realizado" }}"
                _destinatariosState.value = _destinatariosState.value.copy(
                    isEnviando = false,
                    error = mensagemErro
                )
                _evento.emit(NotificacaoEvento.Erro(mensagemErro))
            }
        }
    }

    fun resetarEstadoDeEnvio() {
        _destinatariosState.value = _destinatariosState.value.copy(envioConcluido = false, error = null)
    }
}
