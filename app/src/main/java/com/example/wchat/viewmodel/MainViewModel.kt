package com.example.wchat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.model.Grupo
import com.example.wchat.model.Mensagem
import com.example.wchat.model.Segmento
import com.example.wchat.services.InAppNotificationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _ultimaMensagemRecebida = MutableStateFlow<Pair<Mensagem, String?>?>(null)
    val ultimaMensagemRecebida: StateFlow<Pair<Mensagem, String?>?> = _ultimaMensagemRecebida.asStateFlow()

    private var dismissJob: Job? = null

    init {
        observarEventosInApp()
    }

    fun iniciarOuvinteDeNotificacao(gruposAtuais: List<Grupo>, segmentosAtuais: List<Segmento>) {
        Log.d(
            "MainVM",
            "Popup in-app agora usa eventos do FCM/backend. Grupos=${gruposAtuais.size}, segmentos=${segmentosAtuais.size}"
        )
    }

    private fun observarEventosInApp() {
        viewModelScope.launch {
            InAppNotificationManager.events.collect { event ->
                val mensagem = Mensagem(
                    id = "",
                    texto = event.body,
                    remetenteId = event.remetenteId.orEmpty(),
                    remetenteNome = event.remetenteNome ?: event.title
                )

                val tituloPopup = event.remetenteNome ?: event.title
                _ultimaMensagemRecebida.value = Pair(mensagem, tituloPopup)

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