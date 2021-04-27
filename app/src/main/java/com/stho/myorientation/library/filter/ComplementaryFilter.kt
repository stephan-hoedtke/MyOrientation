package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.Repository
import com.stho.myorientation.library.*
import com.stho.myorientation.library.algebra.Quaternion
import kotlin.math.sqrt


class ComplementaryFilter(accelerationFactor: Double = 0.7, filterCoefficient: Double = 0.98): AbstractOrientationFilter(Entries.Method.ComplementaryFilter, accelerationFactor), OrientationFilter {

    private val interpolationFactor = 1 - filterCoefficient
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)

    private var gyroDeltaRotationQuaternion: Quaternion = Quaternion.default
    private var gyroOrientationQuaternion: Quaternion = Quaternion.default
    private var accelerationOrientationQuaternion: Quaternion = Quaternion.default

    private var hasAcceleration: Boolean = false
    private var hasGyro: Boolean = false

    private var timer: Timer = Timer()

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
        val rotationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            val adjustedRotationMatrix = getAdjustedRotationMatrix(rotationMatrix)
            accelerationOrientationQuaternion = Quaternion.fromRotationMatrix(adjustedRotationMatrix)
            hasAcceleration = true
        }
    }

    private fun updateOrientationAnglesFromGyroscope() {
        // If acceleration is not initialized yet, don't continue...
        if (!hasAcceleration)
            return

        // If gyro is not initialized yet, do it now...
        if (!hasGyro) {
            gyroOrientationQuaternion *= accelerationOrientationQuaternion
            hasGyro = true
        }

        // Get updated Gyro delta rotation from gyroscope readings
        getDeltaRotationFromGyroscope()

        // update the gyro orientation
        gyroOrientationQuaternion *= gyroDeltaRotationQuaternion  // TODO: ? .inverse()

        // update orientation
        updateOrientationFromGyroOrientation()
    }


    /**
     * updates deltaRotationQuaternion which represents the gyroscope rotation changes
     */
    private fun getDeltaRotationFromGyroscope() {
        val dt = timer.getNextTime()
        if (dt > 0) {
            // Axis of the rotation sample, not normalized yet.
            var x: Double = gyroscopeReading[0].toDouble() // rotation around x-axis: opposite to pitch (positive = front upwards)
            var y: Double = gyroscopeReading[1].toDouble() // rotation around y-axis: opposite to roll (positive = left side upwards)
            var z: Double = gyroscopeReading[2].toDouble() // rotation around z-axis: opposite to azimuth (positive = front westwards)

            // Calculate the angular speed of the sample
            val omegaMagnitude: Double = sqrt(x * x + y * y + z * z)

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > EPSILON) {
                x /= omegaMagnitude
                y /= omegaMagnitude
                z /= omegaMagnitude
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            val theta: Double = omegaMagnitude * dt
            gyroDeltaRotationQuaternion = Quaternion.forRotation(x, y, z, theta)
        }
    }

    override fun fuseSensors() {
        if (hasGyro && hasAcceleration) {
            // calculateFusedOrientation()
        }
    }

    /**
     * fuse gyro with acceleration to overcome the gyro bias
     */
    private fun calculateFusedOrientation() {
        val fusedOrientation = Quaternion.interpolate(gyroOrientationQuaternion, accelerationOrientationQuaternion, interpolationFactor)

        // update the gyro orientation to reflect the correction by the acceleration and magnetometer sensor
        gyroOrientationQuaternion = fusedOrientation
    }

    private fun updateOrientationFromGyroOrientation() {
        val matrix = gyroOrientationQuaternion.toRotationMatrix()
        val adjustedRotationMatrix = getAdjustedRotationMatrix(matrix)
        val angles = getOrientationAnglesFromRotationMatrix(adjustedRotationMatrix)
        onOrientationAnglesChanged(angles)
    }

    companion object {
        private const val EPSILON: Double = 0.000000001
    }
}

