package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import android.util.Log
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Rotation
import com.stho.myorientation.library.algebra.Vector
import kotlin.math.*


/**
 * Fast AHRS Filter for Accelerometer, Magnetometer, and Gyroscope Combination with Separated Sensor Corrections
 *      by Josef Justa, Vaclav Smidt, Alex Hamacek, April 2020
 */
class SeparatedCorrectionFilter(private val mode: Mode, accelerationFactor: Double = 0.7) : AbstractOrientationFilter(
    Entries.Method.SeparatedCorrectionFilter,
    accelerationFactor
), OrientationFilter {

    enum class Mode {
        /**
         * Separated Correction Filter
         */
        SCF,

        /**
         * Fast Separated Correction Filter
         */
        FSCF,
    }

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)

    private var hasMagnetometer: Boolean = false
    private var hasAcceleration: Boolean = false
    private var hasGyro: Boolean = false
    private var hasEstimate: Boolean = false

    private var timer: Timer = Timer()

    override fun updateReadings(type: Measurements.Type, values: FloatArray) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (type) {
            Measurements.Type.Magnetometer -> {
                System.arraycopy(values, 0, magnetometerReading, 0, magnetometerReading.size)
                hasMagnetometer = true
                Log.d("Magnetometer", "Readings")
            }
            Measurements.Type.Accelerometer -> {
                System.arraycopy(values, 0, accelerometerReading, 0, accelerometerReading.size)
                hasAcceleration = true
                Log.d("Accelerometer", "Readings")
            }
            Measurements.Type.Gyroscope -> {
                System.arraycopy(values, 0, gyroscopeReading, 0, gyroscopeReading.size)
                hasGyro = true
                Log.d("Gyro", "Readings")
                updateOrientationAnglesFromGyroscope()
            }
        }
    }

    override fun reset() {
        hasMagnetometer = false
        hasAcceleration = false
        hasGyro = false
    }

    private fun updateOrientationAnglesFromGyroscope() {
        if (!hasMagnetometer)
            return

        if (!hasAcceleration)
            return

        if (!hasGyro)
            return

        val dt = timer.getNextTime()

        filterUpdate(dt)

        updateOrientationFromGyroOrientation()
    }

    private fun updateOrientationFromGyroOrientation() {
        val matrix = estimate.toRotationMatrix()
        val orientation = matrix.toOrientation()
        onOrientationAnglesChanged(orientation)
    }


    /**
     * Estimated orientation quaternion with initial conditions
     */
    private var estimate: Quaternion = Quaternion.default

    /**
     * Function to compute one filter iteration
     */
    private fun filterUpdate(dt: Double) {

        val a = Vector.fromFloatArray(accelerometerReading).normalize()
        val m = Vector.fromFloatArray(magnetometerReading).normalize()
        val w = Vector.fromFloatArray(gyroscopeReading)

        if (!hasEstimate) {
            estimate = super.getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, Quaternion.default)
        }

/*
        val alpha = w.x * dt
        val beta = w.y * dt
        val gamma = w.z * dt

        val qDot = when (mode) {
            Mode.SCF -> {
                val qDotX = sin(alpha / 2)
                val qDotY = sin(beta / 2)
                val qDotZ = sin(gamma / 2)
                val qDotS = sqrt(1 - qDotX * qDotX - qDotY * qDotY - qDotZ * qDotZ)
                Quaternion(s = qDotS, x = qDotX, y = qDotY, z = qDotZ)
            }
            Mode.FSCF -> {
                Quaternion(s = 1.0, x = alpha / 2, y = beta / 2, z = gamma / 2)
            }
        }

*/
        val qDot = Rotation.getRotationFromGyro(w, dt)

        val prediction = estimate * qDot

        val a_pred = Vector(
                x = prediction.m31,
                y = prediction.m32,
                z = prediction.m33,
        ) // == Vector(0.0, 0.0, 1.0).rotateBy(prediction.inverse()) !!

        // reference direction of magnetic field in earth frame after distortion compensation
        val b: Vector = flux(a_pred, m)

        val m_pred = Vector(
            x = prediction.m21 * b.y + prediction.m31 * b.z,
            y = prediction.m22 * b.y + prediction.m32 * b.z,
            z = prediction.m23 * b.y + prediction.m33 * b.z,
        ) // = Vector(0.0, b.y, b.z).rotateBy(prediction.inverse())

        val alpha_a = acos(Vector.dot(a, a_pred))
        val a_corr = Vector.cross(a, a_pred).normalize()

        val alpha_m = acos(Vector.dot(m, m_pred))
        val m_corr = Vector.cross(m, m_pred).normalize()

        val lambda_a = 0.01
        val lambda_m = 0.01
        val beta_a = f_correction(alpha = alpha_a, lambda = lambda_a)
        val beta_m = f_correction(alpha = alpha_m, lambda = lambda_m)

        val f_corr = a_corr * (beta_a / 2) + m_corr * (beta_m / 2)

        val q_corr = when(mode) {
            Mode.SCF -> {
                val n = f_corr.norm()
                val c = sin(n) / n
                val v = f_corr * c
                Quaternion(s = sqrt(1 - v.normSquare()), v = v)
            }
            Mode.FSCF -> {
                Quaternion(s = 1.0, v = f_corr)
            }
        }

        estimate = prediction * q_corr


        hasEstimate = true
    }

    companion object {
        private const val EPSILON: Double = 0.000000001
        private const val GAIN: Double = 0.1

        private fun f_correction(alpha: Double, lambda: Double): Double =
            min(alpha * lambda, lambda)

        /**
         * Returns the magnetic field in earth frame after distortion correction:
         */
        internal fun flux(a: Vector, m: Vector): Vector {
            val bz = (a.x * m.x + a.y * m.y + a.z * m.z) / (a.norm() * m.norm())
            val by = sqrt(1 - bz * bz)
            return Vector(
                x = 0.0,
                y = by,
                z = bz,
            )
        }
    }
}

