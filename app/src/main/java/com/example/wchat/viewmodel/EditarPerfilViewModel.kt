package com.example.wchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.UsuarioRepository
import com.example.wchat.model.Usuario
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
enum class UpdateStatus { IDLE, SUCCESS, ERROR }

class EditarPerfilViewModel(
    private val usuarioRepository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

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
            val usuarioCarregado = usuarioRepository.getUsuarioPorId(usuarioId)
            _usuario.value = usuarioCarregado
        }
    }

    fun salvarAlteracoes(
        novoNome: String
    ) {
        val usuarioAtual = _usuario.value ?: return
        val nomeMudou = novoNome.isNotBlank() && novoNome != usuarioAtual.nome

        if (!nomeMudou) return

        viewModelScope.launch {
            val resultado = usuarioRepository.atualizarNomeUsuario(usuarioAtual.id, novoNome)

            if (resultado.isSuccess) {
                _usuario.update { currentUser ->
                    currentUser?.copy(nome = novoNome)
                }
                _updateStatus.value = UpdateStatus.SUCCESS
            } else {
                _updateStatus.value = UpdateStatus.ERROR
            }
        }
    }

    fun atualizarSenha(novaSenha: String, senhaAtual: String) {
        if (novaSenha.isBlank() || senhaAtual.isBlank()) return

        viewModelScope.launch {
            val resultado = usuarioRepository.atualizarSenhaUsuario(novaSenha, senhaAtual)
            if (resultado.isSuccess) {
                _updateStatus.value = UpdateStatus.SUCCESS
            } else {
                _updateStatus.value = UpdateStatus.ERROR
            }
        }
    }

    fun deletarConta(senhaAtual: String) {
        if (senhaAtual.isBlank()) {
            _updateStatus.value = UpdateStatus.ERROR
            return
        }

        viewModelScope.launch {
            val resultado = usuarioRepository.deletarUsuario(senhaAtual)
            if (resultado.isSuccess) {
                _updateStatus.value = UpdateStatus.SUCCESS
            } else {
                _updateStatus.value = UpdateStatus.ERROR
            }
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.IDLE
    }
}