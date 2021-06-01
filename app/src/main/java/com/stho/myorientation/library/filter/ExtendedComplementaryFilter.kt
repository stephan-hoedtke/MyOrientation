package com.stho.myorientation.library.filter

import android.util.Log
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import com.stho.myorientation.library.f11
import kotlin.math.min


/**
 *  An Extended Complementary Filter for Full-Body MARG Orientation Estimation
 *      by Sebastian Madgwick, Samuel Wilson, Ruth Turk,
 *      Jane Burridge, Christos Kapatos and Ravi Vaidyananthan,
 *      August 2020
 */
class ExtendedComplementaryFilter(accelerationFactor: Double = 0.7) : AbstractOrientationFilter(
    Entries.Method.ExtendedComplementaryFilter,
    accelerationFactor
), OrientationFilter {

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

        super.onOrientationAnglesChanged(estimate.toOrientation())
    }


    /**
     * Estimated orientation quaternion with initial conditions: rotating from sensor frame to earth frame
     */
    private var estimate: Quaternion = Quaternion.default

    /**
     * The adjustable gain ...
     */
    private var gain: Double = GAIN

    /**
     * Use the previous |error| to adjust the gain in each step...
     */
    private var previousErrorNormSquare: Double = 0.0

    /**
     * Function to compute one filter iteration
     */
    private fun filterUpdate(dt: Double) {

        val a = Vector.fromFloatArray(accelerometerReading)
        val m = Vector.fromFloatArray(magnetometerReading)
        val w = Vector.fromFloatArray(gyroscopeReading)

        Log.d("ECF", "Estimate: s=${estimate.s.f11()}, x=${estimate.x.f11()}, y=${estimate.y.f11()}, z=${estimate.z.f11()}")

        if (!hasEstimate) {
            estimate = super.getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, Quaternion.default)
        }

        Log.d("ECF", "Accelerometer: Reading(x=${a.x.f11()}, y=${a.y.f11()}, z=${a.z.f11()}) ")
        Log.d("ECF", "Magnetometer: Reading(x=${m.x.f11()}, y=${m.y.f11()}, z=${m.z.f11()}) ")

        val error = error(estimate, a, m)

        adjustGain(w, error)

        val omega = w - error * gain
        val qDot = estimate * omega.asQuaternion() * 0.5
        val delta = qDot * dt
        estimate = (estimate + delta).normalize()

        Log.d("ECF", "Gyro(x=${w.x.f11()}, y=${w.y.f11()}, z=${w.z.f11()}) " +
                "Error(x=${error.x.f11()}, y=${error.y.f11()}, z=${error.z.f11()}) |e|=${error.norm().f11()} gain=${gain.f11()} " +
                "New Estimate: s=${estimate.s.f11()}, x=${estimate.x.f11()}, y=${estimate.y.f11()}, z=${estimate.z.f11()}")

        hasEstimate = true
    }

    private fun adjustGain(w: Vector, error: Vector) {
        val normSquare = error.normSquare()
        if (hasNoGyroData(w)) {
            when {
                normSquare > previousErrorNormSquare + TOLERANCE -> gain /= 2
                normSquare < previousErrorNormSquare && gain < GAIN -> gain *= 2
            }
        }
        previousErrorNormSquare = normSquare
    }

    private fun hasNoGyroData(w: Vector): Boolean =
        w.normSquare() < TOLERANCE

    companion object {
        private const val GAIN: Double = 0.1
        private const val TOLERANCE: Double = 0.00000001

        internal fun error(q: Quaternion, a: Vector, m: Vector): Vector {
            val aHat = a.normalize()
            val mHat = m.normalize()

            val ea = aError(q, aHat)
            val ee = eError(q, mHat, mHat)

            // TODO: Apply conditions and magnetic disturbance ...
            return ea + ee
        }

        private fun aError(q: Quaternion, aa: Vector): Vector {
            // Prediction of the normalized acceleration after rotating the reference Vector (0, 0, 1) from earth to sensor frame
            // --> aPrediction = (0, 0, 1).rotateBy(q.inverse()) = M(q).transpose() * (0, 0, 1)
            val prediction = Vector(
                x = q.m31,
                y = q.m32,
                z = q.m33,
            )
            return aa.cross(prediction)
        }

        private fun eError(q: Quaternion, aa: Vector, mm: Vector): Vector {

            // Prediction of the normalized cross product of acceleration and magnetic field after rotating the reference Vector (-1, 0, 0) from earth to sensor frame
            // --> ePrediction = (-1, 0, 0).rotateBy(q.inverse()) = M(q).transpose() * (-1, 0, 0)
            val prediction = Vector(
                x = -q.m11,
                y = -q.m12,
                z = -q.m13,
            )

            return aa.cross(mm).cross(prediction)

        }
    }
}
