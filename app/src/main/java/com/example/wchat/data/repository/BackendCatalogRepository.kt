package com.example.wchat.data.repository

import android.content.Context
import com.example.wchat.data.remote.api.RetrofitProvider
import com.example.wchat.data.remote.api.WChatApi
import com.example.wchat.model.Grupo
import com.example.wchat.model.Segmento
import com.example.wchat.model.TipoGrupo
import com.example.wchat.model.TipoSegmento

class BackendCatalogRepository(context: Context) {
    private val api: WChatApi = RetrofitProvider.create(context).create(WChatApi::class.java)

    suspend fun listarGrupos(): Result<List<Grupo>> = try {
        val response = api.listarGrupos()
        if (response.isSuccessful) {
            Result.success(response.body().orEmpty().map { dto ->
                Grupo(
                    id = dto.id.ifBlank { dto.nome },
                    participantesIds = dto.participantesIds,
                    tipo = TipoGrupo.values().find { it.name == dto.nome || it.name == dto.id }
                )
            })
        } else Result.failure(Exception("Erro ao listar grupos: ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun listarSegmentos(): Result<List<Segmento>> = try {
        val response = api.listarSegmentos()
        if (response.isSuccessful) {
            Result.success(response.body().orEmpty().map { dto ->
                Segmento(
                    id = dto.id.ifBlank { dto.nome },
                    participantesIds = dto.participantesIds,
                    tipo = TipoSegmento.values().find { it.name == dto.nome || it.name == dto.id }
                )
            })
        } else Result.failure(Exception("Erro ao listar segmentos: ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun adicionarUsuarioAoGrupo(grupoId: String, usuarioId: String): Result<Unit> = try {
        val response = api.adicionarParticipanteGrupo(grupoId, usuarioId)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Erro ao adicionar grupo: ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun adicionarUsuarioAoSegmento(usuarioId: String, nomeSegmento: String): Result<Unit> = try {
        val response = api.adicionarSegmentoAoUsuario(usuarioId, nomeSegmento)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Erro ao adicionar segmento: ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}