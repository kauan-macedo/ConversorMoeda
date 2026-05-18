package br.ufpr.conversormoeda.model

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class AwesomeAPI {
    @GET("json/last/{moedas}")
    suspend fun getCotacao(
        @Path("moedas") moedas: String
    ): Response<Map<String, CotacaoResponse>>
}