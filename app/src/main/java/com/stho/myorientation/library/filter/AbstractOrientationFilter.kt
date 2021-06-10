package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import android.view.Surface
import com.stho.myorientation.Entries
import com.stho.myorientation.Repository
import com.stho.myorientation.library.Acceleration
import com.stho.myorientation.library.algebra.*


abstract class AbstractOrientationFilter(private val method: Entries.Method, accelerationFactor: Double) : OrientationFilter {

    private val azimuthAcceleration: Acceleration = Acceleration(accelerationFactor)
    private val pitchAcceleration: Acceleration = Acceleration(accelerationFactor)
    private val rollAcceleration: Acceleration = Acceleration(accelerationFactor)
    private val centerAzimuthAcceleration: Acceleration = Acceleration(accelerationFactor)
    private val centerAltitudeAcceleration: Acceleration = Acceleration(accelerationFactor)

    override val currentOrientation: Orientation
        get() = Orientation(
                azimuth = Degree.normalize(azimuthAcceleration.position),
                pitch = Degree.normalizeTo180(pitchAcceleration.position),
                roll = Degree.normalizeTo180(rollAcceleration.position),
                centerAzimuth = Degree.normalize(centerAzimuthAcceleration.position),
                centerAltitude = Degree.normalizeTo180(centerAltitudeAcceleration.position)
        )

    override var deviceRotation: Int = Surface.ROTATION_0

    protected open fun onOrientationAnglesChanged(orientation: Orientation) {
        if (Rotation.requireAdjustmentForLookingAtThePhoneFromBelow(orientation)) {
            setOrientation(Rotation.adjustForLookingAtThePhoneFromBelow(orientation))
        } else {
            setOrientation(orientation)
        }
    }

    private fun setOrientation(orientation: Orientation) {
        Repository.instance.recordEntry(method, orientation)
        azimuthAcceleration.rotateTo(orientation.azimuth)
        pitchAcceleration.rotateTo(orientation.pitch)
        rollAcceleration.rotateTo(orientation.roll)
        centerAzimuthAcceleration.rotateTo(orientation.centerAzimuth)
        centerAltitudeAcceleration.rotateTo(orientation.centerAltitude)
    }

    internal fun getAdjustedRotationMatrix(rotationMatrix: RotationMatrix): RotationMatrix =
        getAdjustedRotationMatrix(rotationMatrix, deviceRotation)

    internal fun getAdjustedRotationMatrix(rotationMatrix: FloatArray): FloatArray =
        getAdjustedRotationMatrix(rotationMatrix, deviceRotation)


    internal fun getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading: FloatArray, magnetometerReading: FloatArray, defaultValue: Quaternion): Quaternion =
        AbstractOrientationFilter.getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, deviceRotation, defaultValue)

    override fun fuseSensors() {
        // Nothing to do in the general case. Only on fusion filter...
    }

    companion object {

        private const val EPSILON: Double = 0.000000001

        /**
         * Returns the rotation changes measured by the gyroscope as a Quaternion
         */
        internal fun getDeltaRotationFromGyroscope(omega: Vector, dt: Double): Quaternion {

            // Quaternion integration:
            // ds/dt = omega x s
            // with s = q # s0 # q* follows
            //      dq/dt = 0.5 * omega # q
            //      q(t) = exp(0.5 * omega * (t - t0)) # q0
            //      q(t) = cos(|v|) + v / |v| * sin(|v|) # q0 with v = 0.5 * omega * (t - t0)
            //      this is equivalent to a rotation by theta around the rotation vector omega/|omega| with theta = |omega| * (t - t0)

            // Calculate the angular speed
            val omegaMagnitude: Double = omega.norm()

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            return if (omegaMagnitude > EPSILON) {
                Quaternion.forRotation(u = omega / omegaMagnitude, theta = omegaMagnitude * dt)
            } else {
                Quaternion.forRotation(omega, omegaMagnitude * dt)
            }
        }

        /**
         * Return the quaternion for the rotation defined by accelerometer and magnetometer readings, of possible, or null otherwise
         */
        internal fun getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading: FloatArray, magnetometerReading: FloatArray, deviceRotation: Int, defaultValue: Quaternion): Quaternion {
            val rotationMatrix: FloatArray = FloatArray(9)
            if (!SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
                return defaultValue
            }
            val adjustedRotationMatrix = RotationMatrix.fromFloatArray(getAdjustedRotationMatrix(rotationMatrix, deviceRotation))
            return adjustedRotationMatrix.toQuaternion()
        }

        internal fun getQuaternionFromAccelerationMagnetometer(accelerometer: Vector, magnetometer: Vector, deviceRotation: Int, defaultValue: Quaternion): Quaternion {
            val rotationMatrix = Rotation.getRotationMatrixFromAccelerationMagnetometer(accelerometer, magnetometer)
            if (rotationMatrix == null) {
                // free fall etc
                return defaultValue
            }
            val adjustedRotationMatrix = getAdjustedRotationMatrix(rotationMatrix, deviceRotation)
            return adjustedRotationMatrix.toQuaternion()
        }

        private fun getAdjustedRotationMatrix(rotationMatrix: RotationMatrix, deviceRotation: Int): RotationMatrix {
            val rotationMatrixAdjusted = getAdjustedRotationMatrix(rotationMatrix.toFloatArray(), deviceRotation)
            return RotationMatrix.fromFloatArray(rotationMatrixAdjusted)
        }

        /**
         * See the following training materials from google.
         * https://codelabs.developers.google.com/codelabs/advanced-android-training-sensor-orientation/index.html?index=..%2F..advanced-android-training#0
         */
        private fun getAdjustedRotationMatrix(rotationMatrix: FloatArray, deviceRotation: Int): FloatArray {
            val adjustedRotationMatrix = FloatArray(9)
            when (deviceRotation) {
                Surface.ROTATION_90 -> {
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_X,
                        adjustedRotationMatrix
                    )
                    return adjustedRotationMatrix
                }
                Surface.ROTATION_180 -> {
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_MINUS_X,
                        SensorManager.AXIS_MINUS_Y,
                        adjustedRotationMatrix
                    )
                    return adjustedRotationMatrix
                }
                Surface.ROTATION_270 -> {
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_X,
                        adjustedRotationMatrix
                    )
                    return adjustedRotationMatrix
                }
                else -> {
                    return rotationMatrix
                }
            }
        }

    }
}

