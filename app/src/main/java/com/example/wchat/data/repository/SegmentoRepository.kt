package com.example.wchat.data.repository

import com.example.wchat.model.Segmento
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SegmentoRepository {
    private val segmentosCollection = Firebase.firestore.collection("segmentos")

    fun getSegmentosEmTempoReal(): Flow<Result<List<Segmento>>> =
        callbackFlow {
            val listener = segmentosCollection.addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val segmentos = snapshot.toObjects(Segmento::class.java)
                    trySend(Result.success(segmentos))
                }
            }

            awaitClose { listener.remove() }
        }

    suspend fun getTodosSegmentos(): Result<List<Segmento>> {
        return try {
            val snapshot = segmentosCollection.get().await()
            val segmentos = snapshot.toObjects(Segmento::class.java)
            Result.success(segmentos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adicionarUsuarioAoSegmento(segmentoId: String, usuarioId: String) {
        if (segmentoId.isBlank() || usuarioId.isBlank()) return

        val segmentoRef = segmentosCollection.document(segmentoId)

        try {
            segmentoRef.update("participantesIds", FieldValue.arrayUnion(usuarioId)).await()
        } catch (e: Exception) {
            val dadosIniciais = mapOf(
                "id" to segmentoId,
                "tipo" to segmentoId,
                "participantesIds" to listOf(usuarioId)
            )
            segmentoRef.set(dadosIniciais).await()
        }
    }
}