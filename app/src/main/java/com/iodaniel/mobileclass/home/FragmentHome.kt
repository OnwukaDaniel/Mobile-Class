package com.iodaniel.mobileclass.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.course.ActivitySelectedCourse
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.OwnedCourse
import com.iodaniel.mobileclass.data_class.PersonRatingData
import com.iodaniel.mobileclass.databinding.FragmentMarketBinding
import com.iodaniel.mobileclass.liveDataClasses.OwnedCoursesLiveData
import com.iodaniel.mobileclass.repository.HomeRepo
import com.iodaniel.mobileclass.util.BackgroundHelper
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.util.Util.removeComma
import com.iodaniel.mobileclass.util.uistate_manager.UIStateDialogs
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import com.iodaniel.mobileclass.viewModel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentHome : Fragment(), BackgroundHelper {
    private lateinit var binding: FragmentMarketBinding
    private lateinit var homeRepo: HomeRepo
    private var dialogs = Dialogs()
    private var uIStateViewModel = UIStateViewModel()
    private lateinit var uIStateDialogs: UIStateDialogs
    private var fetchedData = false
    private val otherCoursesAdapter = OtherCoursesAdapter()
    private val auth = FirebaseAuth.getInstance().currentUser
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMarketBinding.inflate(inflater, container, false)
        binding.otherShimmer.startShimmer()
        homeRepo = HomeRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)
        uIStateDialogs = UIStateDialogs(uIStateViewModel, requireContext(), viewLifecycleOwner, requireActivity())
        uIStateDialogs.uiState(fetchedData, dialogs)
        uiState()
        homeRepo.getSimilarCourses(binding.marketUserSimilarCoursesRv, binding.similarInterestRoot, binding.similarInterestShimmer)
        homeRepo.getOtherCourses(binding.marketOtherCoursesRv, uIStateViewModel, this@FragmentHome, otherCoursesAdapter, binding.otherShimmer)
        homeRepo.getTopCourses(binding.marketTopCoursesRv)

        //homeRepo.getRelatedPreference(binding.marketPreferenceRv, binding.preferencesRoot) // Don't call when user is not registered
        homeRepo.getTopSchemes(binding.marketSchemesRv)
        homeRepo.getSelectedSchemes(binding.marketUserSchemeRv)
        return binding.root
    }

    private fun initHomeRepo() {

    }

    private fun uiState() {
        var dialog = Dialog(requireContext())
        uIStateViewModel.setUIState(UiState.stateLoading)
        uIStateViewModel.uIState.observe(viewLifecycleOwner) {
            when (it) {
                UiState.stateData -> {
                    fetchedData = true
                    dialog.dismiss()
                }
                UiState.stateLoading -> {
                    dialog.dismiss()
                    dialog = dialogs.circularProgressDialog(text = "We are getting your courses", activity = requireActivity())
                }
                UiState.stateNoData -> {
                    dialog.dismiss()
                }
                UiState.stateNetworkError -> {
                    dialog.dismiss()
                    dialog = dialogs.networkErrorDialog(text = "No network connection", activity = requireActivity())
                }
                UiState.stateSuccess -> {
                    dialog.dismiss()
                    dialog = dialogs.successDialog(text = "Success", activity = requireActivity())
                }
            }
        }
        scope.launch(Dispatchers.Main) {
            delay(10_000)
            if (!fetchedData) uIStateViewModel.setUIState(UiState.stateNetworkError) else uIStateViewModel.setUIState(UiState.stateData)
        }
    }

    override fun empty() {
    }

    override fun notEmpty() {
    }

    override fun noInternet() {
    }
}

class TopCourseAdapter : RecyclerView.Adapter<TopCourseAdapter.ViewHolder>() {
    private lateinit var context: Context
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    var userTagDataset: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val row_top_course_title: TextView = itemView.findViewById(R.id.row_top_course_title)
        val row_top_course_description: TextView = itemView.findViewById(R.id.row_top_course_description)
        val row_top_course_rating_bar: RatingBar = itemView.findViewById(R.id.row_top_course_rating_bar)
        val row_top_course_price: TextView = itemView.findViewById(R.id.row_top_course_price)
        val row_top_course_image: ImageView = itemView.findViewById(R.id.row_top_course_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_top_course_landing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.row_top_course_image)
        holder.row_top_course_title.text = datum.courseName
        holder.row_top_course_description.text = datum.shortDescription
        holder.row_top_course_rating_bar.rating = datum.rating.toFloat()
        val price = "$ ${datum.price}"
        holder.row_top_course_price.text = price
    }

    override fun getItemCount() = dataset.size
}

