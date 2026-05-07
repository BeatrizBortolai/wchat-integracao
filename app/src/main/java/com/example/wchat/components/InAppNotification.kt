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
import com.example.wchat.viewmodel.PopupNotificationInfo

@Composable
fun InAppNotification(
    notificationInfo: PopupNotificationInfo?,
    onNotificationClick: (PopupNotificationInfo) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = notificationInfo != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(durationMillis = 200))
        ) {
            notificationInfo?.let { info ->
                Box(modifier = Modifier.clickable { onNotificationClick(info) }) {
                    PopupNotificationCard(
                        titulo = info.titulo,
                        descricao = info.descricao
                    )
                }
            }
        }
    }
}
