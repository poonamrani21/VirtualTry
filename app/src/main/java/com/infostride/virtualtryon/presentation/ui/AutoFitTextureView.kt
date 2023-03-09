package com.infostride.virtualtryon.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 * {@https://github.com/android/camera-samples/blob/main/CameraXAdvanced/utils/src/main/java/com/example/android/camera/utils/AutoFitSurfaceView.kt
 */
class AutoFitTextureView : TextureView {
    private var mRatioWidth = 0
    private var mRatioHeight = 0

    //Constructors
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size can not be negative" }
        mRatioWidth = width
        mRatioHeight = height
        requestLayout()
        Log.d("AutoFitTextureView", "setAspectRatio Width: $width and Height: $height")

    } //end setAspectRatio


    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (mRatioWidth == 0 || mRatioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth)
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height)
            }
        }
    } //End onMeasure
} //end class
