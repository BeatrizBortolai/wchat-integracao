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

    /**
     * Catálogo administrativo de grupos.
     *
     * A tela de campanha precisa exibir todos os grupos possíveis do app, mesmo quando
     * ainda não existe nenhum cliente cadastrado naquele grupo. Por isso fazemos merge
     * entre o catálogo fixo do app (TipoGrupo) e os dados vindos do backend, preservando
     * participantesIds quando o grupo já existe no banco.
     */
    suspend fun listarGrupos(): Result<List<Grupo>> = try {
        val response = api.listarGrupos()
        if (response.isSuccessful) {
            val gruposBackendPorId = response.body()
                .orEmpty()
                .associateBy { dto ->
                    (dto.id.ifBlank { dto.nome }).uppercase()
                }

            val gruposCatalogo = TipoGrupo.todos().map { tipo ->
                val dto = gruposBackendPorId[tipo.name]
                Grupo(
                    id = tipo.name,
                    participantesIds = dto?.participantesIds.orEmpty(),
                    tipo = tipo
                )
            }

            val gruposExtrasBackend = gruposBackendPorId
                .filterKeys { id -> TipoGrupo.values().none { it.name == id } }
                .values
                .map { dto ->
                    val id = dto.id.ifBlank { dto.nome }.uppercase()
                    Grupo(
                        id = id,
                        participantesIds = dto.participantesIds,
                        tipo = TipoGrupo.values().find { it.name == id }
                    )
                }

            Result.success(gruposCatalogo + gruposExtrasBackend)
        } else {
            Result.failure(Exception("Erro ao listar grupos: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Catálogo administrativo de segmentos.
     *
     * Mesmo raciocínio de grupos: a campanha deve permitir escolher qualquer segmento
     * previsto pelo app, ainda que não exista cliente nele no momento.
     */
    suspend fun listarSegmentos(): Result<List<Segmento>> = try {
        val response = api.listarSegmentos()
        if (response.isSuccessful) {
            val segmentosBackendPorId = response.body()
                .orEmpty()
                .associateBy { dto ->
                    (dto.id.ifBlank { dto.nome }).uppercase()
                }

            val segmentosCatalogo = TipoSegmento.todos().map { tipo ->
                val dto = segmentosBackendPorId[tipo.name]
                Segmento(
                    id = tipo.name,
                    participantesIds = dto?.participantesIds.orEmpty(),
                    tipo = tipo
                )
            }

            val segmentosExtrasBackend = segmentosBackendPorId
                .filterKeys { id -> TipoSegmento.values().none { it.name == id } }
                .values
                .map { dto ->
                    val id = dto.id.ifBlank { dto.nome }.uppercase()
                    Segmento(
                        id = id,
                        participantesIds = dto.participantesIds,
                        tipo = TipoSegmento.values().find { it.name == id }
                    )
                }

            Result.success(segmentosCatalogo + segmentosExtrasBackend)
        } else {
            Result.failure(Exception("Erro ao listar segmentos: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun adicionarUsuarioAoGrupo(grupoId: String, usuarioId: String): Result<Unit> = try {
        val response = api.adicionarParticipanteGrupo(grupoId, usuarioId)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Erro ao adicionar grupo: ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun adicionarUsuarioAoSegmento(usuarioId: String, nomeSegmento: String): Result<Unit> = try {
        val response = api.adicionarSegmentoAoUsuario(usuarioId, nomeSegmento)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Erro ao adicionar segmento: ${response.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}