package com.anvipus.explore.di

import android.app.Application
import com.anvipus.explore.App
import com.anvipus.explore.di.module.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        MainActivityModule::class,
        ServiceModule::class]
)
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: App)
    fun okhttpClient(): OkHttpClient
}