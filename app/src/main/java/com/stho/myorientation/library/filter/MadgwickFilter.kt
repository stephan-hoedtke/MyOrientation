package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import android.util.Log
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import com.stho.myorientation.library.f11
import com.stho.myorientation.library.f2
import kotlin.math.PI
import kotlin.math.sqrt


/**
 * An efficient orientation filter for inertial and inertial/magnetic sensor arrays
 *      by Sebastian O.H. Madgwick, 2010
 *      https://courses.cs.washington.edu/courses/cse474/17wi/labs/l4/madgwick_internal_report.pdf
 *
 *      implementation sample: https://github.com/Josef4Sci/AHRS_Filter/blob/master/Filters/MadgwickAHRS3.m
 */
class MadgwickFilter(accelerationFactor: Double = 0.7) : AbstractOrientationFilter(
    Entries.Method.MadgwickFilter,
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

        updateOrientationFromGyroOrientation()
    }

    private fun orientationFromMagnetometerAcceleration(): Quaternion {
        val rotationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            // TODO: get quaternion from readings directly
            // Mind:
            // the rotation matrix from sensor is: rotating a sensor frame vector into the earth frame
            // while the quaternion rotates an earth frame vector into the sensor frame
            val adjustedRotationMatrix = Matrix.fromFloatArray(getAdjustedRotationMatrix(rotationMatrix)).transpose()
            return Quaternion.fromRotationMatrix(adjustedRotationMatrix)
        }
        else {
            return Quaternion.default
        }
    }

    private fun updateOrientationFromGyroOrientation() {
        // TODO: get angles from quaternion directly
        val matrix = estimate.toRotationMatrix().transpose()
        val angles = getOrientationForRotationMatrix(matrix)
        onOrientationAnglesChanged(angles)
    }

