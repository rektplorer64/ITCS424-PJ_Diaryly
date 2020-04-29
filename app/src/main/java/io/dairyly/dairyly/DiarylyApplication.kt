package io.dairyly.dairyly

import androidx.multidex.MultiDexApplication
import com.jakewharton.threetenabp.AndroidThreeTen
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins

class DiarylyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())
    }

}