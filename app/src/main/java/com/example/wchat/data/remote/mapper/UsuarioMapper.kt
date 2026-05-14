package com.example.wchat.data.remote.mapper

import com.example.wchat.data.remote.dto.UsuarioResponseDto
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario

fun UsuarioResponseDto.toModel(): Usuario {
    val tipoUsuario = runCatching { TipoUsuario.valueOf(tipo.uppercase()) }
        .getOrDefault(TipoUsuario.CLIENTE)

    return Usuario(
        id = id,
        nome = nome,
        email = email,
        tipo = tipoUsuario,
        cargo = cargo,
        segmentos = segmentos.orEmpty(),
        anotacoesOperador = anotacoesOperador
    )
}