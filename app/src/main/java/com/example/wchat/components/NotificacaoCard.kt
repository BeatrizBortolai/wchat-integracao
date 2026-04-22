package com.example.wchat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun NotificacaoCard(
    titulo: String,
    descricao: String,
    linkEvento: String?,
    urlSaberMais: String?,
    urlInscrever: String?
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título
            Text(
                text = "🔔 $titulo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            if (descricao.isNotBlank()) {
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!linkEvento.isNullOrBlank()) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "Link do Evento:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = linkEvento,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        uriHandler.openUri(linkEvento!!)
                    }
                )
            }

            val mostrarSaberMais = !urlSaberMais.isNullOrBlank()
            val mostrarInscrever = !urlInscrever.isNullOrBlank()

            if (mostrarSaberMais || mostrarInscrever) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (mostrarSaberMais) {
                        TextButton(onClick = { uriHandler.openUri(urlSaberMais!!) }) {
                            Text("SABER MAIS")
                        }
                    }
                    if (mostrarInscrever) {
                        TextButton(onClick = { uriHandler.openUri(urlInscrever!!) }) {
                            Text("INSCREVER-SE")
                        }
                    }
                }
            }
        }
    }
}