package com.example.wchat.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Grupo(
    @DocumentId val id: String = "",
    @get:PropertyName("participantesIds")
    val participantesIds: List<String> = emptyList(),
    val tipo: TipoGrupo? = null
)
