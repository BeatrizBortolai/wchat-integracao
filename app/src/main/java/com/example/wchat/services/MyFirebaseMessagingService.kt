package com.example.wchat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.wchat.MainActivity
import com.example.wchat.R
import com.example.wchat.data.remote.api.RetrofitProvider
import com.example.wchat.data.remote.api.WChatApi
import com.example.wchat.data.remote.dto.FcmTokenRequestDto
import com.example.wchat.session.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Mensagem recebida de: ${remoteMessage.from}")

        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            Log.d("FCM", "Payload de Dados: $data")
        }

        val title = data["title"] ?: remoteMessage.notification?.title ?: "Nova mensagem"
        val body = data["body"] ?: remoteMessage.notification?.body ?: "Você recebeu uma nova mensagem."

        if (AppLifecycleTracker.isForeground()) {
            Log.d("FCM", "App em primeiro plano. Mostrando popup in-app.")
            InAppNotificationManager.show(
                InAppNotificationEvent(
                    title = title,
                    body = body,
                    chatId = data["chatId"],
                    collection = data["collection"],
                    remetenteId = data["remetenteId"],
                    remetenteNome = data["remetenteNome"]
                )
            )
            return
        }

        sendNotification(title, body, data)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Novo Token gerado: $token")
        sendTokenToBackend(token)
    }

    private fun sendTokenToBackend(token: String) {
        val sessionManager = SessionManager(this)
        val usuarioId = sessionManager.getBackendUserId()

        if (usuarioId.isNullOrBlank()) {
            Log.w("FCM", "Não foi possível enviar token: usuário backend ausente na sessão.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitProvider.create(this@MyFirebaseMessagingService).create(WChatApi::class.java)
                val response = api.atualizarFcmToken(usuarioId, FcmTokenRequestDto(token))
                if (response.isSuccessful) {
                    Log.d("FCM", "Token FCM enviado ao backend com sucesso.")
                } else {
                    Log.w("FCM", "Falha ao enviar token FCM ao backend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Erro ao enviar token FCM ao backend.", e)
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String, data: Map<String, String>) {
        val channelId = "wchat_messages_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Novas Mensagens",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações para novas mensagens do WChat"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("chatId", data["chatId"])
            putExtra("collection", data["collection"])
            putExtra("remetenteId", data["remetenteId"])
            putExtra("remetenteNome", data["remetenteNome"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_wchat_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}