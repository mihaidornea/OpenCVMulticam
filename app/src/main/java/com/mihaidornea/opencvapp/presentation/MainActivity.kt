package com.mihaidornea.opencvapp.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mihaidornea.opencvapp.BR
import com.mihaidornea.opencvapp.MainActivityBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    private val viewModel by viewModel<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater).also {
            it.lifecycleOwner = this
            it.setVariable(BR.viewModel, viewModel)
        }
        setContentView(binding.root)
    }

    /**
     * A native method that is implemented by the 'opencvapp' native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): String

    companion object {
        // Used to load the 'opencvapp' library on application startup.
        init {
            System.loadLibrary("opencvapp")
        }
    }
}