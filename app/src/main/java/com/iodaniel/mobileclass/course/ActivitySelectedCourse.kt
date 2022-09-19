package com.iodaniel.mobileclass.course

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.accessing_mobile_app.SignInOrSignUp
import com.iodaniel.mobileclass.data_class.*
import com.iodaniel.mobileclass.databinding.ActivitySelectedCourseBinding
import com.iodaniel.mobileclass.databinding.FragmentOverviewBinding
import com.iodaniel.mobileclass.databinding.FragmentPeekBinding
import com.iodaniel.mobileclass.liveDataClasses.CourseCardLiveData
import com.iodaniel.mobileclass.liveDataClasses.OwnedCoursesLiveData
import com.iodaniel.mobileclass.liveDataClasses.PMELiveData
import com.iodaniel.mobileclass.plans.ActivityPlans
import com.iodaniel.mobileclass.rating.ActivityRating
import com.iodaniel.mobileclass.rating.FragmentRatings
import com.iodaniel.mobileclass.teacher_package.profile.ManageProfileCourseType
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.Util.cleanContentPreferences
import com.iodaniel.mobileclass.util.Util.removeComma
import com.iodaniel.mobileclass.viewModel.*
import kotlinx.coroutines.*

class ActivitySelectedCourse : AppCompatActivity(), PlansClickHelper, View.OnClickListener {
    private val binding by lazy { ActivitySelectedCourseBinding.inflate(layoutInflater) }
    private val courseCardViewModel: CourseCardViewModel by viewModels()
    private lateinit var pMELiveData: PMELiveData
    private val pmeViewModel: PMEViewModel by viewModels()
    private var courseCardData: CourseCardData? = null
    private val peekFragment = FragmentPeek()
    private val ownedCourseViewModel by viewModels<SingleToggleViewModel>()
    private val overviewFragment = FragmentOverView()
    private val positionViewModel = PositionViewModel()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val ratingsAdapter = RatingsAdapter()
    private lateinit var sfm: FragmentManager
    private val ratingDisplayViewModel: RatingDisplayViewModel by viewModels()
    private val ownedCourseListViewModel: OwnedCourseViewModel by viewModels()

    private val ratingListeners: ArrayList<ValueEventListener> = arrayListOf()
    private val ratingReferences: ArrayList<DatabaseReference> = arrayListOf()
    private val ratingDisplayDataset: ArrayList<PersonRatingData> = arrayListOf()

