package com.example.wchat.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    @DocumentId val id: String = "",
    val nome: String = "",
    val email: String = "",
    val tipo: TipoUsuario = TipoUsuario.CLIENTE,
    val cargo: String? = null,
    val segmentos: List<String> = emptyList(),
    val anotacoesOperador: String? = null
) : Parcelable
