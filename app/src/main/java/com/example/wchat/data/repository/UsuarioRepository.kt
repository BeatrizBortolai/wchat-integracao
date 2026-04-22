package com.example.wchat.data.repository

import com.example.wchat.model.TipoUsuario
import com.example.wchat.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UsuarioRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usuariosCollection = db.collection("usuarios")

    fun getTodosUsuariosFlow(): Flow<Result<List<Usuario>>> = callbackFlow {
        val auth = Firebase.auth
        val usuarioAtualId = auth.currentUser?.uid

        val listener = usuariosCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val usuarios = snapshot.toObjects(Usuario::class.java)
                val outrosUsuarios = if (usuarioAtualId != null) {
                    usuarios.filter { it.id != usuarioAtualId }
                } else {
                    usuarios
                }
                trySend(Result.success(outrosUsuarios))
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun registrarUsuario(
        nome: String,
        email: String,
        password: String,
        tipo: TipoUsuario,
        cargo: String?,
        segmentos: List<String>
    ): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                val novoUsuario = Usuario(
                    id = firebaseUser.uid,
                    nome = nome,
                    email = email,
                    tipo = tipo,
                    cargo = cargo,
                    segmentos = segmentos
                )

                usuariosCollection.document(firebaseUser.uid).set(novoUsuario).await()

                if (cargo != null) {
                    val grupoRepository = GrupoRepository()
                    grupoRepository.adicionarUsuarioAoGrupo(grupoId = cargo, usuarioId = firebaseUser.uid)
                }

                val segmentoRepository = SegmentoRepository()
                segmentos.forEach { segmentoId ->
                    segmentoRepository.adicionarUsuarioAoSegmento(
                        segmentoId = segmentoId,
                        usuarioId = firebaseUser.uid
                    )
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("Falha ao criar usuário, usuário nulo."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGrupoDoUsuarioLogado(): String? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val document = usuariosCollection.document(userId).get().await()
            document.getString("cargo")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSegmentosDoUsuarioLogado(): List<String> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        try {
            val userDocument = usuariosCollection.document(userId).get().await()
            val usuario = userDocument.toObject(Usuario::class.java)

            return when (usuario?.tipo) {
                TipoUsuario.CLIENTE -> {
                    usuario.segmentos
                }
                TipoUsuario.OPERADOR, TipoUsuario.OPERADOR -> {
                    emptyList()
                }
                else -> {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }
    }

    suspend fun loginUsuario(email: String, password: String): Result<Usuario> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val documento = usuariosCollection.document(firebaseUser.uid).get().await()
                val usuario = documento.toObject(Usuario::class.java)

                if (usuario != null) {
                    Result.success(usuario)
                } else {
                    Result.failure(Exception("Usuário autenticado, mas dados no banco são incompatíveis."))
                }
            } else {
                Result.failure(Exception("Falha no login, usuário nulo."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodosOsUsuarios(): Result<List<Usuario>> {
        return try {
            val usuarioAtualId = auth.currentUser?.uid
            val snapshot = usuariosCollection.get().await()
            val usuarios = snapshot.toObjects(Usuario::class.java)

            val outrosUsuarios = if (usuarioAtualId != null) {
                usuarios.filter { it.id != usuarioAtualId }
            } else {
                usuarios
            }

            Result.success(outrosUsuarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsuarioPorId(usuarioId: String): Usuario? {
        return try {
            usuariosCollection.document(usuarioId).get().await().toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun salvarAnotacoesDoCliente(clienteId: String, texto: String): Boolean {
        return try {
            usuariosCollection.document(clienteId).update("anotacoesOperador", texto).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun atualizarNomeUsuario(usuarioId: String, novoNome: String): Result<Unit> {
        return try {
            usuariosCollection.document(usuarioId).update("nome", novoNome).await()

            val user = auth.currentUser
            if (user != null && user.uid == usuarioId) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(novoNome)
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarUsuario(senhaAtual: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuário não está logado."))

        return try {
            val credential = EmailAuthProvider.getCredential(user.email!!, senhaAtual)
            user.reauthenticate(credential).await()

            val userId = user.uid

            usuariosCollection.document(userId).delete().await()

            user.delete().await()

            Result.success(Unit) // Retorna sucesso

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Senha atual incorreta. Tente novamente."))
        } catch (e: Exception) {
            Result.failure(Exception("Falha ao deletar conta: ${e.localizedMessage}"))
        }
    }

    suspend fun atualizarSenhaUsuario(novaSenha: String, senhaAtual: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuário não está logado."))

        return try {
            val credential = EmailAuthProvider.getCredential(user.email!!, senhaAtual)
            user.reauthenticate(credential).await()

            user.updatePassword(novaSenha).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}