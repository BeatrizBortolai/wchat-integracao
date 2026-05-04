package com.example.wchat.data.repository

import android.content.Context
import com.example.wchat.data.remote.api.RetrofitProvider
import com.example.wchat.data.remote.api.WChatApi
import com.example.wchat.data.remote.dto.UsuarioResponseDto
import com.example.wchat.data.remote.dto.UsuarioUpdateRequestDto
import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario

class UsuarioApiRepository(context: Context) {

    private val api: WChatApi = RetrofitProvider
        .create(context)
        .create(WChatApi::class.java)

    suspend fun buscarPorId(id: String): Result<UsuarioResponseDto> {
        return try {
            val response = api.buscarUsuarioPorId(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar usuário: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarPorEmail(email: String): Result<UsuarioResponseDto> {
        return try {
            val response = api.buscarUsuarioPorEmail(email)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar usuário por e-mail: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizarUsuario(
        id: String,
        nome: String? = null,
        cargo: String? = null,
        anotacoesOperador: String? = null
    ): Result<UsuarioResponseDto> {
        return try {
            val response = api.atualizarUsuario(
                id = id,
                request = UsuarioUpdateRequestDto(
                    nome = nome,
                    cargo = cargo,
                    anotacoesOperador = anotacoesOperador
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao atualizar usuário: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarUsuario(id: String): Result<Unit> {
        return try {
            val response = api.deletarUsuario(id)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Erro ao deletar usuário: ${response.code()} - ${response.errorBody()?.string()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarUsuarios(): Result<List<Usuario>> {
        return try {
            val response = api.listarUsuarios()

            if (response.isSuccessful && response.body() != null) {
                val usuarios = response.body()!!.content.map { dto ->
                    Usuario(
                        id = dto.id,
                        nome = dto.nome,
                        email = dto.email,
                        tipo = TipoUsuario.valueOf(dto.tipo.uppercase()),
                        cargo = dto.cargo,
                        segmentos = dto.segmentos.orEmpty()
                    )
                }

                Result.success(usuarios)
            } else {
                Result.failure(
                    Exception("Erro ao listar usuários: ${response.code()} - ${response.errorBody()?.string()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}