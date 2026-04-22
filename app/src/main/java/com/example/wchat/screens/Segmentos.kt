package com.example.wchat.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.wchat.components.SecaoHeader
import com.example.wchat.model.TipoSegmento
import com.example.wchat.model.TipoUsuario
import com.example.wchat.viewmodel.ConversasViewModel
import com.example.wchat.viewmodel.SegmentosUiState
import com.example.wchat.viewmodel.SegmentosViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun SegmentosScreen(
    navController: NavController,
    tipoUsuarioLogado: TipoUsuario,
    textoBusca: String,
    segmentosViewModel: SegmentosViewModel = viewModel(),
    conversasViewModel: ConversasViewModel = viewModel()
) {
    val uiState by segmentosViewModel.uiState.collectAsState()

    val contagensNaoLidas by conversasViewModel.contagensNaoLidas.collectAsState()

    Scaffold { innerPadding ->
        ListaDeSegmentosComSecoes(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            todosOsTiposDeSegmento = TipoSegmento.todos(),
            contagensNaoLidas = contagensNaoLidas,
            textoBusca = textoBusca,
            onSegmentoClick = { idDoSegmento ->
                navController.navigate("chatSegmento/$idDoSegmento/${tipoUsuarioLogado.name}")
            }
        )
    }
}


@Composable
private fun ListaDeSegmentosComSecoes(
    modifier: Modifier = Modifier,
    uiState: SegmentosUiState,
    todosOsTiposDeSegmento: List<TipoSegmento>,
    contagensNaoLidas: Map<String, Int>,
    textoBusca: String,
    onSegmentoClick: (String) -> Unit
) {
    val meusSegmentos = todosOsTiposDeSegmento
        .filter { it.name in uiState.idsSegmentosDoUsuario }
        .filter { it.nomeExibicao.contains(textoBusca, ignoreCase = true) }

    val outrosSegmentos = todosOsTiposDeSegmento
        .filter { it.name !in uiState.idsSegmentosDoUsuario }
        .filter { it.nomeExibicao.contains(textoBusca, ignoreCase = true) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (meusSegmentos.isNotEmpty()) {
            item { SecaoHeader(texto = "Seus Segmentos") }
            items(meusSegmentos) { tipoDeSegmento ->
                val dadosDoSegmento = uiState.todosOsSegmentos.find { it.id == tipoDeSegmento.name }
                val contagemParticipantes = dadosDoSegmento?.participantesIds?.size ?: 0
                val contagemMsgsNaoLidas = contagensNaoLidas["segmentos_${tipoDeSegmento.name}"] ?: 0

                SegmentoItem(
                    nome = tipoDeSegmento.nomeExibicao,
                    icone = tipoDeSegmento.icone,
                    participantes = contagemParticipantes,
                    mensagensNaoLidas = contagemMsgsNaoLidas,
                    onClick = { onSegmentoClick(tipoDeSegmento.name) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }

        if (outrosSegmentos.isNotEmpty()) {
            item { SecaoHeader(texto = "Explorar Segmentos") }
            items(outrosSegmentos) { tipoDeSegmento ->
                val dadosDoSegmento = uiState.todosOsSegmentos.find { it.id == tipoDeSegmento.name }
                val contagemParticipantes = dadosDoSegmento?.participantesIds?.size ?: 0
                val contagemMsgsNaoLidas = contagensNaoLidas["segmentos_${tipoDeSegmento.name}"] ?: 0

                SegmentoItem(
                    nome = tipoDeSegmento.nomeExibicao,
                    icone = tipoDeSegmento.icone,
                    participantes = contagemParticipantes,
                    mensagensNaoLidas = contagemMsgsNaoLidas,
                    onClick = { onSegmentoClick(tipoDeSegmento.name) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentoItem(
    nome: String,
    icone: ImageVector,
    participantes: Int,
    mensagensNaoLidas: Int,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(nome, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Participantes: $participantes") },
        leadingContent = {
            Icon(
                imageVector = icone,
                contentDescription = nome
            )
        },
        trailingContent = {
            if (mensagensNaoLidas > 0) {
                Badge { Text("$mensagensNaoLidas") }
            }
        }
    )
}
