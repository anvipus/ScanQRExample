package com.anvipus.explore.di.module

import android.app.Application
import com.anvipus.explore.api.GeneralApi
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Provides
    @Singleton
    fun provideHttpClient(application: Application): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val builder = OkHttpClient.Builder()

        builder.addInterceptor(interceptor)
        builder.addInterceptor(
            ChuckerInterceptor.Builder(application.applicationContext)
                .collector(ChuckerCollector(application.applicationContext))
                .maxContentLength(250000L)
                .redactHeaders(emptySet())
                .alwaysReadResponseBody(false)
                .build()
        )

        return builder
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .connectTimeout(40, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .cache(null)
            .build()
    }

    @Provides
    @Singleton
    fun provideServiceBuilder(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

//    @Provides
//    @Singleton
//    fun provideAuthApi(builder: Retrofit): AuthApi = builder.create(AuthApi::class.java)
}