package com.example.wchat.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.UsuarioRepository
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.example.wchat.data.repository.AuthIntegrationRepository

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val isLoading: Boolean = false
)

sealed class LoginEvento {
    data class Sucesso(val usuario: Usuario) : LoginEvento()
    data class Erro(val mensagem: String) : LoginEvento()
}

class LoginViewModel : ViewModel() {

    private val repository = UsuarioRepository()

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _evento = MutableSharedFlow<LoginEvento>()
    val evento = _evento.asSharedFlow()


    fun onEmailChange(novoEmail: String) {
        uiState = uiState.copy(email = novoEmail)
    }

    fun onSenhaChange(novaSenha: String) {
        uiState = uiState.copy(senha = novaSenha)
    }

    fun login(tipoUsuarioNaTela: TipoUsuario, context: Context) {
        viewModelScope.launch {
            if (uiState.email.isBlank() || uiState.senha.isBlank()) {
                _evento.emit(LoginEvento.Erro("E-mail e senha são obrigatórios."))
                return@launch
            }

            uiState = uiState.copy(isLoading = true)

            val resultado = repository.loginUsuario(uiState.email, uiState.senha)

            resultado.onSuccess { usuarioLogado ->
                if (usuarioLogado.tipo == tipoUsuarioNaTela) {
                    val authIntegrationRepository = AuthIntegrationRepository(context)

                    val syncResult = authIntegrationRepository.syncAuthenticatedFirebaseUser(
                        nome = usuarioLogado.nome,
                        email = usuarioLogado.email,
                        password = uiState.senha,
                        tipo = usuarioLogado.tipo.name,
                        cargo = usuarioLogado.cargo,
                        segmentos = usuarioLogado.segmentos
                    )

                    if (syncResult.isSuccess) {
                        authIntegrationRepository.sendFcmTokenToBackend()
                        uiState = uiState.copy(isLoading = false)
                        _evento.emit(LoginEvento.Sucesso(usuarioLogado))
                    } else {
                        uiState = uiState.copy(isLoading = false)
                        _evento.emit(
                            LoginEvento.Erro(
                                syncResult.exceptionOrNull()?.message
                                    ?: "Login feito, mas falhou ao sincronizar com o backend."
                            )
                        )
                    }
                } else {
                    uiState = uiState.copy(isLoading = false)
                    _evento.emit(LoginEvento.Erro("Login falhou. Verifique o tipo de usuário selecionado (Cliente/Operador)."))
                }
            }

            resultado.onFailure { exception ->
                uiState = uiState.copy(isLoading = false)
                val mensagemErro = when {
                    "INVALID_LOGIN_CREDENTIALS" in (exception.message ?: "") -> "E-mail ou senha inválidos."
                    else -> "Falha no login. Tente novamente."
                }
                _evento.emit(LoginEvento.Erro(mensagemErro))
            }
        }
    }
}