package com.example.wchat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.FirebaseAuthRepository
import com.example.wchat.data.repository.UsuarioApiRepository
import com.example.wchat.data.repository.toModel
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UpdateStatus { IDLE, SUCCESS, ERROR }

class EditarPerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioApiRepository = UsuarioApiRepository(application.applicationContext)
    private val firebaseAuthRepository = FirebaseAuthRepository()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario = _usuario.asStateFlow()

    private val _updateStatus = MutableStateFlow(UpdateStatus.IDLE)
    val updateStatus = _updateStatus.asStateFlow()

    init {
        carregarUsuarioLogado()
    }

    private fun carregarUsuarioLogado() {
        val usuarioId = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            usuarioApiRepository.buscarPorId(usuarioId)
                .onSuccess { dto -> _usuario.value = dto.toModel() }
                .onFailure { _updateStatus.value = UpdateStatus.ERROR }
        }
    }

    fun salvarAlteracoes(novoNome: String) {
        val usuarioAtual = _usuario.value ?: return
        val nomeMudou = novoNome.isNotBlank() && novoNome != usuarioAtual.nome
        if (!nomeMudou) return

        viewModelScope.launch {
            val backendResult = usuarioApiRepository.atualizarUsuario(usuarioAtual.id, nome = novoNome)
            val firebaseResult = firebaseAuthRepository.updateDisplayName(novoNome)

            if (backendResult.isSuccess && firebaseResult.isSuccess) {
                _usuario.update { currentUser -> currentUser?.copy(nome = novoNome) }
                _updateStatus.value = UpdateStatus.SUCCESS
            } else {
                _updateStatus.value = UpdateStatus.ERROR
            }
        }
    }

    fun atualizarSenha(novaSenha: String, senhaAtual: String) {
        if (novaSenha.isBlank() || senhaAtual.isBlank()) return

        viewModelScope.launch {
            val resultado = firebaseAuthRepository.updatePassword(novaSenha, senhaAtual)
            _updateStatus.value = if (resultado.isSuccess) UpdateStatus.SUCCESS else UpdateStatus.ERROR
        }
    }

    fun deletarConta(senhaAtual: String, context: android.content.Context) {
        if (senhaAtual.isBlank()) {
            _updateStatus.value = UpdateStatus.ERROR
            return
        }

        viewModelScope.launch {
            val usuarioId = Firebase.auth.currentUser?.uid
            if (usuarioId.isNullOrBlank()) {
                _updateStatus.value = UpdateStatus.ERROR
                return@launch
            }

            // 1) Primeiro valida a senha no Firebase, sem alterar o backend.
            val reauthResult = firebaseAuthRepository.reauthenticateCurrentUser(senhaAtual)
            if (reauthResult.isFailure) {
                _updateStatus.value = UpdateStatus.ERROR
                return@launch
            }

            // 2) Só depois remove o usuário no backend, incluindo vínculos de grupos/segmentos.
            val deleteBackendResult = usuarioApiRepository.deletarUsuario(usuarioId)
            if (deleteBackendResult.isFailure) {
                _updateStatus.value = UpdateStatus.ERROR
                return@launch
            }

            // 3) Por fim remove a conta Firebase usando a reautenticação recente.
            val deleteFirebaseResult = firebaseAuthRepository.deleteCurrentUserAfterRecentLogin()
            _updateStatus.value = if (deleteFirebaseResult.isSuccess) UpdateStatus.SUCCESS else UpdateStatus.ERROR
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.IDLE
    }
}