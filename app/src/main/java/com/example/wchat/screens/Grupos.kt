package com.example.wchat.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.NavController
import com.example.wchat.components.SecaoHeader
import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoUsuario
import com.example.wchat.viewmodel.ConversasViewModel
import com.example.wchat.viewmodel.GruposUiState
import com.example.wchat.viewmodel.GruposViewModel

@Composable
fun GruposScreen(
    navController: NavController,
    tipoUsuarioLogado: TipoUsuario,
    textoBusca: String,
    gruposViewModel: GruposViewModel = viewModel(),
    conversasViewModel: ConversasViewModel = viewModel()
) {
    val uiState by gruposViewModel.uiState.collectAsState()

    val contagensNaoLidas by conversasViewModel.contagensNaoLidas.collectAsState()

    Scaffold { innerPadding ->
        ListaDeGruposComSecoes(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            todosOsTiposDeGrupo = TipoGrupo.todos(),
            contagensNaoLidas = contagensNaoLidas,
            textoBusca = textoBusca,
            onGrupoClick = { idDoGrupo ->
                navController.navigate("chatGrupo/$idDoGrupo/${tipoUsuarioLogado.name}")
            }
        )
    }
}

@Composable
private fun ListaDeGruposComSecoes(
    modifier: Modifier = Modifier,
    uiState: GruposUiState,
    todosOsTiposDeGrupo: List<TipoGrupo>,
    contagensNaoLidas: Map<String, Int>,
    textoBusca: String,
    onGrupoClick: (String) -> Unit
) {
    val meuGrupo = todosOsTiposDeGrupo.find { it.name == uiState.idGrupoDoUsuario }
    val outrosGrupos = todosOsTiposDeGrupo
        .filter { it.name != uiState.idGrupoDoUsuario }
        .filter {
            it.nomeExibicao.contains(textoBusca, ignoreCase = true)
        }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (meuGrupo != null && textoBusca.isEmpty()) {
            item {
                SecaoHeader(texto = "Seu Grupo")
            }
            item {
                val dadosDoGrupo = uiState.todosOsGrupos.find { it.id == meuGrupo.name }
                val contagemParticipantes = dadosDoGrupo?.participantesIds?.size ?: 0
                val contagemMsgsNaoLidas = contagensNaoLidas["grupos_${meuGrupo.name}"] ?: 0

                GrupoItem(
                    nome = meuGrupo.nomeExibicao,
                    icone = meuGrupo.icone,
                    participantes = contagemParticipantes,
                    mensagensNaoLidas = contagemMsgsNaoLidas,
                    onClick = { onGrupoClick(meuGrupo.name) }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        if (outrosGrupos.isNotEmpty()) {
            item {
                SecaoHeader(texto = "Explorar Grupos")
            }
            items(outrosGrupos) { tipoDeGrupo ->
                val dadosDoGrupo = uiState.todosOsGrupos.find { it.id == tipoDeGrupo.name }
                val contagemParticipantes = dadosDoGrupo?.participantesIds?.size ?: 0
                val contagemMsgsNaoLidas = contagensNaoLidas["grupos_${tipoDeGrupo.name}"] ?: 0

                GrupoItem(
                    nome = tipoDeGrupo.nomeExibicao,
                    icone = tipoDeGrupo.icone,
                    participantes = contagemParticipantes,
                    mensagensNaoLidas = contagemMsgsNaoLidas,
                    onClick = { onGrupoClick(tipoDeGrupo.name) }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrupoItem(
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
