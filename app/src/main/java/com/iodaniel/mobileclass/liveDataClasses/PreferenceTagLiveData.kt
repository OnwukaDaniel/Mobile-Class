package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.util.ChildEventTemplate

class PreferenceTagLiveData(private val ref: DatabaseReference) : LiveData<Pair<Pair<String, String>, Int>>() {

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
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()) {
                val data = snapshot.value.toString()
                val dataKey = data to snapshot.key.toString()
                value = dataKey to ChildEventTemplate.onChildAdded
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()) {
                val data = snapshot.value.toString()
                val dataKey = data to snapshot.key.toString()
                value = dataKey to ChildEventTemplate.onChildChanged
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val data = snapshot.value.toString()
                val dataKey = data to snapshot.key.toString()
                value = dataKey to ChildEventTemplate.onChildRemoved
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()) {
                val data = snapshot.value.toString()
                val dataKey = data to snapshot.key.toString()
                value = dataKey to ChildEventTemplate.onChildMoved
            }
        }

        override fun onCancelled(error: DatabaseError) {
            val dataKey = "" to ""
            value = dataKey to ChildEventTemplate.onCancelled
        }
    }
}