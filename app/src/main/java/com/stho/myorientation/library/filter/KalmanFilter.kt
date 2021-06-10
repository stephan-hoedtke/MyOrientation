package com.stho.myorientation.library.filter

import android.util.Log
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.*
import kotlin.math.sqrt

/**
 * see: Extended Kalman Filter on AHRS: Attitude and Heading Reference Systems
 *      https://ahrs.readthedocs.io/en/latest/index.html
 *      https://ahrs.readthedocs.io/en/stable/filters/ekf.html
 *
 * see also: https://thepoorengineer.com/en/ekf-impl/
 */
class KalmanFilter(accelerationFactor: Double = 0.7) : AbstractOrientationFilter(Entries.Method.KalmanFilter, accelerationFactor), OrientationFilter {

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)
    private var timer: Timer = Timer()
    private var hasMagnetometer: Boolean = false
    private var hasAcceleration: Boolean = false
    private var hasGyro: Boolean = false
    private var hasEstimate: Boolean = false

    private var b: Vector = Vector(0.0, 18.00, -44.00).normalize()

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
                updateOrientationAnglesFromAcceleration()
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
        hasEstimate = false
    }

    private fun updateOrientationAnglesFromAcceleration() {
        // nothing
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
     * Covariance matrix P - [4 x 4]
     */
    private var covariance: Matrix = Matrix.E(4) // Matrix 4 x 4

    /**
     * Function to compute one filter iteration
     */
    private fun filterUpdate(dt: Double) {

        val a = Vector.fromFloatArray(accelerometerReading).normalize()
        val m = Vector.fromFloatArray(magnetometerReading).normalize()
        val omega = Vector.fromFloatArray(gyroscopeReading)

        b = flux(a, m, b)

        if (!hasEstimate) {
            estimate = super.getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, Quaternion.default)
            covariance = Matrix.E(4)
        }

        kalman(a, m, omega, dt)
    }

    /**
     * Predicted state current orientation as Quaternion q
     *
     *      q = f(q, w)
     *
     *      --> q = {I + dt/2 Omega(w)} * q
     */
    private fun predictState(q: Quaternion, omega: Vector, dt: Double): Quaternion {
        val f = dt / 2
        val fwx = f * omega.x
        val fwy = f * omega.y
        val fwz = f * omega.z
        return Quaternion(
            s = q.s - fwx * q.x - fwy * q.y - fwz * q.z,
            x = q.x + fwx * q.s - fwy * q.z + fwz * q.y,
            y = q.y + fwx * q.z + fwy * q.s - fwz * q.x,
            z = q.z - fwx * q.y + fwy * q.x + fwz * q.s,
        )
    }


    /**
     * Jacobian matrix df/dq of f(q,w) - [4 x 4]
     */
    @Suppress("FunctionName")
    private fun F(q: Quaternion, omega: Vector, dt: Double): Matrix {
        val f = dt / 2
        val fwx = f * omega.x
        val fwy = f * omega.y
        val fwz = f * omega.z
        // @formatter:off
        return Matrix.create(4, 4,
            1.0, -fwx, -fwy, -fwz,
            fwx,  1.0,  fwz, -fwy,
            fwy, -fwz,  1.0,  fwx,
            fwz,  fwy, -fwx,  1.0,
        )
        // @formatter:on
    }

    /**
     * Jacobian matrix df/dw of f(q,w) - [4 x 3]
     */
    @Suppress("FunctionName")
    private fun W(q: Quaternion, dt: Double): Matrix {
        val f = dt / 2
        val fqs = f * q.s
        val fqx = f * q.x
        val fqy = f * q.y
        val fqz = f * q.z
        // @formatter:off
        return Matrix.create(4, 3,
            -fqx, -fqy, -fqz,
             fqs, -fqz,  fqy,
             fqz,  fqs, -fqx,
            -fqy,  fqx,  fqs,
        )
        // @formatter:on
    }

    /**
     * Jacobian matrix [dh/dq] for h(q,g,b) = [M(q*) * g, M(q*) * b] - [6 x 4]
     */
    @Suppress("SpellCheckingInspection", "FunctionName")
    private fun H(q: Quaternion, g: Vector, b: Vector): Matrix {
        val jg = Rotation.Jacobian.create(q, g)
        val jb = Rotation.Jacobian.create(q, b)
        // @formatter:off
        return Matrix.create(6, 4,
            jg.dfxds, jg.dfxdx, jg.dfxdy, jg.dfxdz,
            jg.dfyds, jg.dfydx, jg.dfydy, jg.dfydz,
            jg.dfzds, jg.dfzdx, jg.dfzdy, jg.dfzdz,
            jb.dfxds, jb.dfxdx, jb.dfxdy, jb.dfxdz,
            jb.dfyds, jb.dfydx, jb.dfydy, jb.dfydz,
            jb.dfzds, jb.dfzdx, jb.dfzdy, jb.dfzdz,
        )
        // @formatter:on
    }

    /**
     * estimate + rotation --> phi
     */
    @Suppress("LocalVariableName")
    private fun kalman(a: Vector, m: Vector, omega: Vector, dt: Double) {

        // Prediction
        val F = F(estimate, omega, dt) // Matrix [4 x 4]
        val W = W(estimate, dt) // Matrix [4 x 3]
        val prediction = predictState(estimate, omega, dt) // Quaternion
        val covariancePrediction =
            F * covariance * F.transpose() + W * W.transpose() * SIGMA_SQUARE_GYRO // Matrix [4 x 4]

        // Correction
        val g = Vector(0.0, 0.0, 1.0)
        val b = b.normalize()

        // calculate v := z - h(q) where h(q) = [M(q*) * g, M(q*) * b]
        val matrix: RotationMatrix = prediction.toRotationMatrix().transpose() // Matrix [3 x 3]
        val gHat = matrix * g // Vector [3]
        val bHat = matrix * b // Vector [3]
        val v: DoubleArray = DoubleArray(6).apply {
            this[0] = a.x - gHat.x
            this[1] = a.y - gHat.y
            this[2] = a.z - gHat.z
            this[3] = m.x - bHat.x
            this[4] = m.y - bHat.y
            this[5] = m.z - bHat.z
        } // Vector [6]

        val H = H(prediction, g, b) // Matrix [6 x 4]
        val PH = covariancePrediction * H.transpose() // Matrix [4 x 6]
        val S = H * PH // Matrix [6 x 6]
        // plus R (sigma square a * I, sigma square m *I)
        S[0, 0] += SIGMA_SQUARE_ACCELEROMETER
        S[1, 1] += SIGMA_SQUARE_ACCELEROMETER
        S[2, 2] += SIGMA_SQUARE_ACCELEROMETER
        S[3, 3] += SIGMA_SQUARE_MAGNETOMETER
        S[4, 4] += SIGMA_SQUARE_MAGNETOMETER
        S[5, 5] += SIGMA_SQUARE_MAGNETOMETER

        val K = PH * S.invert() // Matrix [4 x 6]

        // calculate new estimate
        val correctionVector = K * v // Vector 4
        estimate = Quaternion(
            s = prediction.s + correctionVector[0],
            x = prediction.x + correctionVector[1],
            y = prediction.y + correctionVector[2],
            z = prediction.z + correctionVector[3],
        )

        val correctionMatrix = Matrix.E(4) - K * H // Matrix [4 x 4]
        covariance = correctionMatrix * covariancePrediction // Matrix [4 x 4]
    }

    companion object {

        private const val LOW_PASS_GAIN = 0.1
        private const val SIGMA_SQUARE_GYRO = 0.3 * 0.3
        private const val SIGMA_SQUARE_ACCELEROMETER = 0.5 * 0.5
        private const val SIGMA_SQUARE_MAGNETOMETER = 0.8 * 0.8


        /**
         * Returns the magnetic field in earth frame after distortion correction
         */
        internal fun flux(a: Vector, m: Vector, b: Vector): Vector {
            val zz = (a.x * m.x + a.y * m.y + a.z * m.z) / (a.norm() * m.norm())
            val bz = b.z + LOW_PASS_GAIN * (zz - b.z)
            val by = sqrt(1 - bz * bz)
            return Vector(
                x = 0.0,
                y = by,
                z = bz,
            )
        }
    }
}

