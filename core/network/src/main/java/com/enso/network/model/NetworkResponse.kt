package com.enso.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkResponse<T>(
    val data: T
)