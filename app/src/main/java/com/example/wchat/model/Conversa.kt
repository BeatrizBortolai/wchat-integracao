package com.example.wchat.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Conversa(
    val id: String = "",
    val nome: String = "",
    @get:PropertyName("participantesIds")
    val participantesIds: List<String> = emptyList(),
    val messages: @RawValue List<Mensagem> = emptyList()
) : Parcelable

