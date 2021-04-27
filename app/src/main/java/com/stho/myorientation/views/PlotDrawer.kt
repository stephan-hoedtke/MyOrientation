package com.stho.myorientation.views

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.MyCollection

class PlotDrawer(private val w: Int, private val h: Int, private val frameTime: Double, private val zoom: Double, private val canvas: Canvas) {

    fun draw(entries: Entries, method: Entries.Method, property: Entries.Property, color: Paint) {
        draw(entries[method], property, color)
    }

    private fun draw(entries: MyCollection<Entries.Entry>, property: Entries.Property, color: Paint) {
        val path: Path = Path()
        var f = true
        val scaleFactor = 0.9 / 360
        var previousValue = 0.0
        // warning: for (entry in entries) may fail with ConcurrentModificationException as the sensor adds new records while the loop is running
        for (entry in entries.elements()) {
            val time = frameTime - entry.time
            val value = entry[property]
            if (isCrossing(value, previousValue)) {
                f = true
            }
            val x = w - time * zoom
            val y = h / 2 + h / 2 * value * scaleFactor
            if (x >= 0 && x <= w) {
                if (f) {
                    path.moveTo(x.toFloat(), y.toFloat())
                    f = false
                } else {
                    path.lineTo(x.toFloat(), y.toFloat())
                }
            }
            previousValue = value
        }
        canvas.drawPath(path, color)
    }

    fun draw(measurements: Measurements, type: Measurements.Type, component: Measurements.Component, color: Paint) {
        draw(measurements[type], component, measurements.scaleFactor(type), color)
    }

    private fun draw(measurements: MyCollection<Measurements.Measurement>, component: Measurements.Component, scaleFactor: Double, color: Paint) {
        val path: Path = Path()
        var f = true
        var previousValue = 0.0
        for (measurement in measurements.elements()) {
            val time = frameTime - measurement.time
            val value = measurement[component]
            if (isCrossing(value, previousValue)) {
                f = true
            }
            val x = w - time * zoom
            val y = h / 2 + h / 2 * value * scaleFactor
            if (x >= 0 && x <= w) {
                if (f) {
                    path.moveTo(x.toFloat(), y.toFloat())
                    f = false
                } else {
                    path.lineTo(x.toFloat(), y.toFloat())
                }
            }
            previousValue = value
        }
        canvas.drawPath(path, color)
    }


    fun drawZero(color: Paint) {
        val path: Path = Path()
        val y = h / 2
        path.moveTo(0f, y.toFloat())
        path.lineTo(w.toFloat(), y.toFloat())
        canvas.drawPath(path, color)
    }

    fun drawSeconds(color: Paint) {
        val path: Path = Path()
        var t = 1.0
        while (t < frameTime) {
            val time = frameTime - t
            val x = w - time * zoom
            val y1 = h / 2 * 0.9
            val y2 = h / 2 * 1.1
            if (x >= 0 && x <= w) {
                path.moveTo(x.toFloat(), y1.toFloat())
                path.lineTo(x.toFloat(), y2.toFloat())
            }
            t += 1.0
        }
        canvas.drawPath(path, color)
    }

    private fun isCrossing(a: Double, b: Double): Boolean =
            (a > 120 && b < -120) || (a < -120 && b > 120)
}
