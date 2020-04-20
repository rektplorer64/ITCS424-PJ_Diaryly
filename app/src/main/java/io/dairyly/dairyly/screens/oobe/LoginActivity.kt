package io.dairyly.dairyly.screens.oobe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.dairyly.dairyly.R
import io.dairyly.dairyly.viewmodels.RegisterViewModel
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Diaryly)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())

        ViewModelProvider(this).get(
                RegisterViewModel::class.java)
    }
}

