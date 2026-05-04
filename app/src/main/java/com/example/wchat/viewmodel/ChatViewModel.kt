package com.example.wchat.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.BackendChatRepository
import com.example.wchat.model.Mensagem
import com.example.wchat.model.TipoChat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: BackendChatRepository,
    private val tipoChat: TipoChat,
    private val chatId: String
) : ViewModel() {

    val usuarioAtualId: String? = Firebase.auth.currentUser?.uid

    private val _mensagens = MutableStateFlow<List<Mensagem>>(emptyList())
    val mensagens: StateFlow<List<Mensagem>> = _mensagens.asStateFlow()

    init {
        carregarMensagens()
    }

    fun carregarMensagens() {
        viewModelScope.launch {
            repository.buscarMensagens(tipoChat, chatId)
                .onSuccess { lista ->
                    _mensagens.value = lista.sortedByDescending { it.timestamp?.time ?: 0L }
                    marcarMensagensComoLidas(lista)
                }
                .onFailure { e -> Log.e("ChatViewModel", "Erro ao carregar mensagens", e) }
        }
    }

    fun enviarMensagem(texto: String, idEnvio: String) {
        viewModelScope.launch {
            try {
                repository.enviarMensagem(tipoChat, chatId, texto)
                    .onSuccess { carregarMensagens() }
                    .onFailure { e -> Log.e("ChatViewModel", "Erro ao enviar mensagem", e) }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Erro ao enviar mensagem", e)
            }
        }
    }

    fun excluirMensagem(mensagem: Mensagem) {
        if (mensagem.id.isBlank()) {
            Log.w("ChatViewModel", "Tentativa de excluir mensagem com ID vazio.")
            return
        }

        viewModelScope.launch {
            repository.excluirMensagem(mensagem.id)
                .onSuccess { carregarMensagens() }
                .onFailure { e -> Log.e("ChatViewModel", "Erro ao excluir mensagem ${mensagem.id}", e) }
        }
    }

    private fun marcarMensagensComoLidas(mensagensAtuais: List<Mensagem>) {
        val usuarioId = usuarioAtualId ?: return
        mensagensAtuais
            .filter { it.id.isNotBlank() && it.remetenteId != usuarioId && !it.lida }
            .forEach { mensagem ->
                viewModelScope.launch {
                    repository.marcarComoLida(mensagem.id)
                }
            }
    }
}

class ChatViewModelFactory(
    private val context: Context,
    private val tipoChat: TipoChat,
    private val chatId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(BackendChatRepository(context.applicationContext), tipoChat, chatId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}