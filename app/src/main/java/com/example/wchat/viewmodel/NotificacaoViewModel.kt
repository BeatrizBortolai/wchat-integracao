package com.example.wchat.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.GrupoRepository
import com.example.wchat.data.NotificacaoRepository
import com.example.wchat.data.SegmentoRepository
import com.example.wchat.data.UsuarioRepository
import com.example.wchat.model.Grupo
import com.example.wchat.model.Notificacao
import com.example.wchat.model.Segmento
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

class NotificacaoViewModel : ViewModel() {
    private val grupoRepository = GrupoRepository()
    private val segmentoRepository = SegmentoRepository()
    private val usuarioRepository = UsuarioRepository()
    private val notificacaoRepository = NotificacaoRepository()

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
            _destinatariosState.value = SelecionarDestinatariosState()
            combine(
                grupoRepository.getGruposEmTempoReal(),
                segmentoRepository.getSegmentosEmTempoReal(),
                usuarioRepository.getTodosUsuariosFlow()
            ) { resultGrupos, resultSegmentos, resultUsuarios ->

                val clientes = resultUsuarios.getOrElse { emptyList() }
                    .filter { it.tipo == TipoUsuario.CLIENTE }

                _destinatariosState.value.copy(
                    grupos = resultGrupos.getOrElse { emptyList() },
                    segmentos = resultSegmentos.getOrElse { emptyList() },
                    usuarios = clientes,
                    isLoading = false
                )
            }.catch { e ->
                _destinatariosState.value = _destinatariosState.value.copy(isLoading = false, error = "Erro ao carregar dados: ${e.message}")
            }.collect { combinedState ->
                _destinatariosState.value = combinedState
            }
        }
    }

    fun enviarNotificacaoComoMensagem(notificacao: Notificacao) {
        viewModelScope.launch {
            val destinatariosParaEnviar = _destinatariosState.value.destinatariosSelecionados.toList()
            if (destinatariosParaEnviar.isEmpty()){
                _evento.emit(NotificacaoEvento.Erro("Selecione ao menos um destinatário."))
                return@launch
            }

            _destinatariosState.value = _destinatariosState.value.copy(isEnviando = true)

            val idEnvio = java.util.UUID.randomUUID().toString()
            val mensagemFormatada = buildString {
                append("[NOTIFICATION]\n")
                append("titulo=${notificacao.titulo}\n")
                append("descricao=${notificacao.descricao}\n")
                if (notificacao.linkEvento.isNotBlank()) append("linkEvento=${notificacao.linkEvento}\n")
                if (notificacao.urlSaberMais.isNotBlank()) append("urlSaberMais=${notificacao.urlSaberMais}\n")
                if (notificacao.urlInscrever.isNotBlank()) append("urlInscrever=${notificacao.urlInscrever}\n")
            }

            try {
                notificacaoRepository.enviarNotificacao(
                    destinatarios = destinatariosParaEnviar,
                    mensagem = mensagemFormatada.trim(),
                    idEnvio = idEnvio
                )
                _destinatariosState.value = _destinatariosState.value.copy(isEnviando = false, envioConcluido = true)
                _evento.emit(NotificacaoEvento.SucessoEnvio("Notificação enviada com sucesso!"))
            } catch (e: Exception) {
                _destinatariosState.value = _destinatariosState.value.copy(isEnviando = false, error = "Falha ao enviar: ${e.message}")
            }
        }
    }

    fun resetarEstadoDeEnvio() {
        _destinatariosState.value = _destinatariosState.value.copy(envioConcluido = false, error = null)
    }
}