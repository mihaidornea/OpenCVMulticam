package com.mihaidornea.opencvapp.shared.utils

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.rotateBitmap(
    degrees: Float
): Bitmap {
    val matrix = Matrix()
    matrix.preRotate(degrees)
    return Bitmap.createBitmap(
        this,
        0,
        0,
        this.getWidth(),
        this.getHeight(),
        matrix,
        true
    )
}