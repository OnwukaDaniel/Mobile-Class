package com.iodaniel.mobileclass.teacher_package.verification

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.ActivityVerificationBinding
import com.iodaniel.mobileclass.liveDataClasses.VerHomeLiveData
import com.iodaniel.mobileclass.util.ChildEventTemplate

class ActivityVerification : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy { ActivityVerificationBinding.inflate(layoutInflater) }
    private val fragmentVerifyId = FragmentVerifyId()
    private lateinit var verHomeLiveData: VerHomeLiveData
    private val auth = FirebaseAuth.getInstance().currentUser
    private var verRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.activityVerificationToolbar)
        supportActionBar!!.title = "Verification"
        binding.activityVerProceed.setOnClickListener(this)
        verRef = verRef.child("verification").child(auth!!.uid)
        verHomeLiveData = VerHomeLiveData(verRef)
        verHomeLiveData.observe(this) {
            when (it.second) {
                ChildEventTemplate.onDataChange -> {
                    val verData = it.first
                    binding.activityCheckId.isChecked = verData["id"]!!
                    binding.activityCheckCertificate.isChecked = verData["certificate"]!!
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.activity_ver_proceed -> supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_left_to_right, R.anim.enter_right_to_left, R.anim.exit_left_to_right)
                .addToBackStack("verification")
                .replace(R.id.verification_root, fragmentVerifyId)
                .commit()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
    }
}