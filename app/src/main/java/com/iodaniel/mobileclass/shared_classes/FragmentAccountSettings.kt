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
import com.iodaniel.mobileclass.databinding.FragmentAccountSettingsBinding
import com.iodaniel.mobileclass.teacher_package.classes.BioClass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class FragmentAccountSettings(val enabled: Boolean) : Fragment(), OnClickListener, TeacherStudentListener, SetUserData {

    private lateinit var binding: FragmentAccountSettingsBinding
    private lateinit var teacherStudentListener: TeacherStudentListener
    private lateinit var setUserData: SetUserData
    private lateinit var pref: SharedPreferences
    private lateinit var bioClass: BioClass

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        init()

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                println("OnBackPressed *********************** OnBackPressed")
            }
        })

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

        pref = requireActivity().getSharedPreferences("sharedPreferenceBio", Context.MODE_PRIVATE)
        val data = pref.getString("BioData", "")
        when (data) {
            "" -> {}
            else -> {
                bioClass = Json.decodeFromString(data!!)
            }
        }
    }

    override fun teacherAccount() {

    }

    override fun studentAccount() {
    }

    private fun spinnerFunctions() {
    }

    override fun setUserData(bioClass: BioClass) {
        binding.accountSettingFullname.setText(bioClass.fullName)
        binding.accountSettingUsername.setText(bioClass.username)
        binding.accountSettingEmailAddress.setText(bioClass.email)
        binding.accountSettingPhoneBio.setText(bioClass.phone)
        binding.accountSettingGender.setSelection(if (bioClass.gender == "Male") 0 else 1)
        //binding.accountSettingAvatar
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            com.iodaniel.mobileclass.R.id.account_setting_cancel -> {
                requireActivity().supportFragmentManager.popBackStack()
            }
            com.iodaniel.mobileclass.R.id.account_setting_save -> {
                val Fullname = binding.accountSettingFullname.text.toString().trim()
                val Username = binding.accountSettingUsername.text.toString().trim()
                val email = binding.accountSettingEmailAddress.text.toString().trim()
                val Phone = binding.accountSettingPhoneBio.text.toString().trim()
                val date = Calendar.getInstance().time.time.toString()
                val Gender = binding.accountSettingGender
                if (Fullname == "" || Username == "" || email == "" || Phone == "") return
                println("TIME *********************** $date")
                pref.edit().apply {
                    val bioClass =
                        BioClass(Fullname, Username, email, phone = Phone, datetime = date)
                    val json = Json.encodeToString(bioClass)
                    this.putString("BioData", json)
                }.apply()
                Snackbar.make(binding.root, "Saved", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}

interface SetUserData {
    fun setUserData(bioClass: BioClass)
}