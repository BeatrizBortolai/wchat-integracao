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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wchat.model.TipoUsuario
import com.example.wchat.R
import com.example.wchat.ui.theme.Barlow
import com.example.wchat.viewmodel.LoginEvento
import com.example.wchat.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(navController: NavController, tipoUsuario: TipoUsuario) {

    val viewModel: LoginViewModel = viewModel()
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val corPrincipal = if (tipoUsuario == TipoUsuario.CLIENTE) colorResource(id = R.color.azul_cinza) else colorResource(id = R.color.laranja_escuro)
    val corBotaoInativo = if (tipoUsuario == TipoUsuario.CLIENTE) colorResource(id = R.color.laranja_escuro) else colorResource(id = R.color.azul_cinza)

    LaunchedEffect(key1 = true) {
        viewModel.evento.collect { evento ->
            when (evento) {
                is LoginEvento.Sucesso -> {
                    Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    navController.navigate("main/${evento.usuario.tipo.name}") {
                        popUpTo("telaInicial") { inclusive = true }
                    }
                }
                is LoginEvento.Erro -> {
                    Toast.makeText(context, evento.mensagem, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(corPrincipal)
        ) {
            Text(
                text = "WChat", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                fontFamily = Barlow, color = Color.White,
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
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Button(
                        onClick = { navController.navigate("telaLogin/${TipoUsuario.CLIENTE}") { popUpTo("telaLogin/${tipoUsuario.name}") { inclusive = true } } },
                        colors = ButtonDefaults.buttonColors(if (tipoUsuario == TipoUsuario.CLIENTE) corPrincipal else corBotaoInativo)
                    ) {
                        Text("Cliente", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate("telaLogin/${TipoUsuario.OPERADOR}") { popUpTo("telaLogin/${tipoUsuario.name}") { inclusive = true } } },
                        colors = ButtonDefaults.buttonColors(if (tipoUsuario == TipoUsuario.OPERADOR) corPrincipal else corBotaoInativo)
                    ) {
                        Text("Operador", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Text(text = stringResource(id = R.string.subtitle), modifier = Modifier.padding(50.dp, 5.dp), textAlign = TextAlign.Center)
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)) {
                        var senhaVisivel by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = stringResource(id = R.string.email)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.senha,
                            onValueChange = viewModel::onSenhaChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = stringResource(id = R.string.password)) },
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
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.login(tipoUsuario, context) },
                            enabled = !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(corPrincipal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(text = stringResource(id = R.string.enter), color = Color.White)
                            }
                        }
                        Spacer (modifier = Modifier.height(10.dp))

                        Button (
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