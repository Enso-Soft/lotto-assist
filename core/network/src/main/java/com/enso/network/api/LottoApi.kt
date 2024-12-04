package com.enso.network.api

import com.enso.network.model.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LottoApi {
    @GET("common.do")
    suspend fun getLottoNumber(
        @Query("method") method: String = "getLottoNumber",
        @Query("drwNo") drwNo: Int
    ): NetworkResponse<String>
}