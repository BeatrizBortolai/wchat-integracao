package com.example.wchat.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.UsuarioApiRepository
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversasViewModel(application: Application) : AndroidViewModel(application) {
    private val usuarioApiRepository = UsuarioApiRepository(application.applicationContext)

    private val _estadoDosUsuarios = MutableStateFlow<Result<List<Usuario>>?>(null)
    val estadoDosUsuarios: StateFlow<Result<List<Usuario>>?> = _estadoDosUsuarios.asStateFlow()

    // REST já busca usuários e mensagens no backend. A contagem em tempo real do Firestore foi removida.
    // Mantemos o StateFlow para não quebrar a tela atual.
    private val _contagensNaoLidas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val contagensNaoLidas: StateFlow<Map<String, Int>> = _contagensNaoLidas.asStateFlow()

    init {
        carregarUsuarios()
    }

    fun carregarUsuarios() {
        viewModelScope.launch {
            val usuarioAtualId = Firebase.auth.currentUser?.uid
            usuarioApiRepository.listarUsuarios()
                .onSuccess { usuarios ->
                    _estadoDosUsuarios.value = Result.success(
                        usuarios.filter { it.id != usuarioAtualId }
                    )
                }
                .onFailure { e ->
                    Log.e("ConversasVM", "Erro ao carregar usuários pelo backend", e)
                    _estadoDosUsuarios.value = Result.failure(e)
                }
        }
    }
}