package com.iodaniel.mobileclass.teacher_package.course

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.data_class.PlanModulesExercise
import com.iodaniel.mobileclass.data_class.Question
import com.iodaniel.mobileclass.databinding.ActivityExercisesBinding
import com.iodaniel.mobileclass.shared_classes.Util
import com.iodaniel.mobileclass.teacher_package.singleclass.AddExerciseFragment

class ActivityExercises : AppCompatActivity() {
    private val binding by lazy { ActivityExercisesBinding.inflate(layoutInflater) }
    private val thisExerciseAdapter = ThisExerciseAdapter()
    private var courseCardDataJson: String = ""
    private var exercisePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val json = intent.getStringExtra("data")
        setSupportActionBar(binding.exercisesToolbar)
        courseCardDataJson = intent.getStringExtra("courseCardDataJson")!!
        exercisePosition = intent.getIntExtra("position", 0)
        val courseCardData = Gson().fromJson(json, PlanModulesExercise::class.java)
        val exerciseQuestion = courseCardData.exercise
        title = courseCardData.plan
        setContentView(binding.root)
        thisExerciseAdapter.dataset = exerciseQuestion.questions
        thisExerciseAdapter.activity = this
        binding.rvExercises.adapter = thisExerciseAdapter
        binding.rvExercises.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                val fragment = AddExerciseFragment()
                val bundle = Bundle()
                bundle.putString("courseCardDataJson", courseCardDataJson)
                bundle.putInt("position", exercisePosition)
                fragment.arguments = bundle
                supportFragmentManager
                    .beginTransaction()
                    .addToBackStack("edit")
                    .replace(R.id.exercise_root, fragment)
                    .commit()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_exercise, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

class ThisExerciseAdapter : RecyclerView.Adapter<ThisExerciseAdapter.ViewHolder>() {
    var courseCardDataJson: String = ""
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var dataset: ArrayList<Question>

    init {
        setHasStableIds(true)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseNumber: TextView = itemView.findViewById(R.id.exercise_number)
        val exerciseTime: TextView = itemView.findViewById(R.id.exercise_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_activity_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        val displayNo = "Exercise ${position + 1}"
        holder.exerciseNumber.text = displayNo
        try {
            val time = Util.convertLongToTime(datum.timeCreated.toLong())
            holder.exerciseTime.text = time
        } catch (e: Exception) {
        }
    }

    override fun getItemCount() = dataset.size

    override fun getItemId(position: Int) = position.toLong()
}