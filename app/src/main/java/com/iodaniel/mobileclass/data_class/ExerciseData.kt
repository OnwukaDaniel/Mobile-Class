package com.iodaniel.mobileclass.data_class

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseData(
    var questions: ArrayList<Question> = arrayListOf()
)

@Serializable
data class Question(
    // Question
    var singleQuestion: String = "",
    val extraNote: String = "",

    //Multi-Question
    var multipleChoiceQuestion: ArrayList<MultipleChoice> = arrayListOf(),

    //DOC
    var docQuestion: String = "",
    var docStorageLinks: ArrayList<Map<String, String>> = arrayListOf(),

    var exerciseType: Int = 0,
    var timeCreated: String = "",
)

@Serializable
data class MultipleChoice(
    var question: String = "",
    var instructions: String = "",
    val extraNote: String = "",
    var media: MutableMap<String, String> = mutableMapOf(),
    var options:  Map<String, String> = mapOf(),
    var solution: String = "",
)

object ExerciseType {
    const val NORMAL_QUESTION: Int = 0
    const val DOC_QUESTION: Int = 1
    const val MULTI_QUESTION: Int = 2
}