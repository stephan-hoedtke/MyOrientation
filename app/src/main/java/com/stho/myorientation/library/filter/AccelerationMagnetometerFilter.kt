package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.OrientationLowPassFilter
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Orientation


class AccelerationMagnetometerFilter(accelerationFactor: Double = 0.7, timeConstant: Double = 0.2) : AbstractOrientationFilter(Entries.Method.AccelerometerMagnetometer, accelerationFactor), OrientationFilter {

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9) { 0f }

    private var hasMagnetometer: Boolean = false

    private val lowPassFilter: OrientationLowPassFilter = OrientationLowPassFilter(timeConstant)

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
            val adjustedRotationMatrix = Matrix.fromFloatArray(getAdjustedRotationMatrix(rotationMatrix))
            val orientation = adjustedRotationMatrix.toOrientation()
            super.onOrientationAnglesChanged(orientation)
        }
    }

    /**
     * use a low pass filter before changing orientation
     */
    override fun onOrientationAnglesChanged(orientation: Orientation) {
        lowPassFilter.onUpdateOrientation(orientation)
        super.onOrientationAnglesChanged(lowPassFilter.orientation)
    }

    override fun reset() {
        hasMagnetometer = false
    }
}

