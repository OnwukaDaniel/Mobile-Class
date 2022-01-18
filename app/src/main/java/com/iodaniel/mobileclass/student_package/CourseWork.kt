package com.iodaniel.mobileclass.student_package

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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentCourseWorkBinding
import com.iodaniel.mobileclass.teacher_package.classes.ClassInfo
import com.iodaniel.mobileclass.teacher_package.classes.Material
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class CourseWork : Fragment() {

    private lateinit var binding: FragmentCourseWorkBinding
    private var courseWorkAdapter = CourseWorkAdapter()
    private var dataset: ArrayList<Material> = arrayListOf()
    private var keyList: ArrayList<String> = arrayListOf()
    private var classMaterialsReference = FirebaseDatabase.getInstance().reference

    override fun onStart() {
        super.onStart()
        val bundle = arguments
        val json = bundle!!.getString("classInfo")
        val classInfo: ClassInfo = Json.decodeFromString(json!!)
        readFromDatabase(classInfo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCourseWorkBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun readFromDatabase(classInfo: ClassInfo) {
        classMaterialsReference = classMaterialsReference
            .child("materials")
            .child(classInfo.teacherInChargeUID)
            .child(classInfo.classCode)

        classMaterialsReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(Material::class.java)
                dataset.add(snap!!)
                keyList.add(snapshot.key!!)
                rvInit(classInfo)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val snap = snapshot.getValue(Material::class.java)
                dataset.add(snap!!)
                keyList.add(snapshot.key!!)
                courseWorkAdapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val index = keyList.indexOf(snapshot.key)
                keyList.removeAt(index)
                dataset.removeAt(index)
                courseWorkAdapter.notifyItemRemoved(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun rvInit(classInfo: ClassInfo) {
        binding.rvCourseWork.adapter = courseWorkAdapter
        binding.rvCourseWork.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        courseWorkAdapter.dataset = dataset
        courseWorkAdapter.context = requireContext()
        courseWorkAdapter.activity = requireActivity()
        courseWorkAdapter.classCode = classInfo.classCode
    }
}

class CourseWorkAdapter : RecyclerView.Adapter<CourseWorkAdapter.ViewHolder>() {

    lateinit var dataset: ArrayList<Material>
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var classCode: String

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val heading: TextView = itemView.findViewById(R.id.lesson_heading)
        val dateCreated: TextView = itemView.findViewById(R.id.lesson_date)
        val numberOfMaterials: TextView = itemView.findViewById(R.id.lesson_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lesson_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.heading.text = datum.heading
        holder.numberOfMaterials.text =
            if (datum.mediaUris.size == 0) "None" else datum.mediaUris.size.toString()
        val date = convertLongToTime(datum.dateCreated.toLong()).split(" ")[0]
        holder.dateCreated.text = date

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ViewMaterial::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val json = Json.encodeToString(datum)
            intent.putExtra("material", json)
            intent.putExtra("classCode", classCode)
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

    override fun getItemCount(): Int = dataset.size
}