package com.iodaniel.mobileclass.data_class

data class SolutionData(
    var singleAnswer: String = "",
    var docLinks: ArrayList<String> = arrayListOf(),
    var timeSent: String = "",
    var uid: String = "",
)
