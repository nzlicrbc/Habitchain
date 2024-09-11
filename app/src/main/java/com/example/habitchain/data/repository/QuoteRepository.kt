package com.example.habitchain.data.repository

import android.util.Log
import com.example.habitchain.data.api.QuoteApiService
import com.example.habitchain.data.model.Quote
import javax.inject.Inject

class QuoteRepository @Inject constructor(private val quoteApiService: QuoteApiService) {
    suspend fun getRandomQuote(): Quote {
        return try {
            //Log.d("QuoteRepository", "Fetching random quote")
            val response = quoteApiService.getRandomQuote()
            //Log.d("QuoteRepository", "Quote response: $response")
            Quote(response.content, response.author)
        } catch (e: Exception) {
            //Log.e("QuoteRepository", "Error fetching quote", e)
            throw e
        }
    }
}