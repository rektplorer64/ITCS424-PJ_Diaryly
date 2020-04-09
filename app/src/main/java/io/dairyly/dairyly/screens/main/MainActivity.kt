package io.dairyly.dairyly.screens.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.dairyly.dairyly.R
import io.dairyly.dairyly.utils.viewModelInjectionHelper
import io.dairyly.dairyly.viewmodels.DiaryDateViewModel
import io.dairyly.dairyly.viewmodels.DiaryViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModelInjectionHelper<DiaryViewModel>(this, 1)
        viewModelInjectionHelper<DiaryDateViewModel>(this, 1)

    }
}
