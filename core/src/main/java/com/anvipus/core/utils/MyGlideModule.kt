package com.anvipus.core.utils

import android.content.Context
import android.util.Log
import com.anvipus.core.BuildConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class MyGlideModule : AppGlideModule(){
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        if(BuildConfig.DEBUG)builder.setLogLevel(Log.ERROR)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        //val client = AppInjector.component.okhttpClient()
        val client = UnsafeOkHttpClient.unsafeOkHttpClient
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
    }
}