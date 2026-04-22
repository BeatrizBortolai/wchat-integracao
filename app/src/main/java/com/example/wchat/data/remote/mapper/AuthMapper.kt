package com.example.wchat.data.remote.mapper

import com.example.wchat.data.remote.dto.AuthSyncResponseDto
import com.example.wchat.domain.model.AuthSession

fun AuthSyncResponseDto.toDomain(): AuthSession {
    return AuthSession(
        token = token,
        usuarioId = usuarioId,
        nome = nome,
        email = email
    )
}