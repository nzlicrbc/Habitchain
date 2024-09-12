package com.example.habitchain.data.model

data class Quote(
    val quoteText: String,
    val quoteAuthor: String
)

data class QuoteResponse(
    val q: String,
    val a: String
)