package com.example.wchat.data

import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.wchat.model.Mensagem
import com.example.wchat.model.TipoChat
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.io.path.name


class ChatRepository(
    private val tipoChat: TipoChat,
    private val chatId: String
) {
    private val firestore = Firebase.firestore
    private val usuarioAtualId = Firebase.auth.currentUser?.uid
    private val outroUsuarioId: String? = if (tipoChat == TipoChat.UM_A_UM) {
        chatId.replace(usuarioAtualId ?: "", "").replace("_", "")
    } else {
        null
    }

    private val collectionPathName = when (tipoChat) {
        TipoChat.GRUPO -> "grupos"
        TipoChat.SEGMENTO -> "segmentos"
        TipoChat.UM_A_UM -> "chats1a1"
    }

    private val NOME_DA_SUBCOLECAO_MENSAGENS = "mensagens"

    private val mensagensCollection = firestore
        .collection(collectionPathName)
        .document(chatId)
        .collection(NOME_DA_SUBCOLECAO_MENSAGENS)

    @OptIn(UnstableApi::class)
    suspend fun enviarMensagem(texto: String, idEnvio: String) {
        if (usuarioAtualId == null || texto.isBlank()) return

        if (tipoChat == TipoChat.UM_A_UM && outroUsuarioId.isNullOrBlank()) {
            return
        }

        var remetenteTipo = "CLIENTE"
        try {
            val usuarioDoc = firestore.collection("usuarios").document(usuarioAtualId).get().await()
            val usuario = usuarioDoc.toObject(Usuario::class.java)
            if (usuario != null) {
                remetenteTipo = usuario.tipo.name
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Falha ao buscar tipo do usuário para a mensagem.", e)
        }

        val novaMensagem = Mensagem(
            remetenteId = usuarioAtualId,
            remetenteNome = Firebase.auth.currentUser?.displayName ?: "Anônimo",
            destinatarioId = outroUsuarioId,
            texto = texto.trim(),
            idEnvio = idEnvio,
            remetenteTipo = remetenteTipo,
            lidoPor = listOf(usuarioAtualId)
        )

        mensagensCollection.add(novaMensagem).await()
    }

    fun getMensagensEmTempoReal(): Flow<List<Mensagem>> = callbackFlow {
        val listener = mensagensCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val mensagens = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Mensagem::class.java)?.copy(id = doc.id)
                    }
                    trySend(mensagens)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun marcarMensagensComoLidas(mensagens: List<Mensagem>) {
        if (usuarioAtualId == null) return

        val batch = firestore.batch()

        mensagens.forEach { msg ->
            if (msg.remetenteId != usuarioAtualId && !msg.lidoPor.contains(usuarioAtualId) && msg.id.isNotBlank()) {
                val docRef = mensagensCollection.document(msg.id)
                batch.update(docRef, "lidoPor", FieldValue.arrayUnion(usuarioAtualId))
            }
        }
        batch.commit().await()
    }

    suspend fun excluirMensagem(mensagemId: String) {
        if (mensagemId.isBlank()) {
            println("ID da mensagem inválido para exclusão.")
            return
        }

        try {
            mensagensCollection.document(mensagemId).delete().await()
            println("Mensagem $mensagemId excluída com sucesso.")
        } catch (e: Exception) {
            println("Erro ao excluir mensagem $mensagemId: $e")
        }
    }
}