package com.example.wchat.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.UsuarioApiRepository
import com.example.wchat.data.remote.mapper.toModel
import com.example.wchat.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val usuarioId: String,
    context: Context
) : ViewModel() {

    private val usuarioApiRepository = UsuarioApiRepository(context.applicationContext)

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario = _usuario.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _anotacoesSalvas = MutableStateFlow(false)
    val anotacoesSalvas = _anotacoesSalvas.asStateFlow()

    init {
        carregarUsuario()
    }

    private fun carregarUsuario() {
        viewModelScope.launch {
            _isLoading.value = true
            usuarioApiRepository.buscarPorId(usuarioId)
                .onSuccess { dto -> _usuario.value = dto.toModel() }
                .onFailure { _usuario.value = null }
            _isLoading.value = false
        }
    }

    fun salvarAnotacoes(texto: String) {
        viewModelScope.launch {
            usuarioApiRepository.salvarAnotacoesDoCliente(usuarioId, texto)
                .onSuccess { dto ->
                    _usuario.value = dto.toModel()
                    _anotacoesSalvas.value = true
                }
                .onFailure {
                    _anotacoesSalvas.value = false
                }
        }
    }

    fun onAnotacoesSalvasConfirmado() {
        _anotacoesSalvas.value = false
    }
}

@Suppress("UNCHECKED_CAST")
class PerfilViewModelFactory(
    private val usuarioId: String,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
            return PerfilViewModel(usuarioId, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}