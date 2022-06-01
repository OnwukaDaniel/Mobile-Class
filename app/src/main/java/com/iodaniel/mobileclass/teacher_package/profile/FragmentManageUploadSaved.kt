package com.iodaniel.mobileclass.teacher_package.profile

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.FragmentManageUploadSavedBinding
import com.iodaniel.mobileclass.liveDataClasses.OtherCourseLiveData
import com.iodaniel.mobileclass.repository.ManageCoursesRepo
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.teacher_package.course.ActivityEditCourse
import com.iodaniel.mobileclass.util.BackgroundHelper
import com.iodaniel.mobileclass.util.ChildEventTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentManageUploadSaved : Fragment(), BackgroundHelper {
    private lateinit var binding: FragmentManageUploadSavedBinding
    private lateinit var manageCoursesRepo: ManageCoursesRepo
    private val connectionListener = ConnectionListener()
    private var pDialog: Dialog? = null
    private val savedCoursesAdapter = SavedCoursesAdapter()
    private var connectionRef = FirebaseDatabase.getInstance().reference
    private var fetchedData = false
    private var networkButEmpty = false
    private val scope = CoroutineScope(Dispatchers.IO)

    inner class ConnectionListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!fetchedData) this@FragmentManageUploadSaved.empty() else this@FragmentManageUploadSaved.notEmpty()
            networkButEmpty = true
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentManageUploadSavedBinding.inflate(inflater, container, false)
        requireActivity().setActionBar(binding.manageUploadSavedToolbar)
        requireActivity().title = "Saved courses"
        connectionRef = connectionRef.child(getString(R.string.network_value))
        manageCoursesRepo = ManageCoursesRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        pDialog = Util.progressDialog("Please wait...", requireContext(), requireActivity())
        pDialog?.show()
        scope.launch {
            delay(12_000)
            if (activity != null) requireActivity().runOnUiThread {
                if (networkButEmpty && !fetchedData) this@FragmentManageUploadSaved.empty()
                if (fetchedData) this@FragmentManageUploadSaved.notEmpty()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSavedCourses()
        connectionRef.addValueEventListener(connectionListener)
    }

    private fun getSavedCourses() {
        val auth = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child(requireContext().getString(R.string.course_path))
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        val datasetKey: MutableSet<String> = mutableSetOf()

        savedCoursesAdapter.dataset = dataset
        savedCoursesAdapter.activity = requireActivity()
        binding.manageUploadSavedRv.adapter = savedCoursesAdapter
        binding.manageUploadSavedRv.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        val otherCourseLiveData = OtherCourseLiveData(ref)
        otherCourseLiveData.observe(viewLifecycleOwner) {
            when (it.second.second) {
                ChildEventTemplate.onChildAdded -> {
                    val key = it.second.first
                    if (key !in datasetKey) {
                        val courseCardData = it.first
                        val satisfied = !courseCardData.courseUploadCompletedCompleted
                                && courseCardData.manageProfileCourseType == ManageProfileCourseType.SAVED
                                && courseCardData.instructorInChargeUID == auth
                        if (satisfied) {
                            dataset.add(courseCardData)
                            datasetKey.add(key)
                            savedCoursesAdapter.notifyItemInserted(dataset.size)
                        }
                    }
                    fetchedData = true
                    this@FragmentManageUploadSaved.notEmpty()
                }
                ChildEventTemplate.onChildRemoved -> {
                    val key = it.second.first
                    if (key in datasetKey) {
                        val index = datasetKey.indexOf(key)
                        datasetKey.minusElement(key)
                        dataset.removeAt(index)
                        savedCoursesAdapter.notifyItemRemoved(index)
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        connectionRef.removeEventListener(connectionListener)
        pDialog?.dismiss()
    }

    override fun empty() {
        binding.manageUploadSavedPlansEmptyRoot.visibility = View.VISIBLE
        binding.manageModulesUploadSavedNoNetworkRoot.visibility = View.GONE
        binding.manageModulesUploadSavedDataRoot.visibility = View.GONE
        pDialog?.dismiss()
    }

    override fun notEmpty() {
        binding.manageModulesUploadSavedDataRoot.visibility = View.VISIBLE
        binding.manageModulesUploadSavedNoNetworkRoot.visibility = View.GONE
        binding.manageUploadSavedPlansEmptyRoot.visibility = View.GONE
        pDialog?.dismiss()
    }

    override fun noInternet() {
        binding.manageModulesUploadSavedNoNetworkRoot.visibility = View.VISIBLE
        binding.manageModulesUploadSavedDataRoot.visibility = View.GONE
        binding.manageUploadSavedPlansEmptyRoot.visibility = View.GONE
        pDialog?.dismiss()
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
        val price = "$ ${datum.price}"
        holder.price.text = price
        val se = "(${datum.studentEnrolled})"
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