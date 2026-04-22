package com.example.wchat.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.UsuarioRepository
import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoSegmento
import com.example.wchat.model.TipoUsuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class CadastroUiState(
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val confirmarSenha: String = "",
    val grupoSelecionado: TipoGrupo? = null,
    val segmentosSelecionados: List<TipoSegmento> = emptyList(),
    val isLoading: Boolean = false
)

sealed class CadastroEvento {
    data class Sucesso(val mensagem: String) : CadastroEvento()
    data class Erro(val mensagem: String) : CadastroEvento()
}

class CadastroViewModel : ViewModel() {
    var uiState by mutableStateOf(CadastroUiState())
        private set

    private val _evento = MutableSharedFlow<CadastroEvento>()
    val evento = _evento.asSharedFlow()

    fun onNomeChange(novoNome: String) {
        uiState = uiState.copy(nome = novoNome)
    }

    fun onEmailChange(novoEmail: String) {
        uiState = uiState.copy(email = novoEmail)
    }

    fun onSenhaChange(novaSenha: String) {
        uiState = uiState.copy(senha = novaSenha)
    }

    fun onConfirmarSenhaChange(novaConfirmacao: String) {
        uiState = uiState.copy(confirmarSenha = novaConfirmacao)
    }

    fun onGrupoSelecionado(grupo: TipoGrupo) {
        uiState = uiState.copy(grupoSelecionado = grupo)
    }

    fun onLimparGrupo() {
        uiState = uiState.copy(grupoSelecionado = null)
    }

    fun onSegmentoClicado(segmento: TipoSegmento) {
        val segmentosAtuais = uiState.segmentosSelecionados
        val novaLista = if (segmentosAtuais.contains(segmento)) {
            segmentosAtuais - segmento
        } else {
            segmentosAtuais + segmento
        }
        uiState = uiState.copy(segmentosSelecionados = novaLista)
    }

    fun registrar(tipoUsuario: TipoUsuario) {
        viewModelScope.launch {
            if (!validarCampos(tipoUsuario)) return@launch

            uiState = uiState.copy(isLoading = true)

            val resultado = UsuarioRepository().registrarUsuario(
                nome = uiState.nome,
                email = uiState.email,
                password = uiState.senha,
                tipo = tipoUsuario,
                cargo = uiState.grupoSelecionado?.name,
                segmentos = uiState.segmentosSelecionados.map { it.name }
            )

            uiState = uiState.copy(isLoading = false)

            resultado.onSuccess {
                _evento.emit(CadastroEvento.Sucesso("Cadastro realizado!"))
            }
            resultado.onFailure { exception ->
                _evento.emit(CadastroEvento.Erro(exception.message ?: "Falha desconhecida no cadastro"))
            }
        }
    }

    private suspend fun validarCampos(tipoUsuario: TipoUsuario): Boolean {
        val nomeValido = uiState.nome.isNotBlank()
        val emailValido = uiState.email.isNotBlank() && "@" in uiState.email
        val senhasCoincidem = uiState.senha.isNotBlank() && uiState.senha == uiState.confirmarSenha

        val associacaoValida = if (tipoUsuario == TipoUsuario.CLIENTE) {
            uiState.grupoSelecionado != null || uiState.segmentosSelecionados.isNotEmpty()
        } else {
            true
        }

        val mensagemDeErro = when {
            !nomeValido -> "O campo Nome não pode estar em branco."
            !emailValido -> "O campo E-mail é inválido."
            !associacaoValida -> "Como cliente, é obrigatório selecionar um Grupo ou Segmento."
            !senhasCoincidem -> "As senhas não coincidem ou estão em branco."
            else -> null
        }

        return if (mensagemDeErro != null) {
            _evento.emit(CadastroEvento.Erro(mensagemDeErro))
            false
        } else {
            true
        }
    }
}