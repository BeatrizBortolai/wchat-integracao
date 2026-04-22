package com.example.wchat.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wchat.model.Mensagem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.identity.util.UUID
import com.example.wchat.components.MessageBubble
import com.example.wchat.components.NotificacaoCard
import com.example.wchat.model.TipoChat
import com.example.wchat.model.TipoUsuario
import com.example.wchat.viewmodel.ChatViewModel
import com.example.wchat.viewmodel.ChatViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    tipoChat: TipoChat,
    chatId: String,
    chatNome: String,
    onVoltarClick: () -> Unit,
    navController: NavController,
    tipoUsuarioLogado: TipoUsuario
) {
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(tipoChat, chatId))
    val mensagens by viewModel.mensagens.collectAsState()
    val lazyListState = rememberLazyListState()
    var mensagemParaExcluir by remember { mutableStateOf<Mensagem?>(null) }

    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = tipoChat == TipoChat.UM_A_UM,
                                onClick = {
                                    val idUsuarioAtual = Firebase.auth.currentUser?.uid
                                    val idOutroUsuario =
                                        chatId
                                            .split("_")
                                            .find { it != idUsuarioAtual }

                                    if (idOutroUsuario != null) {
                                        navController.navigate("perfil/$idOutroUsuario/${tipoUsuarioLogado.name}")
                                    }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chatNome.firstOrNull()?.uppercase() ?: "?",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(chatNome)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVoltarClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            BarraDeEnvioDeMensagem(
                onEnviarClick = { texto ->
                    val idEnvio = UUID.randomUUID().toString()
                    viewModel.enviarMensagem(texto, idEnvio)
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            items(mensagens, key = { it.id }) { mensagem ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { },
                            onLongClick = {
                                if (mensagem.remetenteId == viewModel.usuarioAtualId) {
                                    mensagemParaExcluir = mensagem
                                }
                            }
                        )
                ) {
                    if (mensagem.texto.startsWith("[NOTIFICATION]")) {
                        val notificationData = mensagem.texto.lines()
                            .drop(1)
                            .filter { it.contains("=") }
                            .associate {
                                val (key, value) = it.split("=", limit = 2)
                                key.trim() to value.trim()
                            }

                        val titulo = notificationData["titulo"] ?: "Notificação"
                        val descricao = notificationData["descricao"] ?: ""
                        val linkEvento = notificationData["linkEvento"]
                        val urlSaberMais = notificationData["urlSaberMais"]
                        val urlInscrever = notificationData["urlInscrever"]

                        NotificacaoCard(
                            titulo = titulo,
                            descricao = descricao,
                            linkEvento = linkEvento,
                            urlSaberMais = urlSaberMais,
                            urlInscrever = urlInscrever
                        )

                    } else {
                        MessageBubble(message = mensagem)
                    }
                }
            }
        }
    }

    mensagemParaExcluir?.let { msg ->
        AlertDialog(
            onDismissRequest = { mensagemParaExcluir = null },
            title = { Text("Excluir Mensagem") },
            text = { Text("Você tem certeza de que deseja excluir esta mensagem? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.excluirMensagem(msg)
                        mensagemParaExcluir = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mensagemParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BarraDeEnvioDeMensagem(onEnviarClick: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }

    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Digite uma mensagem...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (texto.isNotBlank()) {
                        onEnviarClick(texto)
                        texto = ""
                    }
                },
                enabled = texto.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
            }
        }
    }
}