package com.example.wchat.data.repository

import android.content.Context
import com.example.wchat.data.remote.api.RetrofitProvider
import com.example.wchat.data.remote.api.WChatApi
import com.example.wchat.data.remote.dto.AuthSyncRequestDto
import com.example.wchat.data.remote.dto.FcmTokenRequestDto
import com.example.wchat.data.remote.mapper.toDomain
import com.example.wchat.domain.model.AuthSession
import com.example.wchat.session.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class AuthIntegrationRepository(
    context: Context
) {
    private val api: WChatApi = RetrofitProvider
        .create(context)
        .create(WChatApi::class.java)

    private val sessionManager = SessionManager(context)

    suspend fun syncAuthenticatedFirebaseUser(
        nome: String,
        email: String,
        password: String,
        tipo: String,
        cargo: String?,
        segmentos: List<String>
    ): Result<AuthSession> {
        return try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(Exception("Usuário Firebase não autenticado."))

            val request = AuthSyncRequestDto(
                id = firebaseUser.uid,
                nome = nome,
                email = email,
                password = password,
                tipo = tipo,
                cargo = cargo,
                segmentos = segmentos
            )

            val response = api.syncUsuario(request)

            if (response.isSuccessful && response.body() != null) {
                val authSession = response.body()!!.toDomain()
                sessionManager.saveJwtToken(authSession.token)
                sessionManager.saveBackendUserId(authSession.usuarioId)
                Result.success(authSession)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Erro ao sincronizar usuário com backend: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFcmTokenToBackend(): Result<Unit> {
        return try {
            val usuarioId = sessionManager.getBackendUserId()
                ?: return Result.failure(Exception("Usuário backend não encontrado."))

            val fcmToken = getFirebaseMessagingToken()

            val response = api.atualizarFcmToken(
                usuarioId = usuarioId,
                request = FcmTokenRequestDto(fcmToken = fcmToken)
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao enviar FCM token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getFirebaseMessagingToken(): String =
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    continuation.resume(token)
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
}