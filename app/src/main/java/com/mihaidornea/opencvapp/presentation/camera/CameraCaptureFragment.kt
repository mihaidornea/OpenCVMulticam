package com.mihaidornea.opencvapp.presentation.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.usb.UsbDevice
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.camera.CameraUVC
import com.mihaidornea.opencvapp.CameraFragmentBinding
import com.mihaidornea.opencvapp.R
import com.mihaidornea.opencvapp.shared.base.BaseCameraFragment
import com.mihaidornea.opencvapp.shared.utils.views.gles3.GLES3JNILib
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

private const val PACKAGE_SCHEME = "package"

class CameraCaptureFragment : BaseCameraFragment<CameraFragmentBinding, CameraViewModel>(
    R.layout.fr_camera
), KoinComponent {

    override val viewModel: CameraViewModel by viewModel()
    private val externalCameraViewModel: ExternalCameraViewModel by viewModel()
    private lateinit var rxPermissions: RxPermissions

    private val hasRequestPermissionList by lazy {
        mutableListOf<MultiCameraClient.ICamera>()
    }

    override fun setupViews() {
        rxPermissions = RxPermissions(this)
        setupSurface(binding?.cameraSv, viewModel.processedImageBitmap)
        setupSurface(binding?.externalCameraSv, externalCameraViewModel.processedImageBitmap)
        setupOpenGLSurface(binding?.openglSv, false)
        setupOpenGLSurface(binding?.externalOpenglSv, true)
    }

    private fun setupOpenGLSurface(surface: SurfaceView?, isExternal: Boolean) {
        surface?.apply {
            setZOrderMediaOverlay(true)
            holder.setFormat(PixelFormat.TRANSPARENT)
        }
        surface?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) = Unit

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                if (isExternal)
                    GLES3JNILib.nativeSetExternalSurface(holder.surface)
                else
                    GLES3JNILib.nativeSetSurface(holder.surface)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                if (isExternal)
                    GLES3JNILib.nativeSetExternalSurface(null)
                else
                    GLES3JNILib.nativeSetSurface(null)
            }
        })
    }

    private fun setupSurface(surface: SurfaceView?, bitmapFeed: LiveData<Bitmap>) {
        surface?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                binding?.root?.post { checkPermissions() }
                bitmapFeed.observe(viewLifecycleOwner) { bitmap ->
                    val canvas = holder.lockCanvas()
                    canvas?.let {
                        it.drawColor(Color.BLACK)
                        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                        val destRect = Rect(0, 0, canvas.width, canvas.height)

                        canvas.drawBitmap(bitmap, srcRect, destRect, null)
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) = Unit

            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
        })
    }

    @SuppressLint("CheckResult")
    private fun requestPermissions() {
        rxPermissions
            .requestEach(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        initializeCamera()
                    }

                    permission.shouldShowRequestPermissionRationale -> displayPermissionRationale()
                    else -> viewModel.displayCameraPermissionRequired()
                }
            }
    }

    private fun checkPermissions() {
        if (rxPermissions.isGranted(Manifest.permission.CAMERA)) {
            initializeCamera()
        } else {
            requestPermissions()
        }
    }

    private fun displayPermissionRationale() {
        viewModel.showCameraPermissionRationale {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                it.setData(Uri.fromParts(PACKAGE_SCHEME, activity?.packageName, null))
            })
        }
    }

    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        viewModel.initializeCamera()
    }


    override fun onStart() {
        super.onStart()
        GLES3JNILib.nativeOnStart()
    }

    override fun onStop() {
        super.onStop()
        GLES3JNILib.nativeOnStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeCamera()
        viewModel.quitCameraThread()
    }

    override fun onResume() {
        super.onResume()
        GLES3JNILib.nativeOnResume()
    }

    override fun onPause() {
        super.onPause()
        GLES3JNILib.nativeOnPause()
    }

    // External USB overrides
    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? = binding?.root

    override fun onCameraAttached(camera: MultiCameraClient.ICamera) {
    }

    override fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        return CameraUVC(ctx, device)
    }

    override fun onCameraConnected(camera: MultiCameraClient.ICamera) {
        camera.openCamera(
            null,
            null
        )
        camera.addPreviewDataCallBack(externalCameraViewModel.previewDataCallback)
        val device = camera.getUsbDevice()
        if (!hasPermission(device)) {
            hasRequestPermissionList.add(camera)
            requestPermission(device)
        }
    }

    override fun onCameraDetached(camera: MultiCameraClient.ICamera) {
        hasRequestPermissionList.remove(camera)
        camera.closeCamera()
    }

    override fun onCameraDisConnected(camera: MultiCameraClient.ICamera) {
        camera.closeCamera()
    }
}