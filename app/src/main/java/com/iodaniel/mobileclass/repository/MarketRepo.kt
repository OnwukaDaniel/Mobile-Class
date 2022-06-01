package com.iodaniel.mobileclass.repository

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.home.OtherCoursesAdapter
import com.iodaniel.mobileclass.home.PreferenceTagAdapter
import com.iodaniel.mobileclass.home.TopCourseAdapter
import com.iodaniel.mobileclass.liveDataClasses.OtherCourseLiveData
import com.iodaniel.mobileclass.liveDataClasses.PreferenceTagLiveData
import com.iodaniel.mobileclass.liveDataClasses.TopCourseLiveData
import com.iodaniel.mobileclass.liveDataClasses.UserTagLiveData
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.ChildEventTemplate.onChildAdded
import com.iodaniel.mobileclass.util.ChildEventTemplate.onChildChanged
import com.iodaniel.mobileclass.util.ChildEventTemplate.onChildMoved
import com.iodaniel.mobileclass.util.ChildEventTemplate.onChildRemoved

class MarketRepo(val activity: Activity, val context: Context, val view: View, private val viewLifecycleOwner: LifecycleOwner) {

    fun getTopCourses(rv: RecyclerView){ // TODO NOTIFY ADAPTER
        val topCourseAdapter = TopCourseAdapter()
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()
        val ref = FirebaseDatabase.getInstance().reference.child("top courses")

        topCourseAdapter.dataset = dataset
        rv.adapter = topCourseAdapter
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val topCourseLiveData = TopCourseLiveData(ref)

        topCourseLiveData.observe(viewLifecycleOwner){
            when (it.second.second) {
                onChildAdded -> {
                    val key = it.second.first
                    if (key !in datasetKey) {
                        val courseCardData = it.first
                        dataset.add(courseCardData)
                        datasetKey.add(key)
                        topCourseAdapter.notifyItemInserted(dataset.size)
                    }
                }
                onChildRemoved -> {
                    val key = it.second.first
                    if (key in datasetKey) {
                        val index = datasetKey.indexOf(key)
                        datasetKey.minusElement(key)
                        dataset.removeAt(index)
                        topCourseAdapter.notifyItemRemoved(index)
                    }
                }
                onChildChanged -> {
                    val index = datasetKey.indexOf(it.second.first)
                    dataset[index] = it.first
                    topCourseAdapter.notifyItemChanged(index)
                }
                onChildMoved -> {}
            }
        }
    }

    fun getRelatedPreference(rv: RecyclerView){
        val preferenceTagAdapter = PreferenceTagAdapter()
        val dataset: ArrayList<String> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()
        var userTagDataset: MutableSet<String> = mutableSetOf()

        val ref = FirebaseDatabase.getInstance().reference.child("user pref tag")
        val userTagLiveData = UserTagLiveData(ref)

        userTagLiveData.observe(viewLifecycleOwner){
            when (it.second) {
                ChildEventTemplate.onDataChange -> {
                    userTagDataset = it.first

                    preferenceTagAdapter.dataset = dataset
                    preferenceTagAdapter.datasetSelected = userTagDataset
                    rv.adapter = preferenceTagAdapter
                    rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    val userPreferenceLiveData = PreferenceTagLiveData(ref)
                    userPreferenceLiveData.observe(viewLifecycleOwner){ prefLiveData ->
                        when (prefLiveData.second) {
                            onChildAdded -> {
                                val key = prefLiveData.first.second
                                if (key !in datasetKey) {
                                    val data = prefLiveData.first.first
                                    dataset.add(data)
                                    datasetKey.add(key)
                                    preferenceTagAdapter.notifyItemInserted(dataset.size)
                                }
                            }
                            onChildRemoved -> {
                                val key = prefLiveData.first.second
                                if (key in datasetKey) {
                                    val index = datasetKey.indexOf(key)
                                    datasetKey.minusElement(key)
                                    dataset.removeAt(index)
                                    preferenceTagAdapter.notifyItemRemoved(index)
                                }
                            }
                            onChildChanged -> {
                                val index = datasetKey.indexOf(prefLiveData.first.second)
                                dataset[index] = prefLiveData.first.first
                                preferenceTagAdapter.notifyItemChanged(index)
                            }
                            onChildMoved -> {}
                        }
                    }

                }
            }
        }
    }

    fun getOtherCourses(rv: RecyclerView){
        val ref = FirebaseDatabase.getInstance().reference
            .child(context.getString(R.string.course_path))
        val otherCoursesAdapter = OtherCoursesAdapter()
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()

        otherCoursesAdapter.dataset = dataset
        rv.adapter = otherCoursesAdapter
        rv.layoutManager = GridLayoutManager(context,2, GridLayoutManager.VERTICAL, false)
        val otherCourseLiveData = OtherCourseLiveData(ref)

        otherCourseLiveData.observe(viewLifecycleOwner){
            when (it.second.second) {
                onChildAdded -> {
                    val key = it.second.first
                    if (key !in datasetKey) {
                        val courseCardData = it.first
                        if (courseCardData.courseUploadCompletedCompleted){
                            dataset.add(courseCardData)
                            datasetKey.add(key)
                            otherCoursesAdapter.notifyItemInserted(dataset.size)
                        }
                    }
                }
                onChildRemoved -> {
                    val key = it.second.first
                    if (key in datasetKey) {
                        val index = datasetKey.indexOf(key)
                        datasetKey.minusElement(key)
                        dataset.removeAt(index)
                        otherCoursesAdapter.notifyItemRemoved(index)
                    }
                }
                onChildChanged -> {
                    val index = datasetKey.indexOf(it.second.first)
                    dataset[index] = it.first
                    otherCoursesAdapter.notifyItemChanged(index)
                }
                onChildMoved -> {}
            }
        }
    }
}