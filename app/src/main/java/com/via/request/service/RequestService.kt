package com.via.request.service

import com.via.request.Request


interface RequestService {
    suspend fun acceptRequest(request: Request): RequestResponse
    suspend fun rejectRequest(request: Request): RequestResponse
}

data class RequestResponse(
    val accepted: Boolean,
    val message: String
)