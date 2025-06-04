package com.example.loginapp.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow // Make sure this is imported
import kotlinx.coroutines.launch
import android.util.Log // For logging

/**
 * Base ViewModel to handle common MVI logic.
 * S: UiState
 * E: UiEvent
 */
abstract class BaseViewModel<S : UiState, E : UiEvent> : ViewModel() {

    private lateinit var _uiState: MutableStateFlow<S>
    val uiState: StateFlow<S> by lazy {
        if (!this::_uiState.isInitialized) { // Corrected: Added 'this::' for proper lateinit check context
            Log.e("BaseViewModel", "_uiState accessed before initialization in uiState getter.")
            throw UninitializedPropertyAccessException("_uiState has not been initialized. Ensure uiState is accessed after ViewModel's init block.")
        }
        _uiState.asStateFlow()
    }

    private val _event: MutableSharedFlow<E> = MutableSharedFlow() // Internal mutable shared flow
    // If you need to expose events externally as a SharedFlow (e.g., for navigation, one-time snackbars)
    // val publicEvents: SharedFlow<E> = _event.asSharedFlow() // Example name

    protected abstract val initialState: S // This will be initialized by the subclass (e.g. LoginViewModel) before BaseViewModel's init

    init {
        // By the time this init block runs, 'initialState' from the subclass
        // should have been initialized because property initializers in the primary constructor
        // of the subclass run before the init blocks of the superclass.
        _uiState = MutableStateFlow(initialState)
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
        if (!this::_uiState.isInitialized) { // Corrected: Added 'this::'
            Log.e("BaseViewModel", "setState called before _uiState is initialized!")
            throw UninitializedPropertyAccessException("_uiState has not been initialized when setState was called.")
        }
        val currentState = _uiState.value // Read the current value
        // It's already established that if an NPE happens here, it's because currentState is null.
        // The UninitializedPropertyAccessException above should catch if _uiState itself wasn't assigned.
        _uiState.value = currentState.reducer() // Apply reducer to the read value
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            _event.collect { // Collect directly from the internal _event MutableSharedFlow
                handleEvent(it)
            }
        }
    }
}
