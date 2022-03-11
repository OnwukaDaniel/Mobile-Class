package com.iodaniel.mobileclass.teacher_package.classes

import kotlinx.serialization.Serializable

@Serializable
class MyCourse(
    var courseName: String = "",
    var courseImageUri: String = "",
    var teacherInCharge: String = "",
    var year: String = "",
    var time: String = "",
    var materials: ArrayList<Material> = arrayListOf(),
    var dateCreated: String = "",
) {}

@Serializable
class Material(
    var courseName: String = "",
    var note: String = "",
    var extraNote: String = "",
    var heading: String = "",
    var mediaUris: ArrayList<String> = arrayListOf(),
    var listOfMediaNames: ArrayList<String> = arrayListOf(),
    var test: ArrayList<String> = arrayListOf(),
    var teacherInCharge: String = "",
    var year: String = "",
    var time: String = "",
    var dateModified: String = "",
    var dateCreated: String = "",
) {}

@Serializable
class ClassInfo(
    var className: String = "",
    var classCode: String = "",
    var classImage: String = "",
    var teacherInChargeName: String = "",
    var teacherInChargeUID: String = "",
    var time: String = "",
    var dateModified: String = "",
    var classCodePushId: String = "",
    var datetime: String = "",
    //colors
    var red: Int = 0,
    var green: Int = 0,
    var blue: Int = 0,
) {}

@Serializable
class AssignmentQuestion(
    var questionType: String = "",
    var className: String = "",
    var classCode: String = "",
    var teacherInChargeName: String = "",
    var teacherInChargeUID: String = "",
    var time: String = "",
    var dateModified: String = "",
    var datetime: String = "",
    var dueDate: String= "",

    var instructions: String = "",
    var question: String = "",
    var solution: String = "",
    var arrAssignment: ArrayList<AssignmentQuestion> = arrayListOf(),
    var mediaUris: ArrayList<String> = arrayListOf(),
    var extraNote: String = "",
    var options: ArrayList<String> = arrayListOf(),
)

@Serializable
class BioClass(
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var gender: String = "",
    var phone: String="",
    var photo: String = "",
    var dateModified: String = "",
    var datetime: String = "",
)

@Serializable
class StudentRegistrationClass(
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var phone: String="",
    var datetimeJoined: String = "",
)