package com.example.wchat.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.BackendCatalogRepository
import com.example.wchat.data.repository.UsuarioApiRepository
import com.example.wchat.model.Grupo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GruposViewModel(application: Application) : AndroidViewModel(application) {
    private val catalogRepository = BackendCatalogRepository(application.applicationContext)
    private val usuarioRepository = UsuarioApiRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(GruposUiState())
    val uiState: StateFlow<GruposUiState> = _uiState

    init {
        carregarGrupos()
    }

    fun carregarGrupos() {
        viewModelScope.launch {
            val usuarioAtualId = Firebase.auth.currentUser?.uid
            val gruposResult = catalogRepository.listarGrupos()
            val cargoUsuario = usuarioAtualId?.let { id ->
                usuarioRepository.buscarPorId(id).getOrNull()?.cargo
            }

            gruposResult
                .onSuccess { grupos ->
                    _uiState.value = GruposUiState(
                        todosOsGrupos = grupos,
                        idGrupoDoUsuario = cargoUsuario
                    )
                }
                .onFailure { e ->
                    Log.e("GruposVM", "Erro ao carregar grupos pelo backend", e)
                    _uiState.value = GruposUiState()
                }
        }
    }

    val grupos: StateFlow<List<Grupo>> = uiState
        .map { it.todosOsGrupos }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val grupoDoCliente: StateFlow<Grupo?> = uiState
        .map { state ->
            state.todosOsGrupos.find { it.id == state.idGrupoDoUsuario || it.tipo?.name == state.idGrupoDoUsuario }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}

data class GruposUiState(
    val todosOsGrupos: List<Grupo> = emptyList(),
    val idGrupoDoUsuario: String? = null
)