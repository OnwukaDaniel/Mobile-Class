package com.iodaniel.mobileclass.util.dialog_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.iodaniel.mobileclass.databinding.InputFragmentBinding
import com.iodaniel.mobileclass.viewModel.MessageFragmentViewModel

class InputFragment : DialogFragment() {
    private lateinit var binding: InputFragmentBinding

    private val viewModel: MessageFragmentViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = InputFragmentBinding.inflate(inflater, container, false)
        viewModel.cancelFunction.observe(viewLifecycleOwner) { func ->
            binding.fragmentInputCancel.setOnClickListener {
                func.first
                if (func.second) this.dismiss()
            }
        }
        viewModel.okFunction.observe(viewLifecycleOwner) { func ->
            binding.fragmentInputOk.setOnClickListener {
                viewModel.setEditTextInput(binding.fragmentInputEditText.text.trim().toString())
                this.dismiss()
            }
        }
        viewModel.editTextInput.observe(viewLifecycleOwner) {
            binding.fragmentInputEditText.setText(it)
        }
        viewModel.cancelText.observe(viewLifecycleOwner) {
            binding.fragmentInputCancel.text = it
        }
        viewModel.okText.observe(viewLifecycleOwner) {
            binding.fragmentInputOk.text = it
        }
        viewModel.displayText.observe(viewLifecycleOwner) {
            binding.fragmentInputMessage.text = it
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