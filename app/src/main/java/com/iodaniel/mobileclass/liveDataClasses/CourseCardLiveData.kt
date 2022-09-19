package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.util.ChildEventTemplate

class CourseCardLiveData(private val ref: DatabaseReference) : LiveData<Pair<CourseCardData, Pair<String, Int>>>() {

    private val listener = Listener()

    override fun onActive() {
        super.onActive()
        ref.addChildEventListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        ref.removeEventListener(listener)
    }

    inner class Listener : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()){
                val courseCardData = snapshot.getValue(CourseCardData::class.java)!!
                val keyTypePair = snapshot.key.toString() to ChildEventTemplate.onChildAdded
                value = courseCardData to keyTypePair
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()){
                val courseCardData = snapshot.getValue(CourseCardData::class.java)!!
                val keyTypePair = snapshot.key.toString() to ChildEventTemplate.onChildChanged
                value = courseCardData to keyTypePair
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                val courseCardData = snapshot.getValue(CourseCardData::class.java)!!
                val keyTypePair = snapshot.key.toString() to ChildEventTemplate.onChildRemoved
                value = courseCardData to keyTypePair
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()){
                val courseCardData = snapshot.getValue(CourseCardData::class.java)!!
                val keyTypePair = snapshot.key.toString() to ChildEventTemplate.onChildMoved
                value = courseCardData to keyTypePair
            }
        }

        override fun onCancelled(error: DatabaseError) {
            val keyTypePair = "" to ChildEventTemplate.onCancelled
            value = CourseCardData() to keyTypePair
        }
    }
}