package com.example.habitchain.data.api

import com.example.habitchain.data.model.QuoteResponse
import retrofit2.http.GET

interface QuoteApiService {
    @GET("random")
    suspend fun getRandomQuote(): List<QuoteResponse>
}