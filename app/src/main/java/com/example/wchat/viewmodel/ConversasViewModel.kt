package com.example.wchat.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.remote.api.RetrofitProvider
import com.example.wchat.data.remote.api.WChatApi
import com.example.wchat.data.remote.dto.ConversaResponseDto
import com.example.wchat.data.repository.UsuarioApiRepository
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ConversasViewModel(application: Application) : AndroidViewModel(application) {
    private val usuarioApiRepository = UsuarioApiRepository(application.applicationContext)
    private val api: WChatApi = RetrofitProvider
        .create(application.applicationContext)
        .create(WChatApi::class.java)

    private val _estadoDosUsuarios = MutableStateFlow<Result<List<Usuario>>?>(null)
    val estadoDosUsuarios: StateFlow<Result<List<Usuario>>?> = _estadoDosUsuarios.asStateFlow()

    private val _contagensNaoLidas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val contagensNaoLidas: StateFlow<Map<String, Int>> = _contagensNaoLidas.asStateFlow()

    private var contagensJob: Job? = null

    init {
        carregarUsuarios()
        iniciarAtualizacaoDeContagensNaoLidas()
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

    private fun iniciarAtualizacaoDeContagensNaoLidas() {
        contagensJob?.cancel()
        contagensJob = viewModelScope.launch {
            while (isActive) {
                carregarContagensNaoLidas()
                delay(3000)
            }
        }
    }

    fun carregarContagensNaoLidas() {
        viewModelScope.launch {
            carregarContagensNaoLidasInterno()
        }
    }

    private suspend fun carregarContagensNaoLidasInterno() {
        val usuarioAtualId = Firebase.auth.currentUser?.uid ?: return

        try {
            val response = api.buscarConversas(usuarioAtualId)
            if (!response.isSuccessful) {
                Log.w("ConversasVM", "Falha ao buscar contagens/conversas: ${response.code()}")
                return
            }

            val contagens = response.body()
                .orEmpty()
                .filter { it.naoLidas > 0 }
                .associate { conversa ->
                    chaveDaConversa(conversa, usuarioAtualId) to conversa.naoLidas.toInt()
                }
                .filterKeys { it.isNotBlank() }

            _contagensNaoLidas.value = contagens
        } catch (e: Exception) {
            Log.e("ConversasVM", "Erro ao atualizar contagens não lidas", e)
        }
    }

    private fun chaveDaConversa(conversa: ConversaResponseDto, usuarioAtualId: String): String {
        val tipo = conversa.tipo?.uppercase()
        val id = conversa.id ?: conversa.usuarioId ?: conversa.grupoId ?: conversa.segmentoId ?: return ""

        return when (tipo) {
            "GRUPO" -> "grupos_$id"
            "SEGMENTO" -> "segmentos_$id"
            "DIRETA", "CHATS1A1", "UM_A_UM" -> listOf(usuarioAtualId, id).sorted().joinToString("_")
            else -> {
                when {
                    conversa.grupoId != null -> "grupos_${conversa.grupoId}"
                    conversa.segmentoId != null -> "segmentos_${conversa.segmentoId}"
                    else -> listOf(usuarioAtualId, id).sorted().joinToString("_")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        contagensJob?.cancel()
    }
}