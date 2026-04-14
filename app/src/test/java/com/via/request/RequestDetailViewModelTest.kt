@file:OptIn(ExperimentalCoroutinesApi::class)

package com.via.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.via.request.details.RequestDestination
import com.via.request.details.RequestDetailViewModel
import com.via.request.details.RequestDetailsEvent
import com.via.request.details.RequestState
import com.via.request.models.Request
import com.via.request.service.RequestResponse
import com.via.request.service.RequestService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(DelicateCoroutinesApi::class)
class RequestDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun setUpViewModelWithSuccessService(): RequestDetailViewModel {
        val mockRequestService = object: RequestService {
            override suspend fun acceptRequest(request: Request): RequestResponse {
                return RequestResponse(
                    accepted = true,
                    message = "accepted"
                )
            }

            override suspend fun rejectRequest(request: Request): RequestResponse {
                return RequestResponse(
                    accepted = false,
                    message = "rejected"
                )
            }

        }

        return RequestDetailViewModel(
            requestService = mockRequestService
        )
    }

    @Test
    fun requestDetailViewModel_onEvent_createNewRequest() = runTest {
        val viewModel = setUpViewModelWithSuccessService()

        val values = mutableListOf<RequestDestination>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.destinationFlow.toList(values)
        }

        viewModel.onEvent(RequestDetailsEvent.CreateNewRequest)

        Assert.assertEquals(
            RequestDestination.RequestDetails::class,
            values[0]::class,

        )
    }

    @Test
    fun requestDetailViewModel_onEvent_ApproveRequest() = runTest {
        val viewModel = setUpViewModelWithSuccessService()

        val request = Request(
            headline = "Headline",
            message = "Message"
        )

        val values = mutableListOf<RequestState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.requestState.toList(values)
        }

        viewModel.onEvent(RequestDetailsEvent.ApproveRequest(request))

        Assert.assertEquals(
            RequestState.Loading::class,
            values[0]::class
        )

        Assert.assertEquals(
            RequestState.Approved::class,
            values[1]::class
        )

    }

    @Test
    fun requestDetailViewModel_onEvent_RejectRequest() = runTest {
        val viewModel = setUpViewModelWithSuccessService()

        val request = Request(
            headline = "Headline",
            message = "Message"
        )

        val values = mutableListOf<RequestState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.requestState.toList(values)
        }

        viewModel.onEvent(RequestDetailsEvent.RejectRequest(request))

        Assert.assertEquals(
            RequestState.Loading::class,
            values[0]::class
        )

        Assert.assertEquals(
            RequestState.Rejected::class,
            values[1]::class
        )

    }

    @Test
    fun requestDetailViewModel_onEvent_RejectRequest_Error() = runTest {
        val errorMessage = "Forced error"

        val mockRequestService = object: RequestService {
            override suspend fun acceptRequest(request: Request): RequestResponse {
                throw Throwable("An error message")
            }

            override suspend fun rejectRequest(request: Request): RequestResponse {
                throw Throwable(errorMessage)
            }

        }

        val viewModel = RequestDetailViewModel(
            requestService = mockRequestService
        )

        val request = Request(
            headline = "Headline",
            message = "Message"
        )

        val values = mutableListOf<RequestState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.requestState.toList(values)
        }

        viewModel.onEvent(RequestDetailsEvent.RejectRequest(request))

        Assert.assertEquals(
            RequestState.Loading::class,
            values[0]::class
        )

        Assert.assertEquals(
            RequestState.Error::class,
            values[1]::class
        )

        Assert.assertEquals(
            errorMessage,
            (values[1] as RequestState.Error).message
        )

    }

}