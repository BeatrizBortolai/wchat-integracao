package com.example.wchat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.UsuarioRepository
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.example.wchat.model.Mensagem

class ConversasViewModel : ViewModel() {
    private val _estadoDosUsuarios = MutableStateFlow<Result<List<Usuario>>?>(null)
    val estadoDosUsuarios: StateFlow<Result<List<Usuario>>?> = _estadoDosUsuarios.asStateFlow()
    val contagensNaoLidas: StateFlow<Map<String, Int>> = getContagensNaoLidasFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    init {
        viewModelScope.launch {
            _estadoDosUsuarios.value = UsuarioRepository().getTodosOsUsuarios()
        }
    }

    private fun getContagensNaoLidasFlow(): Flow<Map<String, Int>> {
        val usuarioId = Firebase.auth.currentUser?.uid ?: return flowOf(emptyMap())

        return callbackFlow {
            val listener = Firebase.firestore.collectionGroup("mensagens")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ConversasVM", "Erro no listener de contagens", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val contagens = mutableMapOf<String, Int>()
                        snapshot.documents.forEach { doc ->
                            val mensagem = doc.toObject(Mensagem::class.java)
                            if (mensagem != null) {
                                val euNaoLi = !mensagem.lidoPor.contains(usuarioId)
                                val naoSouORemetente = mensagem.remetenteId != usuarioId

                                if (euNaoLi && naoSouORemetente) {
                                    val parentDoc = doc.reference.parent.parent
                                    val collectionName = parentDoc?.parent?.id // "grupos", "segmentos" ou "chats1a1"
                                    val chatId = parentDoc?.id

                                    if (collectionName != null && chatId != null) {
                                        // Cria uma chave única combinando a coleção e o ID
                                        val chaveUnica = "${collectionName}_$chatId"
                                        contagens[chaveUnica] = (contagens[chaveUnica] ?: 0) + 1
                                    }
                                }
                            }
                        }
                        trySend(contagens)
                    } else {
                        trySend(emptyMap())
                    }
                }
            awaitClose { listener.remove() }
        }
    }
}