package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.iodaniel.mobileclass.util.ChildEventTemplate

class VerHomeLiveData(val ref: DatabaseReference) : LiveData<Pair<MutableMap<String, Boolean>, Int>>() {
    private val listener = Listener()

    override fun onActive() {
        ref.addValueEventListener(listener)
    }

    override fun onInactive() {
        ref.removeEventListener(listener)
    }

    inner class Listener: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                value = snapshot.value as MutableMap<String, Boolean> to ChildEventTemplate.onDataChange
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }
    }
}