package com.example.wchat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wchat.components.MainScaffold
import com.example.wchat.model.TipoChat
import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoSegmento
import com.example.wchat.model.TipoUsuario
import com.example.wchat.screens.*
import com.example.wchat.ui.theme.WChatTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            WChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController()
                    LaunchedEffect(Unit) {
                        handleIntent(intent)
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable(route = "splash") {
                            SplashScreen(navController = navController)
                        }

                        composable(route = "telaInicial") { TelaInicial(navController) }

                        composable(
                            route = "telaLogin/{tipoUsuario}",
                            arguments = listOf(navArgument("tipoUsuario") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val tipoString = backStackEntry.arguments?.getString("tipoUsuario")
                            val tipoUsuario = TipoUsuario.valueOf(tipoString ?: "CLIENTE")
                            Login(navController = navController, tipoUsuario = tipoUsuario)
                        }

                        composable(
                            "cadastro/{tipoUsuario}",
                            arguments = listOf(navArgument("tipoUsuario") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val tipoString = backStackEntry.arguments?.getString("tipoUsuario")
                            val tipoUsuario = TipoUsuario.valueOf(tipoString ?: "CLIENTE")
                            Cadastro(navController = navController, tipoUsuario = tipoUsuario)
                        }

                        composable(
                            route = "main/{tipoUsuario}",
                            arguments = listOf(navArgument("tipoUsuario") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val tipoString = backStackEntry.arguments?.getString("tipoUsuario")
                            val tipoUsuario = TipoUsuario.valueOf(tipoString ?: "CLIENTE")
                            MainScaffold(mainNavController = navController, tipoUsuario = tipoUsuario)
                        }

                        composable(route = "editarPerfil") {
                            EditarPerfilScreen(navController = navController)
                        }

                        composable(
                            route = "chat1a1/{destinatarioId}/{destinatarioNome}/{tipoUsuarioLogado}",
                            arguments = listOf(
                                navArgument("destinatarioId") { type = NavType.StringType },
                                navArgument("destinatarioNome") { type = NavType.StringType },
                                navArgument("tipoUsuarioLogado") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val destinatarioId = backStackEntry.arguments?.getString("destinatarioId") ?: return@composable
                            val destinatarioNome = backStackEntry.arguments?.getString("destinatarioNome") ?: "Usuário"
                            val tipoUsuarioLogado = TipoUsuario.valueOf(backStackEntry.arguments?.getString("tipoUsuarioLogado") ?: "CLIENTE")

                            val usuarioAtualId = Firebase.auth.currentUser?.uid ?: ""
                            val chatIdFinal = listOf(usuarioAtualId, destinatarioId).sorted().joinToString("_")

                            ChatScreen(
                                tipoChat = TipoChat.UM_A_UM,
                                chatId = chatIdFinal,
                                chatNome = destinatarioNome,
                                onVoltarClick = { navController.popBackStack() },
                                navController = navController,
                                tipoUsuarioLogado = tipoUsuarioLogado
                            )
                        }

                        composable(
                            route = "chatGrupo/{idDoGrupo}/{tipoUsuarioLogado}",
                            arguments = listOf(
                                navArgument("idDoGrupo") { type = NavType.StringType },
                                navArgument("tipoUsuarioLogado") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val idDoGrupo = backStackEntry.arguments?.getString("idDoGrupo") ?: return@composable
                            val tipoUsuarioLogado = TipoUsuario.valueOf(backStackEntry.arguments?.getString("tipoUsuarioLogado") ?: "CLIENTE")
                            val nomeDoGrupo = TipoGrupo.values().find { it.name == idDoGrupo }?.nomeExibicao ?: "Grupo"

                            ChatScreen(
                                tipoChat = TipoChat.GRUPO,
                                chatId = idDoGrupo,
                                chatNome = nomeDoGrupo,
                                onVoltarClick = { navController.popBackStack() },
                                navController = navController,
                                tipoUsuarioLogado = tipoUsuarioLogado
                            )
                        }

                        composable(
                            route = "chatSegmento/{idDoSegmento}/{tipoUsuarioLogado}",
                            arguments = listOf(
                                navArgument("idDoSegmento") { type = NavType.StringType },
                                navArgument("tipoUsuarioLogado") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val idDoSegmento = backStackEntry.arguments?.getString("idDoSegmento") ?: return@composable
                            val tipoUsuarioLogado = TipoUsuario.valueOf(backStackEntry.arguments?.getString("tipoUsuarioLogado") ?: "CLIENTE")
                            val nomeDoSegmento = TipoSegmento.values().find { it.name == idDoSegmento }?.nomeExibicao ?: "Segmento"

                            ChatScreen(
                                tipoChat = TipoChat.SEGMENTO,
                                chatId = idDoSegmento,
                                chatNome = nomeDoSegmento,
                                onVoltarClick = { navController.popBackStack() },
                                navController = navController,
                                tipoUsuarioLogado = tipoUsuarioLogado
                            )
                        }

                        composable(
                            route = "perfil/{usuarioId}/{tipoUsuarioLogado}",
                            arguments = listOf(
                                navArgument("usuarioId") { type = NavType.StringType },
                                navArgument("tipoUsuarioLogado") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val usuarioId = backStackEntry.arguments?.getString("usuarioId")
                            val tipoUsuarioLogadoString = backStackEntry.arguments?.getString("tipoUsuarioLogado")

                            if (usuarioId != null && tipoUsuarioLogadoString != null) {
                                val tipoUsuarioLogado = TipoUsuario.valueOf(tipoUsuarioLogadoString)

                                PerfilScreen(
                                    navController = navController,
                                    usuarioId = usuarioId,
                                    tipoUsuarioLogado = tipoUsuarioLogado
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @OptIn(UnstableApi::class)
    private fun handleIntent(intent: Intent?) {
        if (!this::navController.isInitialized) {
            return
        }

        val chatId = intent?.getStringExtra("chatId")
        val collection = intent?.getStringExtra("collection")

        if (chatId != null && collection != null) {
            Log.d("NotificationClick", "Dados da notificação: collection=$collection, chatId=$chatId")

            val currentUser = Firebase.auth.currentUser
            if (currentUser == null) {
                Log.w("NotificationClick", "Usuário não logado, abortando navegação.")
                return
            }

            val rotaNavegacao: String? = when (collection) {
                "grupos", "segmentos" -> {
                    val tipoUsuario = "OPERADOR"
                    Log.d("NotificationClick", "Montando rota para $collection. Tipo de usuário assumido: $tipoUsuario")

                    if (collection == "grupos") "chatGrupo/$chatId/$tipoUsuario"
                    else "chatSegmento/$chatId/$tipoUsuario"
                }
                "chats1a1" -> {
                    val remetenteNome = intent.getStringExtra("remetenteNome") ?: "Usuário"
                    val destinatarioId = chatId.replace(currentUser.uid, "").replace("_", "")
                    Log.d("NotificationClick", "Montando rota para chat1a1.")
                    "chat1a1/$destinatarioId/$remetenteNome/OPERADOR"
                }
                else -> {
                    Log.w("NotificationClick", "Coleção desconhecida: $collection")
                    null
                }
            }

            rotaNavegacao?.let { rota ->
                Log.i("NotificationClick", "Navegando para a rota: '$rota'")
                navController.navigate(rota)
            }

            setIntent(Intent())
        }
    }
}