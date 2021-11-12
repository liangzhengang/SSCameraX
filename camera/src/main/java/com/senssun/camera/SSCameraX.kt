package com.senssun.camera

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

object SSCameraX {
    @JvmStatic
    fun with(fragment:Fragment?=null, activity: AppCompatActivity?=null) = CameraBuilder(fragment,activity)
}