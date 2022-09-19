package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.iodaniel.mobileclass.util.ChildEventTemplate

class ChildEventLiveData(private val ref: DatabaseReference): LiveData<Pair<DataSnapshot, Int>>() {
    override fun onActive() {
        super.onActive()
    }

    override fun onInactive() {
        super.onInactive()
    }

    inner class Listener : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()){
                value = snapshot to ChildEventTemplate.onChildAdded
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()){
                value = snapshot to ChildEventTemplate.onChildAdded
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                value = snapshot to ChildEventTemplate.onChildAdded
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.exists()){
                value = snapshot to ChildEventTemplate.onChildAdded
            }
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }
}