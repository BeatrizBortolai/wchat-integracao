package com.example.wchat.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.AuthIntegrationRepository
import com.example.wchat.data.repository.FirebaseAuthRepository
import com.example.wchat.data.repository.UsuarioApiRepository
import com.example.wchat.data.remote.mapper.toModel
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

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

    private val firebaseAuthRepository = FirebaseAuthRepository()

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

            firebaseAuthRepository.signIn(uiState.email, uiState.senha)
                .onSuccess { firebaseUser ->
                    val authIntegrationRepository = AuthIntegrationRepository(context.applicationContext)
                    val nomeFirebase = firebaseUser.displayName
                        ?: firebaseUser.email?.substringBefore("@")
                        ?: "Usuário"
                    val emailFirebase = firebaseUser.email ?: uiState.email

                    val syncResult = authIntegrationRepository.syncAuthenticatedFirebaseUser(
                        nome = nomeFirebase,
                        email = emailFirebase,
                        password = uiState.senha,
                        tipo = tipoUsuarioNaTela.name,
                        cargo = null,
                        segmentos = null
                    )

                    syncResult
                        .onSuccess { authSession ->
                            authIntegrationRepository.sendFcmTokenToBackend()

                            val usuarioBackendResult = UsuarioApiRepository(context.applicationContext)
                                .buscarPorId(authSession.usuarioId)

                            usuarioBackendResult
                                .onSuccess { usuarioDto ->
                                    val usuarioLogado = usuarioDto.toModel()

                                    if (usuarioLogado.tipo != tipoUsuarioNaTela) {
                                        firebaseAuthRepository.signOut()
                                        uiState = uiState.copy(isLoading = false)
                                        _evento.emit(
                                            LoginEvento.Erro(
                                                "Login falhou. Verifique o tipo de usuário selecionado (Cliente/Operador)."
                                            )
                                        )
                                        return@launch
                                    }

                                    uiState = uiState.copy(isLoading = false)
                                    _evento.emit(LoginEvento.Sucesso(usuarioLogado))
                                }
                                .onFailure { erro ->
                                    uiState = uiState.copy(isLoading = false)
                                    _evento.emit(
                                        LoginEvento.Erro(
                                            erro.message ?: "Login feito, mas falhou ao carregar usuário no backend."
                                        )
                                    )
                                }
                        }
                        .onFailure { erro ->
                            uiState = uiState.copy(isLoading = false)
                            _evento.emit(
                                LoginEvento.Erro(
                                    erro.message ?: "Login feito, mas falhou ao sincronizar com o backend."
                                )
                            )
                        }
                }
                .onFailure { exception ->
                    uiState = uiState.copy(isLoading = false)
                    val mensagemErro = when {
                        "INVALID_LOGIN_CREDENTIALS" in (exception.message ?: "") -> "E-mail ou senha inválidos."
                        else -> exception.message ?: "Falha no login. Tente novamente."
                    }
                    _evento.emit(LoginEvento.Erro(mensagemErro))
                }
        }
    }
}