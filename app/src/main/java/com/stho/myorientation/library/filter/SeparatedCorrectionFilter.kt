package com.stho.myorientation.library.filter

import com.stho.myorientation.ISeparatedCorrectionFilterOptions
import com.stho.myorientation.Measurements
import com.stho.myorientation.Method
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import kotlin.math.*


/**
 * Fast AHRS Filter for Accelerometer, Magnetometer, and Gyroscope Combination with Separated Sensor Corrections
 *      by Josef Justa, Vaclav Smidl, Alex Hamacek, April 2020
 */
class SeparatedCorrectionFilter(options: ISeparatedCorrectionFilterOptions) :
    AbstractOrientationFilter(Method.SeparatedCorrectionFilter, options) {

    private val lambda1: Double = options.lambda1
    private val lambda2: Double = options.lambda2

    override val pdf: String
        get() = "SeparatedCorrectionFilter.pdf"

    override val link: String
        get() = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7420292/"


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

    private val timer: Timer = Timer()
    private val mode: Mode = options.separatedCorrectionMode

    override fun updateReadings(type: Measurements.Type, values: FloatArray) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (type) {
            Measurements.Type.Magnetometer -> {
                System.arraycopy(values, 0, magnetometerReading, 0, magnetometerReading.size)
                hasMagnetometer = true
            }
            Measurements.Type.Accelerometer -> {
                System.arraycopy(values, 0, accelerometerReading, 0, accelerometerReading.size)
                hasAcceleration = true
            }
            Measurements.Type.Gyroscope -> {
                System.arraycopy(values, 0, gyroscopeReading, 0, gyroscopeReading.size)
                hasGyro = true
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

        super.onOrientationAnglesChanged(estimate.toOrientation())
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
        val omega = Vector.fromFloatArray(gyroscopeReading)

        if (!hasEstimate) {
            estimate = super.getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, Quaternion.default)
        }

        val qDot = getRotationFromGyro(omega, dt, mode)

        val prediction = estimate * qDot

        // prediction of a := Vector(0.0, 0.0, 1.0).rotateBy(prediction.inverse()) --> normalized
        val aPrediction = Vector(
                x = prediction.m31,
                y = prediction.m32,
                z = prediction.m33,
        )

        // reference direction of magnetic field in earth frame after distortion compensation
        val b: Vector = flux(aPrediction, m)

        // prediction of m := Vector(0.0, b.y, b.z).rotateBy(prediction.inverse()) --> normalized
        val mPrediction = Vector(
            x = prediction.m21 * b.y + prediction.m31 * b.z,
            y = prediction.m22 * b.y + prediction.m32 * b.z,
            z = prediction.m23 * b.y + prediction.m33 * b.z,
        )

        val aAlpha = acos(a.dot(aPrediction))
        val aCorrection = a.cross(aPrediction).normalize()

        val mAlpha = acos(Vector.dot(m, mPrediction))
        val mCorrection = Vector.cross(m, mPrediction).normalize()

        val aBeta = min(aAlpha * lambda1, lambda2)
        val mBeta = min(mAlpha * lambda1, lambda2)

        val fCorrection: Vector = aCorrection * (aBeta / 2) + mCorrection * (mBeta / 2)

        val qCorrection = when(mode) {
            Mode.SCF -> {
                val n = fCorrection.norm()
                val c = sin(n) / n
                val v = fCorrection * c
                Quaternion(s = sqrt(1 - v.normSquare()), v = v)
            }
            Mode.FSCF -> {
                Quaternion(s = 1.0, v = fCorrection)
            }
        }

        estimate = (prediction * qCorrection).normalize()

        hasEstimate = true
    }

    private fun getQuaternionFromAccelerometerMagnetometerReadings() {

    }

    companion object {
        /**
         * Returns the magnetic field in earth frame after distortion correction
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

        /**
         * The rotation is not exactly the same, but similar to the "default approach":
         *      theta = ||omega|| * dt
         *      Q = Q(s = cos(theta/2), v = sin(theta2) * |omega|)
         */
        internal fun getRotationFromGyro(omega: Vector, dt: Double, mode: Mode): Quaternion {
            val alpha = omega.x * dt
            val beta = omega.y * dt
            val gamma = omega.z * dt

            return when (mode) {
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
        }
    }
}

