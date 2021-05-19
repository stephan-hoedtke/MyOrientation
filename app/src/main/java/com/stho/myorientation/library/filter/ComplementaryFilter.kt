package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.*
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Quaternion
import kotlin.math.sqrt


class ComplementaryFilter(accelerationFactor: Double = 0.7, filterCoefficient: Double = 0.98): AbstractOrientationFilter(Entries.Method.ComplementaryFilter, accelerationFactor), OrientationFilter {

    private val interpolationFactor = 1 - filterCoefficient
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)

    private var gyroDeltaRotation: Quaternion = Quaternion.default
    private var gyroOrientation: Quaternion = Quaternion.default
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
            gyroOrientation = Quaternion.default * accelerationMagnetometerOrientation
            hasGyro = true
        }

        // Get updated Gyro delta rotation from gyroscope readings
        getDeltaRotationFromGyroscope()

        // update the gyro orientation
        gyroOrientation *= gyroDeltaRotation

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
            //
            // Quaternion integration:
            // ds/dt = omega x s
            // with s = q # s0 # q* follows
            //      dq/dt = 0.5 * omega # q
            //      q(t) = exp(0.5 * omega * (t - t0)) # q0
            //      q(t) = cos(|v|) + v / |v| * sin(|v|) # q0 with v = 0.5 * omega * (t - t0)
            //      this is equivalent to a rotation by theta around the rotation vector omega/|omega| with theta = |omega| * (t - t0)
            val theta: Double = omegaMagnitude * dt
            gyroDeltaRotation = Quaternion.forRotation(x, y, z, theta)
        }
    }

    override fun fuseSensors() {
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
        gyroOrientation = Quaternion.interpolate(gyroOrientation, accelerationMagnetometerOrientation, interpolationFactor)
    }

    private fun updateOrientationFromGyroOrientation() {
        // TODO: get angles from quaternion directly
        val matrix = gyroOrientation.toRotationMatrix()
        val adjustedRotationMatrix = Matrix.fromFloatArray(getAdjustedRotationMatrix(matrix.toFloatArray()))
        val angles = getOrientationForRotationMatrix(adjustedRotationMatrix)
        onOrientationAnglesChanged(angles)
    }

    companion object {
        private const val EPSILON: Double = 0.000000001
    }
}

