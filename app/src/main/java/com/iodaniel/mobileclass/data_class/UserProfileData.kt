package com.iodaniel.mobileclass.data_class

data class UserProfileData(
    var fullName: String = "",
    var email: String = "",
    var age: String = "",
    var username: String = "",
    var uid: String = "",
    var accountType: String = "",
    var dateJoined: String = "",
)

data class InstructorDetails(
    var instructorName: String = "",
    var email: String = "",
    var age: String = "",
    var username: String = "",
    var uid: String = "",
    var dateJoined: String = "",
    var studentsEnrolled: String = "",
    var coursesCreated: String = "",
    var schemesCreated: String = "",
    var instructorImage: String = "",
    var instructorPersonalDescription: String = "",
    var instructorIdentification: String = "",
    var instructorCertificationLink: String = "",
)

data class StudentDetails(
    var fullName: String = "",
    var email: String = "",
    var age: String = "",
    var username: String = "",
    var uid: String = "",
    var accountType: String = "",
    var dateJoined: String = "",
)