package com.mihaidornea.opencvapp.shared.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.base.MultiCameraFragment
import com.mihaidornea.opencvapp.BR
import com.mihaidornea.opencvapp.shared.base.BaseViewModel.BaseCommand.ShowSnackbar
import com.mihaidornea.opencvapp.shared.base.BaseViewModel.BaseCommand.ShowSnackbarById
import com.mihaidornea.opencvapp.shared.base.BaseViewModel.BaseCommand.ShowToast
import com.mihaidornea.opencvapp.shared.base.BaseViewModel.BaseCommand.ShowToastById
import com.mihaidornea.opencvapp.shared.utils.extensions.snackBar

abstract class BaseCameraFragment<BINDING : ViewDataBinding, VIEW_MODEL : BaseViewModel>(
@LayoutRes private val layoutRes: Int
) : MultiCameraFragment() {

    protected var binding: BINDING? = null
    private set
            protected abstract val viewModel: VIEW_MODEL


    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<BINDING>(
        layoutInflater,
        layoutRes,
        null,
        false
    ).also {
        it.lifecycleOwner = viewLifecycleOwner
        it.setVariable(BR.viewModel, viewModel)
        binding = it
    }.root

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeBaseCommands()
    }

    private fun observeBaseCommands() {
        viewModel.baseCmd.observe(viewLifecycleOwner) { command ->
            when (command) {
                is ShowToastById ->
                    Toast.makeText(context ?: return@observe, command.stringId, Toast.LENGTH_LONG)
                        .show()

                is ShowToast ->
                    Toast.makeText(context ?: return@observe, command.message, Toast.LENGTH_LONG)
                        .show()

                is ShowSnackbarById -> snackBar(
                    message = getString(command.stringId),
                    actionText = command.actionStringId?.let { getString (it) },
                    action = command.action
                )
                is ShowSnackbar -> snackBar(
                    message = command.message,
                    actionText = command.actionString,
                    action = command.action
                )
            }
        }
    }

    abstract fun setupViews()
}

