package com.iodaniel.mobileclass.teacher_package.singleclass

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.LessonFragmentBinding
import com.iodaniel.mobileclass.student_package.ViewMaterial
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class LessonsFragment : Fragment() {
    private lateinit var binding: LessonFragmentBinding
    private lateinit var adapter: LessonRvAdapter
    private var listOfLessons: ArrayList<Material> = arrayListOf()
    private var keyList = arrayListOf<String>()
    private lateinit var classInfo: ClassInfo
    private var stTypeRef = FirebaseDatabase.getInstance().reference

    override fun onStart() {
        super.onStart()
        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        classInfo = Json.decodeFromString(json!!)

        stTypeRef = stTypeRef
            .child("materials")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(classInfo.classCode)

        readFromDatabase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = LessonFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun rvInit() {
        adapter = LessonRvAdapter()
        adapter.dataSet = listOfLessons
        adapter.classInfo = classInfo
        binding.rvLessons.adapter = adapter
        adapter.activity = requireActivity()
        binding.rvLessons.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun readFromDatabase() {
        stTypeRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                readData(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                readData(snapshot)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = keyList.indexOf(snapshot.key)
                keyList.removeAt(index)
                listOfLessons.removeAt(index)
                adapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

            @SuppressLint("NotifyDataSetChanged")
            fun readData(snapshot: DataSnapshot) {
                try {
                    val material = snapshot.getValue(Material::class.java)!!
                    listOfLessons.add(material)
                    keyList.add(snapshot.key!!)
                    rvInit()
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    println("Exception from database ******************** ${e.printStackTrace()}")
                }
            }
        })
    }
}

class LessonRvAdapter : RecyclerView.Adapter<LessonRvAdapter.ViewHolder>() {

    var dataSet: ArrayList<Material> = arrayListOf()
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classInfo: ClassInfo

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lesson_heading: TextView = itemView.findViewById(R.id.lesson_heading)
        val lesson_number: TextView = itemView.findViewById(R.id.lesson_number)
        val lesson_date: TextView = itemView.findViewById(R.id.lesson_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lesson_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dataSet.reverse()
        val datum = dataSet[position]
        holder.lesson_date.text = datum.time
        holder.lesson_heading.text = datum.heading
        holder.lesson_number.text =
            if (datum.mediaUris.size == 0) "None" else datum.mediaUris.size.toString()
        val date = convertLongToTime(datum.dateCreated.toLong()).split(" ")[0]
        holder.lesson_date.text = date

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ViewMaterial::class.java)
            val json = Gson().toJson(datum)
            intent.putExtra("material", json)
            val jsonClassInfo = Json.encodeToString(classInfo)
            intent.putExtra("classInfo", jsonClassInfo)
            intent.putExtra("classCode", classInfo.classCode)
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

    override fun getItemCount(): Int = dataSet.size
}
