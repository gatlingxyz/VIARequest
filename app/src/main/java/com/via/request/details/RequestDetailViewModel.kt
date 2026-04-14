package com.via.request.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.via.request.models.Request
import com.via.request.service.RequestResponse
import com.via.request.service.RequestService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
        private val requestService: RequestService
): ViewModel() {

    private val _destinationFlow: MutableSharedFlow<RequestDestination> = MutableSharedFlow()
    val destinationFlow = _destinationFlow.asSharedFlow()

    private val _requestStateFlow: MutableSharedFlow<RequestState> = MutableSharedFlow()
    val requestState = _requestStateFlow.asSharedFlow()

    fun onEvent(event: RequestDetailsEvent) {
        viewModelScope.launch {
            when(event) {
                is RequestDetailsEvent.CreateNewRequest -> {
                    _destinationFlow.emit(RequestDestination.RequestDetails)
                }
                is RequestDetailsEvent.RejectRequest -> {
                    rejectRequest(event.request)
                }
                is RequestDetailsEvent.ApproveRequest -> {
                    acceptRequest(event.request)
                }
            }
        }
    }

    private suspend fun rejectRequest(request: Request) {
        _requestStateFlow.emit(RequestState.Loading(false))

        runCatching {
            requestService.rejectRequest(request)
        }
            .handleResponse()
    }

    private suspend fun acceptRequest(request: Request) {
        _requestStateFlow.emit(RequestState.Loading(true))

        runCatching {
            requestService.acceptRequest(request)
        }
            .handleResponse()

    }

    private suspend fun Result<RequestResponse>.handleResponse() {
        _destinationFlow.emit(RequestDestination.Home)

        onSuccess { response ->
            val state = if (response.accepted) {
                RequestState.Approved(response.message)
            } else {
                RequestState.Rejected(response.message)
            }

            _requestStateFlow.emit(state)
        }
            .onFailure {
                _requestStateFlow.emit(RequestState.Error(it.message))
            }
    }

}