    private val ownedCourseList: ArrayList<OwnedCourse> = arrayListOf()
    private val auth = FirebaseAuth.getInstance().currentUser
    private var studentCoursesRef = FirebaseDatabase.getInstance().reference
    private var studentPrefRef = FirebaseDatabase.getInstance().reference
    private val otherCourseAdapter = OtherCourseAdapter()
    private val allPreferenceList: ArrayList<String> = arrayListOf()
    private val previousPreferenceAllList: ArrayList<ArrayList<String>> = arrayListOf()
    private val previousPreferenceList: ArrayList<String> = arrayListOf()
    private val newPreferenceList: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (!intent.hasExtra("courseCard")) {
            Snackbar.make(binding.root, "An error occurred", Snackbar.LENGTH_LONG).show()
            runBlocking { delay(3000); onBackPressed(); return@runBlocking }
        }
        val json = intent.getStringExtra("courseCard")
        courseCardData = Gson().fromJson(json, CourseCardData::class.java)
        setCourseCardData()
        binding.ratingsRoot.setOnClickListener(this)
        binding.selectedCourseImage.setOnClickListener(this)
        courseCardViewModel.setCC(courseCardData!!)
        similarCourses()
        getUserOwned()
        pmeLiveData()
        toggleAnimation()
        touchToggleAnimation()
        getReviews()
        sfm = supportFragmentManager
        sfm.beginTransaction()
        ownedCourseViewModel.toggle.observe(this) {
            if (it) {
                binding.selectedCourseOwned.visibility = View.VISIBLE
                binding.selectedCourseStartLearning.visibility = View.VISIBLE
                val courseJson = Gson().toJson(courseCardData)
                val intent = Intent(this, ActivityCourseLanding::class.java)
                intent.putExtra("courseJson", courseJson)
                binding.selectedCourseImage.setOnClickListener {
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                binding.selectedCourseStartLearning.setOnClickListener {
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            } else View.GONE
        }
    }

    private fun fetchAndWriteToPreference() {
        // PREFERENCES ARE STORED AS A LONG SENTENCE OF WORDS SEPARATED BY ','
        if (auth == null) return
        studentPrefRef = studentPrefRef.child(studentPreference).child(auth.uid)
        studentPrefRef.get().addOnSuccessListener {
            if (it.exists()) {
                for (i in it.children) {
                    val onePreference = i.value.toString()
                    if (onePreference !in allPreferenceList) allPreferenceList.add(onePreference)
                }
                val currentWords = cleanContentPreferences(courseCardData!!.description)
                for (word in removeComma(currentWords)) if (word !in allPreferenceList) newPreferenceList.add(word)
                var toUpload = ""; for (word in newPreferenceList) toUpload += "$word,"
                toUpload.dropLast(toUpload.length - 1)
                if (toUpload !in allPreferenceList) {
                    allPreferenceList.add(toUpload)
                    studentPrefRef.setValue(allPreferenceList).addOnSuccessListener {}
                }
            } else {
                val currentWords = cleanContentPreferences(courseCardData!!.description)
                for (word in removeComma(currentWords)) if (word !in allPreferenceList) newPreferenceList.add(word)
                var toUpload = ""; for (word in newPreferenceList) toUpload += "$word,"
                toUpload.dropLast(toUpload.length)
                studentPrefRef.setValue(arrayListOf(toUpload)).addOnSuccessListener {}
            }
        }.addOnFailureListener {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for ((index, ref) in ratingReferences.withIndex()) ref.removeEventListener(ratingListeners[index])
    }

    private fun getReviews() {
        val studentDetails: ArrayList<StudentDetails> = arrayListOf()
        ratingsAdapter.dataset = ratingDisplayDataset
        binding.ratingsRv.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        binding.ratingsRv.adapter = ratingsAdapter
        val ratings = courseCardData!!.personRatingReference
        for (userRating in ratings) {
            val ratingUserRef = FirebaseDatabase.getInstance().reference.child("student_details").child(userRating.studentUid)
            val listener = ratingUserRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val studentData = snapshot.getValue(StudentDetails::class.java)!!
                        val personRatingData = PersonRatingData(
                            name = studentData.username,
                            image = studentData.image,
                            timeRated = userRating.timeRated,
                            ratingStars = userRating.ratingValue,
                            ratingText = userRating.ratingText,
                        )
                        if (personRatingData !in ratingDisplayDataset) {
                            ratingDisplayDataset.add(personRatingData)
                            ratingDisplayViewModel.setRatingDisplayList(ratingDisplayDataset)
                            ratingsAdapter.notifyItemInserted(ratingDisplayDataset.size)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
            ratingReferences.add(ratingUserRef)
            ratingListeners.add(listener)
        }
    }

    private fun similarCourses() {
        val dataset: ArrayList<CourseCardData> = arrayListOf()
        otherCourseAdapter.dataset = dataset
        otherCourseAdapter.activity = this
        binding.selectedCourseRelatedRv.adapter = otherCourseAdapter
        binding.selectedCourseRelatedRv.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)

        val ref = FirebaseDatabase.getInstance().reference.child(getString(R.string.course_path))
        val datasetKey: MutableSet<String> = mutableSetOf()
        CourseCardLiveData(ref).observe(this) {
            when (it.second.second) {
                ChildEventTemplate.onChildAdded -> {
                    val key = it.second.first
                    if (key !in datasetKey) {
                        val courseCardData = it.first
                        if (courseCardData.manageProfileCourseType == ManageProfileCourseType.COMPLETE) {
                            binding.similarShimmer.visibility = View.GONE
                            dataset.add(courseCardData)
                            datasetKey.add(key)
                            otherCourseAdapter.notifyItemInserted(dataset.size)
                        }
                    }
                }
                ChildEventTemplate.onChildRemoved -> {
                    val key = it.second.first
                    if (key in datasetKey) {
                        val index = datasetKey.indexOf(key)
                        datasetKey.minusElement(key)
                        dataset.removeAt(index)
                        otherCourseAdapter.notifyItemRemoved(index)
                    }
                }
                ChildEventTemplate.onChildChanged -> {
                    val index = datasetKey.indexOf(it.second.first)
                    dataset[index] = it.first
                    otherCourseAdapter.notifyItemChanged(index)
                }
                ChildEventTemplate.onChildMoved -> {}
            }
        }

    }

    override fun onResume() {
        super.onResume()
        setCourseOverView()
    }

    private fun toggleAnimation() {
        binding.selectedCourseOverviewToggle.setOnClickListener { view ->
            scope.launch {
                delay(700)
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right, R.anim.enter_right_to_left, R.anim.exit_right_to_left)
                    .replace(R.id.selected_course_content_root, overviewFragment)
                    .commit()
            }
            val rect = Rect()
            binding.selectedCourseOverviewToggle.getGlobalVisibleRect(rect)
            positionViewModel.setPosition(0)
            ObjectAnimator.ofFloat(binding.selectedCourseToggleHandle, "x", 0f).apply {
                duration = 700
                start()
            }
            positionViewModel.setPosition(0)
            binding.selectedCourseToggleTitle.text = "Overview"
        }
        positionViewModel.position.observe(this) {
            if (it == 0) {
                binding.selectedCourseToggleHandle.setOnClickListener { view ->
                    scope.launch {
                        delay(700)
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                            .replace(R.id.selected_course_content_root, peekFragment)
                            .commit()
                    }
                    val rect = Rect()
                    binding.selectedCoursePeekToggle.getGlobalVisibleRect(rect)
                    ObjectAnimator.ofFloat(view, "x", rect.left.toFloat()).apply {
                        duration = 700
                        start()
                    }
                    binding.selectedCourseToggleTitle.text = "Peek"
                    positionViewModel.setPosition(1)
                }
            } else if (it == 1) {
                binding.selectedCourseToggleHandle.setOnClickListener { view ->
                    scope.launch {
                        delay(700)
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right, R.anim.enter_right_to_left, R.anim.exit_right_to_left)
                            .replace(R.id.selected_course_content_root, overviewFragment)
                            .commit()
                    }
                    val rect = Rect()
                    binding.selectedCourseOverviewToggle.getGlobalVisibleRect(rect)
                    val rectToggle = Rect()
                    binding.selectedCourseToggleHandle.getGlobalVisibleRect(rectToggle)
                    ObjectAnimator.ofFloat(view, "x", rectToggle.left.toFloat(), 0f).apply {
                        duration = 700
                        start()
                    }
                    positionViewModel.setPosition(0)
                    binding.selectedCourseToggleTitle.text = "Overview"
                }
            }
        }
        binding.selectedCoursePeekToggle.setOnClickListener { view ->
            scope.launch {
                delay(700)
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                    .replace(R.id.selected_course_content_root, peekFragment)
                    .commit()
            }
            val rect = Rect()
            binding.selectedCoursePeekToggle.getGlobalVisibleRect(rect)
            ObjectAnimator.ofFloat(binding.selectedCourseToggleHandle, "x", rect.left.toFloat()).apply {
                duration = 700
                start()
            }
            binding.selectedCourseToggleTitle.text = "Peek"
            positionViewModel.setPosition(1)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun touchToggleAnimation() {
        var dX = 0F
        var dY = 0F
        val rect = Rect()
        binding.selectedCoursePeekToggle.getGlobalVisibleRect(rect)
        binding.selectedCourseToggleHandle.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.x
                    dY = v.y - event.y
                }
                MotionEvent.ACTION_MOVE -> v.animate()
                    .x(rect.right.toFloat())
                    /*.y(event.rawY + dY)*/
                    .setDuration(0)
                    .start()
                else -> return@setOnTouchListener false
            }
            return@setOnTouchListener true
        }
        positionViewModel.position.observe(this) {
            when (it) {
                0 -> {

                }
            }
        }
    }

    private fun pmeLiveData() {
        val courseCardRef = FirebaseDatabase.getInstance().reference
            .child(getString(R.string.plans_module_exercise))
            .child(courseCardData!!.instructorInChargeUID)
            .child(courseCardData!!.courseCode)
        pMELiveData = PMELiveData(courseCardRef)
        pMELiveData.observe(this) { pmeViewModel.setPME(it.first) }
    }

    private fun setCourseCardData() {
        fetchAndWriteToPreference()

        binding.selectedCourseCourseName.text = courseCardData!!.courseName
        val studentsNumText = "(${courseCardData!!.studentsEnrolled}) students enrolled"
        binding.selectedCourseStudentsNum.text = studentsNumText
        binding.selectedCourseDifficulty.text = courseCardData!!.level
        if (courseCardData!!.organisationName == "") binding.selectedCourseOrganisationRoot.visibility = View.GONE
        else binding.selectedCourseOrganisationName.text = courseCardData!!.organisationName
        Glide.with(applicationContext).load(Uri.parse(courseCardData!!.courseImage)).centerCrop().into(binding.selectedCourseImage)
        //--------------------------------------- RATING -------------------------------------------
        val ratingList = courseCardData!!.personRatingReference
        if (ratingList.isEmpty()) return
        var rating = 0.0
        for (i in ratingList) rating += i.ratingValue.toDouble()
        rating /= ratingList.size
        binding.selectedCourseRating.rating = rating.toFloat()
        binding.ratingsRatingNumber.text = rating.toString()
    }

    private fun setCourseOverView() {
        overviewFragment.clickHelper = this
        supportFragmentManager.beginTransaction()
            .replace(R.id.selected_course_content_root, overviewFragment)
            .commit()
    }

    private fun getUserOwned() {
        if (auth == null) return
        studentCoursesRef = studentCoursesRef.child("studentData").child(auth.uid).child("ownedCourses")
        OwnedCoursesLiveData(studentCoursesRef).observe(this) {
            when (it.second) {
                ChildEventTemplate.onDataChange -> {
                    val hash = it.first.value as HashMap<*, *>
                    for (x in hash) {
                        val course = Gson().fromJson(x.value.toString(), OwnedCourse::class.java)
                        if (course.courseCode == courseCardData!!.courseCode && course.instructorAuth == courseCardData!!.instructorInChargeUID) {
                            ownedCourseViewModel.setToggle(true)
                        }
                        ownedCourseList.add(course)
                    }
                    ownedCourseListViewModel.setOwnedCourseList(ownedCourseList)
                }
            }
        }
    }

    override fun onCLickPlan(position: Int) {
        val intent = Intent(this, ActivityPlans::class.java)
        intent.putExtra("position", position)
        startActivity(intent)
        overridePendingTransition(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ratings_rv -> {
                supportFragmentManager.beginTransaction().addToBackStack("ratings")
                    .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                    .replace(R.id.selected_course_root, FragmentRatings())
                    .commit()
            }
        }
    }

    companion object {
        const val studentPreference = "student_preferences"
    }
}

