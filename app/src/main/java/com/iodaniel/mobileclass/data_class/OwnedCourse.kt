package com.iodaniel.mobileclass.data_class

import kotlinx.serialization.Serializable

@Serializable
data class OwnedCourse(
    val timeOwned: String = "",
    val courseCode: String = "",
    val instructorAuth: String = ""
)
