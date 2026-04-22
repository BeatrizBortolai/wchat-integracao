package com.example.wchat.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wchat.model.TipoUsuario
import com.example.wchat.R
import com.example.wchat.ui.theme.Barlow
import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoSegmento
import com.example.wchat.viewmodel.CadastroEvento
import com.example.wchat.viewmodel.CadastroViewModel
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cadastro(navController: NavController, tipoUsuario: TipoUsuario) {
    val viewModel: CadastroViewModel = viewModel()
    val uiState = viewModel.uiState
    val context = LocalContext.current

    val corPrincipal = if (tipoUsuario == TipoUsuario.CLIENTE) colorResource(id = R.color.azul_cinza) else colorResource(id = R.color.laranja_escuro)
    val corBotaoInativo = if (tipoUsuario == TipoUsuario.CLIENTE) colorResource(id = R.color.laranja_escuro) else colorResource(id = R.color.azul_cinza)

    LaunchedEffect(key1 = true) {
        viewModel.evento.collect { evento ->
            when (evento) {
                is CadastroEvento.Sucesso -> {
                    Toast.makeText(context, evento.mensagem, Toast.LENGTH_LONG).show()
                    navController.navigate("telaLogin/${tipoUsuario.name}") {
                        popUpTo("telaInicial") { inclusive = true }
                    }
                }
                is CadastroEvento.Erro -> {
                    Toast.makeText(context, evento.mensagem, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(corPrincipal)
        ) {
            Text(
                text = "WChat",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Barlow,
                color = Color.White,
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp, end = 300.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            navController.navigate("cadastro/CLIENTE") {
                                popUpTo("cadastro/${tipoUsuario.name}") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            if (tipoUsuario == TipoUsuario.CLIENTE) corPrincipal else corBotaoInativo
                        )
                    ) {
                        Text("Cliente", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            navController.navigate("cadastro/OPERADOR") {
                                popUpTo("cadastro/${tipoUsuario.name}") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            if (tipoUsuario == TipoUsuario.OPERADOR) corPrincipal else corBotaoInativo
                        )
                    ) {
                        Text("Operador", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)) {

                        var senhaVisivel by remember { mutableStateOf(false) }
                        var confirmarSenhaVisivel by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = uiState.nome,
                            onValueChange = viewModel::onNomeChange,
                            label = { Text("Nome") },
                            modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            label = { Text("E-mail") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.senha,
                            onValueChange = viewModel::onSenhaChange,
                            label = { Text("Senha") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val imagem = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                val descricao = if (senhaVisivel) "Esconder senha" else "Mostrar senha"

                                IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                    Icon(imageVector = imagem, contentDescription = descricao)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.confirmarSenha,
                            onValueChange = viewModel::onConfirmarSenhaChange,
                            label = { Text("Confirmar Senha") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val imagem = if (confirmarSenhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                val descricao = if (confirmarSenhaVisivel) "Esconder senha" else "Mostrar senha"

                                IconButton(onClick = { confirmarSenhaVisivel = !confirmarSenhaVisivel }) {
                                    Icon(imageVector = imagem, contentDescription = descricao)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (tipoUsuario == TipoUsuario.CLIENTE) {
                            DropdownMenuComponent(
                                label = "Selecione o Grupo",
                                opcoes = TipoGrupo.values().toList(),
                                opcaoSelecionada = uiState.grupoSelecionado,
                                onOpcaoSelecionada = viewModel::onGrupoSelecionado,
                                nomeDaOpcao = { it.nomeExibicao },
                                onLimparSelecao = viewModel::onLimparGrupo
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            MultiSelectDropdownMenu(
                                label = "Selecione os Segmentos",
                                opcoes = TipoSegmento.values().toList(),
                                opcoesSelecionadas = uiState.segmentosSelecionados,
                                onOpcaoClick = viewModel::onSegmentoClicado,
                                nomeDaOpcao = { it.nomeExibicao }
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                        } else {
                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        Button(
                            onClick = { viewModel.registrar(tipoUsuario) },
                            colors = ButtonDefaults.buttonColors(corPrincipal),
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(text = stringResource(id = R.string.signin), color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { navController.navigate("telaInicial") },
                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.azul_escuro)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(id = R.string.back), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownMenuComponent(
    label: String,
    opcoes: List<T>,
    opcaoSelecionada: T?,
    onOpcaoSelecionada: (T) -> Unit,
    nomeDaOpcao: (T) -> String,
    onLimparSelecao: (() -> Unit)? = null
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (opcaoSelecionada != null) nomeDaOpcao(opcaoSelecionada) else "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            if (onLimparSelecao != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Limpar seleção",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onLimparSelecao()
                        expandido = false
                    }
                )
                HorizontalDivider()
            }
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(nomeDaOpcao(opcao)) },
                    onClick = {
                        onOpcaoSelecionada(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> MultiSelectDropdownMenu(
    label: String,
    opcoes: List<T>,
    opcoesSelecionadas: List<T>,
    onOpcaoClick: (T) -> Unit,
    nomeDaOpcao: (T) -> String
) {
    var expandido by remember { mutableStateOf(false) }

    val textoExibido = when (opcoesSelecionadas.size) {
        0 -> ""
        1 -> nomeDaOpcao(opcoesSelecionadas.first())
        else -> "${opcoesSelecionadas.size} segmentos selecionados"
    }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = textoExibido,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opcoes.forEach { opcao ->
                val isSelected = opcoesSelecionadas.contains(opcao)
                DropdownMenuItem(
                    text = { Text(nomeDaOpcao(opcao)) },
                    onClick = {
                        onOpcaoClick(opcao)
                    },
                    leadingIcon = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null
                        )
                    }
                )
            }
        }
    }
}