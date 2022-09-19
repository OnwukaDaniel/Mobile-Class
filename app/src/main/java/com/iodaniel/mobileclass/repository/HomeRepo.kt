package com.iodaniel.mobileclass.repository

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.course.ActivitySelectedCourse
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.home.*
import com.iodaniel.mobileclass.liveDataClasses.CourseCardLiveData
import com.iodaniel.mobileclass.liveDataClasses.TopCourseLiveData
import com.iodaniel.mobileclass.liveDataClasses.ValueEventLiveData
import com.iodaniel.mobileclass.teacher_package.profile.ManageProfileCourseType
import com.iodaniel.mobileclass.util.ChildEventTemplate.onChildAdded
import com.iodaniel.mobileclass.util.ChildEventTemplate.onChildChanged
import com.iodaniel.mobileclass.util.ChildEventTemplate.onChildMoved
import com.iodaniel.mobileclass.util.ChildEventTemplate.onChildRemoved
import com.iodaniel.mobileclass.util.ChildEventTemplate.onDataChange
import com.iodaniel.mobileclass.util.Util.cleanContentPreferences
import com.iodaniel.mobileclass.util.Util.removeComma
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import com.iodaniel.mobileclass.viewModel.UiState
import kotlinx.coroutines.*

class HomeRepo(val activity: Activity, val context: Context, val view: View, private val viewLifecycleOwner: LifecycleOwner) {

