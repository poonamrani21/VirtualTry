package com.infostride.virtualtryon.presentation.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.infostride.virtualtryon.R
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.max


// TODO: 09/02/23 file need to update

class PreviewCamera: Fragment(), ActivityCompat.OnRequestPermissionsResultCallback
{
   private val TAG = "C-PREVIEWMANAGER: " //log TAG
    /** A shape for extracting frame data.   */
    private  val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080
    private val HANDLE_THREAD_NAME = "CameraBackground"

    private val lock = Any()
    private var runClassifier = false
    private  var autoFitTextureView: AutoFitTextureView? = null
    private  var autoFitFrameLayout: AutoFitFrameLayout? = null
    private  var drawView: DrawView? = null //to place outfit on user
    private var classifier: Classifier? = null
    private  var imageReader: ImageReader? = null
    /** The [android.util.Size] of camera preview.  */
    private  var previewSize: Size? = null
    private var cameraId: String? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private  val cameraOpenCloseLock = Semaphore(1)
    /** A [CameraCaptureSession] for camera preview.   */
    private var captureSession: CameraCaptureSession? = null
    /** A reference to the opened [CameraDevice].    */
    private var cameraDevice: CameraDevice? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null

    /****
     * A TextureView can be used to display a content stream, such as that coming from a camera preview, a video, or an OpenGL scene.
     * Unlike SurfaceView, TextureView does not create a separate window but behaves as a regular View.
     * This key difference allows a TextureView to have translucency, arbitrary rotations, and complex clipping.
     * For example, you can make a TextureView semi-translucent by calling myView.setAlpha(0.5f)
     */
    private val surfaceTextureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                try {
                    openCamera(width, height)
                    Log.d(TAG, " camera has opened. . .")
                } catch (e: CameraAccessException) { Log.d(TAG, " camera can not opened ![STL]") }
            }

            override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) { configureTransform(width, height) }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean { return true }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        } //end surfaceTextureListener


    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice = camera
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
            val activity: Activity = requireActivity()
            activity.finish()
        }
    } //end stateCallback



    val captureCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
                //testing
            }
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) { //testing
             }
        }
    /**
     * Creates a new [CameraCaptureSession] for camera preview.
     */
    private fun createCameraPreviewSession() {
        try {
            val texture = autoFitTextureView!!.surfaceTexture
            texture!!.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)
            val surface = Surface(texture)
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder!!.addTarget(surface)

            cameraDevice!!.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) { return }
                        captureSession = session
                        try {
                            previewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            previewRequest = previewRequestBuilder!!.build()
                            captureSession!!.setRepeatingRequest(previewRequest!!, captureCallback, backgroundHandler)
                        } catch (_: CameraAccessException) {
                            //testing
                        }
                    }
                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {} }, null) //End cameraDevice.createCaptureSession
               Log.d(TAG, " camera preview has started. . .")
        } catch (e: CameraAccessException) { Log.d(TAG, " preview session can not be created! [Camera Access Exception]") } //End catch block
    } //End createCameraPreviewSession

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.activity_main, container, false) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        autoFitFrameLayout = view.findViewById<View>(R.id.autofitFrameLayout_fit_preview) as AutoFitFrameLayout
        autoFitTextureView = view.findViewById<View>(R.id.autoFitTextureView_fit_preview) as AutoFitTextureView
        drawView = view.findViewById<View>(R.id.drawView_fit_preview) as DrawView
    }

   override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        classifier = Classifier(requireActivity())
        if (drawView != null) {
            drawView!!.setImgSize(classifier!!.imageSizeX, classifier!!.imageSizeY)
        }
        Log.d(TAG, " activity has been created successfully. . .")
        startBackgroundThread()
    }

   override fun onResume() {
        super.onResume()
        startBackgroundThread()
         if (autoFitTextureView!!.isAvailable) try { openCamera(autoFitTextureView!!.width, autoFitTextureView!!.height) } catch (e: CameraAccessException) { Log.d(TAG, " camera can not be opened [CameraAccessException]") }
         else autoFitTextureView!!.surfaceTextureListener = surfaceTextureListener
    }
    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onDestroy() {
        classifier!!.close()
        super.onDestroy()
    }


    //Classification
    ///////////////////////////7///////////////////////
    private val periodicClassify: Runnable = object : Runnable {
        override fun run() {
            synchronized(lock) { if (runClassifier) { classifyFrame() } }
            backgroundHandler!!.post(this)
        }
    }


    private fun classifyFrame() {
        if (classifier == null || activity == null || cameraDevice == null) {
            Log.d(TAG, " frame can not be classified due to null element(s) !")
            return
        } else {
            val bmp = autoFitTextureView!!.getBitmap(classifier!!.imageSizeX, classifier!!.imageSizeY)
            classifier!!.classifyFrame(bmp!!)
            bmp.recycle()
            drawView!!.setDrawPoint(classifier!!.mPrintPointArray!!, 0.5f)
            activity?.runOnUiThread { drawView!!.invalidate() }
            Log.d(TAG, " frame classified. . .")
        }
    }

    //THREADS
    /////////////////////////////////////////////
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(HANDLE_THREAD_NAME)
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
        synchronized(lock) { runClassifier = true }
        backgroundHandler!!.post(periodicClassify)
        Log.d(TAG, " background thread has started. . .")
    }

    private fun stopBackgroundThread() {
        backgroundThread!!.quitSafely()
        try {
            backgroundThread!!.join()
            backgroundThread = null
            backgroundHandler = null
            synchronized(lock) { runClassifier = false }
            Log.d(TAG, " background thread has successfully stopped. . .")
        } catch (e: InterruptedException) {
            Log.d(TAG, " background thread can not stop successfully !")
        }
    }


    //CAMERA MANIPULATION
    /**
     * Sets up member variables related to camera.
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val activity: Activity =requireActivity()
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) { continue }
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue
                val largest = Collections.max(listOf(*map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea())
                imageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, 2)
                val displayRotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) activity.display?.rotation else activity.windowManager.defaultDisplay.rotation
                val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) { swappedDimensions = true }
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) { swappedDimensions = true }
                    else -> Log.d(TAG, " display rotation is invalid ! [$displayRotation]")
                }
                val displaySize = Point()
                activity.windowManager.defaultDisplay.getSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth = displaySize.x
                var maxPreviewHeight = displaySize.y
                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) { maxPreviewWidth = MAX_PREVIEW_WIDTH }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) { maxPreviewHeight = MAX_PREVIEW_HEIGHT }
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest)
                val orientation: Int = resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    autoFitFrameLayout!!.setAspectRatio(previewSize!!.width, previewSize!!.height)
                    autoFitTextureView!!.setAspectRatio(previewSize!!.width, previewSize!!.height)
                   drawView!!.setAspectRatio(previewSize!!.width, previewSize!!.height)
                } else {
                   autoFitFrameLayout!!.setAspectRatio(previewSize!!.height, previewSize!!.width)
                   autoFitTextureView!!.setAspectRatio(previewSize!!.height, previewSize!!.width)
                   drawView!!.setAspectRatio(previewSize!!.height, previewSize!!.width)
                }
                this.cameraId = cameraId
                Log.d(TAG, " setUpCameraOutputs has successfully. . .")
                return
            }
        } //End try
        catch (e: CameraAccessException) {
            Log.d(TAG, " setUpCameraOutputs has not successfully due to CameraAccessException!")
        } catch (e: NullPointerException) {
            Log.d(TAG, " setUpCameraOutputs has not successfully due to NullPointerException!")
        }
    } //End setUpCameraOutputs

    // From assets

    @SuppressLint("MissingPermission")
    @Throws(CameraAccessException::class)
    private fun openCamera(width: Int, height: Int) {
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val activity: Activity = requireActivity()
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) { throw RuntimeException("Time out waiting to lock camera opening.") }
            manager.openCamera(cameraId!!, stateCallback, backgroundHandler)
        } catch (e: InterruptedException) { throw RuntimeException("Interrupted while trying to lock camera opening.", e)}
        Log.d(TAG, " camera has opened successfully. . .")
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            if (captureSession != null) {
                captureSession!!.close()
                captureSession = null
            }
            if (cameraDevice != null) {
                cameraDevice!!.close()
                cameraDevice = null
            }
            if (imageReader != null) {
                imageReader!!.close()
                imageReader = null
            }
            Log.d(TAG, " camera has closed successfully...")
        } catch (e: InterruptedException) { throw RuntimeException("Interrupted while trying to lock camera closing.", e) } finally { cameraOpenCloseLock.release() }
    }

    private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int, textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size? {
        val bigEnough = ArrayList<Size>()
        val notBigEnough = ArrayList<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) bigEnough.add(option) else notBigEnough.add(option)
            } //end if
        } //end for loop
        if (bigEnough.size > 0) {
            Log.d(TAG, " optimal size has found. . .[b]")
            return Collections.min(bigEnough, CompareSizesByArea())
        }
        return if (notBigEnough.size > 0) {
            Log.d(TAG, " optimal size has found. . . [nb]")
            Collections.max(notBigEnough, CompareSizesByArea())
        } else {
            Log.d(TAG, " optimal size can not found !")
            choices[0]
        }
    } //End chooseOptimalSize


    private class CompareSizesByArea : Comparator<Size>{
        override fun compare(o1: Size?, o2: Size?): Int { return java.lang.Long.signum((o1!!.width * o1.height - o2!!.width * o2.height).toLong()) }
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity: Activity = requireActivity()
        if (autoFitTextureView == null || previewSize == null || activity == null) { return }
        val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) activity.display?.rotation else activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize!!.height.toFloat(), previewSize!!.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = max(viewHeight / previewSize!!.height, viewWidth / previewSize!!.width)
            matrix.postScale(scale.toFloat(), scale.toFloat(), centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (rotation == Surface.ROTATION_180) matrix.postRotate(180f, centerX, centerY)
        autoFitTextureView!!.setTransform(matrix)
    } //End configureTransform
}//End Class
