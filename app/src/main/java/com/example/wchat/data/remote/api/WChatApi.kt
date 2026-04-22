package com.example.wchat.data.remote.api

import com.example.wchat.data.remote.dto.AuthSyncRequestDto
import com.example.wchat.data.remote.dto.AuthSyncResponseDto
import com.example.wchat.data.remote.dto.FcmTokenRequestDto
import retrofit2.Response
import retrofit2.http.Body
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
}