package com.iodaniel.mobileclass.class_assignment_upload.classes

import kotlinx.serialization.Serializable

@Serializable
class MyCourse(
    val courseName: String = "",
    val courseImageUri: String = "",
    val teacherInCharge: String = "",
    val year: String = "",
    val time: String = "",
    val materials: ArrayList<Material> = arrayListOf(),
    val dateCreated: String = "",
)

@Serializable
class Material(
    val courseName: String = "",
    val note: String = "",
    val extraNote: String = "",
    val title: String = "",
    val mediaUris: ArrayList<String> = arrayListOf(),
    val classwork: ArrayList<String> = arrayListOf(),
    val test: ArrayList<String> = arrayListOf(),
    val teacherInCharge: String = "",
    val year: String = "",
    val time: String = "",
    val dateModified: String = "",
    val dateCreated: String = "",
)

@Serializable
class ClassInfo(
    val className: String = "",
    val classCode: String = "",
    val classImage: String = "",
    val teacherInChargeName: String = "",
    val teacherInChargeUID: String = "",
    val time: String = "",
    val dateModified: String = "",
    val dateCreated: String = "",
    //colors
    val red: Int=0,
    val green: Int=0,
    val blue: Int=0,

)