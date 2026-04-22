package com.example.wchat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoSegmento
import com.example.wchat.viewmodel.ConversasViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


@Composable
fun Conversas(
    mainNavController: NavController,
    textoBusca: String,
    viewModel: ConversasViewModel,
    tipoUsuarioLogado: TipoUsuario
) {
    var filtrosAtivos by remember { mutableStateOf<Set<String>>(emptySet()) }

    val estadoDosUsuarios by viewModel.estadoDosUsuarios.collectAsState()
    val contagensNaoLidas by viewModel.contagensNaoLidas.collectAsState()
    val usuarioAtualId = Firebase.auth.currentUser?.uid

    Column(modifier = Modifier.fillMaxSize()) {
        BarraDeFiltros(
            filtrosAtivos = filtrosAtivos,
            onFiltroClick = { tag ->
                filtrosAtivos = if (filtrosAtivos.contains(tag)) {
                    filtrosAtivos - tag
                } else {
                    filtrosAtivos + tag
                }
            }
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val resultado = estadoDosUsuarios
            if (resultado == null) {
                CircularProgressIndicator()
            } else if (resultado.isSuccess) {
                val outrosUsuarios = resultado.getOrNull()?.filter { it.id != usuarioAtualId } ?: emptyList()
                val todosOperadores = outrosUsuarios.filter { it.tipo == TipoUsuario.OPERADOR }
                val todosClientes = outrosUsuarios.filter { it.tipo == TipoUsuario.CLIENTE }

                val listaBaseParaFiltrar = if (filtrosAtivos.isEmpty()) {
                    todosOperadores + todosClientes
                } else {
                    todosClientes
                }

                val usuariosFiltrados = listaBaseParaFiltrar.filter { usuario ->
                    val correspondeAoTexto = usuario.nome.contains(textoBusca, ignoreCase = true)

                    val correspondeAosFiltros = if (filtrosAtivos.isEmpty()) {
                        true
                    } else {
                        val tagsDoUsuarioComPrefixo = mutableSetOf<String>()
                        usuario.cargo?.let { tagsDoUsuarioComPrefixo.add("grupo_$it") }
                        usuario.segmentos.forEach { tagsDoUsuarioComPrefixo.add("segmento_$it") }

                        tagsDoUsuarioComPrefixo.containsAll(filtrosAtivos)
                    }

                    correspondeAoTexto && correspondeAosFiltros
                }

                if (usuariosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum usuário encontrado.")
                    }
                } else {
                    ListaDeConversas(
                        usuarios = usuariosFiltrados,
                        usuarioAtualId = usuarioAtualId,
                        contagensNaoLidas = contagensNaoLidas,
                        onConversaClick = { usuarioClicado ->
                            mainNavController.navigate("chat1a1/${usuarioClicado.id}/${usuarioClicado.nome}/${tipoUsuarioLogado.name}")
                        }
                    )
                }
            } else {
                Text("Falha ao carregar usuários.")
            }
        }
    }
}

@Composable
private fun BarraDeFiltros(
    filtrosAtivos: Set<String>,
    onFiltroClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Grupos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TipoGrupo.values()) { grupo ->
                val filtroId = "grupo_${grupo.name}"
                IconeFiltro(
                    texto = grupo.nomeExibicao,
                    icone = grupo.icone,
                    selecionado = filtrosAtivos.contains(filtroId),
                    onClick = { onFiltroClick(filtroId) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Segmentos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TipoSegmento.values()) { segmento ->
                val filtroId = "segmento_${segmento.name}"
                IconeFiltro(
                    texto = segmento.nomeExibicao,
                    icone = segmento.icone,
                    selecionado = filtrosAtivos.contains(filtroId),
                    onClick = { onFiltroClick(filtroId) }
                )
            }
        }
    }
}

@Composable
private fun IconeFiltro(
    texto: String,
    icone: ImageVector,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    val corDeFundo = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val corDoConteudo = if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = corDeFundo),
        elevation = CardDefaults.cardElevation(if (selecionado) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = texto,
                tint = corDoConteudo,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = texto,
                color = corDoConteudo,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
private fun ListaDeConversas(
    usuarios: List<Usuario>,
    usuarioAtualId: String?,
    contagensNaoLidas: Map<String, Int>,
    onConversaClick: (Usuario) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(usuarios) { usuario ->
            val chatId = if (usuarioAtualId != null) {
                listOf(usuarioAtualId, usuario.id).sorted().joinToString("_")
            } else {
                ""
            }
            val contagem = contagensNaoLidas[chatId] ?: 0

            ConversaItem(
                usuario = usuario,
                contagemNaoLidas = contagem,
                onClick = { onConversaClick(usuario) }
            )
        }
    }
}
@Composable
private fun ConversaItem(
    usuario: Usuario,
    contagemNaoLidas: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = usuario.nome.firstOrNull()?.toString()?.uppercase() ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = usuario.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            when (usuario.tipo) {
                TipoUsuario.OPERADOR -> {
                    Text(
                        text = "OPERADOR",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
                TipoUsuario.CLIENTE -> {
                    if (usuario.cargo != null) {
                        Text(
                            text = usuario.cargo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "CLIENTE",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        if (contagemNaoLidas > 0) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contagemNaoLidas.toString(),
                    color = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}