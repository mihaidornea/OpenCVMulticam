package com.mihaidornea.opencvapp.presentation.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.renderscript.Toolkit
import com.google.android.renderscript.YuvFormat
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.mihaidornea.opencvapp.shared.base.BaseViewModel
import com.mihaidornea.opencvapp.shared.utils.rotateBitmap
import org.koin.core.component.KoinComponent
import java.nio.ByteBuffer

class ExternalCameraViewModel : BaseViewModel(), KoinComponent {

    private val _processedImageBitmap = MutableLiveData<Bitmap>()
    val processedImageBitmap: LiveData<Bitmap> = _processedImageBitmap

    val previewDataCallback = object : IPreviewDataCallBack {
        override fun onPreviewData(
            data: ByteArray?,
            width: Int,
            height: Int,
            format: IPreviewDataCallBack.DataFormat
        ) {
            data?.let { bytes ->
                val image = if (format == IPreviewDataCallBack.DataFormat.NV21) {
                    Toolkit.yuvToRgb(bytes, width, height, YuvFormat.NV21)
                } else {
                    bytes
                }
                processImage(image, width, height)
            }
        }
    }

    private fun processImage(image: ByteArray, width: Int, height: Int) {
        val resultBitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
        val processedByteArray = processExternalCameraFrame(image, width, height)
        resultBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(processedByteArray))
        _processedImageBitmap.postValue(resultBitmap)
    }

    private external fun processExternalCameraFrame(
        data: ByteArray,
        width: Int,
        height: Int
    ): ByteArray

}

