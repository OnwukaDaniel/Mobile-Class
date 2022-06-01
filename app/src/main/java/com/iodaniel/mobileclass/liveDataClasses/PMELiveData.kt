package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.util.ChildEventTemplate

class PMELiveData(val ref: DatabaseReference) : LiveData<Pair<ArrayList<PlanModulesExercise>, Pair<String, Int>>>() {
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
        val list: ArrayList<PlanModulesExercise> = arrayListOf()
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                val key = snapshot.key!!
                val json = Gson().toJson(snapshot.value!!)
                val snap: ArrayList<*> = Gson().fromJson(json, ArrayList::class.java)
                for ((index, i) in snap.withIndex()) {
                    val gson = Gson().toJson(i)
                    val data: PlanModulesExercise = Gson().fromJson(gson, PlanModulesExercise::class.java)
                    list.add(data)
                }
                value = list to (key to ChildEventTemplate.onDataChange)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            value = arrayListOf<PlanModulesExercise>() to ("" to ChildEventTemplate.onCancelled)
        }
    }
}