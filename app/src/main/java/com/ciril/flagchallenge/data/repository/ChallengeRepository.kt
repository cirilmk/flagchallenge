package com.ciril.flagchallenge.data.repository

import com.ciril.flagchallenge.model.FlagQuestion

interface ChallengeRepository {
    suspend fun loadQuestions(): List<FlagQuestion>
}
