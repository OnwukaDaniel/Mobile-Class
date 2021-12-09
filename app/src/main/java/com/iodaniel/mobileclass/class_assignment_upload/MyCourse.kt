package com.iodaniel.mobileclass.class_assignment_upload

import kotlinx.serialization.Serializable

@Serializable
class MyCourse(
    val courseName: String = "",
    val courseImageUri: String = "",
    val teacherInCharge: String = "",
    val year: String = "",
    val time: String = "",
    val dateCreated: String = "",
    val materials: ArrayList<Material> = arrayListOf(),
    val classwork: ArrayList<String> = arrayListOf(),
    val test: ArrayList<String> = arrayListOf(),
)

@Serializable
class Material(
    val courseName: String = "",
    val note: String = "",
    val extraNote: String = "",
    val title: String = "",

    val teacherInCharge: String = "",
    val year: String = "",
    val time: String = "",
    val dateModified: String = "",
    val dateCreated: String = "",
)