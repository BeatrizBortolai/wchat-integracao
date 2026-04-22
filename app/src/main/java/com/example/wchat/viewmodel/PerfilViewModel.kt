package com.example.wchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.UsuarioRepository
import com.example.wchat.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(private val usuarioId: String) : ViewModel() {

    private val repo = UsuarioRepository()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario = _usuario.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _anotacoesSalvas = MutableStateFlow(false)
    val anotacoesSalvas = _anotacoesSalvas.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            _usuario.value = repo.getUsuarioPorId(usuarioId)
            _isLoading.value = false
        }
    }

    fun salvarAnotacoes(texto: String) {
        viewModelScope.launch {
            val sucesso = repo.salvarAnotacoesDoCliente(usuarioId, texto)
            _anotacoesSalvas.value = sucesso
        }
    }

    fun onAnotacoesSalvasConfirmado() {
        _anotacoesSalvas.value = false
    }
}

@Suppress("UNCHECKED_CAST")
class PerfilViewModelFactory(private val usuarioId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
            return PerfilViewModel(usuarioId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}