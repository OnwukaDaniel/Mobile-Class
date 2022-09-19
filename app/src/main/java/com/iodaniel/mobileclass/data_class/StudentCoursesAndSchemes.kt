package com.iodaniel.mobileclass.data_class

data class StudentCoursesAndSchemes(
    var coursesOwned: ArrayList<CoursesHistoryData> = arrayListOf(),

    var schemesOwned: ArrayList<SchemesHistoryData> = arrayListOf(),
)

data class CoursesHistoryData(
    val timeViewed: String = "",
    val courseCode: String = "",
    val instructorAuth: String = "",
)

data class SchemesHistoryData(
    val timeViewed: String = "",
    val schemesCode: String = "",
    val instructorAuth: String = "",
)
