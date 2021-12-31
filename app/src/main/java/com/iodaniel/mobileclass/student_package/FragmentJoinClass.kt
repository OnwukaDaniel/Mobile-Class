package com.iodaniel.mobileclass.student_package

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentJoinClassBinding
import com.iodaniel.mobileclass.student_package.HelperListener.LoadingListener
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.StudentRegistrationClass
import java.text.DateFormat
import java.util.*

class FragmentJoinClass : Fragment(), View.OnClickListener, LoadingListener {

    private lateinit var binding: FragmentJoinClassBinding
    private lateinit var loadingListener: LoadingListener
    private lateinit var snap: Any
    private lateinit var snapX: Any

    private val auth = FirebaseAuth.getInstance().currentUser!!.uid

    private var allCodesData: ArrayList<HashMap<*, *>> = arrayListOf()
    private var myListOfClassCodes: ArrayList<String> = arrayListOf()

    private var registerationRef = FirebaseDatabase.getInstance().reference
    private var teacherRef = FirebaseDatabase.getInstance().reference

    private var newClassCodeRef = FirebaseDatabase.getInstance().reference.child("class_codes")
    private var myClassCodeRef = FirebaseDatabase.getInstance().reference
        .child("student")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child("classes")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentJoinClassBinding.inflate(inflater, container, false)
        loadingListener = this
        requireActivity().setTheme(R.style.Theme_WhiteTheme)
        binding.joinClass.setOnClickListener(this)
        readDatabase()
        readNewClass()
        return binding.root
    }

    private fun readDatabase() {
        myClassCodeRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(ClassInfo::class.java)
                myListOfClassCodes.add(snap!!.classCode)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(ClassInfo::class.java)
                myListOfClassCodes.add(snap!!.classCode)
                readNewClass()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun readNewClass(){
        newClassCodeRef.addChildEventListener(object : ChildEventListener {
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
        val datetime = Calendar.getInstance().time.time
        val dateString = DateFormat.getInstance().format(datetime)
        val registrationData = StudentRegistrationClass()
        registrationData.email = FirebaseAuth.getInstance().currentUser?.email!!
        registrationData.datetimeJoined = dateString

        if (inputClassCode == "") {
            loadingListener.notLoadingProgressBar()
            Snackbar.make(binding.root, "Empty input", Snackbar.LENGTH_LONG).show()
            return
        }
        if (allCodesData.isEmpty()) {
            loadingListener.notLoadingProgressBar()
            Snackbar.make(binding.root, "Class doesn't exist or Poor network connection", Snackbar.LENGTH_LONG).show()
            return
        }
        for (classInfoData in allCodesData) {
            if (inputClassCode in myListOfClassCodes){
                loadingListener.notLoadingProgressBar()
                Snackbar.make(binding.root, "Class already exist", Snackbar.LENGTH_LONG).show()
                val intent = Intent(requireContext(), ActivityMyClasses::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
                return
            }
            if (inputClassCode == classInfoData["classCode"].toString()) {
                try {
                    teacherRef = teacherRef
                        .child("teacher")
                        .child(classInfoData["auth"] as String)
                        .child("classes")
                        .child(classInfoData["classCode"] as String)

                    registerationRef = registerationRef
                        .child("teacher")
                        .child(classInfoData["auth"] as String)
                        .child("registered_students")
                        .child(classInfoData["classCode"] as String)
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        //.child(auth)

                    teacherRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val course = (snapshot.value)
                            myClassCodeRef.push().setValue(course).addOnCompleteListener {
                                registerationRef.setValue(registrationData).addOnCompleteListener {
                                    loadingListener.notLoadingProgressBar()
                                    Snackbar.make(binding.root, "Joined !", Snackbar.LENGTH_LONG).show()
                                    val intent = Intent(requireContext(), ActivityMyClasses::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    requireActivity().overridePendingTransition(0, 0)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            val txt = "Database Error!!! Please try again"
                            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
                        }
                    })

                } catch (e: Exception) {
                    println(" **************************** ${e.printStackTrace()}")
                }
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