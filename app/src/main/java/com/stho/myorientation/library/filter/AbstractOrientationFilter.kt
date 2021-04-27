package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import android.view.Surface
import com.stho.myorientation.Entries
import com.stho.myorientation.Repository
import com.stho.myorientation.library.Acceleration
import com.stho.myorientation.library.Degree
import com.stho.myorientation.library.Orientation


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

    internal fun lookAtThePhoneFromAbove(roll: Double) =
        -90 < roll && roll < 90

    internal fun adjustForLookingAtThePhoneFromBelow(angles: DoubleArray): DoubleArray =
        doubleArrayOf(
            -angles[0],
            180 - angles[1],
            180 - angles[2],
            angles[3],
            angles[4]
        )

    protected open fun onOrientationAnglesChanged(angles: DoubleArray) {
        if (lookAtThePhoneFromAbove(angles[2])) {
            setOrientationAngles(angles)
        } else {
            setOrientationAngles(adjustForLookingAtThePhoneFromBelow(angles))
        }
    }

    private fun setOrientationAngles(angles: DoubleArray) {
        val sensorOrientation = Orientation(
                angles[0],
                angles[1],
                angles[2],
                angles[3],
                angles[4]
        )

        Repository.instance.recordEntry(method, sensorOrientation)

        azimuthAcceleration.rotateTo(sensorOrientation.azimuth)
        pitchAcceleration.rotateTo(sensorOrientation.pitch)
        rollAcceleration.rotateTo(sensorOrientation.roll)
        centerAzimuthAcceleration.rotateTo(sensorOrientation.centerAzimuth)
        centerAltitudeAcceleration.rotateTo(sensorOrientation.centerAltitude)
    }

    /**
     *
     * 1) azimuth and altitude: north vector = (0, 1, 0) --> R * north vector = (x = R[1], y = R[4], z = R[7])
     *      azimuth = atan2(x, y) = atan2(R[1], R[4])
     *      altitude = -asin(R[Z]) = asin(-R[7)
     *
     * 2) roll:
     *      roll = atan2(x, z) = atan2(R[6], R[8])
     *
     *      see: SensorManager.getOrientation()
     *      values[0] = (float) Math.atan2(R[1], R[4]);
     *      values[1] = (float) Math.asin(-R[7]);
     *      values[2] = (float) Math.atan2(-R[6], R[8]);
     *
     * 3) center: center vector = (0, 0, -1) --> R * center vector = (x = -R[2], y = -R[5], z = -R[8])
     *      azimuth = atan2(x, y) = atan2(-R[2], -R[5])
     *      altitude = asin(z) = asin(-R[8])
     */
    internal fun getOrientationAnglesFromRotationMatrix(R: FloatArray) =
        doubleArrayOf(
            Degree.arcTan2(R[1].toDouble(), R[4].toDouble()),       // pointer azimuth
            Degree.arcSin(-R[7].toDouble()),                         // pitch (opposite of pointer altitude)
            Degree.arcTan2(-R[6].toDouble(), R[8].toDouble()),      // roll
            Degree.arcTan2(-R[2].toDouble(), -R[5].toDouble()),     // center azimuth
            Degree.arcSin(-R[8].toDouble())                         // center altitude
        )

    /**
     * See the following training materials from google.
     * https://codelabs.developers.google.com/codelabs/advanced-android-training-sensor-orientation/index.html?index=..%2F..advanced-android-training#0
     */
    internal fun getAdjustedRotationMatrix(rotationMatrix: FloatArray): FloatArray {
        val rotationMatrixAdjusted = FloatArray(9)
        when (deviceRotation) {
            Surface.ROTATION_90 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    rotationMatrixAdjusted
                )
                return rotationMatrixAdjusted
            }
            Surface.ROTATION_180 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_MINUS_X,
                    SensorManager.AXIS_MINUS_Y,
                    rotationMatrixAdjusted
                )
                return rotationMatrixAdjusted
            }
            Surface.ROTATION_270 -> {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_MINUS_Y,
                    SensorManager.AXIS_X,
                    rotationMatrixAdjusted
                )
                return rotationMatrixAdjusted
            }
            else -> {
                return rotationMatrix
            }
        }
    }

    override fun fuseSensors() {
        // Nothing to do in the general case. Only on fusion filter...
    }
}

