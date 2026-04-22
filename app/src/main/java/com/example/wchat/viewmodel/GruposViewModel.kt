package com.example.wchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.GrupoRepository
import com.example.wchat.data.repository.UsuarioRepository
import com.example.wchat.model.Grupo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GruposViewModel : ViewModel() {
    private val todosOsGruposFlow = GrupoRepository().getGruposEmTempoReal()
    private val idGrupoDoUsuarioFlow = kotlinx.coroutines.flow.flow {
        emit(UsuarioRepository().getGrupoDoUsuarioLogado())
    }

    val uiState: StateFlow<GruposUiState> = combine(
        todosOsGruposFlow,
        idGrupoDoUsuarioFlow
    ) { resultadoGrupos, idGrupoUsuario ->
        val grupos = resultadoGrupos.getOrNull() ?: emptyList()
        GruposUiState(
            todosOsGrupos = grupos,
            idGrupoDoUsuario = idGrupoUsuario
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GruposUiState()
    )

    val grupos: StateFlow<List<Grupo>> = uiState
        .map { it.todosOsGrupos }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val grupoDoCliente: StateFlow<Grupo?> = uiState
        .map { state ->
            state.todosOsGrupos.find { it.id == state.idGrupoDoUsuario }
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