    fun getTopCourses(rv: RecyclerView) { // TODO NOTIFY ADAPTER
        val topCourseAdapter = TopCourseAdapter()
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()
        val ref = FirebaseDatabase.getInstance().reference.child("top courses")

        topCourseAdapter.dataset = dataset
        rv.adapter = topCourseAdapter
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val topCourseLiveData = TopCourseLiveData(ref)

        topCourseLiveData.observe(viewLifecycleOwner) {
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

    fun getRelatedPreference(rv: RecyclerView, preferencesRoot: LinearLayout) {
        val preferenceTagAdapter = PreferenceTagAdapter()
        val wordsList: ArrayList<String> = arrayListOf()
        val wordsListX: ArrayList<String> = arrayListOf()
        val auth = FirebaseAuth.getInstance().currentUser ?: return
        val studentPrefRef = FirebaseDatabase.getInstance().reference.child(ActivitySelectedCourse.studentPreference).child(auth.uid)

        preferenceTagAdapter.dataset = wordsListX
        rv.adapter = preferenceTagAdapter
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val liveData = ValueEventLiveData(studentPrefRef)
        liveData.observe(viewLifecycleOwner) {
            when (it.second) {
                onDataChange -> {
                    for (i in it.first.children) {
                        val onePreference = i.value.toString()
                        preferencesRoot.visibility = View.VISIBLE
                        for (word in removeComma(onePreference).shuffled()) if (word !in wordsList) wordsList.add(word)
                        for (x in wordsList.shuffled()) if (x !in wordsListX && wordsListX.size < 10) wordsListX.add(x)
                        preferenceTagAdapter.notifyItemInserted(wordsListX.size)
                    }
                }
            }
        }
    }

    fun getOtherCourses(
        rv: RecyclerView,
        uIStateViewModel: UIStateViewModel,
        fragmentHome: FragmentHome,
        otherCoursesAdapter: OtherCoursesAdapter,
        otherShimmer: ShimmerFrameLayout
    ) {
        val ref = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.course_path))
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()
        var fetchedData = false
        val scope = CoroutineScope(Dispatchers.IO)

        otherCoursesAdapter.activity = this.activity
        otherCoursesAdapter.dataset = dataset
        rv.adapter = otherCoursesAdapter
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val otherCourseLiveData = CourseCardLiveData(ref)
        ref.get().addOnSuccessListener { dataSnapShot ->
            if (dataSnapShot != null) {
                uIStateViewModel.setUIState(UiState.stateData)
                fragmentHome.notEmpty()
            } else {
                uIStateViewModel.setUIState(UiState.stateNoData)
                fragmentHome.empty()
            }
        }

        otherCourseLiveData.observe(viewLifecycleOwner) {
            fetchedData = true
            uIStateViewModel.setUIState(UiState.stateData)
            if (dataset.size < 8) when (it.second.second) {
                onChildAdded -> {
                    val key = it.second.first
                    if (key !in datasetKey) {
                        otherShimmer.stopShimmer(); otherShimmer.visibility = View.GONE
                        val courseCardData = it.first
                        if (courseCardData.manageProfileCourseType == ManageProfileCourseType.COMPLETE) {
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
                    if (dataset.isEmpty()) {
                        otherShimmer.startShimmer()
                        otherShimmer.visibility = View.VISIBLE
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
        runBlocking {
            scope.launch { delay(5_000) }
            if (!fetchedData) uIStateViewModel.setUIState(UiState.stateNoData)
        }
    }

    fun getTopSchemes(marketOtherCoursesRv: RecyclerView) {

    }

    fun getSelectedSchemes(marketPreferenceRv: RecyclerView) {

    }

    fun getSimilarCourses(marketTopCoursesRv: RecyclerView, similarInterestRoot: LinearLayout, similarShimmer: ShimmerFrameLayout) {
        val similarCoursesAdapter = SimilarCoursesAdapter()
        val wordsList: ArrayList<String> = arrayListOf()

        val dataset: ArrayList<CourseCardData> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()

        val auth = FirebaseAuth.getInstance().currentUser ?: return

        val allCourseRef = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.course_path))
        val studentPrefRef = FirebaseDatabase.getInstance().reference.child(ActivitySelectedCourse.studentPreference).child(auth.uid)

        similarCoursesAdapter.dataset = dataset
        similarCoursesAdapter.activity = activity
        similarCoursesAdapter.viewLifecycleOwner = viewLifecycleOwner
        marketTopCoursesRv.adapter = similarCoursesAdapter
        marketTopCoursesRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val studentPrefLiveData = ValueEventLiveData(studentPrefRef)
        studentPrefLiveData.observe(viewLifecycleOwner) { pref ->
            if (pref.first.exists()) {
                for (i in pref.first.children) {
                    val onePreference = i.value.toString()
                    for (word in removeComma(onePreference).shuffled()) if (word !in wordsList) wordsList.add(word)
                }
                CourseCardLiveData(allCourseRef).observe(viewLifecycleOwner) {
                    when (it.second.second) {
                        onChildAdded -> {
                            val courseCardData = it.first
                            val key = it.second.first
                            if (key !in datasetKey && dataset.size < 8) {
                                val content = courseCardData.description
                                val courseWords = removeComma(cleanContentPreferences(content))
                                for (i in courseWords) {
                                    if (i in wordsList && courseCardData.manageProfileCourseType == ManageProfileCourseType.COMPLETE) {
                                        dataset.add(courseCardData)
                                        datasetKey.add(key)
                                        similarCoursesAdapter.notifyItemInserted(dataset.size)
                                        break
                                    }
                                }
                                marketTopCoursesRv.visibility = View.VISIBLE
                                similarInterestRoot.visibility = View.VISIBLE
                            }
                        }
                        onChildRemoved -> {}
                        onChildChanged -> {}
                        onChildMoved -> {}
                    }
                    if (dataset.isNotEmpty()) {
                        marketTopCoursesRv.visibility = View.VISIBLE
                    } else {
                        marketTopCoursesRv.visibility = View.GONE
                        similarInterestRoot.visibility = View.GONE
                    }
                    similarShimmer.visibility = View.GONE
                    similarShimmer.stopShimmer()
                }
            } else {
                similarShimmer.visibility = View.GONE
                similarShimmer.stopShimmer()
            }
        }
    }
}