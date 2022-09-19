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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.FragmentManageUploadCompletedBinding
import com.iodaniel.mobileclass.liveDataClasses.CourseCardLiveData
import com.iodaniel.mobileclass.teacher_package.course.ActivityEditCourse
import com.iodaniel.mobileclass.util.BackgroundHelper
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import com.iodaniel.mobileclass.viewModel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class FragmentManageUploadCompleted : Fragment(), BackgroundHelper {
    private lateinit var binding: FragmentManageUploadCompletedBinding
    private var completedCoursesAdapter = CompletedCoursesAdapter()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var uIStateViewModel = UIStateViewModel()
    private var fetchedData = false
    private var networkButEmpty = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentManageUploadCompletedBinding.inflate(inflater, container, false)
        requireActivity().setActionBar(binding.manageUploadCompletedToolbar)
        getCompletedCourses()
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

    private fun getCompletedCourses() {
        startShimmer()
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child(requireContext().getString(R.string.course_path))
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()

        completedCoursesAdapter.dataset = dataset
        completedCoursesAdapter.activity = requireActivity()
        binding.manageUploadCompletedRv.adapter = completedCoursesAdapter
        binding.manageUploadCompletedRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val otherCourseLiveData = CourseCardLiveData(ref)
        ref.get().addOnSuccessListener { if (it != null) uIStateViewModel.setUIState(UiState.stateData) else uIStateViewModel.setUIState(UiState.stateNoData) }
        otherCourseLiveData.observe(viewLifecycleOwner) {
            when (it.second.second) {
                ChildEventTemplate.onChildAdded -> {
                    val key = it.second.first
                    if (key !in datasetKey) {
                        stopShimmer()
                        val courseCardData = it.first
                        val satisfied = courseCardData.manageProfileCourseType == ManageProfileCourseType.COMPLETE
                                && courseCardData.instructorInChargeUID == auth
                        if (satisfied) {
                            fetchedData = true
                            uIStateViewModel.setUIState(UiState.stateData)
                            dataset.add(courseCardData)
                            datasetKey.add(key)
                            completedCoursesAdapter.notifyItemInserted(dataset.size)
                        }
                    }
                }
                ChildEventTemplate.onChildRemoved -> {
                    val key = it.second.first
                    if (key in datasetKey) {
                        val index = datasetKey.indexOf(key)
                        datasetKey.minusElement(key)
                        dataset.removeAt(index)
                        completedCoursesAdapter.notifyItemRemoved(index)
                    }
                    if (activity != null && isAdded) if (dataset.isEmpty()) startShimmer()
                }
                ChildEventTemplate.onChildChanged -> {
                    val index = datasetKey.indexOf(it.second.first)
                    dataset[index] = it.first
                    completedCoursesAdapter.notifyItemChanged(index)
                }
                ChildEventTemplate.onChildMoved -> {}
            }
        }
    }

    override fun empty() {
        binding.manageUploadCompletedPlansEmptyRoot.visibility = View.VISIBLE
        binding.manageModulesUploadCompletedNoNetworkRoot.visibility = View.GONE
        binding.manageModulesUploadCompletedDataRoot.visibility = View.GONE
    }

    override fun notEmpty() {
        binding.manageModulesUploadCompletedDataRoot.visibility = View.VISIBLE
        binding.manageModulesUploadCompletedNoNetworkRoot.visibility = View.GONE
        binding.manageUploadCompletedPlansEmptyRoot.visibility = View.GONE
    }

    override fun noInternet() {
        binding.manageModulesUploadCompletedNoNetworkRoot.visibility = View.VISIBLE
        binding.manageModulesUploadCompletedDataRoot.visibility = View.GONE
        binding.manageUploadCompletedPlansEmptyRoot.visibility = View.GONE
    }
}

class CompletedCoursesAdapter : RecyclerView.Adapter<CompletedCoursesAdapter.ViewHolder>() {
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
        val price = "$ ${datum.price}"
        holder.price.text = price
        val se = "(${datum.studentsEnrolled})"
        holder.studentsEnrolled.text = se
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