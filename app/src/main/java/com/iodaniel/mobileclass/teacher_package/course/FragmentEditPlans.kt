package com.iodaniel.mobileclass.teacher_package.course

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.CourseCardData
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.databinding.FragmentEditPlansBinding
import com.iodaniel.mobileclass.liveDataClasses.ValueEventLiveData
import com.iodaniel.mobileclass.repository.EditPlanRepo
import com.iodaniel.mobileclass.util.BackgroundHelper
import com.iodaniel.mobileclass.util.ChildEventTemplate
import com.iodaniel.mobileclass.util.CustomLinearLayoutManager
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FragmentEditPlans : Fragment(), View.OnClickListener, BackgroundHelper {
    private lateinit var binding: FragmentEditPlansBinding
    private var planModulesExercise: ArrayList<PlanModulesExercise> = arrayListOf()
    private var pmeRef = FirebaseDatabase.getInstance().reference
    private lateinit var editPlanRepo: EditPlanRepo
    private val mfV: MessageFragmentViewModel by activityViewModels()
    private var editTextList: ArrayList<EditText> = arrayListOf()
    private val planAdapter = PlanAdapter()
    private var courseCardData: CourseCardData? = null
    private lateinit var valueEventLiveData: ValueEventLiveData
    private var courseCardDataJson = ""
    private var courseFetched = false
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditPlansBinding.inflate(inflater, container, false)
        requireActivity().setActionBar(binding.editPlanToolbar)
        requireActivity().actionBar!!.title = "Plans"
        requireActivity().actionBar!!.subtitle = "Create many topics as necessary"
        delayWait(60)
        editPlanRepo = EditPlanRepo(requireActivity(), requireContext(), binding.root, viewLifecycleOwner)

        binding.rowEditTopicCourseAdd1.setOnClickListener(this)
        binding.rowEditTopicCourseSubmit.setOnClickListener(this)
        binding.modulesAndPlansCreatePlanChip.setOnClickListener(this)
        courseCardDataJson = requireArguments().getString(getString(R.string.manage_course_data_intent))!!
        courseCardData = Gson().fromJson(courseCardDataJson, CourseCardData::class.java)
        pmeRef = pmeRef.child(getString(R.string.pme_ref)).child(FirebaseAuth.getInstance().currentUser!!.uid).child(courseCardData!!.courseCode)

        planAdapter.planModulesExercise = planModulesExercise
        planAdapter.editTextList = editTextList
        planAdapter.planModulesExercise = planModulesExercise
        planAdapter.rv = binding.editCoursePlanRoot
        binding.editCoursePlanRoot.layoutManager = CustomLinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.editCoursePlanRoot.adapter = planAdapter
        valueEventLiveData = ValueEventLiveData(pmeRef)
        valueEventLiveData.observe(viewLifecycleOwner) {
            when (it.second) {
                ChildEventTemplate.onDataChange -> {
                    val json = Gson().toJson(it.first.value!!)
                    val snap: ArrayList<*> = Gson().fromJson(json, ArrayList::class.java)
                    planAdapter.notifyItemRangeRemoved(0, planModulesExercise.size)
                    planModulesExercise.clear()
                    for ((index, i) in snap.withIndex()) {
                        val gson = Gson().toJson(i)
                        val data: PlanModulesExercise = Gson().fromJson(gson, PlanModulesExercise::class.java)
                        planModulesExercise.add(data)
                        planAdapter.notifyItemInserted(index)
                    }
                    planAdapter.planModulesExercise = planModulesExercise
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseDatabase.getInstance().reference.get().addOnSuccessListener {
            courseFetched = true
            if (planModulesExercise.isEmpty()) empty() else notEmpty()
        }
    }

    private fun delayWait(input: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val range = (0..input).toList().reversed()
            val flow = range.asSequence().asFlow().onEach { delay(1_000) }
            flow.collect {
                if (it + 1 == input) {
                    if (activity != null && isAdded) requireActivity().runOnUiThread {
                        if (!courseFetched) {
                            //Snackbar.make(binding.root, "Network time out", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.row_edit_topic_course_add1 -> {
                planModulesExercise.add(PlanModulesExercise())
                planAdapter.notifyItemInserted(planModulesExercise.size)
                binding.editCoursePlanRoot.recycledViewPool.clear()
            }
            R.id.modules_and_plans_create_plan_chip -> this@FragmentEditPlans.notEmpty()
            R.id.row_edit_topic_course_submit -> {
                for (pme in planModulesExercise) {
                    if (pme.plan.trim() == "") {
                        Snackbar.make(binding.root, "Fill empty forms or remove them", Snackbar.LENGTH_LONG).show()
                        return
                    }
                }
                editPlanRepo.uploadPlan(pmeRef, planModulesExercise, binding.root, courseCardDataJson, mfV, R.id.fragment_edit_plans_root)
            }
        }
    }

    override fun empty() {
        binding.editPlansEmptyRoot.visibility = View.VISIBLE
        binding.editPlansDataRoot.visibility = View.GONE
        binding.editPlansNoNetworkRoot.visibility = View.GONE
    }

    override fun notEmpty() {
        binding.editPlansDataRoot.visibility = View.VISIBLE
        binding.editPlansEmptyRoot.visibility = View.GONE
        binding.editPlansNoNetworkRoot.visibility = View.GONE
    }

    override fun noInternet() {
        binding.editPlansNoNetworkRoot.visibility = View.VISIBLE
        binding.editPlansEmptyRoot.visibility = View.GONE
        binding.editPlansDataRoot.visibility = View.GONE
    }
}

class PlanAdapter : RecyclerView.Adapter<PlanAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var rv: RecyclerView
    var planModulesExercise: ArrayList<PlanModulesExercise> = arrayListOf()
    var editTextList: ArrayList<EditText> = arrayListOf()

    init {
        setHasStableIds(true)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.row_edit_topic_course_number)
        val input: EditText = itemView.findViewById(R.id.row_edit_topic_course_input)
        val close: ImageView = itemView.findViewById(R.id.row_edit_topic_course_close)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_edit_topic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            class InputWatcher : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    planModulesExercise[position].plan = s?.toString()!!
                }
            }

            holder.input.addTextChangedListener(InputWatcher())
            editTextList.add(holder.input)
            val datum = planModulesExercise[position]
            holder.input.setText(datum.plan)
        } catch (e: Exception) {
            println("Error ***************************************** ${e.printStackTrace()}")
        }
        holder.close.setOnClickListener {
            try {
                val pos = holder.adapterPosition
                editTextList.removeAt(pos)
                planModulesExercise.removeAt(pos)
                notifyItemRemoved(pos)
                rv.recycledViewPool.clear()
            } catch (e: Exception) {
                println("Position removed ***************************************** ${e.printStackTrace()}")
            }
        }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return planModulesExercise.size
    }
}