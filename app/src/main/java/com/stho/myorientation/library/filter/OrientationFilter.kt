package com.stho.myorientation.library.filter

import com.stho.myorientation.Measurements
import com.stho.myorientation.library.algebra.Orientation

interface OrientationFilter {
    var deviceRotation: Int
    val currentOrientation: Orientation
    fun updateReadings(type: Measurements.Type, values: FloatArray)
    fun fuseSensors()
    fun reset()
}

