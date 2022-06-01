package com.iodaniel.mobileclass.data_class

import kotlinx.serialization.Serializable

@Serializable
data class PlanModulesExercise(
    var plan: String = "",
    var modules: ModuleData = ModuleData(),
    var exercise: ExerciseData = ExerciseData(),
)
