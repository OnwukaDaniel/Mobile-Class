package com.iodaniel.mobileclass.home

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
import com.iodaniel.mobileclass.course.ActivitySelectedCourse
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.OwnedCourse
import com.iodaniel.mobileclass.databinding.FragmentMyLearningBinding
import com.iodaniel.mobileclass.liveDataClasses.CoursesLiveData
import com.iodaniel.mobileclass.liveDataClasses.OwnedCoursesLiveData
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.viewModel.SingleToggleViewModel

class FragmentMyLearning : Fragment() {
    private lateinit var binding: FragmentMyLearningBinding
    private val myCoursesAdapter = MyCoursesAdapter()
    private val toggleModel by activityViewModels<SingleToggleViewModel>()
    private val ownedCourseList: ArrayList<OwnedCourse> = arrayListOf()
    private val dataset: ArrayList<CourseCardData> = arrayListOf()
    private val auth = FirebaseAuth.getInstance().currentUser
    private var studentCoursesRef = FirebaseDatabase.getInstance().reference
    private var coursesRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyLearningBinding.inflate(inflater, container, false)
        getUserOwned()
        myCoursesAdapter.activity = requireActivity()
        myCoursesAdapter.dataset = dataset
        binding.myLearningRv.adapter = myCoursesAdapter
        binding.myLearningRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        toggleModel.toggle.observe(viewLifecycleOwner) {
            if (it) binding.myLearningEmpty.visibility = View.GONE else binding.myLearningEmpty.visibility = View.VISIBLE
        }
        return binding.root
    }

    private fun getUserOwned() {
        val classCodes: ArrayList<String> = arrayListOf()
        val keys: ArrayList<String> = arrayListOf()
        if (auth == null) return
        studentCoursesRef = studentCoursesRef.child("studentData").child(auth.uid).child("ownedCourses")
        OwnedCoursesLiveData(studentCoursesRef).observe(viewLifecycleOwner) { pair ->
            when (pair.second) {
                ChildEventTemplate.onDataChange -> {
                    val hash = pair.first.value as HashMap<*, *>
                    for (x in hash) {
                        val course = Gson().fromJson(x.value.toString(), OwnedCourse::class.java)
                        classCodes.add(course.courseCode)
                    }
                    for(code in classCodes){
                        val coursesRef = coursesRef.child("course_path").child(code)
                        val coursesLiveData = CoursesLiveData(coursesRef)
                        coursesLiveData.observe(viewLifecycleOwner) {
                            when (it.second) {
                                ChildEventTemplate.onDataChange -> {
                                    if (it.first.courseCode !in keys){
                                        dataset.add(it.first)
                                        keys.add(it.first.courseCode)
                                        myCoursesAdapter.notifyItemInserted(dataset.size)
                                        toggleModel.setToggle(true)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class MyCoursesAdapter : RecyclerView.Adapter<MyCoursesAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<CourseCardData> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseImage: ImageView = itemView.findViewById(R.id.row_my_courses_image)
        val courseName: TextView = itemView.findViewById(R.id.row_my_courses_title)
        val courseDescription: TextView = itemView.findViewById(R.id.row_my_courses_short_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_my_courses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.courseImage)
        holder.courseName.text = datum.courseName
        holder.courseDescription.text = datum.shortDescription
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivitySelectedCourse::class.java)
            intent.putExtra("courseCard", Gson().toJson(datum))
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount() = dataset.size
}