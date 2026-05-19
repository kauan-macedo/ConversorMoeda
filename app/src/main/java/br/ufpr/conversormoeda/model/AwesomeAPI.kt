package br.ufpr.conversormoeda.model

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AwesomeAPI {
    @GET("last/{pair}")
    suspend fun getCotacao(@Path("pair") pair: String): Response<Map<String, ExchangeResponse>>
}