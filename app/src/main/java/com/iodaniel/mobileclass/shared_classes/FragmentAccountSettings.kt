package com.iodaniel.mobileclass.shared_classes

import android.R
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iodaniel.mobileclass.databinding.FragmentAccountSettingsBinding
import com.iodaniel.mobileclass.teacher_package.classes.BioClass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class FragmentAccountSettings : Fragment(), OnClickListener,
    TeacherStudentListener, SetUserData {

    private lateinit var binding: FragmentAccountSettingsBinding
    private lateinit var teacherStudentListener: TeacherStudentListener
    private lateinit var setUserData: SetUserData
    private lateinit var pref: SharedPreferences
    private lateinit var bioData: BioClass
    private val bioDataKey = "BioData"
    private lateinit var bioValueEventListener: ValueEventListener
    private val personalDataRef = FirebaseDatabase.getInstance().reference
        .child("user_account_data")
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
    private val bioSharedPreferencesKey = "sharedPreferenceBio" + FirebaseAuth.getInstance().currentUser!!.uid

    override fun onStart() {
        super.onStart()
        init()
        requireActivity().onBackPressedDispatcher.addCallback(object :
            OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                println("OnBackPressed *********************** OnBackPressed")
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        teacherStudentListener = this
        binding.accountSettingCancel.setOnClickListener(this)
        binding.accountSettingSave.setOnClickListener(this)
        setUserData = this
        val gender = mutableListOf("Male", "Female")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_dropdown_item, gender)
        binding.accountSettingGender.adapter = adapter

        pref = requireActivity().getSharedPreferences(bioSharedPreferencesKey, Context.MODE_PRIVATE)
        val data = pref.getString(bioDataKey, "")
        println("DATA ****************** $data")

        if (data == "") return else {
            try {
                bioData = Json.decodeFromString(data!!)
                setUserData(bioData)
                println("ONE ***************************************** ")
            } catch (e: Exception) {
            }
        }
    }

    override fun teacherAccount() {

    }

    override fun studentAccount() {
    }

    override fun setUserData(bioClass: BioClass) {
        binding.accountSettingFullname.setText(bioClass.fullName)
        binding.accountSettingUsername.setText(bioClass.username)
        binding.accountSettingEmailAddress.setText(bioClass.email)
        binding.accountSettingPhoneBio.setText(bioClass.phone)
        binding.accountSettingGender.setSelection(if (bioClass.gender == "Male") 0 else 1)

        personalDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bioValueEventListener = this
                val bioData = snapshot.getValue(BioClass::class.java)
                pref.edit().apply {
                    val json = Json.encodeToString(bioData)
                    this.putString(bioDataKey, json)
                }.apply()
                println("ONE X ***************************************** ")
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "Error occurred!!!", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            com.iodaniel.mobileclass.R.id.account_setting_cancel -> {
                requireActivity().supportFragmentManager.popBackStack()
            }
            com.iodaniel.mobileclass.R.id.account_setting_save -> {
                val fullName = binding.accountSettingFullname.text.toString().trim()
                val username = binding.accountSettingUsername.text.toString().trim()
                val email = binding.accountSettingEmailAddress.text.toString().trim()
                val phone = binding.accountSettingPhoneBio.text.toString().trim()
                val date = Calendar.getInstance().time.time.toString()
                val gender = binding.accountSettingGender.selectedItem.toString()
                if (fullName == "" || username == "" || email == "" || phone == "") return
                val bioClass =
                    BioClass(
                        fullName,
                        username,
                        email,
                        phone = phone,
                        datetime = date,
                        gender = gender
                    )
                pref.edit().apply {
                    val json = Json.encodeToString(bioClass)
                    this.putString(bioDataKey, json)
                }.apply()
                Snackbar.make(binding.root, "Saved", Snackbar.LENGTH_LONG).show()

                personalDataRef.setValue(bioClass).addOnCompleteListener {
                    Snackbar.make(binding.root, "Saved Online", Snackbar.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Snackbar.make(binding.root, "Error saving online!!!", Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}

interface SetUserData {
    fun setUserData(bioClass: BioClass)
}