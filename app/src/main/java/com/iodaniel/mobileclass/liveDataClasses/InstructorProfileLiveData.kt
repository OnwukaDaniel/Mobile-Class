package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.util.ChildEventTemplate

class InstructorProfileLiveData(private val ref: DatabaseReference): LiveData<Pair<InstructorDetails, Int>>() {
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
                value = (snapshot.getValue(InstructorDetails::class.java)!! to ChildEventTemplate.onDataChange)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            value = InstructorDetails() to ChildEventTemplate.onCancelled
        }
    }
}