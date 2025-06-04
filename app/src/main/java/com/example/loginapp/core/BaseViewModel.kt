package com.example.loginapp.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel to handle common MVI logic.
 * S: UiState
 * E: UiEvent
 */
abstract class BaseViewModel<S : UiState, E : UiEvent> : ViewModel() {

    private val _uiState: MutableStateFlow<S> by lazy { MutableStateFlow(initialState) }
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _event: MutableSharedFlow<E> = MutableSharedFlow()
    private val event = _event.asSharedFlow()

    protected abstract val initialState: S

    init {
        subscribeToEvents()
    }

    /**
     * Entry point for UI to send events to the ViewModel.
     */
    fun sendEvent(event: E) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    /**
     * Called when an event is emitted from the UI.
     * Subclasses should override this to handle specific events.
     */
    protected abstract fun handleEvent(event: E)

    /**
     * Updates the UI state.
     */
    protected fun setState(reducer: S.() -> S) {
        _uiState.value = uiState.value.reducer()
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            event.collect {
                handleEvent(it)
            }
        }
    }
}
