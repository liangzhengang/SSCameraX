package com.senssun.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy

interface TakeCallback {
    fun onSuccess(bitmap: Bitmap, image: ImageProxy)
    fun onError(exception: ImageCaptureException)
}