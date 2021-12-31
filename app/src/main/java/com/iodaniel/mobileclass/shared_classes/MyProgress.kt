package com.iodaniel.mobileclass.shared_classes

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.iodaniel.mobileclass.R

class MyProgress : View {
    private var mPrimaryPaint: Paint? = null
    private var mSecondaryPaint: Paint? = null
    private var mRectF: RectF? = null
    private var mTextPaint: TextPaint? = null
    private var mBackgroundPaint: Paint? = null
    private var mDrawText = false
    private var mSecondaryProgressColor = 0
    private var mPrimaryProgressColor = 0
    private var mBackgroundColor = 0
    private var mStrokeWidth = 0
    private var mStrokePrimaryWidth = 0
    private var mProgress = 0
    var secodaryProgress = 0
        private set
    private var mTextColor = 0
    private var mPrimaryCapSize = 0
    private var mSecondaryCapSize = 0
    var isPrimaryCapVisible = false
    var isSecondaryCapVisible = false
    private var x = 0
    private var y = 0
    private var mWidth = 0
    private var mHeight = 0

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        val a: TypedArray
        a = if (attrs != null) {
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.MProgressBar,
                0, 0
            )
        } else {
            throw IllegalArgumentException("Must have to pass the attributes")
        }
        try {
            mDrawText = a.getBoolean(R.styleable.MProgressBar_showProgressText, false)
            mBackgroundColor =
                a.getColor(R.styleable.MProgressBar_backgroundColor, Color.DKGRAY)
            mPrimaryProgressColor =
                a.getColor(R.styleable.MProgressBar_progressColor, Color.DKGRAY)
            mSecondaryProgressColor =
                a.getColor(R.styleable.MProgressBar_secondaryProgressColor, Color.BLACK)
            mProgress = a.getInt(R.styleable.MProgressBar_progress, 0)
            secodaryProgress = a.getInt(R.styleable.MProgressBar_secondaryProgress, 0)
            mStrokeWidth = a.getDimensionPixelSize(R.styleable.MProgressBar_strokeWidth, 20)
            mStrokePrimaryWidth = a.getDimensionPixelSize(R.styleable.MProgressBar_strokeWidth, 20)
            mTextColor = a.getColor(R.styleable.MProgressBar_textColor, Color.BLACK)
            mPrimaryCapSize = a.getInt(R.styleable.MProgressBar_primaryCapSize, 20)
            mSecondaryCapSize = a.getInt(R.styleable.MProgressBar_secodaryCapSize, 20)
            isPrimaryCapVisible = a.getBoolean(R.styleable.MProgressBar_primaryCapVisibility, true)
            isSecondaryCapVisible =
                a.getBoolean(R.styleable.MProgressBar_secodaryCapVisibility, true)
        } finally {
            a.recycle()
        }
        mBackgroundPaint = Paint()
        mBackgroundPaint!!.isAntiAlias = true
        mBackgroundPaint!!.style = Paint.Style.STROKE
        mBackgroundPaint!!.strokeWidth = mStrokeWidth.toFloat()
        mBackgroundPaint!!.color = mBackgroundColor
        mPrimaryPaint = Paint()
        mPrimaryPaint!!.isAntiAlias = true
        mPrimaryPaint!!.style = Paint.Style.STROKE
        mPrimaryPaint!!.strokeWidth = mStrokeWidth.toFloat()
        mPrimaryPaint!!.color = mPrimaryProgressColor
        mSecondaryPaint = Paint()
        mSecondaryPaint!!.isAntiAlias = true
        mSecondaryPaint!!.style = Paint.Style.STROKE
        mSecondaryPaint!!.strokeWidth = (mStrokeWidth - 2).toFloat()
        mSecondaryPaint!!.color = mSecondaryProgressColor
        mTextPaint = TextPaint()
        mTextPaint!!.color = mTextColor
        mRectF = RectF()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRectF!![paddingLeft.toFloat(), paddingTop.toFloat(), (w - paddingRight).toFloat()] =
            (h - paddingBottom).toFloat()
        mTextPaint!!.textSize = (w / 5).toFloat()
        x = w / 2 - (mTextPaint!!.measureText("$mProgress%") / 2).toInt()
        y = (h / 2 - (mTextPaint!!.descent() + mTextPaint!!.ascent()) / 2).toInt()
        mWidth = w
        mHeight = h
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPrimaryPaint!!.style = Paint.Style.STROKE
        mSecondaryPaint!!.style = Paint.Style.STROKE

        // for drawing a full progress .. The background circle
        canvas.drawArc(mRectF!!, 0f, 360f, false, mBackgroundPaint!!)

        // for drawing a secondary progress circle
        val secondarySwipeangle = secodaryProgress * 360 / 100
        canvas.drawArc(mRectF!!, 270f, secondarySwipeangle.toFloat(), false, mSecondaryPaint!!)

        // for drawing a main progress circle
        val primarySwipeangle = mProgress * 360 / 100
        canvas.drawArc(mRectF!!, 270f, primarySwipeangle.toFloat(), false, mPrimaryPaint!!)

        // for cap of secondary progress
        val r = (height - paddingLeft * 2) / 2 // Calculated from canvas width
        var trad = (secondarySwipeangle - 90) * (Math.PI / 180.0) // = 5.1051
        var x = (r * Math.cos(trad)).toInt()
        var y = (r * Math.sin(trad)).toInt()
        mSecondaryPaint!!.style = Paint.Style.FILL
        if (isSecondaryCapVisible) canvas.drawCircle(
            (x + mWidth / 2).toFloat(), (y + mHeight / 2).toFloat(), mSecondaryCapSize.toFloat(),
            mSecondaryPaint!!
        )

        // for cap of primary progress
        trad = (primarySwipeangle - 90) * (Math.PI / 180.0) // = 5.1051
        x = (r * Math.cos(trad)).toInt()
        y = (r * Math.sin(trad)).toInt()
        mPrimaryPaint!!.style = Paint.Style.FILL
        if (isPrimaryCapVisible) canvas.drawCircle(
            (x + mWidth / 2).toFloat(), (y + mHeight / 2).toFloat(), mPrimaryCapSize.toFloat(),
            mPrimaryPaint!!
        )
        if (mDrawText) canvas.drawText(
            "$mProgress%", x.toFloat(), y.toFloat(),
            mTextPaint!!
        )
    }

    fun setDrawText(mDrawText: Boolean) {
        this.mDrawText = mDrawText
        invalidate()
    }

    override fun setBackgroundColor(mBackgroundColor: Int) {
        this.mBackgroundColor = mBackgroundColor
        invalidate()
    }

    fun setStrokeWidth(mStrokeWidth: Int) {
        this.mStrokeWidth = mStrokeWidth
        invalidate()
    }

    fun setPrimaryStrokeWidth(mStrokeWidth: Int) {
        this.mStrokeWidth = mStrokeWidth
        invalidate()
    }

    fun setSecondaryProgress(mSecondaryProgress: Int) {
        secodaryProgress = mSecondaryProgress
        invalidate()
    }

    fun setTextColor(mTextColor: Int) {
        this.mTextColor = mTextColor
        invalidate()
    }

    var secondaryProgressColor: Int
        get() = mSecondaryProgressColor
        set(mSecondaryProgressColor) {
            this.mSecondaryProgressColor = mSecondaryProgressColor
            invalidate()
        }
    var primaryProgressColor: Int
        get() = mPrimaryProgressColor
        set(mPrimaryProgressColor) {
            this.mPrimaryProgressColor = mPrimaryProgressColor
            invalidate()
        }
    var progress: Int
        get() = mProgress
        set(mProgress) {
            this.mProgress = mProgress
            invalidate()
        }

    fun getBackgroundColor(): Int {
        return mBackgroundColor
    }

    var primaryCapSize: Int
        get() = mPrimaryCapSize
        set(mPrimaryCapSize) {
            this.mPrimaryCapSize = mPrimaryCapSize
            invalidate()
        }
    var secondaryCapSize: Int
        get() = mSecondaryCapSize
        set(mSecondaryCapSize) {
            this.mSecondaryCapSize = mSecondaryCapSize
            invalidate()
        }
}