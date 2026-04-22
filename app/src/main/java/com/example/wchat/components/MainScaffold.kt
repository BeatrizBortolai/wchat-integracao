package com.example.wchat.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wchat.model.Grupo
import com.example.wchat.model.Notificacao
import com.example.wchat.model.Segmento
import com.example.wchat.model.TipoChat
import com.example.wchat.model.TipoUsuario
import com.example.wchat.screens.ChatScreen
import com.example.wchat.screens.Conversas
import com.example.wchat.screens.CriarNotificacaoScreen
import com.example.wchat.screens.GruposScreen
import com.example.wchat.screens.SegmentosScreen
import com.example.wchat.screens.SelecionarDestinatariosScreen
import com.example.wchat.viewmodel.ConversasViewModel
import com.example.wchat.viewmodel.GruposViewModel
import com.example.wchat.viewmodel.MainViewModel
import com.example.wchat.viewmodel.SegmentosViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(UnstableApi::class)
@Composable
fun MainScaffold(mainNavController: NavHostController, tipoUsuario: TipoUsuario) {
    val conversasViewModel: ConversasViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()
    val gruposViewModel: GruposViewModel = viewModel()
    val segmentosViewModel: SegmentosViewModel = viewModel()

    val ultimaMensagemInfo by mainViewModel.ultimaMensagemRecebida.collectAsState()
    val grupoDoCliente by gruposViewModel.grupoDoCliente.collectAsState()
    val segmentosDoCliente by segmentosViewModel.segmentosDoCliente.collectAsState()
    val todosOsGrupos by gruposViewModel.grupos.collectAsState()
    val todosOsSegmentos by segmentosViewModel.segmentos.collectAsState()

    LaunchedEffect(tipoUsuario, grupoDoCliente, segmentosDoCliente, todosOsGrupos, todosOsSegmentos) {
        val gruposParaOuvinte: List<Grupo>
        val segmentosParaOuvinte: List<Segmento>

        if (tipoUsuario == TipoUsuario.CLIENTE) {
            Log.d("MainScaffold", "Iniciando listener para CLIENTE.")
            gruposParaOuvinte = if (grupoDoCliente != null) listOf(grupoDoCliente!!) else emptyList()
            segmentosParaOuvinte = segmentosDoCliente
        } else {
            Log.d("MainScaffold", "Iniciando listener para OPERADOR/ADMIN.")
            gruposParaOuvinte = todosOsGrupos
            segmentosParaOuvinte = todosOsSegmentos
        }

        mainViewModel.iniciarOuvinteDeNotificacao(
            gruposAtuais = gruposParaOuvinte,
            segmentosAtuais = segmentosParaOuvinte
        )
    }

    val bottomNavController = rememberNavController()
    var mostrandoBusca by remember { mutableStateOf(false) }
    var textoBusca by remember { mutableStateOf("") }
    var menuAberto by remember { mutableStateOf(false) }

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route
    val rotasSemTopBarPrincipal = listOf(
        "notificacao",
        "selecionarDestinatarios/{titulo}/{nomeCampanha}"
    )
    val mostrarTopBarPrincipal = rotasSemTopBarPrincipal.none { rota ->
        rotaAtual?.startsWith(rota.substringBefore('/')) ?: false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (mostrarTopBarPrincipal) {
                    Column {
                        WChatTopBar(
                            tipoUsuario = tipoUsuario,
                            actions = {
                                IconButton(onClick = { mostrandoBusca = !mostrandoBusca }) {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                                }
                                Box {
                                    IconButton(onClick = { menuAberto = true }) {
                                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Mais opções")
                                    }
                                    DropdownMenu(
                                        expanded = menuAberto,
                                        onDismissRequest = { menuAberto = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Atualizar Perfil") },
                                            onClick = {
                                                menuAberto = false
                                                mainNavController.navigate("editarPerfil")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Sair") },
                                            onClick = {
                                                menuAberto = false
                                                Firebase.auth.signOut()
                                                mainNavController.navigate("telaInicial") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        )
                        SearchBar(
                            mostrandoBusca = mostrandoBusca,
                            textoBusca = textoBusca,
                            onTextoBuscaChange = { textoBusca = it },
                            tipoUsuario = tipoUsuario
                        )
                    }
                }
            },
            bottomBar = {
                WChatBottomBar(navController = bottomNavController, tipoUsuario = tipoUsuario)
            }
        ) { innerPadding ->
            BottomNavGraph(
                mainNavController = mainNavController,
                bottomNavController = bottomNavController,
                conversasViewModel = conversasViewModel,
                gruposViewModel = gruposViewModel,
                segmentosViewModel = segmentosViewModel,
                textoBusca = textoBusca,
                modifier = Modifier.padding(innerPadding),
                tipoUsuario = tipoUsuario
            )
        }

        InAppNotification(
            notificationInfo = ultimaMensagemInfo,
            onNotificationClick = { mensagemClicada ->
                val (_, chatId) = ultimaMensagemInfo ?: return@InAppNotification
                val grupoEncontrado = gruposViewModel.grupos.value.find { it.id == chatId }
                val segmentoEncontrado = segmentosViewModel.segmentos.value.find { it.id == chatId }

                val rotaNavegacao = when {
                    grupoEncontrado != null -> "chatGrupo/${grupoEncontrado.id}/${tipoUsuario.name}"
                    segmentoEncontrado != null -> "chatSegmento/${segmentoEncontrado.id}/${tipoUsuario.name}"
                    else -> "chat1a1/${mensagemClicada.remetenteId}/${mensagemClicada.remetenteNome}/${tipoUsuario.name}"
                }

                Log.d("MainScaffold", "Notificação clicada! Navegando para: $rotaNavegacao")
                mainNavController.navigate(rotaNavegacao)
                mainViewModel.dispensarNotificacao()
            },
            onDismiss = {
                mainViewModel.dispensarNotificacao()
            }
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun BottomNavGraph(
    mainNavController: NavHostController,
    bottomNavController: NavHostController,
    conversasViewModel: ConversasViewModel,
    gruposViewModel: GruposViewModel,
    segmentosViewModel: SegmentosViewModel,
    textoBusca: String,
    modifier: Modifier = Modifier,
    tipoUsuario: TipoUsuario
) {
    NavHost(
        navController = bottomNavController,
        startDestination = "conversas",
        modifier = modifier
    ) {
        composable("conversas") {
            Conversas(
                mainNavController = mainNavController,
                textoBusca = textoBusca,
                viewModel = conversasViewModel,
                tipoUsuarioLogado = tipoUsuario
            )
        }
        composable("grupos") {
            GruposScreen(
                navController = mainNavController,
                tipoUsuarioLogado = tipoUsuario,
                gruposViewModel = gruposViewModel,
                conversasViewModel = conversasViewModel,
                textoBusca = textoBusca
            )
        }
        composable("segmentos") {
            SegmentosScreen(
                navController = mainNavController,
                tipoUsuarioLogado = tipoUsuario,
                segmentosViewModel = segmentosViewModel,
                conversasViewModel = conversasViewModel,
                textoBusca = textoBusca
            )
        }

        composable("notificacao") {
            CriarNotificacaoScreen(navController = bottomNavController)
        }

        composable(
            route = "selecionarDestinatarios/{titulo}/{nomeCampanha}" +
                    "?descricao={descricao}&linkEvento={linkEvento}&urlSaberMais={urlSaberMais}&urlInscrever={urlInscrever}",
            arguments = listOf(
                navArgument("titulo") { type = NavType.StringType },
                navArgument("nomeCampanha") { type = NavType.StringType },
                navArgument("descricao") { type = NavType.StringType; defaultValue = " " },
                navArgument("linkEvento") { type = NavType.StringType; defaultValue = " " },
                navArgument("urlSaberMais") { type = NavType.StringType; defaultValue = " " },
                navArgument("urlInscrever") { type = NavType.StringType; defaultValue = " " }
            )
        ) { backStackEntry ->
            val decoder = { valor: String? ->
                if (valor.isNullOrBlank()) ""
                else URLDecoder.decode(valor, StandardCharsets.UTF_8.name())
            }

            val notificacao = Notificacao(
                titulo = decoder(backStackEntry.arguments?.getString("titulo")),
                nomeCampanha = decoder(backStackEntry.arguments?.getString("nomeCampanha")),
                descricao = decoder(backStackEntry.arguments?.getString("descricao")),
                linkEvento = decoder(backStackEntry.arguments?.getString("linkEvento")),
                urlSaberMais = decoder(backStackEntry.arguments?.getString("urlSaberMais")),
                urlInscrever = decoder(backStackEntry.arguments?.getString("urlInscrever"))
            )

            SelecionarDestinatariosScreen(
                navController = bottomNavController,
                notificacao = notificacao
            )
        }

        composable(
            route = "chat1a1/{usuarioId}/{usuarioNome}/{usuarioLogadoTipo}",
            arguments = listOf(
                navArgument("usuarioId") { type = NavType.StringType },
                navArgument("usuarioNome") { type = NavType.StringType },
                navArgument("usuarioLogadoTipo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
            val usuarioNome = backStackEntry.arguments?.getString("usuarioNome") ?: ""
            val usuarioLogadoTipoStr = backStackEntry.arguments?.getString("usuarioLogadoTipo")
            val usuarioLogadoTipo = TipoUsuario.valueOf(usuarioLogadoTipoStr ?: "CLIENTE")

            ChatScreen(
                tipoChat = TipoChat.UM_A_UM,
                chatId = usuarioId,
                chatNome = usuarioNome,
                tipoUsuarioLogado = usuarioLogadoTipo,
                navController = mainNavController,
                onVoltarClick = { mainNavController.popBackStack() }
            )
        }

        composable(
            route = "chatGrupo/{grupoId}/{usuarioLogadoTipo}",
            arguments = listOf(
                navArgument("grupoId") { type = NavType.StringType },
                navArgument("usuarioLogadoTipo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val usuarioLogadoTipoStr = backStackEntry.arguments?.getString("usuarioLogadoTipo")
            val usuarioLogadoTipo = TipoUsuario.valueOf(usuarioLogadoTipoStr ?: "CLIENTE")

            val nomeDoGrupo = gruposViewModel.grupos.value.find { it.id == grupoId }?.tipo?.nomeExibicao ?: grupoId

            ChatScreen(
                tipoChat = TipoChat.GRUPO,
                chatId = grupoId,
                chatNome = nomeDoGrupo,
                tipoUsuarioLogado = usuarioLogadoTipo,
                navController = mainNavController,
                onVoltarClick = { mainNavController.popBackStack() }
            )
        }

        composable(
            route = "chatSegmento/{segmentoId}/{usuarioLogadoTipo}",
            arguments = listOf(
                navArgument("segmentoId") { type = NavType.StringType },
                navArgument("usuarioLogadoTipo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val segmentoId = backStackEntry.arguments?.getString("segmentoId") ?: ""
            val usuarioLogadoTipoStr = backStackEntry.arguments?.getString("usuarioLogadoTipo")
            val usuarioLogadoTipo = TipoUsuario.valueOf(usuarioLogadoTipoStr ?: "CLIENTE")

            val nomeDoSegmento = segmentosViewModel.segmentos.value.find { it.id == segmentoId }?.tipo?.nomeExibicao ?: segmentoId

            ChatScreen(
                tipoChat = TipoChat.SEGMENTO,
                chatId = segmentoId,
                chatNome = nomeDoSegmento,
                tipoUsuarioLogado = usuarioLogadoTipo,
                navController = mainNavController,
                onVoltarClick = { mainNavController.popBackStack() }
            )
        }
    }
}