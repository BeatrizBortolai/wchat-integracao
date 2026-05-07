package com.example.wchat.data.remote.api

import com.example.wchat.data.remote.dto.AuthSyncRequestDto
import com.example.wchat.data.remote.dto.AuthSyncResponseDto
import com.example.wchat.data.remote.dto.FcmTokenRequestDto
import com.example.wchat.data.remote.dto.UsuarioResponseDto
import com.example.wchat.data.remote.dto.UsuarioUpdateRequestDto

import com.example.wchat.data.remote.dto.ConversaResponseDto
import com.example.wchat.data.remote.dto.EnvioSegmentoRequestDto
import com.example.wchat.data.remote.dto.GrupoDto
import com.example.wchat.data.remote.dto.MensagemRequestDto
import com.example.wchat.data.remote.dto.MensagemResponseDto
import com.example.wchat.data.remote.dto.NotificacaoRequestDto
import com.example.wchat.data.remote.dto.NotificacaoResponseDto
import com.example.wchat.data.remote.dto.PageResponseDto
import com.example.wchat.data.remote.dto.SegmentoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Path

interface WChatApi {

    @POST("auth/sync")
    suspend fun syncUsuario(
        @Body request: AuthSyncRequestDto
    ): Response<AuthSyncResponseDto>

    @PUT("auth/fcm-token/{usuarioId}")
    suspend fun atualizarFcmToken(
        @Path("usuarioId") usuarioId: String,
        @Body request: FcmTokenRequestDto
    ): Response<Unit>


    @GET("usuarios")
    suspend fun listarUsuarios(
        @Query("tipo") tipo: String? = null,
        @Query("size") size: Int = 100
    ): Response<PageResponseDto<UsuarioResponseDto>>

    @GET("usuarios/{id}")
    suspend fun buscarUsuarioPorId(
        @Path("id") id: String
    ): Response<UsuarioResponseDto>

    @GET("usuarios/email/{email}")
    suspend fun buscarUsuarioPorEmail(
        @Path("email") email: String
    ): Response<UsuarioResponseDto>

    @PUT("usuarios/{id}")
    suspend fun atualizarUsuario(
        @Path("id") id: String,
        @Body request: UsuarioUpdateRequestDto
    ): Response<UsuarioResponseDto>

    @DELETE("usuarios/{id}")
    suspend fun deletarUsuario(
        @Path("id") id: String
    ): Response<Unit>

    // Mensagens — integração Sprint 2
    @POST("mensagens/enviar")
    suspend fun enviarMensagem(
        @Body request: MensagemRequestDto
    ): Response<MensagemResponseDto>

    @POST("mensagens/enviar-segmento")
    suspend fun enviarMensagemParaSegmento(
        @Body request: EnvioSegmentoRequestDto
    ): Response<List<MensagemResponseDto>>

    @GET("mensagens/diretas/{id1}/{id2}")
    suspend fun buscarMensagensDiretas(
        @Path("id1") id1: String,
        @Path("id2") id2: String
    ): Response<List<MensagemResponseDto>>

    @GET("mensagens/grupo/{grupoId}")
    suspend fun buscarMensagensDoGrupo(
        @Path("grupoId") grupoId: String
    ): Response<List<MensagemResponseDto>>

    @GET("mensagens/segmento/{segmentoId}")
    suspend fun buscarMensagensDoSegmento(
        @Path("segmentoId") segmentoId: String
    ): Response<List<MensagemResponseDto>>

    @GET("mensagens/conversas/{usuarioId}")
    suspend fun buscarConversas(
        @Path("usuarioId") usuarioId: String
    ): Response<List<ConversaResponseDto>>

    @PUT("mensagens/{mensagemId}/lida")
    suspend fun marcarMensagemComoLida(
        @Path("mensagemId") mensagemId: String
    ): Response<MensagemResponseDto>

    @PUT("mensagens/{mensagemId}/lida/{usuarioId}")
    suspend fun marcarMensagemComoLidaPorUsuario(
        @Path("mensagemId") mensagemId: String,
        @Path("usuarioId") usuarioId: String
    ): Response<MensagemResponseDto>

    @DELETE("mensagens/{mensagemId}")
    suspend fun excluirMensagemBackend(
        @Path("mensagemId") mensagemId: String
    ): Response<Unit>

    // Grupos e segmentos
    @GET("grupos")
    suspend fun listarGrupos(): Response<List<GrupoDto>>

    @GET("grupos/usuario/{usuarioId}")
    suspend fun listarGruposDoUsuario(
        @Path("usuarioId") usuarioId: String
    ): Response<List<GrupoDto>>

    @POST("grupos/{grupoId}/participantes/{usuarioId}")
    suspend fun adicionarParticipanteGrupo(
        @Path("grupoId") grupoId: String,
        @Path("usuarioId") usuarioId: String
    ): Response<Unit>

    @GET("segmentos")
    suspend fun listarSegmentos(): Response<List<SegmentoDto>>

    @POST("usuarios/{usuarioId}/segmentos/{nomeSegmento}")
    suspend fun adicionarSegmentoAoUsuario(
        @Path("usuarioId") usuarioId: String,
        @Path("nomeSegmento") nomeSegmento: String
    ): Response<UsuarioResponseDto>

    // Campanhas / push
    @POST("notificacoes/enviar-campanha")
    suspend fun enviarCampanha(
        @Body request: NotificacaoRequestDto
    ): Response<NotificacaoResponseDto>

    @GET("notificacoes")
    suspend fun listarNotificacoes(): Response<List<NotificacaoResponseDto>>

    // Anotações e auditoria
    @POST("anotacoes/{usuarioId}")
    suspend fun criarAnotacao(
        @Path("usuarioId") usuarioId: String,
        @Query("operadorId") operadorId: String,
        @Query("texto") texto: String
    ): Response<Unit>

    @GET("auditoria")
    suspend fun listarAuditoria(): Response<Any>

}