package com.iodaniel.mobileclass.rating

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.PersonRatingReference
import com.iodaniel.mobileclass.databinding.ActivityRatingBinding
import com.iodaniel.mobileclass.util.Dialogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ActivityRating : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy { ActivityRatingBinding.inflate(layoutInflater) }
    private var courseData = CourseCardData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (intent.hasExtra("course data")) {
            courseData = Gson().fromJson(intent.getStringExtra("course data"), CourseCardData::class.java)
            binding.submitRating.setOnClickListener(this)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.submit_rating -> {
                val dialog = Dialogs().circularProgressDialog(activity = this)
                var ratingValue = binding.ratingStars.rating
                var ratingAverage = 0.0
                val ratingStars = binding.ratingStars.rating.toString()
                val ratingText = binding.ratingText.text.toString()
                val ratingHelpful = binding.ratingHelpful.text.toString()
                val ratingDelivery = binding.ratingDelivery.text.toString()
                if (ratingText == "" || ratingHelpful == "" || ratingDelivery == "") {
                    Snackbar.make(binding.root, "Empty input!", Snackbar.LENGTH_LONG).show()
                    dialog.dismiss()
                    return
                }

                val ratingInput = PersonRatingReference(
                    studentUid = FirebaseAuth.getInstance().uid!!,
                    timeRated = Calendar.getInstance().timeInMillis.toString(),
                    ratingValue = ratingStars,
                    ratingText = ratingText,
                    helpful = ratingHelpful,
                    delivery = ratingDelivery,
                )
                val finalRatingList = courseData.personRatingReference
                finalRatingList.add(ratingInput)

                for(rate in finalRatingList){
                    ratingAverage += rate.ratingValue.toDouble()
                }
                ratingAverage /= finalRatingList.size
                val ratingRef = FirebaseDatabase.getInstance().reference.child("course_path").child(courseData.courseCode)
                    .child("rating")
                val ref = FirebaseDatabase.getInstance().reference.child("course_path").child(courseData.courseCode)
                    .child("personRatingReference")
                ref.setValue(finalRatingList).addOnSuccessListener {
                    ratingRef.setValue(ratingAverage.toString()).addOnSuccessListener{}
                    Snackbar.make(binding.root, "Rating submitted", Snackbar.LENGTH_LONG).show()
                    dialog.dismiss()
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000); runOnUiThread { onBackPressed() }
                    }
                }.addOnFailureListener {
                    Snackbar.make(binding.root, "Failed to save rating. Please try again!", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}