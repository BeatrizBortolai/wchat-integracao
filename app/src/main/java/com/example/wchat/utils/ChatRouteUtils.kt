package com.example.wchat.utils

import com.example.wchat.model.TipoChat

/**
 * Centraliza os nomes de coleção usados no payload FCM e nas rotas de chat.
 * Evita divergência entre 1:1, grupos e segmentos.
 */
object ChatRouteUtils {
    const val COLLECTION_DIRECT = "chats1a1"
    const val COLLECTION_GROUPS = "grupos"
    const val COLLECTION_SEGMENTS = "segmentos"

    fun collectionFor(tipoChat: TipoChat): String = when (tipoChat) {
        TipoChat.UM_A_UM -> COLLECTION_DIRECT
        TipoChat.GRUPO -> COLLECTION_GROUPS
        TipoChat.SEGMENTO -> COLLECTION_SEGMENTS
    }

    /**
     * Para chat 1:1, o backend envia o chatId como "uid1_uid2" ordenado.
     * Algumas telas antigas ainda abrem o chat só com o id do outro usuário.
     * Este método normaliza o id rastreado para o ActiveChatTracker.
     */
    fun normalizeChatId(
        chatId: String?,
        collection: String?,
        currentUserId: String?
    ): String? {
        if (chatId.isNullOrBlank()) return null

        return if (collection == COLLECTION_DIRECT && !chatId.contains("_") && !currentUserId.isNullOrBlank()) {
            listOf(currentUserId, chatId).sorted().joinToString("_")
        } else {
            chatId
        }
    }
}
