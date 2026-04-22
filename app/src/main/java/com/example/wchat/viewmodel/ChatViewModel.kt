package com.example.wchat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.ChatRepository
import com.example.wchat.model.Mensagem
import com.example.wchat.model.TipoChat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    val usuarioAtualId: String? = Firebase.auth.currentUser?.uid
    val mensagens: StateFlow<List<Mensagem>> = repository.getMensagensEmTempoReal()
        .onEach { mensagensRecebidas ->
            marcarMensagensComoLidas(mensagensRecebidas)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun enviarMensagem(texto: String, idEnvio: String) {
        viewModelScope.launch {
            try {
                repository.enviarMensagem(texto, idEnvio)
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
            try {
                repository.excluirMensagem(mensagem.id)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Erro ao excluir mensagem ${mensagem.id}", e)
            }
        }
    }

    private fun marcarMensagensComoLidas(mensagensAtuais: List<Mensagem>) {
        viewModelScope.launch { repository.marcarMensagensComoLidas(mensagensAtuais) }
    }
}

class ChatViewModelFactory(private val tipoChat: TipoChat, private val chatId: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(ChatRepository(tipoChat, chatId)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}