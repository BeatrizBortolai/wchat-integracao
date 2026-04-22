package com.example.wchat.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Mensagem(
    @DocumentId val id: String = "",
    val texto: String = "",
    val remetenteId: String = "",
    val remetenteNome: String = "",
    val destinatarioId: String? = null,
    val idEnvio: String = "",
    val remetenteTipo: String = "",

    @ServerTimestamp
    val timestamp: Date? = null,
    val lida: Boolean = false,
    val lidoPor: List<String> = emptyList()
) {
    constructor() : this(
        id = "",
        texto = "",
        remetenteId = "",
        remetenteNome = "",
        destinatarioId = null,
        idEnvio = "",
        remetenteTipo = "",
        timestamp = null,
        lida = false,
        lidoPor = emptyList()
    )
}
