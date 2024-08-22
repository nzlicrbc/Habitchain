package com.example.habitchain.data.api

import com.example.habitchain.data.model.Quote
import retrofit2.http.GET

interface QuoteApiService {
    @GET("/api/v3/quotes/random")
    suspend fun getRandomQuote(): Quote
}