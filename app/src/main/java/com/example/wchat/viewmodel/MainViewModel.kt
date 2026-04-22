package com.example.wchat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.model.Grupo
import com.example.wchat.model.Mensagem
import com.example.wchat.model.Segmento
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _ultimaMensagemRecebida = MutableStateFlow<Pair<Mensagem, String?>?>(null)
    val ultimaMensagemRecebida: StateFlow<Pair<Mensagem, String?>?> = _ultimaMensagemRecebida.asStateFlow()

    private var notificacaoListener: ListenerRegistration? = null
    private var dismissJob: Job? = null
    private var ultimoIdEnvioExibido: String? = null
    private var isInitialLoad = true

    fun iniciarOuvinteDeNotificacao(gruposAtuais: List<Grupo>, segmentosAtuais: List<Segmento>) {
        if (notificacaoListener != null) return

        val usuarioId = Firebase.auth.currentUser?.uid
        if (usuarioId.isNullOrEmpty()) {
            Log.w("MainVM", "ID do usuário nulo, ouvinte não pode ser iniciado.")
            return
        }

        if (gruposAtuais.isEmpty() && segmentosAtuais.isEmpty()) {
            Log.w("MainVM", "Tentativa de iniciar ouvinte com listas de grupos e segmentos vazias. Aguardando dados.")
            return
        }

        Log.d("MainVM", "Iniciando ouvinte com ${gruposAtuais.size} grupos e ${segmentosAtuais.size} segmentos.")

        val idsDosMeusGrupos = gruposAtuais.map { it.id }.toSet()
        val idsDosMeusSegmentos = segmentosAtuais.map { it.id }.toSet()

        Log.d("MainVM_Debug", "Listener iniciado para o usuário: $usuarioId")
        Log.d("MainVM_Debug", "Ouvindo os seguintes IDs de GRUPO: $idsDosMeusGrupos")
        Log.d("MainVM_Debug", "Ouvindo os seguintes IDs de SEGMENTO: $idsDosMeusSegmentos")

        notificacaoListener = Firebase.firestore.collectionGroup("mensagens")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("MainVM", "Erro no listener de notificação", error)
                    return@addSnapshotListener
                }

                if (isInitialLoad) {
                    isInitialLoad = false
                    Log.d("MainVM", "Carregamento inicial do listener concluído. Ignorando ${snapshot.documentChanges.size} mensagens existentes.")
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val mensagem = doc.toObject<Mensagem>()?.copy(id = doc.id)

                        if (mensagem != null) {
                            val naoSouEu = mensagem.remetenteId != usuarioId
                            if (naoSouEu) {
                                val chatId = doc.reference.parent.parent?.id
                                Log.d("MainVM_Debug", "Mensagem recebida do chatId: '$chatId'. Texto: '${mensagem.texto.take(20)}...'")

                                val temPermissao = when {
                                    idsDosMeusGrupos.contains(chatId) -> true
                                    idsDosMeusSegmentos.contains(chatId) -> true
                                    chatId?.contains(usuarioId) == true -> true
                                    else -> false
                                }

                                if (!temPermissao) {
                                    Log.w("MainVM_Debug", "PERMISSÃO NEGADA para o chatId: '$chatId'")
                                }

                                if (temPermissao) {
                                    val ehNova = ultimoIdEnvioExibido != mensagem.idEnvio
                                    if (ehNova) {
                                        val nomeDoChat = when {
                                            idsDosMeusGrupos.contains(chatId) ->
                                                gruposAtuais.find { it.id == chatId }?.tipo?.name

                                            idsDosMeusSegmentos.contains(chatId) ->
                                                segmentosAtuais.find { it.id == chatId }?.tipo?.nomeExibicao

                                            else -> mensagem.remetenteNome
                                        } ?: chatId

                                        Log.d("MainVM", "PERMITIDO (Nova Mensagem): Pop-up para '$nomeDoChat'.")
                                        ultimoIdEnvioExibido = mensagem.idEnvio
                                        _ultimaMensagemRecebida.value = Pair(mensagem, nomeDoChat)
                                        iniciarTimerParaDispensar()
                                        break
                                    } else {
                                        Log.d("MainVM", "IGNORADO (Duplicata): A mensagem para o chat '$chatId' já foi exibida.")
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun iniciarTimerParaDispensar() {
        dismissJob?.cancel()
        dismissJob = viewModelScope.launch {
            delay(5000)
            dispensarNotificacao()
        }
    }

    fun dispensarNotificacao() {
        _ultimaMensagemRecebida.value = null
    }

    override fun onCleared() {
        super.onCleared()
        notificacaoListener?.remove()
        dismissJob?.cancel()
        Log.d("MainVM", "ViewModel limpo e listener removido.")
    }
}