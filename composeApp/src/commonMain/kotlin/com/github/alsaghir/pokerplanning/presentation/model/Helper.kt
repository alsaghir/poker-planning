package com.github.alsaghir.pokerplanning.presentation.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.github.alsaghir.pokerplanning.domain.DataState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Runs a suspending operation with a unified loading state plus error handling.
 *
 * Use for operations that need a loading indicator (e.g., create game, load game).
 * Sets loading=true before the block, loading=false in finally.
 * On exception, emits an error event via [eventFlow].
 *
 * Usage:
 * ```
 * launchWithState(
 *     stateFlow = _uiState,
 *     eventFlow = _uiEvent,
 *     setLoading = { state, loading -> state.copy(isLoading = loading) },
 *     createErrorEvent = { GameUiEvent.ShowMessage(it) },
 *     errorMessage = "Failed to create game"
 * ) {
 *     val result = pokerService.createGame(request).getOrThrow()
 *     // ... handle success
 * }
 * ```
 */
fun <S, E> ViewModel.launchWithState(
    stateFlow: MutableStateFlow<S>,
    eventFlow: MutableSharedFlow<E>,
    setLoading: (S, Boolean) -> S,
    createErrorEvent: (String) -> E,
    errorMessage: String = "Operation failed",
    block: suspend () -> Unit
) {
    viewModelScope.launch {
        stateFlow.update { setLoading(it, true) }
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            eventFlow.emit(createErrorEvent(e.message ?: errorMessage))
        } finally {
            stateFlow.update { setLoading(it, false) }
        }
    }
}

/**
 * Runs a suspending operation with unified error handling only (no loading state).
 *
 * Use for fire-and-forget operations like optimistic updates (vote, reveal, restart)
 * where a loading spinner is unnecessary but errors still need to surface.
 *
 * Usage:
 * ```
 * launchSafe(
 *     eventFlow = _uiEvent,
 *     createErrorEvent = { GameSessionUiEvent.ShowMessage(it) },
 *     errorMessage = "Failed to vote"
 * ) {
 *     pokerService.vote(gameId, playerId, value).getOrThrow()
 * }
 * ```
 */
fun <E> ViewModel.launchSafe(
    eventFlow: MutableSharedFlow<E>,
    createErrorEvent: (String) -> E,
    errorMessage: String = "Operation failed",
    block: suspend () -> Unit
) {
    viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e("Error launch ${e.message ?: errorMessage}", e)
            eventFlow.emit(createErrorEvent(e.message ?: errorMessage))
        }
    }
}

fun <T, E> ViewModel.launchSafe(
    eventFlow: MutableSharedFlow<E>,
    stateFlow: MutableStateFlow<DataState<T>>,
    createErrorEvent: (String) -> E,
    errorMessage: String = "Operation failed",
    block: suspend () -> Unit
) {
    viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.e("Error launch ${e.message ?: errorMessage}", e)
            stateFlow.value = DataState.Error(e)
            eventFlow.emit(createErrorEvent(e.message ?: errorMessage))
        }
    }
}


/**
 * Severity level for user-facing messages.
 * Shared between all ViewModels to ensure consistent handling.
 */
enum class MessageType { Info, Warning, Error, Success }

data class MessageEvent(
    val message: String,
    val type: MessageType = MessageType.Error
)

sealed interface UiEvent {

    data class ShowMessage(val event: MessageEvent) : UiEvent
}
