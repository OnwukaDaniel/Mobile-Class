package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.CreateNewAssignmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.Material
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.CreateDirectQuestion
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.CreateMultiChoice
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.UploadDocs
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CreateNewAssignment : Fragment(), View.OnClickListener {

    private lateinit var binding: CreateNewAssignmentBinding
    private lateinit var classInfo: ClassInfo
    private lateinit var material: Material

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = CreateNewAssignmentBinding.inflate(inflater, container, false)

        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        val materialJson = bundle.getString("materialJson")
        material = Gson().fromJson(materialJson, Material::class.java)
        classInfo = Json.decodeFromString(json!!)

        binding.newAssignmentUpload.setOnClickListener(this)
        binding.newAssignmentDirectQ.setOnClickListener(this)
        binding.newAssignmentMultiChoice.setOnClickListener(this)
        return binding.root
    }

    private fun newFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("")
            .replace(R.id.create_new_assignment_root_layout, fragment)
            .commit()
    }

    override fun onClick(v: View?) {
        val bundle = Bundle()
        val json = Json.encodeToString(classInfo)
        val materialJson = Gson().toJson(material)
        bundle.putString("materialJson", materialJson)
        bundle.putString("classInfo", json)
        when (v?.id) {
            R.id.new_assignment_direct_q -> {
                val fragment = CreateDirectQuestion()
                fragment.arguments = bundle
                newFragment(fragment)
            }
            R.id.new_assignment_multi_choice -> {
                val fragment = CreateMultiChoice()
                fragment.arguments = bundle
                newFragment(fragment)
            }
            R.id.new_assignment_upload -> {
                val fragment = UploadDocs()
                fragment.arguments = bundle
                newFragment(fragment)
            }
        }
    }
}