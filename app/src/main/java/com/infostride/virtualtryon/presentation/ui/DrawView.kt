package com.infostride.virtualtryon.presentation.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.infostride.virtualtryon.domain.model.Outfit

class DrawView : View {
    private var mRatioWidth = 0
    private var mRatioHeight = 0
    private val mDrawPoint = ArrayList<PointF>()
    private var mWidth = 0
    private var mHeight = 0
    private var mRatioX = 0f
    private var mRatioY = 0f
    private var mImgWidth = 0
    private var mImgHeight = 0
    private val mPaint = Paint()
    private val TAG = "C-DRAWVIEW: "

    //Constructors
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    fun setImgSize(width: Int, height: Int) {
        mImgWidth = width
        mImgHeight = height
        requestLayout()
    } //End setImgSize

    fun setDrawPoint(point: Array<FloatArray>, ratio: Float) {
        mDrawPoint.clear()
        var tempX: Float
        var tempY: Float
        for (index in 0..13) {
            tempX = point[0][index] / ratio / mRatioX
            tempY = point[1][index] / ratio / mRatioY
            mDrawPoint.add(PointF(tempX, tempY))
        }
    } //End setDrawPoint

    fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size can not be negative" }
        mRatioWidth = width
        mRatioHeight = height
        requestLayout()
    } //End setAspectRatio

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mDrawPoint.isEmpty() || currentOutfit==null) {
            Log.d(TAG, " mDrawPoint is NULL !!")
            return
        }
        mPaint.flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = dipToFloat(2f)
        val outfit_byte_array = currentOutfit!!.image
        val outfit_bmp = BitmapFactory.decodeByteArray(outfit_byte_array, 0, outfit_byte_array.size)

        //Coordinates to fit "TOP" outfit
        val top_left = (mDrawPoint[2].x - 60).toInt() //The X coordinate of the left side of the rectangle
        val top_top = (mDrawPoint[1].y - 10).toInt() //The Y coordinate of the top of the rectangle
        val top_right = (mDrawPoint[5].x + 60).toInt() //The X coordinate of the right side of the rectangle
        val top_bottom = (mDrawPoint[8].y + 10).toInt() //The Y coordinate of the bottom of the rectangle
        val rect_top = Rect(top_left, top_top, top_right, top_bottom)//Coordinates to fit "LONG WEAR" outfit
        val long_left = (mDrawPoint[2].x - 60).toInt() //The X coordinate of the left side of the rectangle
        val long_top = (mDrawPoint[1].y - 10).toInt() //The Y coordinate of the top of the rectangle
        val long_right = (mDrawPoint[5].x + 60).toInt() //The X coordinate of the right side of the rectangle
        val long_bottom = (mDrawPoint[9].y + 10).toInt() //The Y coordinate of the bottom of the rectangle
        val rect_long = Rect(long_left, long_top, long_right, long_bottom) //Coordinates to fit "TROUSERS" outfit
        val trousers_left = (mDrawPoint[8].x - 60).toInt() //The X coordinate of the left side of the rectangle
        val trousers_top = (mDrawPoint[8].y - 10).toInt() //The Y coordinate of the top of the rectangle
        val trousers_right = (mDrawPoint[11].x + 60).toInt() //The X coordinate of the right side of the rectangle
        val trousers_bottom = (mDrawPoint[10].y + 10).toInt() //The Y coordinate of the bottom of the rectangle
        val rect_trousers = Rect(trousers_left, trousers_top, trousers_right, trousers_bottom) //Coordinates to fit "SHORTS N SKIRTS" outfit
        val short_left = (mDrawPoint[8].x - 60).toInt() //The X coordinate of the left side of the rectangle
        val short_top = (mDrawPoint[8].y - 10).toInt() //The Y coordinate of the top of the rectangle
        val short_right = (mDrawPoint[11].x + 60).toInt() //The X coordinate of the right side of the rectangle
        val short_bottom = (mDrawPoint[9].y + 10).toInt() //The Y coordinate of the bottom of the rectangle
        val rect_short = Rect(short_left, short_top, short_right, short_bottom)
        var dst_rect = rect_top
        if (currentOutfit!!.category == "top") { dst_rect = rect_top }
        if (currentOutfit!!.category == "long_wears") { dst_rect = rect_long }
        if (currentOutfit!!.category == "trousers") { dst_rect = rect_trousers }
        if (currentOutfit!!.category == "shorts_n_skirts") { dst_rect = rect_short }
        canvas.drawBitmap(outfit_bmp, null, dst_rect, null)
        Log.d(TAG, " points has been drawed")
    } //End onDraw

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (mRatioWidth == 0 || mRatioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                mWidth = width
                mHeight = width * mRatioHeight / mRatioWidth
            } else {
                mWidth = height * mRatioWidth / mRatioHeight
                mHeight = height
            }
        }
        setMeasuredDimension(mWidth, mHeight)
        try {
            mRatioX = mImgWidth.toFloat() / mWidth
            mRatioY = mImgHeight.toFloat() / mHeight
        } catch (e: ArithmeticException) {
            Log.d(TAG, " mRatioX|mRatioY Arithmetic Exception !!")
            mRatioX = 1f
            mRatioY = 1f
        }
    } //End onMeasure

    //Convert a dip value into a float
    private fun dipToFloat(value: Float): Float { return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics) } //end dipToFloat
    companion object { var currentOutfit: Outfit? = null }
} //End class
