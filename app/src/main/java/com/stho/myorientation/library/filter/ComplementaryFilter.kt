package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.*
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import kotlin.math.sqrt


class ComplementaryFilter(accelerationFactor: Double = 0.7, filterCoefficient: Double = 0.98): AbstractOrientationFilter(Entries.Method.ComplementaryFilter, accelerationFactor), OrientationFilter {

    private val interpolationFactor = 1 - filterCoefficient
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)

    private var estimate: Quaternion = Quaternion.default
    private var accelerationMagnetometerOrientation: Quaternion = Quaternion.default

    private var hasMagnetometer: Boolean = false
    private var hasAccelerationMagnetometer: Boolean = false
    private var hasGyro: Boolean = false

    private var timer: Timer = Timer()

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
            Measurements.Type.Gyroscope -> {
                System.arraycopy(values, 0, gyroscopeReading, 0, gyroscopeReading.size)
                updateOrientationAnglesFromGyroscope()
            }
        }
    }

    private fun updateOrientationAnglesFromAcceleration() {
        // If magnetometer is not initialized yet, don't continue...
        if (!hasMagnetometer)
            return

        val rotationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            // TODO: get quaternion from readings directly
            val adjustedRotationMatrix = Matrix.fromFloatArray(getAdjustedRotationMatrix(rotationMatrix))
            accelerationMagnetometerOrientation = Quaternion.fromRotationMatrix( adjustedRotationMatrix)
            hasAccelerationMagnetometer = true
        }
    }

    private fun updateOrientationAnglesFromGyroscope() {
        // If acceleration is not initialized yet, don't continue...
        if (!hasAccelerationMagnetometer)
            return

        // If gyro is not initialized yet, do it now...
        if (!hasGyro) {
            estimate = Quaternion.default * accelerationMagnetometerOrientation
            hasGyro = true
        }

        val dt = timer.getNextTime()
        if (dt > 0) {

            filterUpdate(dt)

            onOrientationAnglesChanged(estimate.toOrientation())
        }
    }

    private fun filterUpdate(dt: Double) {

        val omega = Vector.fromFloatArray(gyroscopeReading)

        // Get updated Gyro delta rotation from gyroscope readings
        val deltaRotation = AbstractOrientationFilter.getDeltaRotationFromGyroscope(omega, dt)

        // update the gyro orientation
        estimate *= deltaRotation
    }

    override fun fuseSensors() {

        // Fusion happens as a separate process to update the estimate using the accelerometer / magnetometer readings
        if (hasGyro && hasAccelerationMagnetometer) {
            calculateFusedOrientation()
        }
    }

    override fun reset() {
        hasMagnetometer= false
        hasAccelerationMagnetometer = false
        hasGyro = false
    }

    /**
     * fuse gyro with acceleration to overcome the gyro bias
     */
    private fun calculateFusedOrientation() {
        estimate = Quaternion.interpolate(estimate, accelerationMagnetometerOrientation, interpolationFactor)
    }
}

