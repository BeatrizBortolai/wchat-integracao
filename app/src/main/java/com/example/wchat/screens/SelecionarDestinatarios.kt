package com.example.wchat.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Segment
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wchat.R
import com.example.wchat.components.WChatTopBar
import com.example.wchat.model.Grupo
import com.example.wchat.model.Notificacao
import com.example.wchat.model.Segmento
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import com.example.wchat.viewmodel.NotificacaoEvento
import com.example.wchat.viewmodel.NotificacaoViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarDestinatariosScreen(
    navController: NavController,
    notificacao: Notificacao,
    viewModel: NotificacaoViewModel = viewModel()
) {
    val uiState by viewModel.destinatariosState.collectAsState()
    val context = LocalContext.current

    val gruposFiltrados = remember(uiState.grupos, uiState.textoBusca) {
        uiState.grupos.filter { it.id.contains(uiState.textoBusca, ignoreCase = true) }
    }
    val segmentosFiltrados = remember(uiState.segmentos, uiState.textoBusca) {
        uiState.segmentos.filter { it.id.contains(uiState.textoBusca, ignoreCase = true) }
    }
    val usuariosFiltrados = remember(uiState.usuarios, uiState.textoBusca) {
        uiState.usuarios.filter { it.nome.contains(uiState.textoBusca, ignoreCase = true) }
    }

    LaunchedEffect(key1 = true) {
        viewModel.carregarDestinatarios()
    }

    LaunchedEffect(key1 = true) {
        viewModel.evento.collect { evento ->
            when (evento) {
                is NotificacaoEvento.SucessoEnvio -> {
                    Toast.makeText(context, evento.mensagem, Toast.LENGTH_LONG).show()
                    navController.popBackStack("main", false)
                    viewModel.resetarEstadoDeEnvio()
                }
                is NotificacaoEvento.Erro -> {
                    Toast.makeText(context, evento.mensagem, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            WChatTopBar(
                tipoUsuario = TipoUsuario.OPERADOR,
                titulo = "Selecionar Destinatários",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            OutlinedTextField(
                value = uiState.textoBusca,
                onValueChange = viewModel::onBuscaChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar grupos, segmentos, clientes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (gruposFiltrados.isNotEmpty()) {
                        item {
                            SecaoGrupos(
                                grupos = gruposFiltrados,
                                destinatariosSelecionados = uiState.destinatariosSelecionados,
                                onToggleGrupo = viewModel::onDestinatarioToggle
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    if (segmentosFiltrados.isNotEmpty()) {
                        item {
                            SecaoSegmentos(
                                segmentos = segmentosFiltrados,
                                destinatariosSelecionados = uiState.destinatariosSelecionados,
                                onToggleSegmento = viewModel::onDestinatarioToggle
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    if (usuariosFiltrados.isNotEmpty()) {
                        item {
                            Text(
                                text = "Clientes",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(usuariosFiltrados, key = { it.id }) { usuario ->
                            ContatoItem(
                                usuario = usuario,
                                isSelected = uiState.destinatariosSelecionados.contains(usuario),
                                onToggle = { viewModel.onDestinatarioToggle(usuario) }
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.enviarNotificacaoComoMensagem(notificacao) },
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.laranja_escuro)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !uiState.isEnviando
                ) {
                    if (uiState.isEnviando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(text ="ENVIAR NOTIFICAÇÃO", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SecaoGrupos(
    grupos: List<Grupo>,
    destinatariosSelecionados: Set<Any>,
    onToggleGrupo: (Grupo) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Group, contentDescription = "Grupos", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grupos", style = MaterialTheme.typography.titleMedium)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(grupos, key = { it.id }) { grupo ->
                ChipSelecionavel(
                    texto = grupo.id,
                    isSelected = destinatariosSelecionados.contains(grupo),
                    onSelectChange = { onToggleGrupo(grupo) }
                )
            }
        }
    }
}

@Composable
fun SecaoSegmentos(
    segmentos: List<Segmento>,
    destinatariosSelecionados: Set<Any>,
    onToggleSegmento: (Segmento) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.Segment, contentDescription = "Segmentos", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Segmentos", style = MaterialTheme.typography.titleMedium)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(segmentos, key = { it.id }) { segmento ->
                ChipSelecionavel(
                    texto = segmento.id,
                    isSelected = destinatariosSelecionados.contains(segmento),
                    onSelectChange = { onToggleSegmento(segmento) }
                )
            }
        }
    }
}


@Composable
fun ContatoItem(
    usuario: Usuario,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = usuario.nome.firstOrNull()?.toString()?.uppercase() ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = usuario.nome,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = "Selecionar",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipSelecionavel(
    texto: String,
    isSelected: Boolean,
    onSelectChange: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelectChange,
        label = { Text(texto) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Filled.Done, contentDescription = "Selecionado", modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else {
            null
        }
    )
}