class FragmentOverView : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentOverviewBinding
    private val courseCardViewModel: CourseCardViewModel by activityViewModels()
    private val ownedCourseViewModel by activityViewModels<SingleToggleViewModel>()
    private val ownedCourseListViewModel: OwnedCourseViewModel by activityViewModels()
    private lateinit var pref: SharedPreferences
    lateinit var clickHelper: PlansClickHelper
    private var json = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOverviewBinding.inflate(inflater, container, false)
        pref = requireContext().getSharedPreferences(getString(R.string.ALL_SHARED_PREFERENCES), Context.MODE_PRIVATE)
        checkUserType()
        binding.overviewBasic.setOnClickListener(this)
        binding.courseRatingSection.setOnClickListener(this)
        binding.overviewPremium.setOnClickListener(this)
        binding.overviewGroup.setOnClickListener(this)
        binding.overviewEnrollButton.setOnClickListener(this)
        courseCardViewModel.courseCard.observe(viewLifecycleOwner) {
            json = Gson().toJson(it)
            binding.overviewDescription.text = it.description
            val price = "$${it.price}"
            binding.overviewEnrollPrice.text = price
        }
        ownedCourseViewModel.toggle.observe(viewLifecycleOwner) {
            binding.overviewEnrollButton.visibility
            if (it) binding.overviewEnrollButton.visibility = View.VISIBLE else View.GONE
        }
        ownedCourseListViewModel.ownedCourseList.observe(viewLifecycleOwner) {
            for (owned in it) {
                val courseCardData = Gson().fromJson(json, CourseCardData::class.java)
                if (owned.courseCode == courseCardData.courseCode) binding.overviewEnrollButton.visibility = View.GONE
            }
        }
        return binding.root
    }

    private fun checkUserType() {
        when (pref.getString(getString(R.string.studentTeacherPreference), "")) {
            getString(R.string.teacher) -> binding.overviewStudentOptions.visibility = View.GONE
            else -> binding.overviewStudentOptions.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.overview_basic -> clickHelper.onCLickPlan(1)
            R.id.overview_premium -> clickHelper.onCLickPlan(2)
            R.id.overview_group -> clickHelper.onCLickPlan(3)
            R.id.overview_enroll_button -> {
                when (pref.getString(getString(R.string.studentTeacherPreference), "")) {
                    "" -> {// Not signed in
                        val intent = Intent(requireContext(), SignInOrSignUp::class.java)
                        startActivity(intent)
                    }
                    else -> {
                        if (json == "") return
                        val intent = Intent(requireActivity(), ActivityPayment::class.java)
                        intent.putExtra("course card data", json)
                        startActivity(intent)
                    }
                }
            }
            R.id.course_rating_section -> {
                val intent = Intent(requireContext(), ActivityRating::class.java)
                intent.putExtra("course data", json)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
            }
        }
    }
}

