package com.infostride.virtualtryon.presentation.ui

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/****
 * Created by poonam on 23 Jan 2023
 */
// TODO: Need to make this class correct
class Classifier(activity: Activity) {
    //Constant variables
    private val DIM_BATCH_SIZE = 1
    private val DIM_PIXEL_SIZE = 3
    private val outputW = 96
    private val outputH = 96
    @JvmField
    val imageSizeX = 192
    @JvmField
    val imageSizeY = 192
    private val modelPath = "model.tflite"
    private val numBytesPerChannel = 4
    private val TAG = "C-CLASSIFIER: "
    private val LABEL_LENGTH = 14

    //Other variables
    private var tflite: Interpreter? = null
    private var imgData: ByteBuffer? = null
    private var mMat: Mat? = null
    private val intValues = IntArray(imageSizeX * imageSizeY)
    @JvmField
    var mPrintPointArray: Array<FloatArray>? = null
    private val heatmapArray = Array(1) { Array(outputW) { Array(outputH) { FloatArray(14) } } }

    //Constructor
    init {
        tflite = Interpreter(loadModelFile(activity))
        imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_PIXEL_SIZE * imageSizeX * imageSizeY * numBytesPerChannel)
        imgData!!.order(ByteOrder.nativeOrder())
        Log.d(TAG, " classifier has created.")
    } //End constructor

    //Load model file
    /** Preload and memory map the model file, returning a MappedByteBuffer containing the model. */
    @Throws(IOException::class)
    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        Log.d(TAG, " model file loaded. . .")
        return inputStream.channel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    } //End loadModelFile

    fun classifyFrame(bitmap: Bitmap) {
        if (tflite == null) {
            Log.d(TAG, " tflite model is null !")
            return
        } //end if statement
        bitmapToByteBuffer(bitmap)
        runInference()
        Log.d(TAG, " frame classified. . .")
    }

    //Store bitmap data into byteBuffer
    private fun bitmapToByteBuffer(bitmap: Bitmap) {
        if (imgData == null) {
            return
        } //do nothing
        imgData!!.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0 //as counter
        for (i in 0 until imageSizeX) {
            for (j in 0 until imageSizeY) {
                val value = intValues[pixel++]
                imgData!!.putFloat((value and 0xFF).toFloat()) //B
                imgData!!.putFloat((value shr 8 and 0xFF).toFloat()) //G
                imgData!!.putFloat((value shr 16 and 0xFF).toFloat()) //R
            }
        }
        Log.d(TAG, " bitmap stored into a bytebuffer. . .")
    } //end bitmapToByteBuffer

    private fun runInference() {
        tflite!!.run(imgData, heatmapArray)
        if (mPrintPointArray == null) {
            mPrintPointArray = Array(2) { FloatArray(14) }
        }
        if (mMat == null) {
            mMat = Mat(outputW, outputH, CvType.CV_32F)
        }
        val tempArray = FloatArray(outputW * outputH)
        val outTempArray = FloatArray(outputW * outputH)
        for (i in 0 until LABEL_LENGTH) {
            var index = 0
            for (x in 0 until outputW) {
                for (y in 0 until outputH) {
                    tempArray[index] = heatmapArray[0][y][x][i]
                    index++
                } //end for loop
            } //end for loop
            mMat!!.put(0, 0, tempArray)
            Imgproc.GaussianBlur(mMat, mMat, Size(5.0, 5.0), 0.0, 0.0)
            mMat!![0, 0, outTempArray]
            var xMax = 0f
            var yMax = 0f
            var vMax = 0f
            for (x in 0 until outputW) {
                for (y in 0 until outputH) {
                    val value = get(x, y, outTempArray)
                    if (value > vMax) {
                        vMax = value
                        xMax = x.toFloat()
                        yMax = y.toFloat()
                    } //end if statement
                } //end for loop
            } //end for loop
            mPrintPointArray!![0][i] = xMax
            mPrintPointArray!![1][i] = yMax
        } //end for-loop
    } //end runInference

    private operator fun get(x: Int, y: Int, arr: FloatArray): Float {
        return if (x < 0 || y < 0 || x >= outputW || y >= outputH) {
            -1f
        } else {
            arr[x * outputW + y]
        }
    } //end get

    fun close() {
        tflite!!.close()
        tflite = null
        Log.d(TAG, " tensorflow model has closed..")
    } //end close
} //END Classifier
