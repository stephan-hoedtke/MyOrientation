package com.stho.myorientation.library.filter

import com.stho.myorientation.IExtendedComplementaryFilterOptions
import com.stho.myorientation.Measurements
import com.stho.myorientation.Method
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector


/**
 *  An Extended Complementary Filter for Full-Body MARG Orientation Estimation
 *      by Sebastian Madgwick, Samuel Wilson, Ruth Turk,
 *      Jane Burridge, Christos Kapatos and Ravi Vaidyananthan,
 *      August 2020
 */
class ExtendedComplementaryFilter(options: IExtendedComplementaryFilterOptions) :
    AbstractOrientationFilter(Method.ExtendedComplementaryFilter, options) {

    private val kNorm: Double = options.kNorm
    private val kInit: Double = options.kInit
    private val tInit: Double = options.tInit
    private val mMax: Double = options.mMax
    private val mMin: Double = options.mMin

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)

    private var hasMagnetometer: Boolean = false
    private var hasAcceleration: Boolean = false
    private var hasGyro: Boolean = false
    private var hasEstimate: Boolean = false

    private val timer: Timer = Timer()

    override val pdf: String =
        "ExtendedComplementaryFilter.pdf"

    override val link: String =
        "https://www.researchgate.net/publication/341718163_An_Extended_Complementary_Filter_ECF_for_Full-Body_MARG_Orientation_Estimation"

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
        hasEstimate = false
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
     * Estimated orientation quaternion with initial conditions: rotating from sensor frame to earth frame
     */
    private var estimate: Quaternion = Quaternion.default

    /**
     * Function to compute one filter iteration
     */
    private fun filterUpdate(dt: Double) {

        val a = Vector.fromFloatArray(accelerometerReading)
        val m = Vector.fromFloatArray(magnetometerReading)
        val w = Vector.fromFloatArray(gyroscopeReading)

        if (!hasEstimate) {
            estimate = super.getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, Quaternion.default)
        }

        val error = error(estimate, a, m)

        val gain = gain()
        val omega = w + error * gain
        val qDot = estimate * omega.asQuaternion() * 0.5
        val delta = qDot * dt
        estimate = (estimate + delta).normalize()

        hasEstimate = true
    }

    private fun gain(): Double {
        val t = timer.getTime()
        return if (t < tInit) {
            kNorm + ((tInit - t) / tInit) * (kInit - kNorm)
        } else {
            kNorm
        }
    }

    private fun error(q: Quaternion, a: Vector, m: Vector): Vector {
        val aNorm = a.norm()
        val mNorm = m.norm()
        return when {
            aNorm > 0 && mMin < mNorm && mNorm < mMax -> {
                val aHat = a / aNorm
                val mHat = m / mNorm
                aError(q, aHat) + eError(q, aHat, mHat)
            }
            aNorm > 0 -> {
                val aHat = a / aNorm
                aError(q, aHat)
            }
            else -> {
                Vector.default
            }
        }
    }


    companion object {

        /**
         * Prediction of the normalized acceleration after rotating the reference Vector (0, 0, 1) from earth to sensor frame
         *      aPrediction = (0, 0, 1).rotateBy(q.inverse()) = M(q).transpose() * (0, 0, 1)
         */
        internal fun aError(q: Quaternion, a: Vector): Vector {
            val prediction = Vector(
                x = q.m31,
                y = q.m32,
                z = q.m33,
            )
            return a.cross(prediction)
        }

        /**
         * Prediction of the normalized cross product of acceleration and magnetic field after rotating the reference Vector (-1, 0, 0) from earth to sensor frame
         *      ePrediction = (-1, 0, 0).rotateBy(q.inverse()) = M(q).transpose() * (-1, 0, 0)
         */
        internal fun eError(q: Quaternion, a: Vector, m: Vector): Vector {
            val prediction = Vector(
                x = -q.m11,
                y = -q.m12,
                z = -q.m13,
            )
            val e = a.cross(m)
            return e.cross(prediction)
        }
    }
}