interface PlansClickHelper {
    fun onCLickPlan(position: Int)
}

class FragmentPeek : Fragment() {
    private lateinit var binding: FragmentPeekBinding
    private val pMEAdapter = PMEAdapter()
    private val pmeViewModel: PMEViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPeekBinding.inflate(inflater, container, false)
        pmeViewModel.planModulesExercise.observe(viewLifecycleOwner) {
            pMEAdapter.dataset = it
            binding.peekRv.adapter = pMEAdapter
            binding.peekRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        return binding.root
    }
}

class PMEAdapter : RecyclerView.Adapter<PMEAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<PlanModulesExercise> = arrayListOf()
    private val rvMaterialPool = RecyclerView.RecycledViewPool()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val count: TextView = itemView.findViewById(R.id.row_peek_header_count)
        val plan: TextView = itemView.findViewById(R.id.row_peek_header_text)
        val rvMaterial: RecyclerView = itemView.findViewById(R.id.row_peek_materials_rv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_pme_peek, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.itemView.setOnClickListener {

        }

        holder.plan.text = datum.plan
        val count = "${position + 1}."
        holder.count.text = count

        val lmM = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        lmM.isItemPrefetchEnabled = true
        lmM.initialPrefetchItemCount = datum.modules.uris.size
        val moduleChildAdapter = ModuleChildAdapter()
        moduleChildAdapter.dataset = datum.modules.uris
        holder.rvMaterial.setRecycledViewPool(rvMaterialPool)
        holder.rvMaterial.layoutManager = lmM
        holder.rvMaterial.adapter = moduleChildAdapter
    }

    override fun getItemCount() = dataset.size
}

