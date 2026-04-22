package com.example.wchat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wchat.model.Mensagem


@Composable
fun InAppNotification(
    notificationInfo: Pair<Mensagem, String?>?,
    onNotificationClick: (Mensagem) -> Unit,
    onDismiss: () -> Unit
) {
    val (mensagem, nomeDoChat) = notificationInfo ?: Pair(null, null)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = mensagem != null && nomeDoChat != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(durationMillis = 200))
        ) {
            if (mensagem != null && nomeDoChat != null) {
                Box(modifier = Modifier.clickable { onNotificationClick(mensagem) }) {
                    val descricaoFinal = if (nomeDoChat == mensagem.remetenteNome) {
                        mensagem.texto
                    } else {
                        "${mensagem.remetenteNome}: ${mensagem.texto}"
                    }
                    PopupNotificationCard(
                        titulo = nomeDoChat,
                        descricao = descricaoFinal
                    )
                }
            }
        }
    }
}