package com.example.wchat.data.repository

import android.content.Context
import com.example.wchat.data.remote.api.RetrofitProvider
import com.example.wchat.data.remote.api.WChatApi
import com.example.wchat.data.remote.dto.EnvioSegmentoRequestDto
import com.example.wchat.data.remote.dto.MensagemRequestDto
import com.example.wchat.data.remote.dto.MensagemResponseDto
import com.example.wchat.model.Mensagem
import com.example.wchat.model.TipoChat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Repository REST para cumprir a Sprint 2: o app deixa de depender apenas de mocks/Firestore
 * e passa a consumir o backend Spring Boot/MongoDB.
 */
class BackendChatRepository(context: Context) {
    private val api: WChatApi = RetrofitProvider.create(context).create(WChatApi::class.java)
    private val usuarioAtualId: String? get() = Firebase.auth.currentUser?.uid

    suspend fun enviarMensagem(tipoChat: TipoChat, chatId: String, texto: String): Result<Mensagem> {
        val remetenteId = usuarioAtualId ?: return Result.failure(Exception("Usuário não autenticado"))
        if (texto.isBlank()) return Result.failure(Exception("Mensagem vazia"))

        val response = when (tipoChat) {
            TipoChat.UM_A_UM -> {
                val destinatarioId = chatId.replace(remetenteId, "").replace("_", "")
                api.enviarMensagem(MensagemRequestDto(remetenteId = remetenteId, destinatarioId = destinatarioId, texto = texto.trim()))
            }
            TipoChat.GRUPO -> api.enviarMensagem(MensagemRequestDto(remetenteId = remetenteId, grupoId = chatId, texto = texto.trim()))
            TipoChat.SEGMENTO -> {
                val segmentoResponse = api.enviarMensagemParaSegmento(
                    EnvioSegmentoRequestDto(remetenteId = remetenteId, segmento = chatId, texto = texto.trim())
                )
                if (segmentoResponse.isSuccessful) {
                    val primeira = segmentoResponse.body()?.firstOrNull()
                    return if (primeira != null) Result.success(primeira.toModel()) else Result.success(Mensagem(texto = texto.trim(), remetenteId = remetenteId))
                }
                return Result.failure(Exception("Erro ao enviar para segmento: ${segmentoResponse.code()} - ${segmentoResponse.errorBody()?.string()}"))
            }
        }

        return if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.toModel())
        } else {
            Result.failure(Exception("Erro ao enviar mensagem: ${response.code()} - ${response.errorBody()?.string()}"))
        }
    }

    suspend fun buscarMensagens(tipoChat: TipoChat, chatId: String): Result<List<Mensagem>> {
        val remetenteId = usuarioAtualId ?: return Result.failure(Exception("Usuário não autenticado"))
        val response = when (tipoChat) {
            TipoChat.UM_A_UM -> {
                val outroUsuarioId = chatId.replace(remetenteId, "").replace("_", "")
                api.buscarMensagensDiretas(remetenteId, outroUsuarioId)
            }
            TipoChat.GRUPO -> api.buscarMensagensDoGrupo(chatId)
            TipoChat.SEGMENTO -> return Result.success(emptyList()) // envio por segmento gera mensagens para clientes; histórico pode ser consultado pelas conversas.
        }

        return if (response.isSuccessful) {
            Result.success(response.body().orEmpty().map { it.toModel() })
        } else {
            Result.failure(Exception("Erro ao buscar mensagens: ${response.code()} - ${response.errorBody()?.string()}"))
        }
    }

    suspend fun marcarComoLida(mensagemId: String): Result<Mensagem> {
        val response = api.marcarMensagemComoLida(mensagemId)
        return if (response.isSuccessful && response.body() != null) Result.success(response.body()!!.toModel())
        else Result.failure(Exception("Erro ao marcar como lida: ${response.code()}"))
    }

    suspend fun excluirMensagem(mensagemId: String): Result<Unit> {
        val response = api.excluirMensagemBackend(mensagemId)
        return if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ao excluir mensagem: ${response.code()}"))
    }
}

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
