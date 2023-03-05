package com.apmolokanova.clockview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.time.LocalTime
import java.util.LinkedList
import kotlin.concurrent.fixedRateTimer
import kotlin.math.*
import kotlin.properties.Delegates

class ClockView(
    context: Context,
    attrSet: AttributeSet?,
    defStyleAttr : Int,
    defStyleRes: Int
): View(context,attrSet,defStyleAttr,defStyleRes) {
    private var hourHandColor by Delegates.notNull<Int>()
    private var minuteHandColor by Delegates.notNull<Int>()
    private var secondHandColor by Delegates.notNull<Int>()
    private var dialColor by Delegates.notNull<Int>()
    private var numbersSize by Delegates.notNull<Int>()

    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private var hourLength: Float = 0f
    private var minuteLength: Float = 0f
    private var secondLength: Float = 0f
    private var numberRadius: Float = 0f

    private lateinit var hourPaint: Paint
    private lateinit var minutePaint: Paint
    private lateinit var secondPaint: Paint
    private lateinit var dialPaint: Paint
    private lateinit var numbersPaint: Paint

    constructor(context: Context,attrSet: AttributeSet?,defStyleAttr: Int) : this(context,attrSet,defStyleAttr,R.style.DefaultClockStyle)
    constructor(context: Context,attrSet: AttributeSet?) : this(context,attrSet,R.attr.clockStyle)
    constructor(context: Context) : this(context,null)

    init {
        if(attrSet != null) {
            initAttr(attrSet,defStyleAttr,defStyleRes)
        } else {
            initDefaultAttr()
        }
        initPaints()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minSide = max(suggestedMinimumWidth + paddingLeft + paddingRight, suggestedMinimumHeight + paddingTop + paddingBottom)
        setMeasuredDimension(
            resolveSize(minSide,widthMeasureSpec),
            resolveSize(minSide,heightMeasureSpec)
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        GlobalClock.subscribeClock(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateMeasures()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateMeasures()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val hourX = getX(GlobalClock.hourAngle)
        val hourY = getY(GlobalClock.hourAngle)
        val minuteX = getX(GlobalClock.minuteAngle)
        val minuteY = getY(GlobalClock.minuteAngle)
        val secondX = getX(GlobalClock.secondAngle)
        val secondY = getY(GlobalClock.secondAngle)

        canvas.drawCircle(centerX,centerY,radius,dialPaint)
        drawHand(canvas,hourX,hourY,hourLength,hourPaint)
        drawHand(canvas,minuteX,minuteY,minuteLength,minutePaint)
        drawHand(canvas,secondX,secondY,secondLength,secondPaint)
        canvas.drawCircle(centerX,centerY,0.01f*radius,hourPaint)

        for(i in DIAL_ANGLES.indices) {
            canvas.drawText("${i+1}",centerX+numberRadius*getX(DIAL_ANGLES[i]),centerY+numberRadius* getY(DIAL_ANGLES[i])+numbersPaint.textSize/2,numbersPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        GlobalClock.unsubscribeClock(this)
    }

    private fun drawHand(canvas: Canvas, x: Float,y: Float, length: Float, paint: Paint) {
        canvas.drawLine(centerX-0.2f*length*x,centerY-0.2f*length*y,x*length+centerX,y*length+centerY,paint)
    }

    private fun initPaints() {
        hourPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        hourPaint.color = hourHandColor
        hourPaint.style = Paint.Style.STROKE

        minutePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        minutePaint.color = minuteHandColor
        minutePaint.style = Paint.Style.STROKE

        secondPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        secondPaint.color = secondHandColor
        secondPaint.style = Paint.Style.STROKE

        dialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dialPaint.color = dialColor
        dialPaint.style = Paint.Style.STROKE

        numbersPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        numbersPaint.color = dialColor
        numbersPaint.textAlign = Paint.Align.CENTER
    }

    private fun updateMeasures() {
        radius = min(width - paddingLeft - paddingRight,height - paddingBottom - paddingTop)/2.toFloat()
        radius -= ceil(radius/DIAL_STROKE_RATIO)
        centerX = paddingLeft + (width - paddingLeft - paddingRight)/2.toFloat()
        centerY = paddingTop + (height - paddingTop + paddingBottom.toFloat())/2
        hourLength = radius*0.4f
        minuteLength = radius*0.8f
        secondLength = radius*0.8f
        numberRadius = radius*0.9f

        hourPaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius/ HOUR_STROKE_RATIO,resources.displayMetrics)
        minutePaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius/ MINUTE_STROKE_RATIO,resources.displayMetrics)
        secondPaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius/ SECOND_STROKE_RATIO,resources.displayMetrics)
        dialPaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius/ DIAL_STROKE_RATIO,resources.displayMetrics)

        numbersPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius * numbersSize/500 ,resources.displayMetrics)
    }

    private fun getY(angle: Double) : Float = sin(angle).toFloat()
    private fun getX(angle: Double) : Float = cos(angle).toFloat()

    private fun initAttr(attrSet: AttributeSet?, defStyleAttr : Int, defStyleRes: Int) {
        val attrArray = context.obtainStyledAttributes(attrSet,R.styleable.ClockView,defStyleAttr,defStyleRes)
        hourHandColor = attrArray.getColor(R.styleable.ClockView_hourHandColor, HOUR_DEFAULT_COLOR)
        minuteHandColor = attrArray.getColor(R.styleable.ClockView_minuteHandColor, MINUTE_DEFAULT_COLOR)
        secondHandColor = attrArray.getColor(R.styleable.ClockView_secondHandColor, SECOND_DEFAULT_COLOR)
        dialColor = attrArray.getColor(R.styleable.ClockView_dialColor, DIAL_DEFAULT_COLOR)
        numbersSize = attrArray.getInt(R.styleable.ClockView_numbersSize, NUMBERS_DEFAULT_SIZE)
        attrArray.recycle()
    }

    private fun initDefaultAttr() {
        hourHandColor = HOUR_DEFAULT_COLOR
        minuteHandColor = MINUTE_DEFAULT_COLOR
        secondHandColor = SECOND_DEFAULT_COLOR
        dialColor = DIAL_DEFAULT_COLOR
        numbersSize = NUMBERS_DEFAULT_SIZE
    }

    companion object{
        private val DIAL_ANGLES = DoubleArray(12) {i -> Math.PI*(i+1)/6 - Math.PI/2}

        const val DIAL_DEFAULT_COLOR = Color.BLACK
        const val HOUR_DEFAULT_COLOR = Color.BLACK
        const val MINUTE_DEFAULT_COLOR = Color.BLACK
        const val SECOND_DEFAULT_COLOR = Color.RED
        const val NUMBERS_DEFAULT_SIZE = 15

        const val DIAL_STROKE_RATIO = 85
        const val HOUR_STROKE_RATIO = 50
        const val MINUTE_STROKE_RATIO = 75
        const val SECOND_STROKE_RATIO = 85
    }
    private class GlobalClock() {
        companion object {
            private const val TIMER_NAME = "clockViewInvalidationTimer"
            private const val INVALIDATION_PERIOD = 1000L

            private val clocksList: LinkedList<ClockView> = LinkedList()

            var hourAngle: Double = 0.0
                private set
            var minuteAngle: Double = 0.0
                private set
            var secondAngle: Double= 0.0
                private set

            val timer = fixedRateTimer(TIMER_NAME,false,0L, INVALIDATION_PERIOD) {
                updateTime()
                clocksList.forEach { it.invalidate() }
            }

            fun subscribeClock(clock: ClockView) {
                clocksList.add(clock)
            }
            fun unsubscribeClock(clock: ClockView) {
                clocksList.remove(clock)
            }

            private fun updateTime() {
                val time = LocalTime.now()
                hourAngle = Math.PI*(time.hour+time.minute/60.toDouble())/6 - Math.PI/2
                minuteAngle = (Math.PI*(time.minute+time.second/60.toDouble())/30 - Math.PI/2)
                secondAngle = (Math.PI*time.second/30 - Math.PI/2)
            }
        }
    }
}

