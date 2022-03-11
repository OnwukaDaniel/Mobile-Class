package com.iodaniel.mobileclass.student_package

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityViewMaterialBinding
import com.iodaniel.mobileclass.shared_classes.FragmentMaterials
import com.iodaniel.mobileclass.shared_classes.FragmentViewLesson
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.Material
import com.iodaniel.mobileclass.teacher_package.singleclass.CreateNewAssignment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class ViewMaterial : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy { ActivityViewMaterialBinding.inflate(layoutInflater) }
    private lateinit var material: Material
    private lateinit var classCode: String
    private lateinit var classInfo: ClassInfo
    private var viewPagesDataSet: ArrayList<Fragment> = arrayListOf()
    private lateinit var pref: SharedPreferences
    private val teacher = "teacher"
    private val student = "student"
    private var userType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.uploadQuestionToolbar)
        title = ""
        pref = getSharedPreferences("userType", Context.MODE_PRIVATE)
        userType = pref.getString("studentTeacher", "")!!
        println("*************************************** $userType")

        binding.viewMaterialBackArrow.setOnClickListener(this)
        if (intent.hasExtra("material")) {
            val json = intent.getStringExtra("material")
            val jsonClassInfo = intent.getStringExtra("classInfo")
            classCode = intent.getStringExtra("classCode")!!
            material = Json.decodeFromString(json!!)
            classInfo = Json.decodeFromString(jsonClassInfo!!)
            viewPager()
        }
    }

    private fun inflateCreateNewAssignment() {
        val createNewAssignmentFragment = CreateNewAssignment()
        val bundle = Bundle()
        val materialJson = Gson().toJson(material)
        val json = Json.encodeToString(classInfo)
        bundle.putString("classInfo", json)
        bundle.putString("materialJson", materialJson)
        createNewAssignmentFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.view_material_nested_root, createNewAssignmentFragment)
            .addToBackStack("newAssi").commit()
    }

    private fun viewPager() {
        val data = arrayListOf("Lesson", "Exercise")
        val fragmentViewLesson = FragmentViewLesson()
        val fragmentMaterial = FragmentMaterials()
        viewPagesDataSet = arrayListOf(fragmentViewLesson, fragmentMaterial)

        val bundle = Bundle()
        val materialJson = Json.encodeToString(material)
        bundle.putString("lesson", materialJson)
        val classInfoJson = Gson().toJson(classInfo)
        bundle.putString("classInfo", classInfoJson)
        fragmentViewLesson.arguments = bundle
        fragmentMaterial.arguments = bundle

        val viewPagerAdapter = ViewMaterialViewPagerAdapter(this)
        viewPagerAdapter.viewPagesDataSet = viewPagesDataSet
        binding.viewMaterialViewPager.adapter = viewPagerAdapter
        TabLayoutMediator(
            binding.viewMaterialTabLayout, binding.viewMaterialViewPager
        ) { tab, position ->
            tab.text = data[position]
        }
            .attach()
    }

    inner class ViewMaterialViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        var viewPagesDataSet = arrayListOf<Fragment>()
        override fun getItemCount() = viewPagesDataSet.size
        override fun createFragment(position: Int) = viewPagesDataSet[position]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (userType) {
            teacher -> menuInflater.inflate(R.menu.menu_view_material, menu)

            student -> menuInflater.inflate(R.menu.menu_view_material_student, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create_assignment -> inflateCreateNewAssignment()
            R.id.menu_create_assignment_student_report -> {
                val text = "Teacher-Student interaction coming soon"
                Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.view_material_back_arrow -> onBackPressed()
        }
    }
}