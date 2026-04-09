package com.via.request.mock

import android.net.http.HttpException
import com.via.request.Request
import com.via.request.service.RequestResponse
import com.via.request.service.RequestService
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class MockRequestService @Inject constructor(): RequestService {

    private val mockAcceptedResponse = RequestResponse(
        accepted = true,
        message = "Request approved"
    )

    private val mockRejectedResponse = RequestResponse(
        accepted = false,
        message = "Request rejected"
    )

    override suspend fun acceptRequest(request: Request): RequestResponse {
        delay(2.seconds)
        val isError = listOf(true, false).random()
        if (isError) {
            throw Throwable("Something went wrong")
        } else {
            return mockAcceptedResponse
        }
    }

    override suspend fun rejectRequest(request: Request): RequestResponse {
        return mockRejectedResponse
    }

}