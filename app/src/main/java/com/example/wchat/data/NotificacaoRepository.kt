package com.example.wchat.data

import com.example.wchat.model.Grupo
import com.example.wchat.model.Segmento
import com.example.wchat.model.TipoChat
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class NotificacaoRepository {
    private val usuarioAtualId = Firebase.auth.currentUser?.uid ?: ""

    suspend fun enviarNotificacao(
        destinatarios: List<Any>,
        mensagem: String,
        idEnvio: String
    ) {
        if (mensagem.isBlank() || usuarioAtualId.isBlank()) return

        destinatarios.forEach { destinatario ->
            val chatInfo = when (destinatario) {
                is Grupo -> {
                    Pair(destinatario.id, TipoChat.GRUPO)
                }
                is Segmento -> {
                    Pair(destinatario.id, TipoChat.SEGMENTO)
                }
                is Usuario -> {
                    if (destinatario.id != usuarioAtualId) {
                        val chatId = listOf(usuarioAtualId, destinatario.id).sorted().joinToString("_")
                        Pair(chatId, TipoChat.UM_A_UM)
                    } else {
                        null
                    }
                }
                else -> {
                    null
                }
            }

            chatInfo?.let { (chatId, tipoChat) ->
                if (chatId.isNotBlank()) {
                    val chatRepo = ChatRepository(tipoChat = tipoChat, chatId = chatId)
                    chatRepo.enviarMensagem(
                        texto = mensagem,
                        idEnvio = idEnvio
                    )
                }
            }
        }
    }
}