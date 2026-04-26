package com.example.wchat.data.remote.api

import com.example.wchat.data.remote.dto.AuthSyncRequestDto
import com.example.wchat.data.remote.dto.AuthSyncResponseDto
import com.example.wchat.data.remote.dto.FcmTokenRequestDto
import com.example.wchat.data.remote.dto.UsuarioResponseDto
import com.example.wchat.data.remote.dto.UsuarioUpdateRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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
}