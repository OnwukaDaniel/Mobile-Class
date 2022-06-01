package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.iodaniel.mobileclass.util.ChildEventTemplate

class UserTagLiveData(private val ref: DatabaseReference) : LiveData<Pair<MutableSet<String>, Int>>() {

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
                val json = Gson().toJson(snapshot.value.toString())
                val data: MutableSet<String> = Gson().fromJson(json, MutableSet::class.java) as MutableSet<String>
                value = data to ChildEventTemplate.onDataChange
            }
        }

        override fun onCancelled(error: DatabaseError) {
            value = mutableSetOf<String>() to ChildEventTemplate.onCancelled
        }
    }
}