package com.mihaidornea.opencvapp.shared.utils.extensions

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.snackBar(message: String, actionText: String? = null,  action: ((View) -> Unit)? = {}) {
    this.view?.let {
        Snackbar.make(
            it,
            message,
            Snackbar.LENGTH_LONG
        ).setAction(actionText, action).show()
    }
}