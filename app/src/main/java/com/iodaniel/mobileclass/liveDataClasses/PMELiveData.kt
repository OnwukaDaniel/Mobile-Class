package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.util.ChildEventTemplate

class PMELiveData(val ref: DatabaseReference) : LiveData<Pair<ArrayList<PlanModulesExercise>, Int>>() {
    private val listener = Listener()

    override fun onActive() {
        super.onActive()
        ref.addValueEventListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        ref.removeEventListener(listener)
    }

    inner class Listener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val pmeList: ArrayList<PlanModulesExercise> = arrayListOf()
                for ((index, i) in snapshot.children.withIndex()) {
                    val value = Gson().toJson(i.value)
                    val data: PlanModulesExercise = Gson().fromJson(value, PlanModulesExercise::class.java)
                    pmeList.add(data)
                }
                value = pmeList to ChildEventTemplate.onDataChange
            }
        }

        override fun onCancelled(error: DatabaseError) {
            value = arrayListOf<PlanModulesExercise>() to ChildEventTemplate.onCancelled
        }
    }
}