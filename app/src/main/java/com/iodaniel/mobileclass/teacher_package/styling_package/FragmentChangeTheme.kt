package com.iodaniel.mobileclass.teacher_package.styling_package

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iodaniel.mobileclass.R
import com.iodaniel.mobileclass.databinding.FragmentChangeThemeBinding
import com.iodaniel.mobileclass.databinding.FragmentDialogChangeBinding

class FragmentChangeTheme : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentChangeThemeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeThemeBinding.inflate(layoutInflater, container, false)
        //Snackbar.make(binding.root, "Tap on Widgets to change Style", Snackbar.LENGTH_LONG).show()
        changes()
        return binding.root
    }

    private fun changes() {
        binding.fabMyClassAddClassesTheme.setOnClickListener(this)
        binding.rvListOfCoursesTheme.setOnClickListener(this)
        binding.toolbarMyClasssesTheme.setOnClickListener(this)
    }

    private fun dialogChangeFabColor() {
        val arr: ArrayList<String> = arrayListOf("Red", "Yellow", "Green", "Black", "White")
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("change")
            .replace(R.id.student_root_theme, FragmentChangeDialog(arr))
            .commit()
    }

    private fun dialogChangeToolbarTheme() {
        val arr: ArrayList<String> = arrayListOf("Red", "Yellow", "Green", "Black", "White")
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("change")
            .replace(R.id.student_root_theme, FragmentChangeDialog(arr))
            .commit()
    }

    private fun dialogChangeLayoutManager() {
        val arr: ArrayList<String> = arrayListOf("Linear", "Grid")
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack("change")
            .replace(R.id.student_root_theme, FragmentChangeDialog(arr))
            .commit()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rv_listOfCourses_theme -> {
                dialogChangeLayoutManager()
            }
            R.id.toolbar_my_classses_theme -> {
                dialogChangeToolbarTheme()
            }
            R.id.fab_my_class_add_classes_theme -> {
                dialogChangeFabColor()
            }
        }
    }
}

class FragmentChangeDialog(val arrayListOf: ArrayList<String>) : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentDialogChangeBinding
    private var adapter = DialogAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDialogChangeBinding.inflate(layoutInflater, container, false)
        binding.rvChange.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvChange.adapter = adapter
        adapter.dataset = arrayListOf
        binding.fragmentDialogChangeRoot.setOnClickListener(this)
        return binding.root
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fragment_dialog_change_root-> requireActivity().onBackPressed()
        }
    }
}

class DialogAdapter : RecyclerView.Adapter<DialogAdapter.ViewHolder>() {

    lateinit var context: Context
    var dataset: ArrayList<String> = arrayListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.change_dialog_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.change_dialog_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        println("SHOWING ********************************** SHOWING")
        holder.text.text = datum
    }

    override fun getItemCount(): Int = dataset.size
}