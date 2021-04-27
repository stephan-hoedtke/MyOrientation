package com.stho.myorientation.library.filter

import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements

class KalmanFilter(accelerationFactor: Double = 0.7) : AbstractOrientationFilter(Entries.Method.KalmanFilter, accelerationFactor), OrientationFilter {

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)

    override fun updateReadings(type: Measurements.Type, values: FloatArray) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (type) {
            Measurements.Type.Magnetometer -> {
                System.arraycopy(values, 0, magnetometerReading, 0, magnetometerReading.size)
            }
            Measurements.Type.Accelerometer -> {
                System.arraycopy(values, 0, accelerometerReading, 0, accelerometerReading.size)
                updateOrientationAnglesFromAcceleration()
            }
            Measurements.Type.Gyroscope -> {
                System.arraycopy(values, 0, gyroscopeReading, 0, gyroscopeReading.size)
                updateOrientationAnglesFromGyroscope()
            }
        }
    }

    private fun updateOrientationAnglesFromAcceleration() {

    }

    private fun updateOrientationAnglesFromGyroscope() {

    }

}

