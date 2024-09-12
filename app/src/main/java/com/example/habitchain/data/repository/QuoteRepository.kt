package com.example.habitchain.data.repository

import android.util.Log
import com.example.habitchain.data.api.QuoteApiService
import com.example.habitchain.data.model.Quote
import com.example.habitchain.utils.Constants.NO_QUOTE_AVAILABLE
import javax.inject.Inject

class QuoteRepository @Inject constructor(private val quoteApiService: QuoteApiService) {
    suspend fun getRandomQuote(): Quote {
        val response = quoteApiService.getRandomQuote()
        val quoteData = response.firstOrNull() ?: throw Exception(NO_QUOTE_AVAILABLE)
        return Quote(quoteData.q, quoteData.a)
    }
}