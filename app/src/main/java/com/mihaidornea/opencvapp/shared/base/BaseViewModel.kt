package com.mihaidornea.opencvapp.shared.base

import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

    protected val _baseCmd = MutableLiveData<BaseCommand>()
    val baseCmd: LiveData<BaseCommand> = _baseCmd

    sealed class BaseCommand {
        class ShowToastById(@StringRes val stringId: Int) : BaseCommand()
        class ShowToast(val message: String) : BaseCommand()
        class ShowSnackbarById(
            @StringRes val stringId: Int,
            @StringRes val actionStringId: Int? = null,
            val action: ((View) -> Unit)? = null
        ) : BaseCommand()

        class ShowSnackbar(
            val message: String,
            val actionString: String? = null,
            val action: ((View) -> Unit)? = null
        ) : BaseCommand()
    }
}