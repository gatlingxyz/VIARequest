package com.via.request.models

import kotlinx.serialization.Serializable


data class Request(
    val headline: String,
    val message: String,
)