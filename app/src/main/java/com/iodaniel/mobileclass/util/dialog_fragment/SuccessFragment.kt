package com.iodaniel.mobileclass.util.dialog_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.iodaniel.mobileclass.databinding.SuccessFragmentBinding
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel

class SuccessFragment: DialogFragment() {
    private lateinit var binding: SuccessFragmentBinding

    private val viewModel: MessageFragmentViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SuccessFragmentBinding.inflate(inflater, container, false)
        viewModel.okFunction.observe(viewLifecycleOwner) { func ->
            binding.successOk.setOnClickListener {
                this.dismiss()
            }
        }
        viewModel.okText.observe(viewLifecycleOwner) {
            binding.successOk.text = it
        }
        viewModel.displayText.observe(viewLifecycleOwner) {
            binding.successMessage.text = it
        }
        /*
    private val mfV: MessageFragmentViewModel by activityViewModels()

        val dialog = requireActivity().supportFragmentManager.findFragmentByTag("dialog")
        if (dialog != null) ft.remove(dialog)

    val mf = MessageFragment()
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        mfV.setDisplayText("Loren Ipsum")
        mf.show(ft, "dialog")*/
        return binding.root
    }
}