package com.example.wchat.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wchat.data.UsuarioRepository
import com.example.wchat.ui.theme.Barlow
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.wchat.R

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2000),
        label = "Alpha Animation"
    )

    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            scaleAnim.animateTo(
                targetValue = 1.1f, // Aumenta para 110%
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000)

        val firebaseUser = Firebase.auth.currentUser
        if (firebaseUser != null) {
            val repository = UsuarioRepository()
            this.launch {
                val usuario = repository.getUsuarioPorId(firebaseUser.uid)
                if (usuario != null) {
                    navController.navigate("main/${usuario.tipo.name}") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    Firebase.auth.signOut()
                    navController.navigate("telaInicial") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        } else {
            navController.navigate("telaInicial") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.azul_cinza)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WChat",
            color = Color.White,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Barlow,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim)
        )

        Spacer(modifier = Modifier.height(100.dp))

        CircularProgressIndicator(
            modifier = Modifier
                .size(40.dp)
                .alpha(alphaAnim),
            color = colorResource(id = R.color.laranja_escuro),
            strokeWidth = 4.dp
        )
    }
}