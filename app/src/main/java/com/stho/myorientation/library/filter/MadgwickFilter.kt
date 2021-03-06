package com.stho.myorientation.library.filter

import com.stho.myorientation.*
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import kotlin.math.sqrt


/**
 * An efficient orientation filter for inertial and inertial/magnetic sensor arrays
 *      by Sebastian O.H. Madgwick, 2010
 *      https://courses.cs.washington.edu/courses/cse474/17wi/labs/l4/madgwick_internal_report.pdf
 *
 *      implementation sample: https://github.com/Josef4Sci/AHRS_Filter/blob/master/Filters/MadgwickAHRS3.m
 */
class MadgwickFilter(options: IMadgwickFilterOptions) :
    AbstractOrientationFilter(Method.MadgwickFilter, options) {

    private val beta: Double = sqrt(3.0f / 4.0f) * options.gyroscopeMeanError
    private val gamma: Double = sqrt(3.0f / 4.0f) * options.gyroscopeDrift

    override val pdf: String
        get() = "MadgwickFilter.pdf"

    override val link: String
        get() = "http://x-io.co.uk/res/doc/madgwick_internal_report.pdf"

    enum class Mode {
        /**
         * Jacobian Matrix
         * Reverse calculation of magnetometer reference using the current estimate
         */
        Default,

        /**
         * Tangential Approximation
         * Calculation of magnetometer reference from acceleration and magnetometer readings
         */
        Modified,
    }

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)

    private var hasMagnetometer: Boolean = false
    private var hasAcceleration: Boolean = false
    private var hasGyro: Boolean = false
    private var hasEstimate: Boolean = false

    private val timer: Timer = Timer()
    private val mode = options.madgwickMode

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
        if (dt > 0) {

            filterUpdate(dt)

            super.onOrientationAnglesChanged(estimate.toOrientation())
        }
    }

    /**
     * Estimated orientation quaternion with initial conditions
     */
    private var estimate: Quaternion = Quaternion.default

    /**
     * estimated gyroscope bias error
     */
    private var gyroBias: Quaternion = Quaternion.zero

    /**
     * Function to compute one filter iteration
     */
    private fun filterUpdate(dt: Double) {

        val aHat = Vector.fromFloatArray(accelerometerReading).normalize()
        val mHat = Vector.fromFloatArray(magnetometerReading).normalize()
        val w = Vector.fromFloatArray(gyroscopeReading).asQuaternion()

        if (!hasEstimate) {
            estimate = super.getQuaternionFromAccelerometerMagnetometerReadings(accelerometerReading, magnetometerReading, Quaternion.default)
        }

        // compute the gradient (matrix multiplication) estimated direction of the gyroscope error
        val gradient: Quaternion = gradient(estimate, aHat, mHat)
        val gradientNorm = gradient.norm()

        // use the normalized gradient as correction error of the gyroscope readings.
        val qDotError: Quaternion = gradient / gradientNorm

        // compute angular estimated direction of the gyroscope error: omega_err
        val omegaError = estimate * qDotError * 2.0

        // compute gyroscope biases and correct gyro angle rotation
        gyroBias += omegaError.inverse() * (gamma * dt)

        val omega = w - gyroBias

        val qDotGyro = estimate * omega * 0.5
        val qDotCorrection = qDotError * beta
        val qDot = qDotGyro - qDotCorrection
        val delta = qDot * dt
        estimate = (estimate + delta).normalize()

        hasEstimate = true
    }

    private fun gradient(q: Quaternion, aHat: Vector, mHat: Vector): Quaternion =
        // compute the gradient (matrix multiplication) estimated direction of the gyroscope error
        when (mode) {
            Mode.Default -> {
                val b: Vector = flux(q, mHat)
                gradientAsJacobianTimesObjectiveFunction(q, b.y, b.z, aHat, mHat)
            }
            Mode.Modified -> {
                val b: Vector = flux(aHat, mHat)
                getGradientAsTangentialApproximation(q, b.y, b.z, aHat, mHat)
            }
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

        /**
         * Return the normalized gradient as the product of the Jacobian matrix with the objective function
         *      fa = q # g0 # q* - a for gravity (0, 0, -1)
         *      fb = q # b0 # q* - m for magnetic field (0, by, bz)
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
         * Mind, this gradient differs slightly from the jacobian * objectionFunction, as it incorporates the normalization step
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
         *   fa := q* # g0 # q - a  with  g0 = (0, 0, -1)
         *   fm := q* # b0 # q - m  with  b0 = (0, by, bz)
         *
         * @param q: current estimated orientation
         * @param a: normalized acceleration
         * @param m: normalized magnetometer
         * @param b: estimated magnetic field in earth frame, e.g. something like (x = 0, y = 0.4, z = -0.8)
         */
        @Suppress("SpellCheckingInspection")
        internal fun objectiveFunction(q: Quaternion, by: Double, bz: Double, a: Vector, m: Vector): ObjectiveFunction {
            return ObjectiveFunction(
                f1 = q.m31 - a.x,
                f2 = q.m32 - a.y,
                f3 = q.m33 - a.z,
                f4 = (q.m21 * by + q.m31 * bz) - m.x,
                f5 = (q.m22 * by + q.m32 * bz) - m.y,
                f6 = (q.m23 * by + q.m33 * bz) - m.z,
            )
        }

        @Suppress("SpellCheckingInspection")
        internal fun jacobian(q: Quaternion, by: Double, bz: Double): Jacobian {
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
                    df1ds = -qy2,
                    df1dx = qz2,
                    df1dy = -qs2,
                    df1dz = qx2,
                    df2ds = qx2,
                    df2dx = qs2,
                    df2dy = qz2,
                    df2dz = qy2,
                    df3ds = 0.0,
                    df3dx = -qx4,
                    df3dy = -qy4,
                    df3dz = 0.0,
                    df4ds = byqz2 - bzqy2,
                    df4dx = byqy2 + bzqz2,
                    df4dy = byqx2 - bzqs2,
                    df4dz = byqs2 + bzqx2,
                    df5ds = bzqx2,
                    df5dx = -byqx4 + bzqs2,
                    df5dy = bzqz2,
                    df5dz = -byqz4 + bzqy2,
                    df6ds = -byqx2,
                    df6dx = -byqs2 - bzqx4,
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
            // computed flux in the earth frame: h := q # m # q* = m.rotateBy(q)
            // normalise the flux vector to have only components in the x and z
            val h: Vector = m.rotateBy(q)
            return Vector(
                    x = 0.0,
                    y = sqrt((h.x * h.x) + (h.y * h.y)),
                    z = h.z
            ).normalize()
        }

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

