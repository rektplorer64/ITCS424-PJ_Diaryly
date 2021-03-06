package io.dairyly.dairyly.screens.oobe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.dairyly.dairyly.R
import io.dairyly.dairyly.viewmodels.RegisterViewModel


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Diaryly)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ViewModelProvider(this).get(
                RegisterViewModel::class.java)
    }
}

