package com.example.wchat.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wchat.data.repository.BackendCatalogRepository
import com.example.wchat.data.repository.UsuarioApiRepository
import com.example.wchat.model.Segmento
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SegmentosViewModel(application: Application) : AndroidViewModel(application) {
    private val catalogRepository = BackendCatalogRepository(application.applicationContext)
    private val usuarioRepository = UsuarioApiRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(SegmentosUiState())
    val uiState: StateFlow<SegmentosUiState> = _uiState

    private var segmentosJob: Job? = null

    init {
        iniciarAtualizacaoDeSegmentos()
    }

    fun carregarSegmentos() {
        viewModelScope.launch {
            carregarSegmentosInterno()
        }
    }

    private fun iniciarAtualizacaoDeSegmentos() {
        segmentosJob?.cancel()
        segmentosJob = viewModelScope.launch {
            while (isActive) {
                carregarSegmentosInterno()
                delay(3000)
            }
        }
    }

    private suspend fun carregarSegmentosInterno() {
        val usuarioAtualId = Firebase.auth.currentUser?.uid
        val segmentosResult = catalogRepository.listarSegmentos()
        val segmentosDoUsuario = usuarioAtualId?.let { id ->
            usuarioRepository.buscarPorId(id).getOrNull()?.segmentos.orEmpty()
        }.orEmpty()

        segmentosResult
            .onSuccess { segmentos ->
                _uiState.value = SegmentosUiState(
                    todosOsSegmentos = segmentos,
                    idsSegmentosDoUsuario = segmentosDoUsuario.toSet()
                )
            }
            .onFailure { e ->
                Log.e("SegmentosVM", "Erro ao carregar segmentos pelo backend", e)
                _uiState.value = SegmentosUiState()
            }
    }

    val segmentos: StateFlow<List<Segmento>> = uiState
        .map { it.todosOsSegmentos }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val segmentosDoCliente: StateFlow<List<Segmento>> = uiState
        .map { state ->
            state.todosOsSegmentos.filter { segmento ->
                segmento.id in state.idsSegmentosDoUsuario || segmento.tipo?.name in state.idsSegmentosDoUsuario
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override fun onCleared() {
        super.onCleared()
        segmentosJob?.cancel()
    }
}

data class SegmentosUiState(
    val todosOsSegmentos: List<Segmento> = emptyList(),
    val idsSegmentosDoUsuario: Set<String> = emptySet()
)