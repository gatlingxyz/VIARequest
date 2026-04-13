package com.via.request.details


sealed interface RequestState {
    data class Loading(val approving: Boolean): RequestState
    data class Approved(val message: String): RequestState
    data class Rejected(val reason: String): RequestState
    data class Error(val message: String?): RequestState
}