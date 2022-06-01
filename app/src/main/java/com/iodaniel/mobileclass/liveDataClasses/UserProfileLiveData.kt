package com.iodaniel.mobileclass.liveDataClasses

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.iodaniel.mobileclass.data_class.UserProfileData
import com.iodaniel.mobileclass.util.ChildEventTemplate

class UserProfileLiveData(private val ref: DatabaseReference) : LiveData<Pair<String, Int>>() {

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
        private val arrayOfUserInfo: ArrayList<UserProfileData> = arrayListOf()

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                value = snapshot.value.toString() to ChildEventTemplate.onDataChange
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }
}