package com.example.wchat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.model.Grupo
import com.example.wchat.model.Mensagem
import com.example.wchat.model.Segmento
import com.example.wchat.services.InAppNotificationManager
import com.example.wchat.utils.DisplayNameUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PopupNotificationInfo(
    val mensagem: Mensagem,
    val titulo: String,
    val descricao: String,
    val chatId: String,
    val collection: String?
)

class MainViewModel : ViewModel() {

    private val _ultimaMensagemRecebida = MutableStateFlow<PopupNotificationInfo?>(null)
    val ultimaMensagemRecebida: StateFlow<PopupNotificationInfo?> = _ultimaMensagemRecebida.asStateFlow()

    private var dismissJob: Job? = null

    init {
        observarEventosInApp()
    }

    fun iniciarOuvinteDeNotificacao(gruposAtuais: List<Grupo>, segmentosAtuais: List<Segmento>) {
        Log.d(
            "MainVM",
            "Popup in-app usa eventos FCM/backend. Grupos=${gruposAtuais.size}, segmentos=${segmentosAtuais.size}"
        )
    }

    private fun observarEventosInApp() {
        viewModelScope.launch {
            InAppNotificationManager.events.collect { event ->
                val chatId = event.chatId.orEmpty()
                val collection = event.collection
                val collectionNormalizada = collection?.lowercase()
                val remetenteNome = event.remetenteNome ?: event.title
                val corpoMensagem = event.body

                if (chatId.isBlank()) {
                    Log.w("MainVM", "Notificação in-app ignorada: chatId ausente. Evento=$event")
                    return@collect
                }

                val tituloPopup = when (collectionNormalizada) {
                    "grupos" -> DisplayNameUtils.grupoComPrefixo(event.chatNome ?: chatId)
                    "segmentos" -> DisplayNameUtils.segmentoComPrefixo(event.chatNome ?: chatId)
                    "chats1a1" -> remetenteNome
                    else -> event.chatNome ?: event.title
                }

                val isCampanha = event.tipoMensagem.equals("NOTIFICATION", ignoreCase = true)

                val descricaoPopup = when {
                    isCampanha -> corpoMensagem
                    collectionNormalizada == "grupos" || collectionNormalizada == "segmentos" -> {
                        if (corpoMensagem.startsWith("$remetenteNome:")) {
                            corpoMensagem
                        } else {
                            "$remetenteNome: $corpoMensagem"
                        }
                    }
                    else -> corpoMensagem
                }

                val mensagem = Mensagem(
                    id = event.mensagemId.orEmpty(),
                    texto = corpoMensagem,
                    remetenteId = event.remetenteId.orEmpty(),
                    remetenteNome = remetenteNome
                )

                _ultimaMensagemRecebida.value = PopupNotificationInfo(
                    mensagem = mensagem,
                    titulo = tituloPopup,
                    descricao = descricaoPopup,
                    chatId = chatId,
                    collection = collection
                )

                iniciarTimerParaDispensar()
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
        dismissJob?.cancel()
        Log.d("MainVM", "ViewModel limpo.")
    }
}