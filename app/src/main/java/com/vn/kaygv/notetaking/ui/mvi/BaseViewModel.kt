package com.vn.kaygv.notetaking.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<I : MviIntent, S : MviState, E : MviEvent>(
    initialState: S
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _event = Channel<E>()
    val event = _event.receiveAsFlow()

    protected fun setState(reducer: S.() -> S) {
        val newState = _state.value.reducer()
        _state.value = newState
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    abstract fun processIntent(intent: I)
}