package com.senssun.camera

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.internal.compat.workaround.TargetAspectRatio.RATIO_16_9
import androidx.camera.core.*
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.impl.ImageOutputConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

class CameraBuilder(val fragment: Fragment?, val activity: AppCompatActivity?) {


    private lateinit var imageCapture: ImageCapture
    private var takeCallback: TakeCallback? = null
    private var rotation = Surface.ROTATION_0
    private var ratio = RATIO_4_3

    private val executors = Executors.newSingleThreadExecutor()
    private var camera: Camera? = null
    private lateinit var cameraProvider: ProcessCameraProvider

    private var cameraSelector: CameraSelector? = null


    fun callback(callback: TakeCallback): CameraBuilder {
        takeCallback = callback
        return this
    }

    fun setCaptureMode() {

    }

    fun setTargetAspectRatio(@AspectRatio.Ratio aspectRatio: Int): CameraBuilder {

        ratio = aspectRatio
        return this
    }

    //@ImageOutputConfig.RotationValue
    fun setTargetRotation(@ImageOutputConfig.RotationValue rotation: Int): CameraBuilder {
        this.rotation = rotation
        return this
    }


    private var preview: Preview? = null

    fun setPreview(previewView: PreviewView) {
        preview = Preview.Builder().setTargetAspectRatio(ratio).setTargetRotation(rotation).build()
        preview?.setSurfaceProvider(previewView.getSurfaceProvider())
    }

    fun setCamera(cameraSelector: CameraSelector) {
        this.cameraSelector = cameraSelector
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

    fun bind(): CameraBuilder {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(fragment?.requireContext() ?: activity!!)
        // Set up the capture use case to allow users to take photos.
        cameraProviderFuture.addListener(Runnable {
            // CameraProvider
            // Select lensFacing depending on the available cameras
            cameraProvider = cameraProviderFuture.get()
            val lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }
            // CameraSelector
            cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            bindCamera()

        }, ContextCompat.getMainExecutor(fragment?.requireContext() ?: activity!!))
        return this
    }

    fun bindCamera() {

        cameraProvider.unbindAll()

        imageCapture =
            ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setIoExecutor(executors)
                // We request aspect ratio but no resolution to match preview config, but letting
                // CameraX optimize for whatever specific resolution best fits our use cases
                .setTargetAspectRatio(ratio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation).build()
        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            if (preview == null) {
                camera = cameraProvider.bindToLifecycle(
                    fragment ?: activity!!,
                    cameraSelector ?: CameraSelector.DEFAULT_FRONT_CAMERA,
                )
            } else {
                camera = cameraProvider.bindToLifecycle(
                    fragment ?: activity!!,
                    cameraSelector ?: CameraSelector.DEFAULT_FRONT_CAMERA,
                    imageCapture, preview
                )
            }


            // Attach the viewfinder's surface provider to preview use case

        } catch (exc: Exception) {
            exc.printStackTrace()
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