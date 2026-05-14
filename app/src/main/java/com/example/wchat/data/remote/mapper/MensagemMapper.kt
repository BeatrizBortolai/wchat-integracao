package com.example.wchat.data.remote.mapper

import com.example.wchat.data.remote.dto.MensagemResponseDto
import com.example.wchat.model.Mensagem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun MensagemResponseDto.toModel(): Mensagem = Mensagem(
    id = id,
    texto = texto,
    remetenteId = remetenteId,
    remetenteNome = remetenteNome,
    destinatarioId = destinatarioId,
    timestamp = dataEnvio?.toDateOrNull(),
    lida = lida
)

private fun String.toDateOrNull(): Date? = try {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    parser.timeZone = TimeZone.getDefault()
    parser.parse(this.substringBefore('.'))
} catch (_: Exception) {
    null
}