class SimilarCoursesAdapter : RecyclerView.Adapter<SimilarCoursesAdapter.ViewHolder>() {
    lateinit var viewLifecycleOwner: LifecycleOwner
    private lateinit var context: Context
    lateinit var activity: Activity
    private val ownedCourseList: ArrayList<OwnedCourse> = arrayListOf()
    private val ownedCourseCode: ArrayList<String> = arrayListOf()
    var dataset: ArrayList<CourseCardData> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseOwned: CardView = itemView.findViewById(R.id.similar_course_owned)
        val courseImage: ImageView = itemView.findViewById(R.id.row_top_course_image)
        val courseName: TextView = itemView.findViewById(R.id.row_top_course_title)
        val courseShortDescription: TextView = itemView.findViewById(R.id.row_top_course_description)
        val courseRating: RatingBar = itemView.findViewById(R.id.row_top_course_rating_bar)

        val price: TextView = itemView.findViewById(R.id.row_top_course_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        getUserOwned()
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_top_course_landing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.courseImage)
        holder.courseName.text = datum.courseName

        val price = "$ ${datum.price}"
        holder.price.text = price
        holder.courseShortDescription.text = datum.shortDescription

        getReviews(datum, holder)
        if (datum.courseCode in ownedCourseCode) holder.courseOwned.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivitySelectedCourse::class.java)
            intent.putExtra("courseCard", Gson().toJson(datum))
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getUserOwned() {
        val auth = FirebaseAuth.getInstance().currentUser ?: return
        val studentCoursesRef = FirebaseDatabase.getInstance().reference.child("studentData").child(auth.uid).child("ownedCourses")
        OwnedCoursesLiveData(studentCoursesRef).observe(viewLifecycleOwner) {
            when (it.second) {
                ChildEventTemplate.onDataChange -> {
                    val hash = it.first.value as HashMap<*, *>
                    for (x in hash) {
                        val course = Gson().fromJson(x.value.toString(), OwnedCourse::class.java)
                        if (course.courseCode !in ownedCourseCode) ownedCourseList.add(course)
                    }
                    for (i in ownedCourseList) if (i.courseCode !in ownedCourseCode) ownedCourseCode.add(i.courseCode)
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun getReviews(courseCardData: CourseCardData, holder: ViewHolder) {
        val ratingDisplayDataset: ArrayList<PersonRatingData> = arrayListOf()
        var overAllRating = 0.0
        val ratings = courseCardData.personRatingReference
        for (userRating in ratings) {
            overAllRating += userRating.ratingValue.toDouble()
        }
        overAllRating /= ratings.size
        holder.courseRating.rating = overAllRating.toFloat()
    }

    override fun getItemCount() = dataset.size
}

class OtherCoursesAdapter : RecyclerView.Adapter<OtherCoursesAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var activity: Activity
    private lateinit var pref: SharedPreferences
    private var userType = ""
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    var ownedCourse: ArrayList<OwnedCourse> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseImage: ImageView = itemView.findViewById(R.id.row_other_courses_image)
        val courseName: TextView = itemView.findViewById(R.id.row_other_courses_title)
        val studentEnrolled: TextView = itemView.findViewById(R.id.row_other_courses_students_enrolled)
        val price: TextView = itemView.findViewById(R.id.row_other_courses_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        pref = activity.getSharedPreferences(context.getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        userType = pref.getString(context.getString(R.string.studentTeacherPreference), "")!!
        val view = LayoutInflater.from(context).inflate(R.layout.row_other_courses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.courseImage)
        holder.courseName.text = datum.courseName
        //holder.courseDescription.text = datum.shortDescription
        val price = "$ ${datum.price}"
        holder.price.text = price
        val studentEnrolled = if (datum.studentsEnrolled == "") "" else "${datum.studentsEnrolled} students enrolled"
        holder.studentEnrolled.text = studentEnrolled
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivitySelectedCourse::class.java)
            intent.putExtra("courseCard", Gson().toJson(datum))
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
        }
    }

    override fun getItemCount() = dataset.size
}

class PreferenceTagAdapter : RecyclerView.Adapter<PreferenceTagAdapter.ViewHolder>() {
    var studentPrefRef = FirebaseDatabase.getInstance().reference
    private lateinit var context: Context
    var ref = FirebaseDatabase.getInstance().reference
    private val allPreferenceList: ArrayList<String> = arrayListOf()
    var dataset: ArrayList<String> = arrayListOf()
    var datasetSelected: MutableSet<String> = mutableSetOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.row_pref_landing_chip_card)
        val text: TextView = itemView.findViewById(R.id.row_pref_landing_chip_text)
        val cancel: ImageView = itemView.findViewById(R.id.row_pref_landing_chip_cancel)
    }

    init {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        studentPrefRef = FirebaseDatabase.getInstance().reference.child(ActivitySelectedCourse.studentPreference).child(uid)
        setHasStableIds(true)
        ref = ref.child("user pref tag")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_pref_landing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.text.text = datum

        if (datum in datasetSelected) holder.card.background = ColorDrawable(Color.GREEN)
        else holder.card.background = ColorDrawable(Color.CYAN)

        holder.cancel.setOnClickListener {
            dataset.remove(datum)
            notifyItemRemoved(holder.absoluteAdapterPosition)
        }
        holder.card.setOnClickListener {
            if (datum in datasetSelected) {
                holder.card.setCardBackgroundColor(Color.GREEN)
            } else {
                holder.card.setCardBackgroundColor(Color.CYAN)
                datasetSelected.add(datum)
            }
            studentPrefRef.get().addOnSuccessListener {
                if (it.exists()) {
                    for (i in it.children) {
                        var onePreference = i.value.toString()
                        for (markedForRemoval in datasetSelected)
                            if (markedForRemoval in removeComma(onePreference)) onePreference = onePreference.removeSurrounding(markedForRemoval)
                        if (onePreference !in allPreferenceList) allPreferenceList.add(onePreference)
                    }
                    allPreferenceList.add(datum)
                    studentPrefRef.setValue(allPreferenceList).addOnSuccessListener {}
                }
            }
        }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = dataset.size
}

