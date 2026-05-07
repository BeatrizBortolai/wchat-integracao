package com.example.wchat.services

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class InAppNotificationEvent(
    val title: String,
    val body: String,
    val chatId: String?,
    val collection: String?,
    val remetenteId: String?,
    val remetenteNome: String?,
    val mensagemId: String? = null,
    val chatNome: String? = null,
    val tipoMensagem: String? = null
)

object InAppNotificationManager {
    private val _events = MutableSharedFlow<InAppNotificationEvent>(extraBufferCapacity = 10)
    val events = _events.asSharedFlow()

    fun show(event: InAppNotificationEvent) {
        _events.tryEmit(event)
    }
}