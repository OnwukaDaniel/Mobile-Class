package com.iodaniel.mobileclass.teacher_package.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.SignInOrSignUp
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.InstructorDetails
import com.iodaniel.mobileclass.databinding.ActivityTeacherProfileBinding
import com.iodaniel.mobileclass.databinding.FragmentInstructorProfileBinding
import com.iodaniel.mobileclass.liveDataClasses.CourseCardLiveData
import com.iodaniel.mobileclass.liveDataClasses.InstructorProfileLiveData
import com.iodaniel.mobileclass.teacher_package.course.ActivityEditCourse
import com.iodaniel.mobileclass.teacher_package.course.ActivityManageUpload
import com.iodaniel.mobileclass.teacher_package.course.CourseUpload
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.viewModel.InstructorEditProfileViewModel
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import com.iodaniel.mobileclass.viewModel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FragmentInstructorProfile : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentInstructorProfileBinding
    private var instructorDetails: InstructorDetails? = null
    private var fetchedData = false
    private var incompleteFragment = Fragment()
    private var uIStateViewModel = UIStateViewModel()
    private val instructorEditProfileViewModel: InstructorEditProfileViewModel by activityViewModels()
    private var dataset: ArrayList<CourseCardData> = arrayListOf()
    private var datasetKey: MutableSet<String> = mutableSetOf()
    private var coursesAdapter = CoursesAdapter()
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInstructorProfileBinding.inflate(inflater, container, false)
        binding.instructorProfileSavedCourses.setOnClickListener(this)
        binding.instructorProfileCreateCourse.setOnClickListener(this)
        binding.instructorProfileCreateScheme.setOnClickListener(this)
        binding.instructorProfileAuditedCourses.setOnClickListener(this)
        binding.instructorCreativeTools.setOnClickListener(this)
        binding.instructorProfileCompletedCourses.setOnClickListener(this)
        binding.instructorProfileMenu.setOnClickListener(this)
        getData()
        //delay(60)
        return binding.root
    }

    private fun startLayoutShimmer() {
        binding.instructorShimmer.startShimmer()
        binding.instructorDataRoot.visibility = View.GONE
    }

    private fun stopLayoutShimmer() {
        binding.instructorShimmer.visibility = View.GONE
        binding.instructorDataRoot.visibility = View.VISIBLE
        binding.instructorShimmer.stopShimmer()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var dX: Float = 0F
        var dY: Float = 0F

        /*binding.instructorProfileDisplayImage.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> view.animate()
                    .x(event.rawX + dX)
                    */
        /*.y(event.rawY + dY)*/
        /*
                        .setDuration(0)
                        .start()
                    else -> return@setOnTouchListener false
                }
                return@setOnTouchListener true
            }*/
    }

    private fun getData() {
        startLayoutShimmer()
        binding.instructorProfileTopCourseRv.adapter = coursesAdapter
        binding.instructorProfileTopCourseRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        coursesAdapter.activity = requireActivity()
        coursesAdapter.dataset = dataset
        val instructorDetailsRef = FirebaseDatabase.getInstance().reference.child(requireContext().getString(R.string.instructor_details)).child(auth)
        InstructorProfileLiveData(instructorDetailsRef).observe(viewLifecycleOwner) {
            if (it.second == ChildEventTemplate.onDataChange) {
                stopLayoutShimmer()
                fetchedData = true
                instructorDetails = it.first

                Glide.with(requireContext()).load(instructorDetails!!.instructorImage).centerCrop().into(binding.instructorProfileDisplayImage)
                binding.instructorProfileName.text = instructorDetails!!.instructorName
                instructorEditProfileViewModel.setInstructorDetails(it.first)
                checkIncompleteProfile()
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
                        stopLayoutShimmer()
                        // Students enrolled
                        val courseCardData = it.first
                        val enrolledX = if (courseCardData.studentsEnrolled == "") 0 else courseCardData.studentsEnrolled.toInt()
                        studentsEnrolled += enrolledX
                        binding.instructorProfileStudentNumber.text = studentsEnrolled.toString()
                        val satisfied = courseCardData.instructorInChargeUID == auth
                        if (satisfied) {
                            dataset.add(courseCardData)
                            binding.instructorProfileCourseNumber.text = dataset.size.toString()

                            datasetKey.add(key)
                            coursesAdapter.notifyItemInserted(dataset.size)
                            uIStateViewModel.setUIState(UiState.stateData)
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
        instructorDetailsRef.get().addOnSuccessListener { dataSnapShot ->
            if (dataSnapShot != null) uIStateViewModel.setUIState(UiState.stateData) else uIStateViewModel.setUIState(UiState.stateNoData)
        }
    }

    private fun checkIncompleteProfile() {
        val handler = Handler(Looper.myLooper()!!)
        val incompleteProfile = instructorDetails!!.instructorImage == "" || instructorDetails!!.instructorPersonalDescription == ""
        val fragment = ProfileDialogFragment()
        if (incompleteProfile && !fragment.isVisible) {
            handler.post {
                kotlin.run {
                    if (fragment.isAdded) return@run
                    try {
                        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("dialog")
                            .replace(R.id.instructor_profile_root, fragment)
                            .commit()
                        incompleteFragment = fragment
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        if (!incompleteProfile) {
            try {
                requireActivity().supportFragmentManager.beginTransaction().remove(incompleteFragment).commit()
            } catch (e: Exception) {
            }
        }
    }

    private fun delay(input: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val range = (0..input).toList().reversed()
            val flow = range.asSequence().asFlow().onEach { delay(1_000) }
            flow.collect {
                if (it + 1 == input) {
                    if (activity != null && isAdded && binding.instructorShimmer.isShimmerVisible) requireActivity().runOnUiThread {
                        Snackbar.make(binding.root, "Network time out", Snackbar.LENGTH_LONG).show()
                        stopLayoutShimmer()
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
            R.id.instructor_profile_menu -> {
                val menu = PopupMenu(requireContext(), binding.instructorProfileMenu)
                menu.inflate(R.menu.menu_instructor_profile)
                menu.show()
                menu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.instructor_sign_out -> {
                            signOut()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.instructor_edit -> {
                            if (instructorDetails == null) {
                                Snackbar.make(binding.root, "Unable to retrieve Instructor's data. Poor Network connection. Please retry", Snackbar.LENGTH_LONG)
                                    .show()
                                return@setOnMenuItemClickListener true
                            }
                            if (instructorDetails != null) {
                                val intent = Intent(context, ActivityEditProfile::class.java)
                                val json = Gson().toJson(instructorDetails)
                                intent.putExtra("instructorDetails", json)
                                requireActivity().startActivity(intent)
                                requireActivity().overridePendingTransition(0, 0)
                            }
                            return@setOnMenuItemClickListener true
                        }
                        else -> {
                            return@setOnMenuItemClickListener false
                        }
                    }
                }
            }
            R.id.instructor_creative_tools -> {
                val visible = binding.instructorToolsRoot.visibility != View.GONE
                if (visible) {
                    binding.instructorCreativeToolsDrop.animate()
                        .setDuration(500)
                        .rotation(180.0F)
                        .start()
                    binding.instructorToolsRoot.visibility = View.GONE
                } else {
                    binding.instructorCreativeToolsDrop.animate()
                        .setDuration(500)
                        .rotation(180.0F)
                        .start()
                    binding.instructorToolsRoot.visibility = View.VISIBLE
                }
            }
            R.id.instructor_profile_create_course -> {
                requireActivity().startActivity(Intent(context, CourseUpload::class.java))
                requireActivity().overridePendingTransition(0, 0)
            }
            R.id.instructor_profile_create_scheme -> {

            }
            R.id.instructor_profile_completed_courses -> {
                val intent = Intent(requireContext(), ActivityManageUpload::class.java)
                intent.putExtra(getString(R.string.manage_course_data_intent), ManageProfileCourseType.COMPLETE)
                startActivity(intent)
            }
            R.id.instructor_profile_saved_courses -> {
                val intent = Intent(requireContext(), ActivityManageUpload::class.java)
                intent.putExtra(getString(R.string.manage_course_data_intent), ManageProfileCourseType.SAVED)
                startActivity(intent)
            }
            R.id.instructor_profile_audited_courses -> {
                val intent = Intent(requireContext(), ActivityManageUpload::class.java)
                intent.putExtra(getString(R.string.manage_course_data_intent), ManageProfileCourseType.AUDITED)
                startActivity(intent)
            }
            R.id.instructor_profile_top_courses_see_more -> {

            }
        }
    }
}

object ManageProfileCourseType {
    const val COMPLETE = 0
    const val SAVED = 1
    const val AUDITED = 2
}

class ProfileDialogFragment : Fragment() {
    var type = 0
    private lateinit var binding: ActivityTeacherProfileBinding
    private var instructorDetails: InstructorDetails? = null
    private val viewModel: InstructorEditProfileViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActivityTeacherProfileBinding.inflate(inflater, container, false)
        viewModel.instructorDetails.observe(viewLifecycleOwner) {
            instructorDetails = it
        }
        binding.teacherProfileContinue.setOnClickListener {
            if (instructorDetails != null) {
                val intent = Intent(context, ActivityEditProfile::class.java)
                val json = Gson().toJson(instructorDetails)
                intent.putExtra("instructorDetails", json)
                requireActivity().startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            } else {
                Snackbar.make(binding.root, "Unable to retrieve Instructor's data. Please retry.", Snackbar.LENGTH_LONG).show()
            }
        }
        return binding.root
    }
}

class CoursesAdapter : RecyclerView.Adapter<CoursesAdapter.ViewHolder>() {
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.row_courses_image)
        val title: TextView = itemView.findViewById(R.id.row_courses_title)
        val level: TextView = itemView.findViewById(R.id.row_courses_level)
        val studentsEnrolled: TextView = itemView.findViewById(R.id.row_courses_students_enrolled)
        val price: TextView = itemView.findViewById(R.id.row_courses_price)
    }

    class SeeMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val seeMore: TextView = itemView.findViewById(R.id.instructor_profile_top_courses_see_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_courses, parent, false)
        val seeMoreView = LayoutInflater.from(context).inflate(R.layout.see_more, parent, false)
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