class SelectedSchemesAdapter : RecyclerView.Adapter<SelectedSchemesAdapter.ViewHolder>() {
    private lateinit var context: Context
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    var userTagDataset: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseImage: ImageView = itemView.findViewById(R.id.row_other_courses_image)
        val courseName: TextView = itemView.findViewById(R.id.row_other_courses_title)

        //val courseDescription: TextView = itemView.findViewById(R.id.row_other_courses_short_description)
        val studentEnrolled: TextView = itemView.findViewById(R.id.row_other_courses_students_enrolled)
        val price: TextView = itemView.findViewById(R.id.row_other_courses_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_other_courses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.courseImage)
        holder.courseName.text = datum.courseName
        //holder.courseDescription.text = datum.shortDescription
        val price = "$ ${datum.price}"
        holder.price.text = price
        val studentEnrolled = if (datum.studentsEnrolled == "") "(0)" else "(${datum.studentsEnrolled})"
        holder.studentEnrolled.text = studentEnrolled
        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = dataset.size
}

class TopSchemesAdapter : RecyclerView.Adapter<TopSchemesAdapter.ViewHolder>() {
    private lateinit var context: Context
    var dataset: ArrayList<CourseCardData> = arrayListOf()
    var userTagDataset: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseImage: ImageView = itemView.findViewById(R.id.row_other_courses_image)
        val courseName: TextView = itemView.findViewById(R.id.row_other_courses_title)

        //val courseDescription: TextView = itemView.findViewById(R.id.row_other_courses_short_description)
        val studentEnrolled: TextView = itemView.findViewById(R.id.row_other_courses_students_enrolled)
        val price: TextView = itemView.findViewById(R.id.row_other_courses_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_other_courses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.courseImage)
        holder.courseName.text = datum.courseName
        //holder.courseDescription.text = datum.shortDescription
        val price = "$ ${datum.price}"
        holder.price.text = price
        val studentEnrolled = if (datum.studentsEnrolled == "") "(0)" else "(${datum.studentsEnrolled})"
        holder.studentEnrolled.text = studentEnrolled
        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = dataset.size
}