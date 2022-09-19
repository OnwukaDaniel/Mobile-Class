package com.iodaniel.mobileclass.teacher_package.course

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.ActivityEditCourseBinding
import com.iodaniel.mobileclass.util.dialog_fragment.MessageFragment
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityEditCourse : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy { ActivityEditCourseBinding.inflate(layoutInflater) }
    private lateinit var preference: SharedPreferences
    private var courseCardData: CourseCardData? = null
    private var courseCardDataJson = ""
    private var bundle = Bundle()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val messageViewModel: MessageFragmentViewModel by viewModels()
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
        binding.closeGuideline.setOnClickListener(this)
        binding.editCourseModules.setOnClickListener(this)
        binding.editCourseExercise.setOnClickListener(this)
        binding.editCourseFeedback.setOnClickListener(this)
        binding.editCourseSubmit.setOnClickListener(this)
        if (preference.getBoolean(getString(R.string.SHOW_CREATE_COURSE_GUIDELINES), true)) showGuidelines()
    }

    private fun showGuidelines() = scope.launch(Dispatchers.Main) {
        val mf = MessageFragment()
        supportFragmentManager.beginTransaction()
            .addToBackStack("dialog")
            .replace(R.id.edit_course_root, mf)
            .commit()
        messageViewModel.setDisplayText(getString(R.string.Guidelines))
        messageViewModel.showAgain.observe(this@ActivityEditCourse) {
            val checked = !it
            preference.edit().putBoolean(getString(R.string.SHOW_CREATE_COURSE_GUIDELINES), checked).apply()
        }
        val r = Runnable { fun run() {} }
        messageViewModel.setCancelFunction(r.run() to true)
        messageViewModel.setOkFunction((Fragment() to 0) to false)
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
            R.id.close_guideline -> binding.guidelineRoot.visibility = View.GONE
            R.id.edit_course_exercise -> {
                val fragmentExercise = FragmentModulesAndExercise()
                fragmentExercise.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .addToBackStack("modules")
                    .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                    .replace(R.id.edit_course_root, fragmentExercise)
                    .commit()
            }
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