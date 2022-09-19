package com.iodaniel.mobileclass.teacher_package.course

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.ExerciseType
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.data_class.Question
import com.iodaniel.mobileclass.databinding.FragmentExerciseBinding
import com.iodaniel.mobileclass.liveDataClasses.ValueEventLiveData
import com.iodaniel.mobileclass.shared_classes.FragmentStudentViewAssignment
import com.iodaniel.mobileclass.util.ChildEventTemplate.onDataChange
import com.iodaniel.mobileclass.viewModel.QuestionTransferViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class FragmentModulesAndExercise : Fragment() {
    private lateinit var binding: FragmentExerciseBinding
    private var courseCardDataJson = ""
    private var courseCardData: CourseCardData? = null
    private var courseFetched = false
    private val exerciseAndPlansAdapter = ExerciseAndPlansAdapter()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var pmeRef = FirebaseDatabase.getInstance().reference
    private val questionTransferViewModel by activityViewModels<QuestionTransferViewModel>()
    private var plansAndExercisesList: ArrayList<PlanModulesExercise> = arrayListOf()
    private val questionList: ArrayList<String> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentExerciseBinding.inflate(inflater, container, false)
        courseCardDataJson = requireArguments().getString(getString(R.string.manage_course_data_intent))!!
        courseCardData = Gson().fromJson(courseCardDataJson, CourseCardData::class.java)
        requireActivity().setActionBar(binding.modulesAndPlansToolbar)
        requireActivity().actionBar!!.title = "Exercise"
        questionTransferViewModel.setCourseCardData(courseCardData!!)

        pmeRef = pmeRef.child(getString(R.string.pme_ref)).child(FirebaseAuth.getInstance().currentUser!!.uid).child(courseCardData!!.courseCode)
        val dataSnapshotLiveData = ValueEventLiveData(pmeRef)
        dataSnapshotLiveData.observe(viewLifecycleOwner) {
            when (it.second) {
                onDataChange -> {
                    plansAndExercisesList.clear()
                    for (i in it.first.children) {
                        val value = Gson().toJson(i.value)
                        val data = Gson().fromJson(value, PlanModulesExercise::class.java)
                        plansAndExercisesList.add(data)
                        exerciseAndPlansAdapter.notifyDataSetChanged()
                    }
                }
            }
            dataAvailable()
            exerciseAndPlansAdapter.dataset = plansAndExercisesList
        }
        exerciseAndPlansAdapter.activity = requireActivity()
        exerciseAndPlansAdapter.courseCardDataJson = courseCardDataJson
        exerciseAndPlansAdapter.questionTransferViewModel = questionTransferViewModel
        exerciseAndPlansAdapter.dataset = plansAndExercisesList
        binding.plansModulesAndExerciseRv.adapter = exerciseAndPlansAdapter
        binding.plansModulesAndExerciseRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseDatabase.getInstance().reference.get().addOnSuccessListener {
            courseFetched = true
            if (plansAndExercisesList.isEmpty()) noData() else dataAvailable()
        }
    }

    private fun noData() {
        binding.modulesAndPlansNoNetworkRoot.visibility = View.VISIBLE
        binding.plansModulesAndExerciseRv.visibility = View.GONE
    }

    private fun dataAvailable() {
        binding.modulesAndPlansNoNetworkRoot.visibility = View.GONE
        binding.plansModulesAndExerciseRv.visibility = View.VISIBLE
    }
}

class ExerciseAdapter : RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {
    lateinit var questionTransferViewModel: QuestionTransferViewModel
    var courseCardDataJson: String = ""
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var dataset: ArrayList<Question>
    var exercisePosition = 0

    init {
        setHasStableIds(true)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseNumber: TextView = itemView.findViewById(R.id.exercise_number)
        val exerciseType: TextView = itemView.findViewById(R.id.exercise_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val displayNo = "Exercise ${position + 1}"
        holder.exerciseNumber.text = displayNo
        when (datum.exerciseType) {
            ExerciseType.NORMAL_QUESTION -> holder.exerciseType.visibility = View.GONE
            ExerciseType.DOC_QUESTION -> holder.exerciseType.text = "Document"
            ExerciseType.MULTI_QUESTION -> holder.exerciseType.text = "Multiple choice"
        }
        holder.itemView.setOnClickListener {
            questionTransferViewModel.setQuestion(datum)
            (activity as AppCompatActivity).supportFragmentManager.beginTransaction().addToBackStack("question")
                .replace(R.id.modules_and_plans_root, FragmentStudentViewAssignment())
                .commit()
        }
    }

    override fun getItemCount() = dataset.size

    override fun getItemId(position: Int) = position.toLong()
}

class ExerciseAndPlansAdapter : RecyclerView.Adapter<ExerciseAndPlansAdapter.ViewHolder>() {
    lateinit var questionTransferViewModel: QuestionTransferViewModel
    var courseCardDataJson: String = ""
    private lateinit var context: Context
    lateinit var activity: Activity
    var dataset: ArrayList<PlanModulesExercise> = arrayListOf()
    private val rvPool = RecyclerView.RecycledViewPool()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val planTitle: TextView = itemView.findViewById(R.id.row_exercise_plans_header_text)
        val count: TextView = itemView.findViewById(R.id.row_exercise_plans_header_count)
        val rowExerciseEmptyMsg: TextView = itemView.findViewById(R.id.row_exercise_empty_msg)
        val rvExercise: RecyclerView = itemView.findViewById(R.id.row_exercise_plans_rv)
        val card: CardView = itemView.findViewById(R.id.row_exercise_plans_root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_exercise_plans, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.planTitle.text = datum.plan
        val count = (position + 1).toString()
        holder.count.text = count
        holder.rowExerciseEmptyMsg.visibility = if (datum.exercise.questions.isEmpty()) View.VISIBLE else View.GONE

        val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        lm.isItemPrefetchEnabled = true
        lm.initialPrefetchItemCount = datum.exercise.questions.size
        val exerciseAdapter = ExerciseAdapter()

        exerciseAdapter.dataset = datum.exercise.questions
        exerciseAdapter.context = context
        exerciseAdapter.activity = activity
        exerciseAdapter.questionTransferViewModel = questionTransferViewModel
        holder.rvExercise.adapter = exerciseAdapter
        holder.rvExercise.layoutManager = lm
        holder.rvExercise.setRecycledViewPool(rvPool)

        holder.card.setOnClickListener {
            val json = Gson().toJson(datum)
            val intent = Intent(activity, ActivityExercises::class.java)
            intent.putExtra("data", json)
            intent.putExtra("courseCardDataJson", courseCardDataJson)
            intent.putExtra("position", holder.absoluteAdapterPosition)
            context.startActivity(intent)
            (activity as AppCompatActivity).overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun getItemCount() = dataset.size
}