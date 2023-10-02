package com.mihaidornea.opencvapp.application

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OpenCVApp: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(AppModules.modules)
            androidContext(this@OpenCVApp)
        }
    }
}