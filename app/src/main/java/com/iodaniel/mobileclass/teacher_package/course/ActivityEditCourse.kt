package com.iodaniel.mobileclass.teacher_package.course

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.ActivityEditCourseBinding

class ActivityEditCourse : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy { ActivityEditCourseBinding.inflate(layoutInflater) }
    private lateinit var preference: SharedPreferences
    private var courseCardData: CourseCardData? = null
    private var courseCardDataJson = ""
    private var bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.editCourseToolbar)
        preference = getSharedPreferences(getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        if (intent.hasExtra(getString(R.string.manage_course_data_intent))) {
            courseCardDataJson = intent.getStringExtra(getString(R.string.manage_course_data_intent))!!
            bundle.putString(getString(R.string.manage_course_data_intent), courseCardDataJson)

            courseCardData = Gson().fromJson(courseCardDataJson, CourseCardData::class.java)
            title = courseCardData!!.courseName
            supportActionBar!!.subtitle = "Organise course and material"
        }

        binding.editCoursePlans.setOnClickListener(this)
        binding.editCourseModules.setOnClickListener(this)
        binding.editCourseExercise.setOnClickListener(this)
        binding.editCourseFeedback.setOnClickListener(this)
        binding.editCourseSubmit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.edit_course_plans -> {
                val fragmentEditPlans = FragmentEditPlans()
                fragmentEditPlans.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .addToBackStack("plans")
                    .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                    .replace(R.id.edit_course_root, fragmentEditPlans)
                    .commit()
            }
            R.id.edit_course_modules -> {
                val fragmentModulesAndPlans = FragmentModulesAndPlans()
                fragmentModulesAndPlans.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .addToBackStack("modules")
                    .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                    .replace(R.id.edit_course_root, fragmentModulesAndPlans)
                    .commit()
            }
            R.id.edit_course_exercise -> {}
            R.id.edit_course_feedback -> {}
            R.id.edit_course_submit -> {
                val fragmentSubmit = FragmentSubmit()
                fragmentSubmit.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .addToBackStack("modules")
                    .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                    .replace(R.id.edit_course_root, fragmentSubmit)
                    .commit()
            }
        }
    }
}