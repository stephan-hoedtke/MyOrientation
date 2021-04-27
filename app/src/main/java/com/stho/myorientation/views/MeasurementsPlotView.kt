package com.stho.myorientation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.stho.myorientation.Measurements
import com.stho.myorientation.Repository


class MeasurementsPlotView(context: Context?, attrs: AttributeSet?) : AbstractPlotView(context, attrs) {

    private val red: Paint = Paint().apply { color = Color.RED; style = Paint.Style.STROKE }
    private val cyan: Paint = Paint().apply { color = Color.CYAN; style = Paint.Style.STROKE }
    private val gray: Paint = Paint().apply { color = Color.GRAY; style = Paint.Style.STROKE; }
    private val yellow: Paint = Paint().apply { color = Color.YELLOW; style = Paint.Style.STROKE }

    private val repository: Repository by lazy { Repository.instance }
    private var type: Measurements.Type = Measurements.Type.Accelerometer

    fun setType(newType: Measurements.Type) {
        type = newType
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val drawer: PlotDrawer = PlotDrawer(width, height, frameTime, zoom, canvas)

        drawer.drawZero(gray)
        drawer.drawSeconds(gray)
        drawer.draw(repository.measurements, type, Measurements.Component.X, cyan)
        drawer.draw(repository.measurements, type, Measurements.Component.Y, yellow)
        drawer.draw(repository.measurements, type, Measurements.Component.Z, red)
    }
}

