package com.infostride.virtualtryon.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout

class AutoFitFrameLayout : FrameLayout {
    private var mRatioWidth = 0
    private var mRatioHeight = 0

    //Constructors
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size can not be negative" }
        mRatioWidth = width
        mRatioHeight = height
        Log.d("AutoFitFrameLayout", "setAspectRatio Width: $width and Height: $height")
        requestLayout()
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
} //End class
