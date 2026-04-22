package com.example.wchat.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.wchat.model.TipoUsuario

@Composable
fun WChatBottomBar(navController: NavController, tipoUsuario: TipoUsuario) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route

    val rotasPrincipais = listOf("conversas", "grupos", "segmentos", "notificacao")

    if (rotaAtual in rotasPrincipais) {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Conversas") },
                label = { Text("Conversas") },
                selected = rotaAtual == "conversas",
                onClick = {
                    navController.navigate("conversas") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            NavigationBarItem(
                icon = { Icon(Icons.Default.People, contentDescription = "Grupos") },
                label = { Text("Grupos") },
                selected = rotaAtual == "grupos",
                onClick = {
                    navController.navigate("grupos") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            NavigationBarItem(
                icon = { Icon(Icons.Default.Category, contentDescription = "Segmentos") },
                label = { Text("Segmentos") },
                selected = rotaAtual == "segmentos",
                onClick = {
                    navController.navigate("segmentos") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            if (tipoUsuario == TipoUsuario.OPERADOR) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Criar Notificação") },
                    label = { Text("Notificar") },
                    selected = rotaAtual == "notificacao",
                    onClick = {
                        navController.navigate("notificacao") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}