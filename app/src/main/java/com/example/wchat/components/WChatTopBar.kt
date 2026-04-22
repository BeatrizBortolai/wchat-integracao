package com.example.wchat.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.wchat.R
import com.example.wchat.model.TipoUsuario
import com.example.wchat.ui.theme.Barlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WChatTopBar(
    tipoUsuario: TipoUsuario,
    titulo: String = "WChat",
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    var menuAberto by remember { mutableStateOf(false) }
    val corDaBarra = if (tipoUsuario == TipoUsuario.OPERADOR) {
        colorResource(id = R.color.laranja_escuro)
    } else {
        colorResource(id = R.color.azul_cinza)
    }

    TopAppBar(
        title = {
            Text(
                text = titulo,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Barlow,
                color = Color.White
            )
        },
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            }
        },
        actions = {
            if (actions != null) {
                actions()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = corDaBarra,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}