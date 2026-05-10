package com.example.wchat.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/**
 * Responsável apenas pela autenticação Firebase.
 *
 * A regra da Sprint fica preservada assim:
 * - Firebase Auth identifica o usuário no app.
 * - Backend REST/Mongo guarda dados de domínio, mensagens e permissões.
 * - Firebase Cloud Messaging é usado para push instantâneo enviado pelo backend.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = try {
        val user = auth.signInWithEmailAndPassword(email, password).await().user
        if (user != null) Result.success(user)
        else Result.failure(Exception("Falha no login: usuário Firebase nulo."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createUser(nome: String, email: String, password: String): Result<FirebaseUser> = try {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: return Result.failure(Exception("Falha no cadastro: usuário Firebase nulo."))

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(nome)
                .build()
        ).await()

        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateDisplayName(novoNome: String): Result<Unit> = try {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuário não está logado."))
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(novoNome)
                .build()
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePassword(novaSenha: String, senhaAtual: String): Result<Unit> = try {
        reauthenticateCurrentUser(senhaAtual).getOrThrow()
        val user = auth.currentUser ?: return Result.failure(Exception("Usuário não está logado."))
        user.updatePassword(novaSenha).await()
        Result.success(Unit)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.failure(Exception("Senha atual incorreta."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Reautentica o usuário sem apagar nada.
     *
     * Esse método é usado antes da exclusão da conta para evitar o problema de
     * deletar o usuário no backend e só depois descobrir que a senha Firebase
     * estava incorreta.
     */
    suspend fun reauthenticateCurrentUser(senhaAtual: String): Result<Unit> = try {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuário não está logado."))
        val email = user.email ?: return Result.failure(Exception("E-mail do usuário não encontrado."))
        user.reauthenticate(EmailAuthProvider.getCredential(email, senhaAtual)).await()
        Result.success(Unit)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.failure(Exception("Senha atual incorreta."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Remove a conta Firebase após uma reautenticação recente.
     */
    suspend fun deleteCurrentUserAfterRecentLogin(): Result<Unit> = try {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuário não está logado."))
        user.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteCurrentUser(senhaAtual: String): Result<Unit> = try {
        reauthenticateCurrentUser(senhaAtual).getOrThrow()
        deleteCurrentUserAfterRecentLogin().getOrThrow()
        Result.success(Unit)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.failure(Exception("Senha atual incorreta."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() {
        auth.signOut()
    }
}
