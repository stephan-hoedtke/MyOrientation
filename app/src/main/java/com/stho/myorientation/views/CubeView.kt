package com.stho.myorientation.views

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.Orientation
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.graphics.CubeRenderer
import java.lang.Math.atan2

class CubeView(context: Context, attr: AttributeSet): GLSurfaceView(context, attr) {

    interface IEventListener {
        fun rotateBy(alpha: Double, beta: Double)
    }

    private val renderer: CubeRenderer
    private var listener: IEventListener? = null

    private var previousX: Double = 0.0
    private var previousY: Double = 0.0

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = CubeRenderer()

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    fun setEventListener(listener: IEventListener) {
        this.listener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        val x: Double = event.x - width / 2.0
        val y: Double = height / 2.0 - event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {

                val alpha1 = Degree.arcTan2(previousX, previousY)
                val alpha2 = Degree.arcTan2(x, previousY)

                val beta1 = Degree.arcTan2(previousY, previousX)
                val beta2 = Degree.arcTan2(y, previousX)

                listener?.rotateBy(
                    alpha2 - alpha1,
                    beta2 - beta1,
                )
            }
        }

        previousX = x
        previousY = y
        return true
    }

    fun setAngles(alpha: Double, beta: Double) {
        renderer.alpha = alpha.toFloat()
        renderer.beta = beta.toFloat()
        requestRender()
    }

    fun setOrientation(orientation: Orientation) {
        renderer.orientation = Quaternion.fromRotationMatrix(orientation.rotation)
        requestRender()
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
    }
}

