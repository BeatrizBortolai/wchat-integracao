package com.example.wchat.services

/**
 * Mantém, em memória, qual chat está aberto no momento.
 *
 * Usado para evitar popup in-app apenas quando a mensagem recebida pertence
 * exatamente ao chat que o usuário já está visualizando. Mensagens de outros
 * chats continuam exibindo popup mesmo dentro de uma tela de conversa.
 */
object ActiveChatTracker {
    private var currentChatId: String? = null
    private var currentCollection: String? = null

    fun enterChat(chatId: String, collection: String) {
        currentChatId = chatId
        currentCollection = collection
    }

    fun leaveChat(chatId: String, collection: String) {
        if (currentChatId == chatId && currentCollection == collection) {
            currentChatId = null
            currentCollection = null
        }
    }

    fun isCurrentChat(chatId: String?, collection: String?): Boolean {
        return !chatId.isNullOrBlank() &&
                !collection.isNullOrBlank() &&
                chatId == currentChatId &&
                collection == currentCollection
    }
}