package com.stho.myorientation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.stho.myorientation.Entries
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
    private var property: Entries.Property = Entries.Property.Azimuth

    fun setProperty(newProperty: Entries.Property) {
        property = newProperty
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val drawer: PlotDrawer = PlotDrawer(width, height, frameTime, zoom, canvas)

        drawer.drawZero(gray)
        drawer.drawSeconds(gray)
        
        drawer.draw(repository.entries, Entries.Method.AccelerometerMagnetometer, property, red)
        drawer.draw(repository.entries, Entries.Method.RotationVector, property, yellow)
        drawer.draw(repository.entries, Entries.Method.ComplementaryFilter, property, cyan)
        drawer.draw(repository.entries, Entries.Method.MadgwickFilter, property, green)
        drawer.draw(repository.entries, Entries.Method.SeparatedCorrectionFilter, property, magenta)
        drawer.draw(repository.entries, Entries.Method.ExtendedComplementaryFilter, property, orange)
        drawer.draw(repository.entries, Entries.Method.KalmanFilter, property, pink)

        drawer.draw(repository.entries, Entries.Method.Damped, property, gray)
    }
}