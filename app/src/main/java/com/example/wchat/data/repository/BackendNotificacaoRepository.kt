package com.example.wchat.data.repository

import android.content.Context
import com.example.wchat.data.remote.api.RetrofitProvider
import com.example.wchat.data.remote.api.WChatApi
import com.example.wchat.data.remote.dto.NotificacaoRequestDto
import com.example.wchat.data.remote.dto.NotificacaoResponseDto

class BackendNotificacaoRepository(context: Context) {
    private val api: WChatApi = RetrofitProvider.create(context).create(WChatApi::class.java)

    suspend fun enviarCampanha(
        titulo: String,
        descricao: String,
        nomeCampanha: String,
        remetenteId: String,
        tipoDestinatario: String,
        destinatariosIds: List<String> = emptyList(),
        grupo: String? = null,
        segmento: String? = null,
        linkEvento: String? = null,
        urlSaberMais: String? = null,
        urlInscrever: String? = null,
        deeplink: String? = null
    ): Result<NotificacaoResponseDto> = try {
        val response = api.enviarCampanha(
            NotificacaoRequestDto(
                titulo = titulo,
                descricao = descricao,
                nomeCampanha = nomeCampanha,
                remetenteId = remetenteId,
                linkEvento = linkEvento,
                urlSaberMais = urlSaberMais,
                urlInscrever = urlInscrever,
                deeplink = deeplink,
                grupo = grupo,
                segmento = segmento,
                destinatariosIds = destinatariosIds,
                tipoDestinatario = tipoDestinatario
            )
        )

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(
                Exception("Erro ao enviar campanha: ${response.code()} - ${response.errorBody()?.string()}")
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}