/*
    Description of the algorithm
    q: current estimation

    // 1) magnetic distortion compensation
    m -->
          h := q # m # q*
          b := [0, sqrt(hx^2+ hy^2), 0, hz]

    // 2) gradient as correction of q_dot using the magnetic field and acceleration
    m, a -->
          fa := q # a0 # q* - a.normalize()
          fm := q # b0 # q* - m.normalize()
          f := fa + fm
          J := Jacobian
          grad f := J * f

    // 3) correction of q_dot = dq/dt
          q_dot_error := grad f / || grad f ||

    // 4) correction of gyro
          omega_error := 2 * q # q_dot_error
          omega_bias := zeta * SUM(omega_error * dt)

    // 5) gyroscope measurement
    w ->
          omega = w - omega_bias
          q_dot_gyro := 1/2 q # omega

    // 6) sensor fusion
          q_dot := q_dot_gyro - beta * q_dot_error
          q := (q + q_dot * dt).normalize()
 */

    /**
     * Estimated orientation quaternion with initial conditions
     * SEq
     */
    private var estimate: Quaternion = Quaternion.default

    /**
     * estimated gyroscope bias error
     * w_b
     */
    private var gyroBias: Quaternion = Quaternion.default

    /**
     * Function to compute one filter iteration
     */
    private fun filterUpdate(dt: Double) {

        val a = Vector.fromFloatArray(accelerometerReading).normalize()
        val m = Vector.fromFloatArray(magnetometerReading).normalize()
        val w = Vector.fromFloatArray(gyroscopeReading).asQuaternion()

        if (!hasEstimate) {
            estimate = orientationFromMagnetometerAcceleration()
        }

        // reference direction of magnetic field in earth frame after distortion compensation
        val b: Vector = flux(estimate, m)
        //val b: Vector = flux(a, m)

        // EVALUATE
        val n0 = objectiveFunction(estimate, b.y, b.z, a, m).normSquare()
        Log.d("Estimate", "error=${n0.f11()} b=(0, ${b.y.f2()}, ${b.z.f2()}, 0) estimate x=${estimate.x.f11()}, y=${estimate.y.f11()}, z=${estimate.z.f11()}, s=${estimate.s.f11()}")


        // compute the gradient (matrix multiplication) estimated direction of the gyroscope error
        val qDotError: Quaternion = gradient(estimate, b.y, b.z, a, m).normalize()

        // compute angular estimated direction of the gyroscope error: omega_err
        val omegaError = estimate * qDotError * 2.0


        // compute gyroscope biases and correct gyro angle rotation
        gyroBias += omegaError * (dt * zeta)
        //val omega = w - gyroBias
        val omega = w

        // EVALUATE
        Log.d("Gyro", "omega x=${omega.x.f11()}, y=${omega.y.f11()}, z=${omega.z.f11()}, s=${omega.s.f11()}")

        if (omega.normSquare() > 0) {
            val qDotGyro = estimate * omega * 0.5
            val delta = qDotGyro * dt
            estimate += delta


            // EVALUATE
            val n1 = objectiveFunction(estimate, b.y, b.z, a, m).normSquare()
            Log.d("Gyro", "qDotGyro x=${qDotGyro.x.f11()}, y=${qDotGyro.y.f11()}, z=${qDotGyro.z.f11()}, s=${qDotGyro.s.f11()}")
            Log.d("Gyro", "dt=${dt.f11()} delta x=${delta.x.f11()}, y=${delta.y.f11()}, z=${delta.z.f11()}, s=${delta.s.f11()} n1=${n1.f11()}")
        }


/*
        var f = 1.0

        for (i in 1..100) {
            // compute then integrate the estimated quaternion rate
            val qDot = qDotError * (f * beta)
            val delta = qDot * dt
            val newEstimate = (estimate - delta).normalize()

            // EVALUATE
            val n2 = objectiveFunction(newEstimate, b.y, b.z, a, m).normSquare()
            if (n2 < n0) {
                estimate = newEstimate
                Log.d("Correction", "delta=${delta.norm().f11()} x=${delta.x.f11()}, y=${delta.y.f11()}, z=${delta.z.f11()}, s=${delta.s.f11()} n2=${n2.f11()} Step=$i")
                break
            }

            f /= 5
        }
*/

        // EVALUATE
        val n3 = objectiveFunction(estimate, b.y, b.z, a, m).normSquare()
        Log.d("Final", "error=${n3.f11()} estimate x=${estimate.x.f11()}, y=${estimate.y.f11()}, z=${estimate.z.f11()}, s=${estimate.s.f11()}")

        hasEstimate = true
    }

    internal data class ObjectiveFunction(
        val f1: Double, val f2: Double, val f3: Double, val f4: Double, val f5: Double, val f6: Double) {

        internal fun normSquare() =
                f1 * f1 + f2 * f2 + f3 * f3 + f4 * f4 + f5 * f5 + f6 * f6
    }

    internal data class Jacobian(
        val df1ds: Double, val df1dx: Double, val df1dy: Double, val df1dz: Double,
        val df2ds: Double, val df2dx: Double, val df2dy: Double, val df2dz: Double,
        val df3ds: Double, val df3dx: Double, val df3dy: Double, val df3dz: Double,
        val df4ds: Double, val df4dx: Double, val df4dy: Double, val df4dz: Double,
        val df5ds: Double, val df5dx: Double, val df5dy: Double, val df5dz: Double,
        val df6ds: Double, val df6dx: Double, val df6dy: Double, val df6dz: Double) {

        internal operator fun times(f: ObjectiveFunction): Quaternion =
                Quaternion(
                        s = 2 * (df1ds * f.f1 + df2ds * f.f2 + df3ds * f.f3 + df4ds * f.f4 + df5ds * f.f5 + df6ds * f.f6),
                        x = 2 * (df1dx * f.f1 + df2dx * f.f2 + df3dx * f.f3 + df4dx * f.f4 + df5dx * f.f5 + df6dx * f.f6),
                        y = 2 * (df1dy * f.f1 + df2dy * f.f2 + df3dy * f.f3 + df4dy * f.f4 + df5dy * f.f5 + df6dy * f.f6),
                        z = 2 * (df1dz * f.f1 + df2dz * f.f2 + df3dz * f.f3 + df4dz * f.f4 + df5dz * f.f5 + df6dz * f.f6),
                )
    }

    companion object {
        private const val gyroMeasurementError: Double = PI * (5.0f / 180.0f) // gyroscope measurement error in rad/s (shown as 5 deg/s)
        private const val gyroMeasurementDrift: Double = PI * (0.2f / 180.0f) // gyroscope measurement error in rad/s/s (shown as 0.2f deg/s/s)
        private val beta: Double = sqrt(3.0f / 4.0f) * gyroMeasurementError
        private val zeta: Double = sqrt(3.0f / 4.0f) * gyroMeasurementDrift

        /**
         * Return the normalized gradient of the objective function
         *      fa = q # g0 # q* - a for gravity (0, 0, -1)
         *      fb = q # b0 # q* - m for magnetic field (0, by, bz)
         *
         */
        internal fun gradient(q: Quaternion, by: Double, bz: Double, a: Vector, m: Vector): Quaternion =
            getGradientAsTangentialApproximation(q, by, bz, a, m)

        /**
         * Return the normalized gradient as the product of the Jacobian matrix with the objective function
         *      fa = q # g0 # q* - a for gravity (0, 0, -1)
         *      fb = q # b0 # q* - m for magnetic field (0, by, bz)
         *
         */
        internal fun gradientAsJacobianTimesObjectiveFunction(q: Quaternion, by: Double, bz: Double, a: Vector, m: Vector): Quaternion {
            val f: ObjectiveFunction = objectiveFunction(q, by, bz, a, m)
            val J: Jacobian = jacobian(q, by, bz)
            return J * f
        }

        /**
         * Return the normalized gradient as the approximation [F(q + dq) - F(q)] / dq  function
         *      fa = q # g0 # q* - a for gravity (0, 0, -1)
         *      fb = q # b0 # q* - m for magnetic field (0, by, bz)
         *
         */
        internal fun getGradientAsTangentialApproximation(q: Quaternion, by: Double, bz: Double, a: Vector, m: Vector): Quaternion {
            val h = 1E-8
            val f0 = objectiveFunction(q, by, bz, a, m).normSquare()
            val fx = objectiveFunction(Quaternion(x = q.x + h, y = q.y, z = q.z, s = q.s).normalize(), by, bz, a, m).normSquare()
            val fy = objectiveFunction(Quaternion(x = q.x, y = q.y + h, z = q.z, s = q.s).normalize(), by, bz, a, m).normSquare()
            val fz = objectiveFunction(Quaternion(x = q.x, y = q.y, z = q.z + h, s = q.s).normalize(), by, bz, a, m).normSquare()
            val fs = objectiveFunction(Quaternion(x = q.x, y = q.y, z = q.z, s = q.s + h).normalize(), by, bz, a, m).normSquare()
            return Quaternion(
                s = (fs - f0) / h,
                x = (fx - f0) / h,
                y = (fy - f0) / h,
                z = (fz - f0) / h,
            )
        }


        /**
         * return the objective function for the current estimate and the normalized acceleration
         *   fa := q # g0 # q* - a  with  g0 = (0, 0, 0, -1)
         *   fm := q # b0 # q* - m  with  b0 = (0, 0, by, bz)
         *
         * @param q: current estimated orientation
         * @param a: normalized acceleration
         * @param m: normalized magnetometer
         * @param b: estimated magnetic field in earth frame, e.g. something like (x = 0, y = 0.4, z = -0.8)
         */
        @Suppress("SpellCheckingInspection")
        internal fun objectiveFunction(q: Quaternion, by: Double, bz: Double, a: Vector, m: Vector): ObjectiveFunction {
            val qxqx = 2 * q.x * q.x
            val qxqy = 2 * q.x * q.y
            val qxqz = 2 * q.x * q.z
            val qyqy = 2 * q.y * q.y
            val qyqz = 2 * q.y * q.z
            val qzqz = 2 * q.z * q.z
            val qsqx = 2 * q.s * q.x
            val qsqy = 2 * q.s * q.y
            val qsqz = 2 * q.s * q.z

            return ObjectiveFunction(
                    f1 = (qxqz + qsqy) - a.x,
                    f2 = (qyqz - qsqx) - a.y,
                    f3 = (1 - qxqx - qyqy) - a.z,
                    f4 = by * (qxqy - qsqz) + bz * (qxqz + qsqy) - m.x,
                    f5 = by * (1 - qxqx - qzqz) + bz * (qyqz - qsqx) - m.y,
                    f6 = by * (qsqx + qyqz) + bz * (1 - qxqx - qyqy) - m.z,
            )
        }

        @Suppress("SpellCheckingInspection")
        private fun jacobian(q: Quaternion, by: Double, bz: Double): Jacobian {
            val byqs2 = 2 * by * q.s
            val byqx2 = 2 * by * q.x
            val byqy2 = 2 * by * q.y
            val byqz2 = 2 * by * q.z
            val bzqs2 = 2 * bz * q.s
            val bzqx2 = 2 * bz * q.x
            val bzqy2 = 2 * bz * q.y
            val bzqz2 = 2 * bz * q.z
            val byqx4 = 4 * by * q.x
            val byqz4 = 4 * by * q.z
            val bzqx4 = 4 * bz * q.x
            val bzqy4 = 4 * bz * q.y
            val qx2 = 2 * q.x
            val qy2 = 2 * q.y
            val qz2 = 2 * q.z
            val qs2 = 2 * q.s
            val qx4 = 4 * q.x
            val qy4 = 4 * q.y

            return Jacobian(
                    df1ds = qy2,
                    df1dx = qz2,
                    df1dy = qs2,
                    df1dz = qx2,
                    df2ds = -qx2,
                    df2dx = -qs2,
                    df2dy = qz2,
                    df2dz = qy2,
                    df3ds = 0.0,
                    df3dx = -qx4,
                    df3dy = -qy4,
                    df3dz = 0.0,
                    df4ds = -byqz2 + bzqy2,
                    df4dx = byqy2 + bzqz2,
                    df4dy = byqx2 + bzqs2,
                    df4dz = -byqs2 + bzqx2,
                    df5ds = -bzqx2,
                    df5dx = -byqx4 - bzqs2,
                    df5dy = bzqz2,
                    df5dz = -byqz4 + bzqy2,
                    df6ds = byqx2,
                    df6dx = byqs2 - bzqx4,
                    df6dy = byqz2 - bzqy4,
                    df6dz = byqy2,
            )
        }

        /**
         * Returns the magnetic field in earth frame after distortion correction:
         *      by using
         *      - the current estimate for h := q* # m # q = m.rotateBy(q)
         *      - b0 := (x = 0, y = sqrt(hx^2 + hy^2), z = hz
         *
         *
         * @param q: current orientation estimate
         */
        internal fun flux(q: Quaternion, m: Vector): Vector {
            // computed flux in the earth frame: h := q* # m # q = m.rotateBy(q)
            // normalise the flux vector to have only components in the x and z
            val h: Vector = m.rotateBy(q.conjugate())
            return Vector(
                    x = 0.0,
                    y = sqrt((h.x * h.x) + (h.y * h.y)),
                    z = h.z
            ).normalize()
        }

        /**
         * Returns the magnetic field in earth frame after distortion correction:
         *      by using
         *      - the accelerometer A and magnetometer M
         *      - finding H as the M component in direction of A
         *      - finding R as the M component perpendicular to A
         *      - hz =
         *
         *      Formulas:
         *          H = lambda * A, H + R = M, R _|_ A
         *                  --> SUM(rx * ax) = 0
         *                  --> SUM(ax * (mx - hx)) = (A dot M) - lambda * (A dot A)
         *                  --> lambda = (A dot M) / |A|^2
         *          |H|^2 = SUM(hx^2) = SUM(lambda^2 * ax^2) = lambda^2 * |A|^2 = (A dot M)^2 / |A|^2
         *                  --> |H| = ABS(A dot M) / |A|
         *          |R|^2 = SUM((mx - lambda * ax)^2) = |M|^2 - 2 * (A dot M)^2 / |A|^2 + (A dot M)^2 / |A|^2
         *                  --> |R| = SQRT(|M|^2 - |H|^2)
         *
         *          b = (x = 0, y = by, z = bz)
         *                  bz = - |H| = (A*M) / |A|
         *                  by = |R| = SQRT(|M|^2 - |H|^2)
         *
         */
        internal fun flux(a: Vector, m: Vector): Vector {
            val mm = Vector.dot(m, m)
            val am = Vector.dot(a, m)
            val aa = Vector.dot(a, a)
            val hh = am * am / aa
            val bz = - sqrt(hh)
            val by = sqrt(mm - hh)
            return Vector(
                x = 0.0,
                y = by,
                z = bz,
            ).normalize()
        }

    }
}

