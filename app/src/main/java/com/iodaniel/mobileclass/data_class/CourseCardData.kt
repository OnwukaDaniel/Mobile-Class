package com.iodaniel.mobileclass.data_class

data class CourseCardData(
    var courseName: String = "",
    var courseImage: String = "",
    var organisationName: String = "",
    var rating: String = "",
    var price: String = "",
    var shortDescription: String = "",
    var studentsEnrolled: String = "",
    var description: String = "",
    var dateCreated: String = "",
    var others: String = "",
    var materialLink: String = "",
    val manageProfileCourseType: Int = 1,
    var courseRemoved: Boolean = false,
    var personRatingReference: ArrayList<PersonRatingReference> = arrayListOf(),
    var instructorName: String = "",
    val instructorInChargeUID: String = "",
    val courseCode: String = "",
    val courseCodePushId: String = "",
    val level: String = "",
)

data class PersonRatingReference(
    var studentUid: String = "",
    var timeRated: String = "",

    var ratingValue: String = "",
    var ratingText: String = "",
    var helpful: String = "",
    var delivery: String = "",
)