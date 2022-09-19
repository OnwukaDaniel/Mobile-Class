package com.iodaniel.mobileclass.student_package

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.SignInOrSignUp
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.StudentCoursesAndSchemes
import com.iodaniel.mobileclass.data_class.StudentDetails
import com.iodaniel.mobileclass.databinding.FragmentStudentProfileBinding
import com.iodaniel.mobileclass.liveDataClasses.CourseCardLiveData
import com.iodaniel.mobileclass.liveDataClasses.CoursesLiveData
import com.iodaniel.mobileclass.liveDataClasses.StudentCoursesAndSchemesLiveData
import com.iodaniel.mobileclass.liveDataClasses.StudentProfileLiveData
import com.iodaniel.mobileclass.teacher_package.course.ActivityEditCourse
import com.iodaniel.mobileclass.teacher_package.profile.CoursesAdapter
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.util.uistate_manager.UIStateDialogs
import com.iodaniel.mobileclass.viewModel.SingleToggleViewModel
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import com.iodaniel.mobileclass.viewModel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class FragmentStudentProfile : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentStudentProfileBinding
    private var studentDetails: StudentDetails? = null
    private var fetchedData = false
    private var dialogs = Dialogs()
    private var uIStateViewModel = UIStateViewModel()
    private var dataset: ArrayList<CourseCardData> = arrayListOf()
    private var datasetKey: MutableSet<String> = mutableSetOf()

    private var datasetRecent: ArrayList<StudentCoursesAndSchemes> = arrayListOf()
    private var datasetKeyRecent: MutableSet<String> = mutableSetOf()

    private var datasetRecentCourses: ArrayList<CourseCardData> = arrayListOf()
    private var datasetRecentKeysCourses: MutableSet<String> = mutableSetOf()

    private var coursesAdapter = CoursesAdapter()
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var uIStateDialogs: UIStateDialogs
    private val recentViewsCoursesAdapter = RecentViewsCoursesAdapter()
    private var studentCoursesAndSchemes = StudentCoursesAndSchemes()
    private val displayEmptyRecentPlaceholderViewModel = SingleToggleViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        uIStateDialogs = UIStateDialogs(uIStateViewModel, requireContext(), viewLifecycleOwner, requireActivity())
        uIStateDialogs.uiState(fetchedData, dialogs)
        binding.studentProfileMenu.setOnClickListener(this)
        recentViewsCoursesAdapter.activity = requireActivity()
        binding.studentProfileCoursesRv.adapter = recentViewsCoursesAdapter
        getData()
        viewModels()
        return binding.root
    }

    private fun viewModels() {
        displayEmptyRecentPlaceholderViewModel.toggle.observe(viewLifecycleOwner) {
            if (it) binding.studentProfileLearningRoot.visibility = View.GONE else binding.studentProfileLearningRoot.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        val studentRef = FirebaseDatabase.getInstance().reference.child(requireContext().getString(R.string.student_details)).child(auth)
        StudentProfileLiveData(studentRef).observe(viewLifecycleOwner) {
            if (it.second == ChildEventTemplate.onDataChange) {
                fetchedData = true
                studentDetails = it.first
                Glide.with(requireContext()).load(studentDetails!!.image).centerCrop().into(binding.studentProfileDisplayImage)
                binding.studentProfileUsername.text = studentDetails!!.username
                binding.studentProfileName.text = studentDetails!!.fullName
                binding.studentProfileOrganisation.text = studentDetails!!.organisation
            }
        }

        val ref = FirebaseDatabase.getInstance().reference.child(requireContext().getString(R.string.course_path))
        val courseCardLiveData = CourseCardLiveData(ref)
        courseCardLiveData.observe(viewLifecycleOwner) {
            when (it.second.second) {
                ChildEventTemplate.onChildAdded -> {
                    var studentsEnrolled = 0

                    val key = it.second.first
                    if (key !in datasetKey) {
                        // Students enrolled
                        val courseCardData = it.first
                        val enrolledX = if (courseCardData.studentsEnrolled == "") 0 else courseCardData.studentsEnrolled.toInt()
                        studentsEnrolled += enrolledX
                        val satisfied = courseCardData.instructorInChargeUID == auth
                        if (satisfied) {
                            dataset.add(courseCardData)
                            datasetKey.add(key)
                            coursesAdapter.notifyItemInserted(dataset.size)
                            uIStateViewModel.setUIState(UiState.stateData)
                            uIStateDialogs.uiState(fetchedData, dialogs)
                        }
                    }
                }
                ChildEventTemplate.onChildRemoved -> {
                    val key = it.second.first
                    if (key in datasetKey) {
                        val index = datasetKey.indexOf(key)
                        datasetKey.minusElement(key)
                        dataset.removeAt(index)
                        coursesAdapter.notifyItemRemoved(index)
                    }
                }
                ChildEventTemplate.onChildChanged -> {
                    val index = datasetKey.indexOf(it.second.first)
                    dataset[index] = it.first
                    coursesAdapter.notifyItemChanged(index)
                }
                ChildEventTemplate.onChildMoved -> {}
            }
        }
        studentRef.get().addOnSuccessListener { dataSnapShot ->
            if (dataSnapShot != null) {
                uIStateViewModel.setUIState(UiState.stateData)
                uIStateDialogs.uiState(fetchedData, dialogs)
            } else uIStateViewModel.setUIState(UiState.stateNoData)
        }

        val studentCoursesRef = FirebaseDatabase.getInstance().reference
            .child("studentData")
            .child(auth)
            .child("ownedCourses")
        StudentCoursesAndSchemesLiveData(studentCoursesRef).observe(viewLifecycleOwner) { CandS ->
            if (CandS.second == ChildEventTemplate.onDataChange) { // Get all recent viewed data
                studentCoursesAndSchemes = CandS.first
                if (studentCoursesAndSchemes.coursesOwned.isNotEmpty()) {
                    binding.studentProfileCoursesRv.visibility = View.VISIBLE
                    binding.studentProfileLearningRoot.visibility = View.GONE
                }
                for (i in studentCoursesAndSchemes.coursesOwned) {
                    if (i.courseCode !in datasetKeyRecent) {
                        datasetRecent.add(studentCoursesAndSchemes)
                        datasetKeyRecent.add(i.courseCode)
                        val courseCardRef = FirebaseDatabase.getInstance().reference
                            .child(requireContext().getString(R.string.course_path))
                            .child(i.courseCode).child(i.instructorAuth)
                        CoursesLiveData(courseCardRef).observe(viewLifecycleOwner) { // Get all single courseCard
                            if (it.second == ChildEventTemplate.onDataChange) {
                                if (it.first.courseCode !in datasetRecentKeysCourses) {
                                    datasetRecentCourses.add(it.first)
                                    datasetRecentKeysCourses.add(it.first.courseCode)
                                    recentViewsCoursesAdapter.notifyDataSetChanged()
                                    displayEmptyRecentPlaceholderViewModel.setToggle(true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), SignInOrSignUp::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.student_profile_menu -> {
                val menu = PopupMenu(requireContext(), binding.studentProfileMenu)
                menu.inflate(R.menu.student_profile_menu)
                menu.show()
                menu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.student_signout -> {
                            signOut()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.edit_profile -> {
                            childFragmentManager.beginTransaction().addToBackStack("edit")
                                .replace(R.id.student_profile_root, Fragment())
                                .commit()
                            return@setOnMenuItemClickListener true
                        }
                        else -> return@setOnMenuItemClickListener false
                    }
                }
            }
        }
    }
}

class RecentViewsCoursesAdapter : RecyclerView.Adapter<RecentViewsCoursesAdapter.ViewHolder>() {
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    private lateinit var context: Context
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
        val view = LayoutInflater.from(context).inflate(R.layout.row_student_profile_display, parent, false)
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