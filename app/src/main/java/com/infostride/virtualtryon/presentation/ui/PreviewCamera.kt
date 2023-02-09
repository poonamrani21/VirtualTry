package com.infostride.virtualtryon.presentation.ui

import android.app.Activity
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.infostride.virtualtryon.R
import java.util.concurrent.Semaphore
import kotlin.math.max
// TODO: 09/02/23 file need to update

class PreviewCamera : Fragment(){
    private var autoFitTextureView: AutoFitTextureView? = null
    private var autoFitFrameLayout: AutoFitFrameLayout? = null
    private var drawView: DrawView? = null
    private val classifier: Classifier? = null

    private val TAG = "C-PREVIEWMANAGER: " //log TAG
    private val HANDLE_THREAD_NAME = "CameraBackground"
    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080

    private val lock = Any()
    private val runClassifier = false
    private val imageReader: ImageReader? = null
    private val previewSize: Size? = null
    private val cameraId: String? = null
    private val backgroundHandler: Handler? = null
    private val backgroundThread: HandlerThread? = null
    private val cameraOpenCloseLock = Semaphore(1)
    private val captureSession: CameraCaptureSession? = null
    private val cameraDevice: CameraDevice? = null
    private val previewRequestBuilder: CaptureRequest.Builder? = null
    private val previewRequest: CaptureRequest? = null


    private val surfaceTextureListener: TextureView.SurfaceTextureListener =
        object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                try {
                 //   openCamera(width, height)
                    Log.d(TAG, " camera has opened. . .")
                } catch (e: CameraAccessException) {
                    Log.d(TAG, " camera can not opened ![STL]")
                }
            }

            override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
                //configureTransform(width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        } //end surfaceTextureListener


    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity: Activity? = activity
        if (autoFitTextureView == null || previewSize == null || activity == null) {
            return
        }

        val rotation = activity.windowManager.defaultDisplay.rotation
        //val display = activity.getSystemService(DisplayManager::class.java).defaultDisplay.rotation

       // val defaultDisplay = getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY).displayId

        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize!!.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = max(viewHeight / previewSize.height, viewWidth / previewSize.width)
            matrix.postScale(scale.toFloat(), scale.toFloat(), centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (rotation == Surface.ROTATION_180) {
            matrix.postRotate(180f, centerX, centerY)
        }
        autoFitTextureView!!.setTransform(matrix)
    } //End configureTransform

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_main, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        autoFitFrameLayout = view.findViewById<View>(R.id.autofitFrameLayout_fit_preview) as AutoFitFrameLayout
        autoFitTextureView = view.findViewById<View>(R.id.autoFitTextureView_fit_preview) as AutoFitTextureView
        drawView = view.findViewById<View>(R.id.drawView_fit_preview) as DrawView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

companion object{

    fun newInstance(): PreviewCamera? {
        return PreviewCamera()
    }
}

}