package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import com.stho.myorientation.IAccelerationMagnetometerFilterOptions
import com.stho.myorientation.IFilterOptions
import com.stho.myorientation.Measurements
import com.stho.myorientation.Method
import com.stho.myorientation.library.algebra.RotationMatrix


class AccelerationMagnetometerFilter(options: IFilterOptions) :
    AbstractOrientationFilter(Method.AccelerometerMagnetometer, options) {

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9) { 0f }

    private var hasMagnetometer: Boolean = false

    override fun updateReadings(type: Measurements.Type, values: FloatArray) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (type) {
            Measurements.Type.Magnetometer -> {
                System.arraycopy(values, 0, magnetometerReading, 0, magnetometerReading.size)
                hasMagnetometer = true
            }
            Measurements.Type.Accelerometer -> {
                System.arraycopy(values, 0, accelerometerReading, 0, accelerometerReading.size)
                updateOrientationAnglesFromAcceleration()
            }
        }
    }

    /**
     * Compute the three orientation angles based on the most recent readings from magnetometer and accelerometer
     */
    private fun updateOrientationAnglesFromAcceleration() {
        if (!hasMagnetometer)
            return

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            val adjustedRotationMatrix = RotationMatrix.fromFloatArray(getAdjustedRotationMatrix(rotationMatrix))
            val orientation = adjustedRotationMatrix.toOrientation()
            super.onOrientationAnglesChanged(orientation)
        }
    }

    override fun reset() {
        hasMagnetometer = false
    }
}

