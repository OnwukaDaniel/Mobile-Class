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
    var instructorImage: String = "",
    var instructorPersonalDescription: String = "",
    var instructorIdentificationHash: HashMap<String, String> = hashMapOf(),
    var instructorCertificationHash: HashMap<String, String> = hashMapOf(),
    var instructorIdentificationVerified: Boolean = false,
    var instructorCertificationVerified: Boolean = false,
)

data class StudentDetails(
    var fullName: String = "",
    var email: String = "",
    var age: String = "",
    var username: String = "",
    var uid: String = "",
    var accountType: String = "",
    var organisation: String = "",
    var dateJoined: String = "",
    var image: String = "",
)