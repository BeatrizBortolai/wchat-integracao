package com.example.wchat.data

import com.example.wchat.model.Grupo
import com.example.wchat.model.TipoGrupo
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class GrupoRepository {
    private val gruposCollection = Firebase.firestore.collection("grupos")

    fun getGruposEmTempoReal(): Flow<Result<List<Grupo>>> = callbackFlow {
        val listener = gruposCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val gruposDoFirebase = snapshot.toObjects(Grupo::class.java)
                trySend(Result.success(gruposDoFirebase))
            }
        }
        awaitClose { listener.remove() }
    }.map { result ->
        result.map { listaDeGrupos ->
            listaDeGrupos.map { grupo ->
                grupo.copy(tipo = TipoGrupo.values().find { it.name == grupo.id } as TipoGrupo?)
            }
        }
    }

    suspend fun adicionarUsuarioAoGrupo(grupoId: String, usuarioId: String) {
        if (grupoId.isBlank() || usuarioId.isBlank()) return

        try {
            val grupoRef = gruposCollection.document(grupoId)
            grupoRef.update("participantesIds", FieldValue.arrayUnion(usuarioId)).await()
        } catch (e: Exception) {
            val grupoRef = gruposCollection.document(grupoId)

            val dadosIniciais = mapOf(
                "participantesIds" to listOf(usuarioId)
            )
            grupoRef.set(dadosIniciais).await()
        }
    }
}