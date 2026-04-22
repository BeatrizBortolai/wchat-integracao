package com.example.wchat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wchat.model.Mensagem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun MessageBubble(message: Mensagem) {
    val idUsuarioAtual = Firebase.auth.currentUser?.uid
    val isMinhaMensagem = message.remetenteId == idUsuarioAtual

    val containerColor = if (isMinhaMensagem) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = if (isMinhaMensagem) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        if (!isMinhaMensagem) {
            Text(
                text = message.remetenteNome,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .background(containerColor, shape = RoundedCornerShape(12.dp))
                .padding(10.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(text = message.texto)
        }
    }
}