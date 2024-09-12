package com.example.habitchain.data.repository

import android.util.Log
import com.example.habitchain.data.api.QuoteApiService
import com.example.habitchain.data.model.Quote
import javax.inject.Inject

class QuoteRepository @Inject constructor(private val quoteApiService: QuoteApiService) {
    suspend fun getRandomQuote(): Quote {
        val response = quoteApiService.getRandomQuote()
        val quoteData = response.firstOrNull() ?: throw Exception("No quote available")
        return Quote(quoteData.q, quoteData.a)
    }
}