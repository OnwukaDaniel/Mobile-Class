package com.iodaniel.mobileclass.util.uistate_manager

import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.iodaniel.mobileclass.util.Dialogs
import com.iodaniel.mobileclass.viewModel.UIStateViewModel
import com.iodaniel.mobileclass.viewModel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UIStateDialogs(val uIStateViewModel: UIStateViewModel, val context: Context, val viewLifecycleOwner: LifecycleOwner, val activity: Activity) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun uiState(fetchedData: Boolean, dialogs: Dialogs) {
        var dialog = Dialog(context)
        if (!fetchedData) uIStateViewModel.setUIState(UiState.stateLoading) else uIStateViewModel.setUIState(UiState.stateData)
        uIStateViewModel.uIState.observe(viewLifecycleOwner) {
            when (it) {
                UiState.stateData -> {
                    dialog.dismiss()
                }
                UiState.stateLoading -> {
                    dialog.dismiss()
                    dialog = dialogs.circularProgressDialog(text = "Please wait", activity = activity)
                }
                UiState.stateNoData -> {
                    dialog.dismiss()
                }
                UiState.stateNetworkError -> {
                    dialog.dismiss()
                    dialog = dialogs.networkErrorDialog(text = "No network connection", activity = activity)
                }
                UiState.stateSuccess -> {
                    dialog.dismiss()
                    dialog = dialogs.successDialog(text = "Success", activity = activity)
                }
            }
        }
        scope.launch(Dispatchers.Main) {
            delay(10_000)
            if (!fetchedData) uIStateViewModel.setUIState(UiState.stateNetworkError) else uIStateViewModel.setUIState(UiState.stateData)
        }
    }

}