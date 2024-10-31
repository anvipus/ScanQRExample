package com.anvipus.core.utils

import android.content.Intent

interface ActivityResultListener {
    fun onSuccessGetResult(data: Intent?)

    fun onCancel()

    fun onForgotPin()
}