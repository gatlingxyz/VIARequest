package com.via.request.details

import com.via.request.models.Request


sealed interface RequestDetailsEvent {
    data object CreateNewRequest: RequestDetailsEvent
    data class RejectRequest(val request: Request): RequestDetailsEvent
    data class ApproveRequest(val request: Request): RequestDetailsEvent
}