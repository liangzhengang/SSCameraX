package com.senssun.camera

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.Executors

class CameraBuilder(fragment: Fragment?, activity: AppCompatActivity?) {


    private var imageCapture: ImageCapture =
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    private var takeCallback: TakeCallback? = null
    fun callback(callback: TakeCallback): CameraBuilder {
        takeCallback = callback
        return this
    }

    fun setCaptureMode() {

    }

    fun start() {
        imageCapture.takePicture(executors, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer[bytes]
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                takeCallback?.onSuccess(image = image, bitmap = bitmap)

            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                takeCallback?.onError(exception)
            }
        })
    }

    private val executors = Executors.newSingleThreadExecutor()
    private var camera: Camera? = null
    private lateinit var cameraProvider: ProcessCameraProvider

    init {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(fragment?.requireContext() ?: activity!!)
        // Set up the capture use case to allow users to take photos.
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider


            // Select lensFacing depending on the available cameras
            val lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Enable or disable switching between cameras
//            updateCameraSwitchButton()

            // Build and bind the camera use cases
//            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(fragment?.requireContext() ?: activity!!))
        cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                fragment ?: activity!!, CameraSelector.DEFAULT_BACK_CAMERA, imageCapture
            )

            // Attach the viewfinder's surface provider to preview use case

        } catch (exc: Exception) {
//            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

}