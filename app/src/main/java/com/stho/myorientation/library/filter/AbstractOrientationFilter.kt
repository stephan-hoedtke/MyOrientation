package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import android.view.Surface
import com.stho.myorientation.Entries
import com.stho.myorientation.Repository
import com.stho.myorientation.library.Acceleration
import com.stho.myorientation.library.Degree
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Orientation
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Rotation


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

    /**
     * Returns the orientation for a rotation matrix which converts a vector from device frame into earth frame
     *      as it is used by the sensor manager
     */
    internal fun getOrientationForRotationMatrix(r: Matrix): Orientation =
        Rotation.rotationMatrixToOrientation(r.transpose())

    /**
     * Returns the orientation for a quaternion which converts a vector from earth frame into device frame
     */
    internal fun getOrientationForQuaternion(q: Quaternion): Orientation =
        Rotation.quaternionToOrientation(q)

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

