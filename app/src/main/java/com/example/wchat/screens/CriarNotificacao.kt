package com.example.wchat.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wchat.R
import com.example.wchat.components.WChatTopBar
import com.example.wchat.model.TipoUsuario
import com.example.wchat.viewmodel.NotificacaoEvento
import com.example.wchat.viewmodel.NotificacaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarNotificacaoScreen(navController: NavController) {
    val viewModel: NotificacaoViewModel = viewModel()
    val formState = viewModel.formState
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.evento.collect { evento ->
            when (evento) {
                is NotificacaoEvento.NavegarParaDestinatarios -> {
                    navController.navigate(evento.rota)
                }
                is NotificacaoEvento.Erro -> {
                    Toast.makeText(context, evento.mensagem, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = { WChatTopBar(tipoUsuario = TipoUsuario.OPERADOR) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Criar Nova Notificação",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = formState.titulo,
                onValueChange = viewModel::onTituloChange,
                label = { Text("Título da Notificação*") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.descricao,
                onValueChange = viewModel::onDescricaoChange,
                label = { Text("Descrição (texto principal)") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Dados e Links da Campanha", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = formState.nomeCampanha,
                onValueChange = viewModel::onNomeCampanhaChange,
                label = { Text("Nome da Campanha* (Uso interno)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.linkEvento,
                onValueChange = viewModel::onLinkEventoChange,
                label = { Text("Link Visível no Texto (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.urlSaberMais,
                onValueChange = viewModel::onUrlSaberMaisChange,
                label = { Text("URL para o Botão 'Saber Mais' (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.urlInscrever,
                onValueChange = viewModel::onUrlInscreverChange,
                label = { Text("URL para o Botão 'Inscrever-se' (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.onAvancarParaDestinatarios()
                },
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.laranja_escuro)),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(text = "SELECIONAR DESTINATÁRIOS", color = Color.White)
            }
        }
    }
}