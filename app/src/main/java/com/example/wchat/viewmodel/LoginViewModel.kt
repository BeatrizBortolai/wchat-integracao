package com.example.wchat.viewmodel

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.wchat.data.UsuarioRepository
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        if (novaSenha.length <= 8) {
            uiState = uiState.copy(senha = novaSenha)
        }
    }

    fun login(tipoUsuarioNaTela: TipoUsuario) {
        viewModelScope.launch {
            if (uiState.email.isBlank() || uiState.senha.isBlank()) {
                _evento.emit(LoginEvento.Erro("E-mail e senha são obrigatórios."))
                return@launch
            }

            uiState = uiState.copy(isLoading = true)

            val resultado = repository.loginUsuario(uiState.email, uiState.senha)

            uiState = uiState.copy(isLoading = false)

            resultado.onSuccess { usuarioLogado ->
                if (usuarioLogado.tipo == tipoUsuarioNaTela) {
                    updateFcmTokenAfterLogin()
                    _evento.emit(LoginEvento.Sucesso(usuarioLogado))
                } else {
                    _evento.emit(LoginEvento.Erro("Login falhou. Verifique o tipo de usuário selecionado (Cliente/Operador)."))
                }
            }

            resultado.onFailure { exception ->
                val mensagemErro = when {
                    "INVALID_LOGIN_CREDENTIALS" in (exception.message ?: "") -> "E-mail ou senha inválidos."
                    else -> "Falha no login. Tente novamente."
                }
                _evento.emit(LoginEvento.Erro(mensagemErro))
            }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun updateFcmTokenAfterLogin() {
        val userId = Firebase.auth.currentUser?.uid ?: return

        try {
            val token = Firebase.messaging.token.await()

            Firebase.firestore.collection("usuarios").document(userId)
                .update("fcmToken", token)
                .await()

            Log.d("FCM_TOKEN", "Token salvo com sucesso após o login para o usuário: $userId")
        } catch (e: Exception) {
            Log.e("FCM_TOKEN", "Falha ao salvar o token após o login para o usuário: $userId", e)
        }
    }
}