package com.stho.myorientation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.stho.myorientation.Entries
import com.stho.myorientation.Method
import com.stho.myorientation.Property
import com.stho.myorientation.Repository

class MainPlotView(context: Context?, attrs: AttributeSet?) : AbstractPlotView(context, attrs) {

    private val red: Paint = Paint().apply { color = Color.RED; style = Paint.Style.STROKE }
    private val cyan: Paint = Paint().apply { color = Color.CYAN; style = Paint.Style.STROKE }
    private val gray: Paint = Paint().apply { color = Color.GRAY; style = Paint.Style.STROKE; }
    private val yellow: Paint = Paint().apply { color = Color.YELLOW; style = Paint.Style.STROKE }
    private val green: Paint = Paint().apply { color = Color.GREEN; style = Paint.Style.STROKE }
    private val pink: Paint = Paint().apply { color = Color.rgb(0xFC, 0x0F, 0xC0); style = Paint.Style.STROKE }
    private val orange: Paint = Paint().apply { color = Color.rgb(0xFF, 0x88, 0x00); style = Paint.Style.STROKE }
    private val magenta: Paint = Paint().apply { color = Color.MAGENTA; style = Paint.Style.STROKE }

    private val repository: Repository by lazy { Repository.instance }
    private var property: Property = Property.Azimuth

    fun setProperty(newProperty: Property) {
        property = newProperty
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val drawer: PlotDrawer = PlotDrawer(width, height, frameTime, zoom, canvas)

        drawer.drawZero(gray)
        drawer.drawSeconds(gray)
        
        drawer.draw(repository.entries, Method.AccelerometerMagnetometer, property, red)
        drawer.draw(repository.entries, Method.RotationVector, property, yellow)
        drawer.draw(repository.entries, Method.ComplementaryFilter, property, cyan)
        drawer.draw(repository.entries, Method.MadgwickFilter, property, green)
        drawer.draw(repository.entries, Method.SeparatedCorrectionFilter, property, magenta)
        drawer.draw(repository.entries, Method.ExtendedComplementaryFilter, property, orange)
        drawer.draw(repository.entries, Method.KalmanFilter, property, pink)

        drawer.draw(repository.entries, Method.Damped, property, gray)
    }
}