package com.mihaidornea.opencvapp.presentation.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.renderscript.Toolkit
import com.google.android.renderscript.YuvFormat
import com.mihaidornea.opencvapp.R
import com.mihaidornea.opencvapp.shared.base.BaseViewModel
import com.mihaidornea.opencvapp.shared.utils.YuvByteBuffer
import com.mihaidornea.opencvapp.shared.utils.rotateBitmap
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val CAMERA_THREAD = "CameraThread"
private const val IMAGE_READER_THREAD = "ImageReaderThread"

class CameraViewModel : BaseViewModel(), KoinComponent {

    private val cameraManager by inject<CameraManager>()
    private val cameraId by lazy { getBackJPEGCameraId(cameraManager) }
    private val cameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(cameraId)
    }

    private val cameraThread = HandlerThread(CAMERA_THREAD).apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var camera: CameraDevice
    private lateinit var session: CameraCaptureSession

    private var imageReader: ImageReader? = null
    private val imageReaderThread = HandlerThread(IMAGE_READER_THREAD).apply { start() }
    private val imageReaderHandler = Handler(imageReaderThread.looper)

    private var yuvBits: ByteBuffer? = null

    private val _processedImageBitmap = MutableLiveData<Bitmap>()
    val processedImageBitmap: LiveData<Bitmap> = _processedImageBitmap

    fun initializeCamera() {
        initialiseImageReader()
        viewModelScope.launch {
            imageReader?.let { imageReader ->
                camera = openCamera()
                session = createCaptureSession(camera, listOf(imageReader.surface))
                setRepeatingRequest(imageReader.surface)
            }
        }
    }

    private fun initialiseImageReader() {
        cameraCharacteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )?.getOutputSizes(ImageFormat.YUV_420_888)?.find { it.height * it.width == 1280 * 720 }
            ?.let { size ->
                imageReader = ImageReader.newInstance(
                    size.width,
                    size.height,
                    ImageFormat.YUV_420_888,
                    IMAGE_BUFFER_SIZE
                )
            }
    }

    @SuppressLint("MissingPermission")
    suspend fun openCamera(): CameraDevice = suspendCancellableCoroutine { cont ->
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) = cont.resume(camera)

            override fun onDisconnected(camera: CameraDevice) {
                _baseCmd.postValue(BaseCommand.ShowToastById(R.string.camera_disconnected))
            }

            override fun onError(camera: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                _baseCmd.postValue(BaseCommand.ShowToast(msg))
            }
        }, cameraHandler)
    }

    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
    ): CameraCaptureSession = suspendCoroutine { cont ->
        try {
            device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    _baseCmd.postValue(BaseCommand.ShowToastById(R.string.camera_configuration_failed))
                }
            }, cameraHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setRepeatingRequest(surface: Surface) {
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            image?.let {
                convertImageToRGB(it)
                it.close()
            }
        }, imageReaderHandler)
        val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            addTarget(surface)
            imageReader?.let { imageReader ->
                addTarget(imageReader.surface)
            }
        }
        session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)
    }

    private fun convertImageToRGB(image: Image) {
        val imageReader = imageReader ?: return
        val yuvBuffer = YuvByteBuffer(image, yuvBits)
        yuvBits = yuvBuffer.buffer
        yuvBits?.let { buffer ->
            val data = Toolkit.yuvToRgb(
                buffer.array(),
                imageReader.width,
                imageReader.height,
                YuvFormat.NV21
            )
            val resultBitmap = Bitmap.createBitmap(
                imageReader.width, imageReader.height, Bitmap.Config.ARGB_8888
            )

            val processedByteArray = processFrame(data, image.width, image.height)
            resultBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(processedByteArray))

            _processedImageBitmap.postValue(resultBitmap.rotateBitmap(90f))
        }
    }

    fun showCameraPermissionRationale(action: (View) -> Unit) {
        _baseCmd.value = BaseCommand.ShowSnackbarById(
            stringId = R.string.camera_permission_rationale,
            actionStringId = R.string.camera_permission_rationale_action,
            action = action
        )
    }

    fun displayCameraPermissionRequired() {
        _baseCmd.value = BaseCommand.ShowToastById(
            stringId = R.string.camera_permission_rationale
        )
    }

    private fun getBackJPEGCameraId(cameraManager: CameraManager): String {
        return cameraManager.cameraIdList.filter { cameraId ->
            cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                ?.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE)
                ?: false
        }.first { cameraId ->
            cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    fun closeCamera() {
        session.close()
        camera.close()
        imageReader?.close()
    }

    fun quitCameraThread() {
        cameraThread.quitSafely()
    }

    private external fun processFrame(data: ByteArray, width: Int, height: Int): ByteArray

    companion object {
        private const val IMAGE_BUFFER_SIZE: Int = 3
    }
}