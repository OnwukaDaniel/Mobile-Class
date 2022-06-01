package com.iodaniel.mobileclass.data_class

import kotlinx.serialization.Serializable

@Serializable
data class ModuleData(
    var content: String = "",
    var uris: ArrayList<MutableMap<String, String>> = arrayListOf(),
    var extraNote: String = "",
)
