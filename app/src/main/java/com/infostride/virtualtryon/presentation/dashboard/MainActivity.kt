package com.infostride.virtualtryon.presentation.dashboard

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.databinding.ActivityMainBinding
import com.infostride.virtualtryon.presentation.ui.PreviewCamera
import com.infostride.virtualtryon.util.Constant.CAMERA_TYPE
import com.infostride.virtualtryon.util.Constant.GENDER_TYPE
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

/****
 * Created by poonam Rani on 23 Jan 2023
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                INCOMPATIBLE_MANAGER_VERSION -> return
                INIT_FAILED -> return
                INSTALL_CANCELED -> return
                MARKET_ERROR -> return
                else -> super.onManagerConnected(status)
            }
        }
    } //End mLoaderCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Replace CameraPreview fragment
        intent?.also {
            val bundle = Bundle()
            bundle.putString(GENDER_TYPE, intent.getStringExtra(GENDER_TYPE))
            bundle.putString(CAMERA_TYPE, intent.getStringExtra(CAMERA_TYPE))
             val previewCameraFragment= PreviewCamera()
            previewCameraFragment.arguments=bundle
            savedInstanceState ?: supportFragmentManager.beginTransaction().replace(R.id.FrameContainer_fit_preview, previewCameraFragment).commit()
        }

    }

    override fun onResume() {
            super.onResume()
            if(OpenCVLoader.initDebug())
            {
                Log.i("Test Model -- ", "OpenCV initialize success")
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
            }
            else Log.i("Test Model -- ", "OpenCV initialize failed")
        } //End onResume

}