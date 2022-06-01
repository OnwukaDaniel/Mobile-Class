package com.iodaniel.mobileclass.data_class

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseData(
    var multipleChoice: ArrayList<String> = arrayListOf(),
    var document: String = "",
    var question: String = "",
)