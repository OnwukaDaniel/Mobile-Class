package com.iodaniel.mobileclass.util.dialog_fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.iodaniel.mobileclass.databinding.ProgressDialogBinding

class MyDialogFragment: DialogFragment() {
    var type = 0
    private lateinit var binding : ProgressDialogBinding

    fun newInstance(type: Int = 0): MyDialogFragment {
        val f = MyDialogFragment()
        val bundle = Bundle()
        bundle.putInt("type", type)
        f.arguments = bundle
        return f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //println("******************************************** ${requireArguments().isEmpty}")
        //if (requireArguments().containsKey("type")){ type = requireArguments().getInt("type") }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ProgressDialogBinding.inflate(inflater, container, false)
        binding.root.background = ColorDrawable(Color.TRANSPARENT)
        return binding.root
    }
}