package com.example.wchat.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.values
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoSegmento
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import com.example.wchat.viewmodel.PerfilViewModel
import com.example.wchat.viewmodel.PerfilViewModelFactory
import kotlin.text.firstOrNull
import kotlin.text.uppercase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    usuarioId: String,
    tipoUsuarioLogado: TipoUsuario
) {
    val viewModel: PerfilViewModel = viewModel(factory = PerfilViewModelFactory(usuarioId))
    val usuario by viewModel.usuario.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val anotacoesSalvas by viewModel.anotacoesSalvas.collectAsState()

    var anotacoesText by remember(usuario) { mutableStateOf(usuario?.anotacoesOperador ?: "") }
    val context = LocalContext.current

    LaunchedEffect(anotacoesSalvas) {
        if (anotacoesSalvas) {
            Toast.makeText(context, "Anotações salvas com sucesso!", Toast.LENGTH_SHORT).show()
            viewModel.onAnotacoesSalvasConfirmado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            usuario?.let {
                PerfilContent(
                    modifier = Modifier.padding(innerPadding),
                    usuario = it,
                    anotacoes = anotacoesText,
                    onAnotacoesChange = { newText -> anotacoesText = newText },
                    onSalvarClick = { viewModel.salvarAnotacoes(anotacoesText) },
                    tipoUsuarioLogado = tipoUsuarioLogado
                )
            } ?: run {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("Usuário não encontrado.")
                }
            }
        }
    }
}

@Composable
private fun PerfilContent(
    modifier: Modifier = Modifier,
    usuario: Usuario,
    anotacoes: String,
    onAnotacoesChange: (String) -> Unit,
    onSalvarClick: () -> Unit,
    tipoUsuarioLogado: TipoUsuario
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Avatar, Nome, Contato)
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = usuario.nome.firstOrNull()?.uppercase() ?: "?",
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(usuario.nome, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        InfoSection(title = "Contato", icon = Icons.Default.Email) {
            Text(usuario.email, style = MaterialTheme.typography.bodyLarge)
        }

        val tagsUnificadas = mutableListOf<String>()
        usuario.cargo?.takeIf { it.isNotBlank() }?.let { cargo ->
            val nomeExibicaoGrupo = TipoGrupo.values().find { it.name == cargo }?.nomeExibicao ?: cargo
            tagsUnificadas.add(nomeExibicaoGrupo)
        }
        usuario.segmentos.forEach { segmentoStr ->
            val nomeExibicaoSegmento = TipoSegmento.values().find { it.name == segmentoStr }?.nomeExibicao ?: segmentoStr
            tagsUnificadas.add(nomeExibicaoSegmento)
        }
        if (tagsUnificadas.isNotEmpty()) {
            InfoSection(title = "Tags", icon = Icons.Default.Style) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tagsUnificadas) { tag ->
                        Chip(texto = tag)
                    }
                }
            }
        }

        if (tipoUsuarioLogado == TipoUsuario.OPERADOR) {
            InfoSection(title = "Anotações Rápidas", icon = Icons.Default.Info) {
                OutlinedTextField(
                    value = anotacoes,
                    onValueChange = onAnotacoesChange,
                    label = { Text("Anotações visíveis apenas para operadores") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onSalvarClick, modifier = Modifier.align(Alignment.End)) {
                    Text("Salvar")
                }
            }
        }
    }
}

@Composable
private fun InfoSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(24.dp))
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(
            Modifier.padding(vertical = 8.dp),
            DividerDefaults.Thickness,
            DividerDefaults.color
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Chip(texto: String) {
    SuggestionChip(onClick = {}, label = { Text(texto) })
}