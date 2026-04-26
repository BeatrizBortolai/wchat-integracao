package com.example.wchat.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wchat.R
import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoSegmento
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import com.example.wchat.viewmodel.EditarPerfilViewModel
import com.example.wchat.viewmodel.UpdateStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    navController: NavController,
    viewModel: EditarPerfilViewModel = viewModel()
) {
    val usuario by viewModel.usuario.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateStatus) {
        if (updateStatus == UpdateStatus.SUCCESS) {
            if (Firebase.auth.currentUser == null) {
                snackbarHostState.showSnackbar("Conta excluída com sucesso.")
                navController.navigate("telaInicial") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                snackbarHostState.showSnackbar("Perfil atualizado com sucesso!")
            }
            viewModel.resetUpdateStatus()
        } else if (updateStatus == UpdateStatus.ERROR) {
            snackbarHostState.showSnackbar("Operação falhou. Verifique seus dados.")
            viewModel.resetUpdateStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Atualizar Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (usuario == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ConteudoEditarPerfil(
                usuario = usuario!!,
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ConteudoEditarPerfil(
    usuario: Usuario,
    modifier: Modifier = Modifier,
    viewModel: EditarPerfilViewModel

) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var nome by remember(usuario.nome) { mutableStateOf(usuario.nome) }
    var email by remember(usuario.email) { mutableStateOf(usuario.email) }
    var mostrarDialogoNovaSenha by remember { mutableStateOf(false) }
    var mostrarDialogoExcluirConta by remember { mutableStateOf(false) }

    if (mostrarDialogoNovaSenha) {
        NovaSenhaDialog(
            onConfirm = { senhaAtual, novaSenha ->
                mostrarDialogoNovaSenha = false
                viewModel.atualizarSenha(novaSenha, senhaAtual)
            },
            onDismiss = { mostrarDialogoNovaSenha = false }
        )
    }

    if (mostrarDialogoExcluirConta) {
        ReautenticacaoDialog(
            onConfirm = { senhaAtual ->
                mostrarDialogoExcluirConta = false
                viewModel.deletarConta(senhaAtual, context)
            },
            onDismiss = { mostrarDialogoExcluirConta = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = usuario.nome.firstOrNull()?.uppercase() ?: "?",
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { },
            label = { Text("E-mail (não pode ser alterado)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val nomeMudou = nome != usuario.nome
                if (nomeMudou) {
                    viewModel.salvarAlteracoes(novoNome = nome)
                }
            },
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.azul_escuro)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Alterações", color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { mostrarDialogoNovaSenha = true },
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.azul_cinza)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Alterar Senha", color = Color.White)
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 24.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        Text(
            text = "Tipo de Usuário",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            InfoChip(
                texto = usuario.tipo.name.replaceFirstChar { it.uppercase() },
                icone = if (usuario.tipo == TipoUsuario.CLIENTE) Icons.Default.Person else Icons.Default.SupervisedUserCircle
            )
        }
        Spacer(Modifier.height(16.dp))

        usuario.cargo?.let { cargoStr ->
            val tipoGrupo = remember(cargoStr) { TipoGrupo.values().find { it.name == cargoStr } }
            if (tipoGrupo != null) {
                Text(
                    text = "Grupo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    InfoChip(
                        texto = tipoGrupo.nomeExibicao,
                        icone = tipoGrupo.icone
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        if (usuario.segmentos.isNotEmpty()) {
            Text(
                text = "Segmentos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(usuario.segmentos, key = { it }) { segmentoStr ->
                    val tipoSegmento = remember(segmentoStr) { TipoSegmento.values().find { it.name == segmentoStr } }
                    if (tipoSegmento != null) {
                        InfoChip(
                            texto = tipoSegmento.nomeExibicao,
                            icone = tipoSegmento.icone
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 24.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        OutlinedButton(
            onClick = { mostrarDialogoExcluirConta = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Text("Excluir Conta")
        }
    }
}

@Composable
fun ReautenticacaoDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirme sua identidade") },
        text = {
            Column {
                Text("Por segurança, digite sua senha atual para continuar.")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha atual") },
                    singleLine = true,
                    visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                            Icon(image, contentDescription = "Mostrar/esconder senha")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(senha) },
                enabled = senha.isNotBlank()
            ) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun NovaSenhaDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var senhaAtual by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alterar Senha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = senhaAtual,
                    onValueChange = { senhaAtual = it },
                    label = { Text("Senha atual") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = novaSenha,
                    onValueChange = { novaSenha = it },
                    label = { Text("Nova senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(senhaAtual, novaSenha) },
                enabled = senhaAtual.isNotBlank() && novaSenha.length >= 6
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoChip(texto: String, icone: ImageVector) {
    AssistChip(
        onClick = {  },
        label = { Text(texto) },
        leadingIcon = {
            Icon(
                imageVector = icone,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}