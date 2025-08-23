package com.ciril.flagchallenge.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuestionsPayload(val questions: List<FlagQuestion>)

@JsonClass(generateAdapter = true)
data class FlagQuestion(
    val answer_id: Int,
    val countries: List<Country>,
    val country_code: String
)

@JsonClass(generateAdapter = true)
data class Country(val country_name: String, val id: Int)