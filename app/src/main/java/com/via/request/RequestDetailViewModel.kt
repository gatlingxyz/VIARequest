package com.via.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.via.request.service.RequestResponse
import com.via.request.service.RequestService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

sealed interface RequestState {
    data object Loading: RequestState
    data class Approved(val message: String): RequestState
    data class Rejected(val reason: String): RequestState
    data class Error(val message: String): RequestState
}

@Serializable data class Request(
    val headline: String,
    val message: String,
)

sealed interface RequestEvent {
    data object CreateNewRequest: RequestEvent
    data class RejectRequest(val request: Request): RequestEvent
    data class ApproveRequest(val request: Request): RequestEvent
}



@HiltViewModel
class RequestDetailViewModel @Inject constructor(
        private val requestService: RequestService
): ViewModel() {

    private val _destinationFlow: MutableStateFlow<RequestDestination> = MutableStateFlow(
        RequestDestination.Home)
    val destinationFlow = _destinationFlow.asStateFlow()

    private val _requestStateFlow: MutableSharedFlow<RequestState> = MutableSharedFlow()
    val requestState = _requestStateFlow.asSharedFlow()

    fun onEvent(event: RequestEvent) {
        viewModelScope.launch {
            handleEvent(event)
        }
    }

    private suspend fun handleEvent(event: RequestEvent) {
        when(event) {
            is RequestEvent.CreateNewRequest -> {
                _destinationFlow.emit(
                    RequestDestination.RequestDetails(
                        headline = requestHeadlines.random(),
                        message = requestMessages.random(),
                    )
                )
            }
            is RequestEvent.RejectRequest -> {
                rejectRequest(event.request)
            }
            is RequestEvent.ApproveRequest -> {
                acceptRequest(event.request)
            }
        }
    }

    private suspend fun rejectRequest(request: Request) {
        _requestStateFlow.emit(RequestState.Loading)

        runCatching {
            requestService.rejectRequest(request)
        }
            .handleResponse()
    }

    private suspend fun acceptRequest(request: Request) {
        _requestStateFlow.emit(RequestState.Loading)

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
                _requestStateFlow.emit(RequestState.Error(it.message ?: "Something went wrong."))
            }
    }

    private val requestHeadlines = listOf(
        "I wanna be the very best",
        "Like no one ever war",
        "To catch them is my real test",
        "To train them is my cause",
        "I will travel across the land",
        "Searching far and wide",
        "Teach Pokemon to understand",
        "The power that's inside"

    )

    private val requestMessages = listOf(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean ultricies, purus quis viverra mattis, justo quam iaculis erat, at cursus velit justo nec libero. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nam et libero facilisis, commodo orci in, hendrerit enim. Etiam congue nec orci a placerat. Donec libero ipsum, aliquet nec lobortis vel, feugiat sed erat. Suspendisse imperdiet, massa in varius lobortis, nisi nibh porta quam, eu ullamcorper mi nulla sed justo. Maecenas fermentum imperdiet lorem quis egestas.",
        "Nunc facilisis mauris nulla, eget lacinia libero euismod a. Aliquam erat volutpat. Vestibulum molestie urna non blandit congue. Nullam risus tellus, dignissim ac ante id, blandit rhoncus magna. Vivamus ornare fermentum consequat. Sed quis ex vestibulum, porttitor felis sit amet, eleifend sapien. Integer tempus molestie egestas. Nunc et orci sit amet lorem molestie mollis varius in leo. Cras mollis elit sed sem aliquet, nec tempus erat interdum. Praesent eu lacus quis libero fermentum pulvinar. Suspendisse potenti. Sed bibendum felis sit amet tellus convallis sollicitudin. Aliquam eget venenatis est, et vulputate magna. Vestibulum iaculis ante eu pellentesque bibendum. Fusce tristique eu urna at bibendum. Maecenas nec quam vel felis vehicula ultrices.",
        "Praesent eu accumsan mi. Pellentesque sollicitudin cursus orci, non rhoncus risus interdum a. Etiam vitae porta lorem. Suspendisse potenti. Maecenas magna urna, commodo eget ex vitae, aliquet blandit odio. Sed pulvinar enim ac lobortis eleifend. Quisque id turpis non eros lobortis consequat nec ut enim. Morbi pellentesque vulputate rhoncus. Morbi faucibus tincidunt erat. Donec diam arcu, iaculis in iaculis non, commodo ut nulla. Curabitur quis urna sit amet nisi faucibus mollis eu ut eros.",
        "Morbi ac turpis urna. Maecenas molestie eros dolor, ut faucibus erat euismod in. Nunc in sapien eget quam cursus rutrum. Nulla varius dictum leo, ut fermentum lectus finibus vel. Vestibulum in turpis ut odio sollicitudin tempus. Pellentesque quis velit sodales, pretium massa vel, consectetur leo. Sed quis euismod libero. Suspendisse commodo mauris non nunc hendrerit viverra. Quisque vel mauris eget nunc tristique auctor. Phasellus nec ultricies orci. Pellentesque eu lorem mollis, vestibulum enim sit amet, venenatis ipsum. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Suspendisse potenti. Duis nec porta arcu. Curabitur feugiat risus vel dignissim feugiat.",
        "Sed volutpat nunc et lorem rhoncus, in bibendum est volutpat. Nunc cursus libero risus, at sagittis augue tincidunt eu. Nunc congue tellus tortor, et commodo nunc laoreet id. Donec id gravida tellus, sit amet porta eros. Aenean egestas diam a sapien gravida, sed congue nulla pulvinar. Maecenas lacinia rutrum enim. Proin aliquam massa nunc, vitae pellentesque ante porta ut. Integer hendrerit sollicitudin iaculis. Suspendisse vitae vestibulum nibh.",
    )


}