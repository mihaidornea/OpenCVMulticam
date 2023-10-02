package com.mihaidornea.opencvapp.shared.utils.views.gles3

import android.view.Surface

object GLES3JNILib {
    external fun nativeOnStart()
    external fun nativeOnResume()
    external fun nativeOnPause()
    external fun nativeOnStop()
    external fun nativeSetSurface(surface: Surface?)
    external fun nativeSetExternalSurface(surface: Surface?)
}

