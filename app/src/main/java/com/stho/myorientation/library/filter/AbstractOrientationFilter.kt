package com.stho.myorientation.library.filter

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.hardware.SensorManager
import android.os.ParcelFileDescriptor
import android.view.Surface
import com.stho.myorientation.*
import com.stho.myorientation.library.QuaternionAcceleration
import com.stho.myorientation.library.algebra.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


abstract class AbstractOrientationFilter(private val method: Method, options: IFilterOptions) : IOrientationFilter {

    private val acceleration: QuaternionAcceleration = QuaternionAcceleration(options.accelerationFactor)

    override val currentOrientation: Orientation
        get() = acceleration.position.toOrientation()

    override var deviceRotation: Int = Surface.ROTATION_0

    protected open fun onOrientationAnglesChanged(orientation: Orientation) {
        if (Rotation.requireAdjustmentForLookingAtThePhoneFromBelow(orientation)) {
            setOrientation(Rotation.adjustForLookingAtThePhoneFromBelow(orientation))
        } else {
            setOrientation(orientation)
        }
    }

    override val pdf: String =
        "Unknown.pdf"

    override val link: String =
        "https://ahrs.readthedocs.io/en/latest/"

    private fun setOrientation(orientation: Orientation) {
        Repository.instance.recordEntry(method, orientation)
        acceleration.rotateTo(orientation.rotation)
    }

    internal fun getAdjustedRotationMatrix(rotationMatrix: RotationMatrix): RotationMatrix =
        getAdjustedRotationMatrix(rotationMatrix, deviceRotation)

    internal fun getAdjustedRotationMatrix(rotationMatrix: FloatArray): FloatArray =
        getAdjustedRotationMatrix(rotationMatrix, deviceRotation)


    internal fun getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading: FloatArray, magnetometerReading: FloatArray, defaultValue: Quaternion): Quaternion =
        getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, deviceRotation, defaultValue)

    override fun fuseSensors() {
        // Nothing to do in the general case. Only on fusion filter...
    }

    companion object {

        private const val EPSILON: Double = 0.000000001

        internal fun readDocumentation(context: Context, fileName: String): String =
            try {
                val asset: InputStream = context.assets.open(fileName)
                val size: Int = asset.available()
                val buffer = ByteArray(size)
                asset.read(buffer)
                asset.close()
                String(buffer)
            } catch(ex: Exception) {
                ex.toString()
            }

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
    }
}

