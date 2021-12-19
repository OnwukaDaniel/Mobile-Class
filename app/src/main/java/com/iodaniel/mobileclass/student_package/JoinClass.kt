package com.iodaniel.mobileclass.student_package

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentJoinClassBinding
import com.iodaniel.mobileclass.student_package.HelperListener.LoadingListener
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.MyCourse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JoinClass : Fragment(), View.OnClickListener, LoadingListener {

    private lateinit var binding: FragmentJoinClassBinding
    private lateinit var loadingListener: LoadingListener
    private lateinit var snap: Any
    private lateinit var snapX: Any

    private var allCodesData: ArrayList<HashMap<*, *>> = arrayListOf()
    private var listOfCourses: ArrayList<ClassInfo> = arrayListOf()
    private var classCodeRef = FirebaseDatabase.getInstance().reference.child("class_codes")
    private var classRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentJoinClassBinding.inflate(inflater, container, false)
        loadingListener = this
        binding.joinClass.setOnClickListener(this)
        readDatabase()
        return binding.root
    }

    private fun readDatabase() {
        classCodeRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snap = snapshot.value!! as HashMap<*, *>
                allCodesData.add(snap as HashMap<*, *>)
                println("JoinClass onChildAdded *************************** $snap")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snap = snapshot.value!! as HashMap<*, *>
                allCodesData.add(snap as HashMap<*, *>)
                println("JoinClass onChildChanged *************************** $snap")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                snap = snapshot.value!!
                println("*************************** $snap")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                snap = snapshot.value!!
                println("*************************** $snap")
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun joinClass() {
        loadingListener.loadingProgressBar()
        val inputClassCode = binding.classCodeEt.text.toString().trim()
        if (inputClassCode == "") {
            loadingListener.notLoadingProgressBar()
            return
        }
        if (allCodesData.isEmpty()) {
            loadingListener.notLoadingProgressBar()
            Snackbar.make(binding.root, "Class doesn't exist", Snackbar.LENGTH_LONG).show()
            //return
        }
        for (i in allCodesData) {
            if (inputClassCode == i["classCode"].toString()) {
                loadingListener.notLoadingProgressBar()
                val intent = Intent(requireContext(), MyClasses::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val json = Gson().toJson(i)
                intent.putExtra("class_detail", json)
                startActivity(intent)
                requireActivity().overridePendingTransition(0,0)
            }
        }
    }

    override fun loadingProgressBar() {
        binding.joinClassProgressBar.visibility = View.VISIBLE
        binding.joinClassText.visibility = View.INVISIBLE
    }

    override fun notLoadingProgressBar() {
        binding.joinClassProgressBar.visibility = View.INVISIBLE
        binding.joinClassText.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.join_class -> {
                joinClass()
            }
        }
    }
}