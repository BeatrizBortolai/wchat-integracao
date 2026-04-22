package com.example.wchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.wchat.data.SegmentoRepository
import com.example.wchat.data.UsuarioRepository
import com.example.wchat.model.Segmento
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@UnstableApi
class SegmentosViewModel : ViewModel() {
    private val todosOsSegmentosFlow = SegmentoRepository().getSegmentosEmTempoReal()
    private val idsSegmentosDoUsuarioFlow = kotlinx.coroutines.flow.flow {
        Log.d("SegmentosVM", "Buscando IDs de segmentos do usuário...")
        val ids = UsuarioRepository().getSegmentosDoUsuarioLogado()
        Log.d("SegmentosVM", "IDs encontrados: $ids")
        emit(ids)
    }

    val uiState: StateFlow<SegmentosUiState> = combine(
        todosOsSegmentosFlow,
        idsSegmentosDoUsuarioFlow
    ) { resultadoSegmentos, idsSegmentosUsuario ->
        val segmentos = resultadoSegmentos.getOrNull() ?: emptyList()
        SegmentosUiState(
            todosOsSegmentos = segmentos,
            idsSegmentosDoUsuario = idsSegmentosUsuario.toSet()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SegmentosUiState()
    )

    val segmentos: StateFlow<List<Segmento>> = uiState
        .map { it.todosOsSegmentos }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val segmentosDoCliente: StateFlow<List<Segmento>> = uiState
        .map { state ->
            state.todosOsSegmentos.filter { it.id in state.idsSegmentosDoUsuario }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

data class SegmentosUiState(
    val todosOsSegmentos: List<Segmento> = emptyList(),
    val idsSegmentosDoUsuario: Set<String> = emptySet()
)