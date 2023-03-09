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
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.infostride.virtualtryon.R
import com.infostride.virtualtryon.domain.model.CostumeDetails
import com.infostride.virtualtryon.domain.model.CostumeType
import com.infostride.virtualtryon.domain.model.Outfit
import com.infostride.virtualtryon.presentation.add_outfit.UploadUserCostumeActivity
import com.infostride.virtualtryon.presentation.dashboard.adapter.CostumeListAdapter
import com.infostride.virtualtryon.presentation.dashboard.adapter.CostumeTypeAdapter
import com.infostride.virtualtryon.util.*
import com.infostride.virtualtryon.util.Constant.BACK_CAMERA
import com.infostride.virtualtryon.util.Constant.men
import com.infostride.virtualtryon.util.Constant.women
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.max


/****
 * Created by poonam Rani on 23 Jan 2023
 */

// TODO: need to make this class correct

class PreviewCamera : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback,
    LifecycleObserver {
    private val TAG = "C-PREVIEWMANAGER: " //log TAG

    /** A shape for extracting frame data.   */
    private  val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080
    private val HANDLE_THREAD_NAME = "CameraBackground"
    /** Standard High Definition size for pictures and video */
    val SIZE_1080P: SmartSize = SmartSize(1920, 1080)

    private val lock = Any()
    private var runClassifier = false
    private var autoFitTextureView: AutoFitTextureView? = null
    private var autoFitFrameLayout: AutoFitFrameLayout? = null
    private var drawView: DrawView? = null //to place outfit on user
    private var classifier: Classifier? = null
    private var imageReader: ImageReader? = null

    /** The [android.util.Size] of camera preview.  */
    private var previewSize: Size? = null
    private var cameraId: String? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private val cameraOpenCloseLock = Semaphore(1)

    /** A [CameraCaptureSession] for camera preview.   */
    private var captureSession: CameraCaptureSession? = null

    /** A reference to the opened [CameraDevice].    */
    private var cameraDevice: CameraDevice? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null

    private lateinit var costumeListAdapter: CostumeListAdapter
    private lateinit var costumeTypeAdapter: CostumeTypeAdapter
    private lateinit var costumeList: ArrayList<CostumeDetails>

    private var costumeType = 1//for type
    private lateinit var genderType: String
    private  var cameraType = BACK_CAMERA
    private var categoryName: String? = null

    //Views instance to show over screen
    private lateinit var  rvCostume: RecyclerView
    private lateinit var rvCostumeType: RecyclerView
    private lateinit var outsideDetector: View
    private lateinit var btnUploadOutfit: AppCompatButton
    private lateinit var llSelectOutfit: RelativeLayout

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
                    Log.d(TAG, " onSurfaceTextureSizeChanged SurfaceTexture Width: $width and Height: $height ")
                } catch (e: CameraAccessException) {
                    Log.d(TAG, " camera can not opened ![STL]")
                }
            }

            override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, " onSurfaceTextureSizeChanged SurfaceTexture Width: $width and Height: $height ")
                configureTransform(width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                Log.d(TAG, "onSurfaceTextureUpdated: ")
            }
        }

    /***
     * [stateCallback] updates include notifications about the device completing startup
     * (allowing for createCaptureSession to be called),
     * about device disconnection or closure, and about unexpected device errors states.
     */
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
            requireActivity().finish()
        }
    }

    /***
     * [captureCallback] is providing a set of target output surfaces to createCaptureSession,
     * or by providing an android.hardware.camera2.params.InputConfiguration and
     * a set of target output surfaces to createReprocessableCaptureSession for a reprocessable capture session.
     */
    val captureCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) { //testing
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

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Log.d(TAG, "onConfigureFailed: ")
                    }
                }, null
            ) //End cameraDevice.createCaptureSession
            Log.d(TAG, " camera preview has started. . .")
        } catch (e: CameraAccessException) {
            Log.d(TAG, " preview session can not be created! [Camera Access Exception]")
        } //End catch block
    } //End createCameraPreviewSession

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        genderType = requireArguments().getString(Constant.GENDER_TYPE)!!
        cameraType = requireArguments().getString(Constant.CAMERA_TYPE)!!
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        autoFitFrameLayout = view.findViewById<View>(R.id.autofitFrameLayout_fit_preview) as AutoFitFrameLayout
        autoFitTextureView = view.findViewById<View>(R.id.autoFitTextureView_fit_preview) as AutoFitTextureView
        drawView = view.findViewById<View>(R.id.drawView_fit_preview) as DrawView
        rvCostumeType = view.findViewById<View>(R.id.rv_costume_type) as RecyclerView
        rvCostume = view.findViewById<View>(R.id.rv_costume) as RecyclerView
        btnUploadOutfit = view.findViewById<View>(R.id.btn_upload_outfit) as AppCompatButton
        outsideDetector = view.findViewById(R.id.outside_detector) as View
        llSelectOutfit = view.findViewById<View>(R.id.ll_select_outfit) as RelativeLayout
        //showHideViewOverCameraView()
        setOnClickListeners()
        showCostumeList()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        classifier = Classifier(requireActivity())
        if (drawView != null) {drawView!!.setImgSize(classifier!!.imageSizeX, classifier!!.imageSizeY)}
        Log.d(TAG, " activity has been created successfully. . .")
        startBackgroundThread()
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (autoFitTextureView!!.isAvailable) try {
            openCamera(autoFitTextureView!!.width, autoFitTextureView!!.height)
        } catch (e: CameraAccessException) {
            Log.d(TAG, " camera can not be opened [CameraAccessException]")
        }
        else autoFitTextureView!!.surfaceTextureListener = surfaceTextureListener
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onDestroy() {
        classifier!!.close()
        DrawView.currentOutfit =null
        super.onDestroy()
    }

    //Classification
    //////////////////////////////////////////////////
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


    /**
     * [setUpCameraOutputs] Sets up member variables related to camera.
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val activity: Activity = requireActivity()
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
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
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),rotatedPreviewWidth,rotatedPreviewHeight,maxPreviewWidth,maxPreviewHeight,largest)
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
        // Exception is handled, because to check whether
        // the camera resource is being used by another
        // service or not.
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            //cameraId= O means back camera unit,
            //cameraId= 1 means front camera unit
            manager.openCamera(cameraId!!, stateCallback, backgroundHandler)
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
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
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun chooseOptimalSize(
        choices: Array<Size>,
        textureViewWidth: Int,
        textureViewHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        aspectRatio: Size
    ): Size? {
        val bigEnough = ArrayList<Size>()
        val notBigEnough = ArrayList<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight)
                    bigEnough.add(option)
                else
                    notBigEnough.add(option)
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


    private class CompareSizesByArea : Comparator<Size> {
        override fun compare(o1: Size?, o2: Size?): Int {
            return java.lang.Long.signum((o1!!.width * o1.height - o2!!.width * o2.height).toLong())
        }
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity: Activity = requireActivity()
        if (autoFitTextureView == null || previewSize == null || activity == null) {
            return
        }
        val rotation =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) activity.display?.rotation
            else activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect =
            RectF(0f, 0f, previewSize!!.height.toFloat(), previewSize!!.width.toFloat())
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

    @SuppressLint("ClickableViewAccessibility")
    private fun showHideViewOverCameraView() {
        llSelectOutfit.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (rvCostume.visibility == View.VISIBLE) {
                        requireActivity().showToast("Full Camera view will visible here")
                        rvCostume.makeGone()
                       // llSelectOutfit.makeVisible()
                    } else {
                        requireActivity().showToast("Full Camera  view hidden")
                        llSelectOutfit.makeVisible()
                      //  rvCostume.makeVisible()
                    }
                }
            }
            true
        }
    }

    /***
     * [setOnClickListeners] for on click events
     */
    private fun setOnClickListeners() { btnUploadOutfit.setOnClickListener { requireActivity().launchActivity<UploadUserCostumeActivity>() } }

    /***
     * Show costume list as per selected gender
     */
    private fun showCostumeList() {
        val costumeTypeList = listOfCostumesType(genderType)
        categoryName = costumeTypeList[0].type
        listOfCostumes(costumeType, genderType)
        costumeTypeAdapter = CostumeTypeAdapter(costumeTypeList) {
            if (costumeType != it.id) {
                costumeType = it.id
                categoryName = it.type
                updateCostumeList()
                DrawView.currentOutfit =null
            }
        }
        rvCostumeType.adapter = costumeTypeAdapter
        costumeListAdapter = CostumeListAdapter(costumeList) { it, bitmapInstance ->
            ImageProcessor().extractOutfit(bitmapInstance, 15)
            val stream = ByteArrayOutputStream()
            bitmapInstance.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imgByte = stream.toByteArray()
            DrawView.currentOutfit = Outfit(category = it.category, image = imgByte)
            applyCostumeToModel()
        }
        rvCostume.adapter = costumeListAdapter
    }

    private fun applyCostumeToModel() { requireActivity().showToast(getString(R.string.costume_applied)) }

    /***
     * [updateCostumeList] will update
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateCostumeList() {
        costumeList.clear()
        listOfCostumes(costumeType, genderType)
        costumeListAdapter.notifyDataSetChanged()
    }

    /***
     * [listOfCostumesType] will get types of costume as per [genderType] -> 1 for [women] and 2 -> for [men]
     */
    private fun listOfCostumesType(genderType: String?): List<CostumeType> {
        val costumeTypeList = ArrayList<CostumeType>()
        when (genderType) {
            men -> {
                costumeTypeList.add(CostumeType(1, CostumeTypes.MEN_SHIRTS.dress))
                costumeTypeList.add(CostumeType(2, CostumeTypes.MEN_LONG_WEAR.dress))
                costumeTypeList.add(CostumeType(3, CostumeTypes.MEN_TROUSERS.dress))
                costumeTypeList.add(CostumeType(4, CostumeTypes.MEN_SHORTS.dress))
            }
            women -> {
                costumeTypeList.add(CostumeType(1, CostumeTypes.WOMEN_TOPS.dress))
                costumeTypeList.add(CostumeType(2, CostumeTypes.WOMEN_LONG_WEAR.dress))
                costumeTypeList.add(CostumeType(3, CostumeTypes.WOMEN_TROUSERS.dress))
                costumeTypeList.add(CostumeType(4, CostumeTypes.WOMEN_SHORTS.dress))
            }
        }
        return costumeTypeList
    }

    /**
     * [listOfCostumes] will get list of costumes as per [costumeType] and [genderType]
     * [costumeType] -> will get types from [CostumeTypes]
     * [genderType] -> 1 for [women] and 2 -> for [men]
     */
    private fun listOfCostumes(costumeType: Int, genderType: String?): ArrayList<CostumeDetails> {
        when (genderType) {
            men -> {
                when (costumeType) {
                    Constant.men_shirts -> {
                        costumeList.add(CostumeDetails(1, costumeType, getCategoryName(categoryName!!,genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_shirt1)))
                        costumeList.add(CostumeDetails(1, costumeType, getCategoryName(categoryName!!,genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_shirt2)))
                    }
                    Constant.men_long_wears -> {
                        costumeList.add(CostumeDetails(1, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_long_wear1)))
                    }
                    Constant.men_trousers -> {
                        costumeList.add(CostumeDetails(1, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_trouser1)))
                        costumeList.add(CostumeDetails(2, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_jean1)))
                        costumeList.add(CostumeDetails(3, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_jean2)))
                    }
                    Constant.men_shorts -> {
                        costumeList.add(CostumeDetails(1, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_shorts1)))
                        costumeList.add(CostumeDetails(2, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_shorts2)))
                    }
                }
            }
            women -> {
                when (costumeType) {
                    Constant.women_top -> {
                        costumeList.add(CostumeDetails(1, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_dress1)))
                        costumeList.add(CostumeDetails(2, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_top2)))
                        costumeList.add(CostumeDetails(3, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_top3)))
                        costumeList.add(CostumeDetails(4, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_dress2)))
                    }
                    Constant.women_long_wears -> {
                        costumeList.add(CostumeDetails(1, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_long_wear1)))
                        costumeList.add(CostumeDetails(2, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_long_wear2)))
                    }
                    Constant.women_trousers -> {
                        costumeList.add(CostumeDetails(1, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_jeans1)))
                        costumeList.add(CostumeDetails(2, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_trouser1)))
                        costumeList.add(CostumeDetails(3, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_trouser2)))
                    }
                    Constant.women_shorts_n_skirts -> {
                        costumeList.add(CostumeDetails(1, costumeType, getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_shorts1)))
                        costumeList.add(CostumeDetails(2, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.women_shorts2)))
                        costumeList.add(CostumeDetails(3, costumeType,  getCategoryName(categoryName!!, genderType), requireActivity().convertDrawableToBitmap(R.drawable.mens_shorts2)))
                    }
                }
            }
        }
        return costumeList
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        costumeList = ArrayList()
    }
}//End Class
