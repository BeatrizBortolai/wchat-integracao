package com.example.wchat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.wchat.R
import com.example.wchat.model.TipoUsuario

@Composable
fun SearchBar(
    mostrandoBusca: Boolean,
    textoBusca: String,
    onTextoBuscaChange: (String) -> Unit,
    tipoUsuario: TipoUsuario
) {
    AnimatedVisibility(
        visible = mostrandoBusca,
        enter = expandVertically(animationSpec = tween(300)),
        exit = shrinkVertically(animationSpec = tween(300))
    ) {
        val corDaBarra = if (tipoUsuario == TipoUsuario.OPERADOR) {
            colorResource(id = R.color.laranja_escuro)
        } else {
            colorResource(id = R.color.azul_cinza)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(corDaBarra)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textoBusca,
                onValueChange = onTextoBuscaChange,
                placeholder = { Text("Pesquisar...", color = Color.LightGray) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.LightGray,
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}