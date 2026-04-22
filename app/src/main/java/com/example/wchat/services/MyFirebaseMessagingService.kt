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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Mensagem recebida de: ${remoteMessage.from}")

        if (AppLifecycleTracker.isForeground()) {
            Log.d("FCM", "App está em primeiro plano. Suprimindo notificação de sistema (push).")
            return
        }

        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            Log.d("FCM", "Payload de Dados: $data")
        }

        val title = data["title"]
        val body = data["body"]

        if (title != null && body != null) {
            sendNotification(title, body, data)
        } else {
            Log.w("FCM", "Título ou corpo da notificação ausentes no payload de dados.")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Novo Token gerado: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String?) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null && token != null) {
            Firebase.firestore.collection("usuarios").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener { Log.d("FCM", "Token atualizado no Firestore com sucesso.") }
                .addOnFailureListener { e -> Log.w("FCM", "Falha ao atualizar o token.", e) }
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
            putExtra("remetenteNome", data["remetenteNome"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val finalTitle = title
        val finalBody = messageBody
        val finalStyle: NotificationCompat.Style = NotificationCompat.BigTextStyle().bigText(messageBody)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_wchat_notification)
            .setContentTitle(finalTitle)
            .setContentText(finalBody)
            .setStyle(finalStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}