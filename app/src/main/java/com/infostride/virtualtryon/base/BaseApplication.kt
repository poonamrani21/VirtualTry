package com.infostride.virtualtryon.base

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

/***
 * Created by poonam on 23rd Jan 2023
 */
class BaseApplication:Application(){
    override fun onCreate() {
        super.onCreate()
        //Check OpenCV installation
        if (OpenCVLoader.initDebug())
            Log.d( "OpenCVLoader: ", "success")
        else

            Log.d( "OpenCVLoader: ", "failure")
    }
}