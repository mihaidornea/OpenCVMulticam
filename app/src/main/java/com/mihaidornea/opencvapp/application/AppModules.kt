package com.mihaidornea.opencvapp.application

import android.content.Context
import android.hardware.camera2.CameraManager
import com.mihaidornea.opencvapp.presentation.MainActivityViewModel
import com.mihaidornea.opencvapp.presentation.camera.CameraViewModel
import com.mihaidornea.opencvapp.presentation.camera.ExternalCameraViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModules {

    private val viewModels = module {
        viewModel { MainActivityViewModel() }
        viewModel { CameraViewModel() }
        viewModel { ExternalCameraViewModel() }
    }

    private val application = module {
        single { androidContext() }
    }

    private val cameraModule = module {
        single<CameraManager> { get<Context>().getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    }

    val modules = listOf(viewModels, application, cameraModule)
}