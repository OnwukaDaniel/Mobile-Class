package com.iodaniel.mobileclass.teacher_package.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.FragmentManageUploadSavedBinding
import com.iodaniel.mobileclass.liveDataClasses.CourseCardLiveData
import com.iodaniel.mobileclass.repository.ManageCoursesRepo
import com.iodaniel.mobileclass.teacher_package.course.ActivityEditCourse
import com.iodaniel.mobileclass.util.BackgroundHelper
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import com.iodaniel.mobileclass.viewModel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class FragmentManageUploadSaved : Fragment(), BackgroundHelper {
    private lateinit var binding: FragmentManageUploadSavedBinding
    private lateinit var manageCoursesRepo: ManageCoursesRepo
    private var uIStateViewModel = UIStateViewModel()
    private var dialogs = Dialogs()
    private val savedCoursesAdapter = SavedCoursesAdapter()
    private var fetchedData = false
    private var networkButEmpty = false
    private val mfV: MessageFragmentViewModel by activityViewModels()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentManageUploadSavedBinding.inflate(inflater, container, false)
        manageCoursesRepo = ManageCoursesRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        getSavedCourses()
        return binding.root
    }

    private fun startShimmer(){
        binding.uploadsShimmer.visibility = View.VISIBLE
        binding.uploadsShimmer.startShimmer()
    }

    private fun stopShimmer(){
        binding.uploadsShimmer.visibility = View.GONE
        binding.uploadsShimmer.stopShimmer()
    }

    private fun getSavedCourses() {
        startShimmer()
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child(requireContext().getString(R.string.course_path))
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()

        savedCoursesAdapter.dataset = dataset
        savedCoursesAdapter.activity = requireActivity()
        binding.manageUploadSavedRv.adapter = savedCoursesAdapter
        binding.manageUploadSavedRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        ref.get().addOnSuccessListener { dataSnapShot ->
            networkButEmpty = true
            if (dataSnapShot != null) uIStateViewModel.setUIState(UiState.stateData) else uIStateViewModel.setUIState(UiState.stateNoData)
        }
        val otherCourseLiveData = CourseCardLiveData(ref)
        otherCourseLiveData.observe(viewLifecycleOwner) {
            when (it.second.second) {
                ChildEventTemplate.onChildAdded -> {
                    val key = it.second.first
                    if (key !in datasetKey) {
                        stopShimmer()
                        val courseCardData = it.first
                        val satisfied = courseCardData.manageProfileCourseType == ManageProfileCourseType.SAVED
                                && courseCardData.instructorInChargeUID == auth
                        if (satisfied) {
                            uIStateViewModel.setUIState(UiState.stateData)
                            fetchedData = true
                            dataset.add(courseCardData)
                            datasetKey.add(key)
                            savedCoursesAdapter.notifyItemInserted(dataset.size)
                        }
                    }
                }
                ChildEventTemplate.onChildRemoved -> {
                    val key = it.second.first
                    if (key in datasetKey) {
                        val index = datasetKey.indexOf(key)
                        datasetKey.minusElement(key)
                        dataset.removeAt(index)
                        savedCoursesAdapter.notifyItemRemoved(index)
                    }
                    if (activity != null && isAdded) if (dataset.isEmpty()) startShimmer()
                }
                ChildEventTemplate.onChildChanged -> {
                    val index = datasetKey.indexOf(it.second.first)
                    dataset[index] = it.first
                    savedCoursesAdapter.notifyItemChanged(index)
                }
                ChildEventTemplate.onChildMoved -> {}
            }
        }
    }

    override fun empty() {
        binding.manageUploadSavedPlansEmptyRoot.visibility = View.VISIBLE
        binding.manageModulesUploadSavedNoNetworkRoot.visibility = View.GONE
        binding.manageModulesUploadSavedDataRoot.visibility = View.GONE
    }

    override fun notEmpty() {
        binding.manageModulesUploadSavedDataRoot.visibility = View.VISIBLE
        binding.manageModulesUploadSavedNoNetworkRoot.visibility = View.GONE
        binding.manageUploadSavedPlansEmptyRoot.visibility = View.GONE
    }

    override fun noInternet() {
        binding.manageModulesUploadSavedNoNetworkRoot.visibility = View.VISIBLE
        binding.manageModulesUploadSavedDataRoot.visibility = View.GONE
        binding.manageUploadSavedPlansEmptyRoot.visibility = View.GONE
    }
}

class SavedCoursesAdapter : RecyclerView.Adapter<SavedCoursesAdapter.ViewHolder>() {
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.row_manage_courses_image)
        val title: TextView = itemView.findViewById(R.id.row_manage_courses_title)
        val level: TextView = itemView.findViewById(R.id.row_manage_courses_level)
        val studentsEnrolled: TextView = itemView.findViewById(R.id.row_manage_courses_students_enrolled)
        val price: TextView = itemView.findViewById(R.id.row_manage_courses_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_manage_upload, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.image)
        holder.title.text = datum.courseName
        holder.level.text = datum.level
        holder.studentsEnrolled.visibility = View.GONE
        val price = "$ ${datum.price}"
        holder.price.text = price
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityEditCourse::class.java)
            val json = Gson().toJson(datum)
            intent.putExtra(context.getString(R.string.manage_course_data_intent), json)
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount() = dataset.size
}