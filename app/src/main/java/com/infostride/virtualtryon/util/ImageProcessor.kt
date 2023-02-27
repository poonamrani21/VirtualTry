package com.infostride.virtualtryon.util

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/****
 * Created by poonam Rani on 23 Jan 2023
 */

// TODO: Need to update code of this class  per image
class ImageProcessor
{
    private val TAG = "C-ImageProcessor"
    fun extractOutfit(bmp: Bitmap, sensitivity_level: Int): Bitmap {
        //Convert bitmap to mat to process
        val mat = Mat()
        Utils.bitmapToMat(bmp, mat)
        //Gaussian blur
        Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)
        //convert from bgr to hsv
        val hsv = Mat()
        Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV)
        val lowerRange = Scalar(0.0, 0.0, (255 - sensitivity_level).toDouble())
        val upperRange = Scalar(255.0, sensitivity_level.toDouble(), 255.0)
        //threshold the HSV image to get only target colors
        val mask = Mat()
        Core.inRange(hsv, lowerRange, upperRange, mask)
        //bitwise-and mask & original image
        val res = Mat()
        Core.bitwise_and(mat, mat, res, mask)
        //create an inverted mask to segment out the outfit
        val mask_2 = Mat()
        Core.bitwise_not(mask, mask_2)
        //Segmenting the outfit out the frame
        var result = Mat()
        Core.bitwise_and(mat, mat, result, mask_2)
        //Crop only the outfit area
        result = cropAOI(result)
        //convert mat to bitmap
        var resultBmp: Bitmap?
        try {
            resultBmp = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(result, resultBmp)
        } catch (e: Exception) {
            resultBmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            Log.d(TAG, "extractOutfit: ${e.message}")
        }
        return resultBmp!!
    } //end extractOutfit
    //This function crop the largest contour in given Mat
    //and returns cropped Mat
    private fun cropAOI(mat: Mat):Mat {
        var largest_area = 0.0
        var largest_contour_index = 0
        var bounding_rect: Rect? = Rect()
        val contours: List<MatOfPoint> = ArrayList()
        val thresh = Mat()
        Imgproc.cvtColor(mat, thresh, Imgproc.COLOR_BGR2GRAY) //grayscale
        Imgproc.threshold(thresh, thresh, 125.0, 255.0, Imgproc.THRESH_BINARY) //threshold
        //find contours
        Imgproc.findContours(thresh, contours, thresh, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE)
        for (contour_index in contours.indices) {
            val contour_area = Imgproc.contourArea(contours[contour_index])
            if (contour_area > largest_area) {
                largest_area = contour_area
                largest_contour_index = contour_index
                bounding_rect = Imgproc.boundingRect(contours[contour_index])
            } //end if statement
        } //end for loop
        return mat.submat(bounding_rect)
    } //end cropAOI
} //end class
