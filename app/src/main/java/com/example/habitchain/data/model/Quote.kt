package com.example.habitchain.data.model

data class Quote(
    val text: String,
    val author: String
)

data class QuoteResponse(
    val content: String,
    val author: String
)