package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.iodaniel.mobileclass.data_class.StudentDetails
import com.iodaniel.mobileclass.util.ChildEventTemplate

class StudentProfileLiveData(private val ref: DatabaseReference): LiveData<Pair<StudentDetails, Int>>() {
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
                value = (snapshot.getValue(StudentDetails::class.java)!! to ChildEventTemplate.onDataChange)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            value = StudentDetails() to ChildEventTemplate.onCancelled
        }
    }
}