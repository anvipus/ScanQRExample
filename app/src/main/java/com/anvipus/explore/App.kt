package com.anvipus.explore

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.anvipus.explore.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class App : Application(), HasAndroidInjector {
    companion object{
        const val TAG:String = "App"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate() {
        super.onCreate()
        context = this
        AppInjector.init(this)
    }
}