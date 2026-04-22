package com.example.wchat.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wchat.R
import com.example.wchat.model.TipoUsuario
import com.example.wchat.ui.theme.Barlow

@Composable
fun TelaInicial(navController: NavController) {
    var showOla by remember() { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showOla = true
    }

    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(colorResource(id = R.color.azul_cinza))
        ) {
            AnimatedVisibility(visible = showOla) {
                Text(
                    text = "Olá",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Barlow,
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                )
            }
            Text(
                text = "Seja bem-vindo ao",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ){
            Text(
                text = "WChat",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Barlow,
                color = (colorResource(id = R.color.laranja_escuro))
            )
        }
        Button(
            onClick = { navController.navigate("telaLogin/${TipoUsuario.CLIENTE}") },
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.azul_cinza)),
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(130.dp)
        ) {
            Text(text = stringResource(id = R.string.login), fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { navController.navigate("cadastro/${TipoUsuario.CLIENTE}") },
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.azul_escuro)),
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(80.dp)
        ) {
            Text(text = stringResource(id = R.string.signin), fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}