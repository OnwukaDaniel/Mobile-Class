package com.iodaniel.mobileclass.teacher_package.singleclass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.databinding.AddExerciseFragmentBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.Material
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.CreateDirectQuestion
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.CreateMultiChoice
import com.iodaniel.mobileclass.teacher_package.singleclass.assignment_package.UploadDocs

class AddExerciseFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: AddExerciseFragmentBinding
    private lateinit var classInfo: ClassInfo
    private lateinit var material: Material
    private var exercisePosition = 0
    private var courseCardDataJson = ""
    private val dataAndPositionViewModel by activityViewModels<DataAndPositionViewModel>()
    private var courseCardData: CourseCardData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = AddExerciseFragmentBinding.inflate(inflater, container, false)
        courseCardDataJson = requireArguments().getString("courseCardDataJson")!!
        exercisePosition = requireArguments().getInt("position", 0)
        courseCardData = Gson().fromJson(courseCardDataJson, CourseCardData::class.java)

        binding.newAssignmentUpload.setOnClickListener(this)
        binding.newAssignmentDirectQ.setOnClickListener(this)
        binding.newAssignmentMultiChoice.setOnClickListener(this)
        return binding.root
    }

    private fun newFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("fragment")
            .replace(R.id.create_new_assignment_root_layout, fragment)
            .commit()
    }

    override fun onClick(v: View?) {
        dataAndPositionViewModel.setDataAndPosition(courseCardData!! to exercisePosition)
        when (v?.id) {
            R.id.new_assignment_direct_q -> {
                val fragment = CreateDirectQuestion()
                newFragment(fragment)
            }
            R.id.new_assignment_multi_choice -> {
                val fragment = CreateMultiChoice()
                newFragment(fragment)
            }
            R.id.new_assignment_upload -> {
                val fragment = UploadDocs()
                newFragment(fragment)
            }
        }
    }
}

class DataAndPositionViewModel: ViewModel(){
    var dataAndPosition = MutableLiveData<Pair<CourseCardData, Int>>()
    fun setDataAndPosition(input: Pair<CourseCardData, Int>){
        dataAndPosition.value = input
    }
}