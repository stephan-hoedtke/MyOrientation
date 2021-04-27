package com.stho.myorientation.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import com.stho.myorientation.Repository

abstract class AbstractPlotView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    interface Listener {
        fun onDoubleTap()
        fun onSingleTap()
        fun onZoom(f: Double)
        fun onScroll(dx: Double, dy: Double)
    }

    protected var zoom: Double = 100.0
        private set

    protected var startTime: Double = 0.0
        private set

    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private val scaleGestureDetector: ScaleGestureDetector by lazy {
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                listener?.onZoom(detector.scaleFactor.toDouble())
                return true
            }
        })
    }

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                listener?.onDoubleTap()
                return false
            }

            override fun onScroll(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
            ): Boolean {
                listener?.onScroll(distanceX.toDouble(), distanceY.toDouble())
                return false // super.onScroll(e1, e2, distanceX, distanceY);
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                listener?.onSingleTap()
                return super.onSingleTapConfirmed(e)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }


    fun setZoom(newZoom: Double) {
        zoom = newZoom
        invalidate()
    }

    fun setStartTime(newStartTime: Double) {
        startTime = newStartTime
        invalidate()
    }

    val frameTime: Double
        get() = Repository.instance.currentTime + startTime
}

