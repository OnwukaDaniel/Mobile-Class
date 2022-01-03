package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.InternetConnection
import com.iodaniel.mobileclass.databinding.CreateNewAssignmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.CreateDirectQuestion
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.CreateMultiChoice
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.UploadDocs
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CreateNewAssignment : Fragment(), View.OnClickListener {

    private lateinit var binding: CreateNewAssignmentBinding
    private lateinit var classInfo: ClassInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = CreateNewAssignmentBinding.inflate(inflater, container, false)

        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        classInfo = Json.decodeFromString(json!!)

        binding.newAssignmentUpload.setOnClickListener(this)
        binding.newAssignmentDirectQ.setOnClickListener(this)
        binding.newAssignmentMultiChoice.setOnClickListener(this)
        return binding.root
    }

    private fun newFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("")
            .replace(R.id.a_class_frame, fragment)
            .commit()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.new_assignment_direct_q -> {
                val fragment = CreateDirectQuestion()
                val bundle = Bundle()
                val json = Json.encodeToString(classInfo)
                bundle.putString("classInfo", json)
                fragment.arguments = bundle
                newFragment(fragment)
            }
            R.id.new_assignment_multi_choice -> {
                val fragment = CreateMultiChoice()
                val bundle = Bundle()
                val json = Json.encodeToString(classInfo)
                bundle.putString("classInfo", json)
                fragment.arguments = bundle
                newFragment(fragment)
            }
            R.id.new_assignment_upload -> {
                val fragment = UploadDocs()
                val bundle = Bundle()
                val json = Json.encodeToString(classInfo)
                bundle.putString("classInfo", json)
                fragment.arguments = bundle
                newFragment(fragment)
            }
        }
    }
}