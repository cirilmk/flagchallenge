package com.ciril.flagchallenge.data.repository

import android.content.Context
import com.ciril.flagchallenge.model.FlagQuestion
import com.ciril.flagchallenge.model.QuestionsPayload
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetsChallengeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) : ChallengeRepository {
    override suspend fun loadQuestions(): List<FlagQuestion> = withContext(Dispatchers.IO) {
        val json = context.assets.open("questions.json").bufferedReader().use { it.readText() }
        moshi.adapter(QuestionsPayload::class.java).fromJson(json)?.questions ?: emptyList()
    }
}