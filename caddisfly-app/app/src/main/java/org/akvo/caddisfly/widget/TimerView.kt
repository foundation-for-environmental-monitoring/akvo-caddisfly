/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */
package org.akvo.caddisfly.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.Constants
import kotlin.math.abs

/**
 * Countdown timer view.
 * based on: https://github.com/maxwellforest/blog_android_timer
 */
class TimerView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {
    private val mCirclePaint: Paint
    private val mArcPaint: Paint
    private val rectangle = Rect()
    private val mEraserPaint: Paint
    private val mTextPaint: Paint
    private val mCircleBackgroundPaint: Paint
    private val mSubTextPaint: Paint
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private var mCircleOuterBounds: RectF? = null
    private var mCircleInnerBounds: RectF? = null
    private var mCircleSweepAngle = -1f
    private var mCircleFinishAngle = -1f
    private var mProgress = 0f

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec) // Trick to make the view square
    }

    override fun onSizeChanged(w: Int, h: Int, oldWidth: Int, oldHeight: Int) {
        if (w != oldWidth || h != oldHeight) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            mBitmap!!.eraseColor(Color.TRANSPARENT)
            mCanvas = Canvas(mBitmap!!)
        }
        super.onSizeChanged(w, h, oldWidth, oldHeight)
        updateBounds()
    }

    override fun onDraw(canvas: Canvas) {
        mCanvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
        if (mCircleSweepAngle > -1) {
            val text: String = mProgress.toString()
            mTextPaint.getTextBounds(text, 0, text.length, rectangle)
            mCanvas!!.drawArc(mCircleOuterBounds!!, ARC_START_ANGLE.toFloat(), 360f, true, mCircleBackgroundPaint)
            if (mCircleSweepAngle > mCircleFinishAngle) {
                mCanvas!!.drawArc(mCircleOuterBounds!!, ARC_START_ANGLE.toFloat(), mCircleSweepAngle, true, mCirclePaint)
                mCanvas!!.drawArc(mCircleOuterBounds!!, ARC_START_ANGLE.toFloat(), mCircleFinishAngle, true, mArcPaint)
            } else {
                mCanvas!!.drawArc(mCircleOuterBounds!!, ARC_START_ANGLE.toFloat(), mCircleSweepAngle, true, mArcPaint)
            }
            mCanvas!!.drawOval(mCircleInnerBounds!!, mEraserPaint)
            var width = mTextPaint.measureText(text)
            mCanvas!!.drawText(text, (getWidth() - width) / 2f,
                    (height + abs(rectangle.height())) / 2f - 10, mTextPaint)
            val mainTextHeight = rectangle.height()
            val subText = context.getString(R.string.seconds)
            width = mSubTextPaint.measureText(subText)
            mSubTextPaint.getTextBounds(subText, 0, subText.length, rectangle)
            mCanvas!!.drawText(subText, (getWidth() - width) / 2f,
                    (height + abs(rectangle.height())) / 2f + mainTextHeight - 10, mSubTextPaint)
        }
        canvas.drawBitmap(mBitmap!!, 0f, 0f, null)
    }

    fun setProgress(progress: Int, max: Int) {
        mProgress = progress.toFloat()
        drawProgress(progress.toFloat(), max.toFloat())
    }

    private fun drawProgress(progress: Float, max: Float) {
        mCircleSweepAngle = progress * 360 / max
        mCircleFinishAngle = Constants.GET_READY_SECONDS * 360 / max
        invalidate()
    }

    private fun updateBounds() {
        val thickness = width * THICKNESS_SCALE
        mCircleOuterBounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
        mCircleInnerBounds = RectF(
                mCircleOuterBounds!!.left + thickness,
                mCircleOuterBounds!!.top + thickness,
                mCircleOuterBounds!!.right - thickness,
                mCircleOuterBounds!!.bottom - thickness)
        invalidate()
    }

    companion object {
        private val BACKGROUND_COLOR = Color.argb(120, 180, 180, 200)
        private val ERASES_COLOR = Color.argb(180, 40, 40, 40)
        private val FINISH_ARC_COLOR = Color.argb(255, 0, 245, 120)
        private const val ARC_START_ANGLE = 270 // 12 o'clock
        private const val THICKNESS_SCALE = 0.1f
    }

    init {
        var circleColor = Color.RED
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.TimerView)
            circleColor = ta.getColor(R.styleable.TimerView_circleColor, circleColor)
            ta.recycle()
        }
        mTextPaint = Paint()
        mTextPaint.isAntiAlias = true
        mTextPaint.color = Color.WHITE
        val typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        mTextPaint.typeface = typeface
        mTextPaint.textSize = resources.getDimensionPixelSize(R.dimen.progressTextSize).toFloat()
        mSubTextPaint = Paint()
        mSubTextPaint.isAntiAlias = true
        mSubTextPaint.color = Color.LTGRAY
        val subTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        mSubTextPaint.typeface = subTypeface
        mSubTextPaint.textSize = resources.getDimensionPixelSize(R.dimen.progressSubTextSize).toFloat()
        mCircleBackgroundPaint = Paint()
        mCircleBackgroundPaint.isAntiAlias = true
        mCircleBackgroundPaint.color = BACKGROUND_COLOR
        mCirclePaint = Paint()
        mCirclePaint.isAntiAlias = true
        mCirclePaint.color = circleColor
        mArcPaint = Paint()
        mArcPaint.isAntiAlias = true
        mArcPaint.color = FINISH_ARC_COLOR
        mEraserPaint = Paint()
        mEraserPaint.isAntiAlias = true
        mEraserPaint.color = ERASES_COLOR
    }
}