class ModuleChildAdapter : RecyclerView.Adapter<ModuleChildAdapter.ViewHolder>() {
    private lateinit var context: Context
    var dataset: ArrayList<MutableMap<String, String>> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: TextView = itemView.findViewById(R.id.module_peek_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_module_peek, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.chip.text = datum["filename"]
    }

    override fun getItemCount() = dataset.size
}

class OtherCourseAdapter : RecyclerView.Adapter<OtherCourseAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<CourseCardData> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.similar_course_name)
        val image: ImageView = itemView.findViewById(R.id.similar_course_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_similar_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.name.text = datum.courseName
        Glide.with(context).load(datum.courseImage).centerCrop().into(holder.image)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivitySelectedCourse::class.java)
            intent.putExtra("courseCard", Gson().toJson(datum))
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
        }
    }

    override fun getItemCount() = dataset.size
}

class RatingsAdapter : RecyclerView.Adapter<RatingsAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<PersonRatingData> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.rating_user_image)
        val name: TextView = itemView.findViewById(R.id.rating_name)
        val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        dataset = if (dataset.size < 3) dataset else dataset.subList(0, 2).toMutableList() as ArrayList<PersonRatingData>
        val view = LayoutInflater.from(context).inflate(R.layout.row_rating_, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.name.text = datum.name
        holder.ratingText.text = datum.ratingText
        holder.ratingBar.rating = if (datum.ratingStars == "") 0F else datum.ratingStars.toFloat()
        Glide.with(context).load(datum.image).centerCrop().into(holder.image)
    }

    override fun getItemCount() = dataset.size
}