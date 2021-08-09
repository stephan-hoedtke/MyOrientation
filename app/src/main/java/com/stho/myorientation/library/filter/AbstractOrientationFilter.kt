package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import android.view.Surface
import com.stho.myorientation.*
import com.stho.myorientation.library.IQuaternionStorage
import com.stho.myorientation.library.QuaternionAcceleration
import com.stho.myorientation.library.QuaternionStorage
import com.stho.myorientation.library.algebra.*


abstract class AbstractOrientationFilter(private val method: Method, options: IFilterOptions) : IOrientationFilter {

    private val storage: IQuaternionStorage = createQuaternionStorage(options)

    override val currentOrientation: Orientation
        get() = storage.position.toOrientation()

    override var deviceRotation: Int = Surface.ROTATION_0

    protected open fun onOrientationAnglesChanged(orientation: Orientation) {
        setOrientation(orientation.normalize())
    }

    override val pdf: String =
        "Unknown.pdf"

    override val link: String =
        "https://ahrs.readthedocs.io/en/latest/"

    private fun setOrientation(orientation: Orientation) {
        Repository.instance.recordEntry(method, orientation)
        storage.setTargetPosition(Quaternion.fromRotationMatrix(orientation.rotation))
    }

    internal fun getAdjustedRotationMatrix(rotationMatrix: RotationMatrix): RotationMatrix =
        getAdjustedRotationMatrix(rotationMatrix, deviceRotation)

    internal fun getAdjustedRotationMatrix(rotationMatrix: FloatArray): FloatArray =
        getAdjustedRotationMatrix(rotationMatrix, deviceRotation)


    internal fun getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading: FloatArray, magnetometerReading: FloatArray, defaultValue: Quaternion): Quaternion =
        getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, deviceRotation, defaultValue)

    companion object {

        private const val EPSILON: Double = 0.000000001

        /**
         * Return the quaternion for the rotation defined by accelerometer and magnetometer readings, of possible, or null otherwise
         */
        internal fun getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading: FloatArray, magnetometerReading: FloatArray, deviceRotation: Int, defaultValue: Quaternion): Quaternion {
            val rotationMatrix = FloatArray(9)
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

        private fun createQuaternionStorage(options: IFilterOptions): IQuaternionStorage =
            when (options.useAcceleration) {
                true -> QuaternionAcceleration(options.accelerationFactor)
                false -> QuaternionStorage()
            }
    }
}

