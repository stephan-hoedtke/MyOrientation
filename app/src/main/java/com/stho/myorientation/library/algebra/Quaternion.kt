package com.stho.myorientation.library.algebra

import kotlin.math.*

/**
 * https://mathworld.wolfram.com/Quaternion.html
 * https://www.ashwinnarayan.com/post/how-to-integrate-quaternions/
 */
data class Quaternion(val v: Vector, val s: Double) : IRotation {
    val x: Double = v.x
    val y: Double = v.y
    val z: Double = v.z

    private val x2: Double by lazy { 2 * x * x }
    private val y2: Double by lazy { 2 * y * y }
    private val z2: Double by lazy { 2 * z * z }
    private val xy: Double by lazy { 2 * x * y }
    private val xz: Double by lazy { 2 * x * z }
    private val yz: Double by lazy { 2 * y * z }
    private val sz: Double by lazy { 2 * s * z }
    private val sy: Double by lazy { 2 * s * y }
    private val sx: Double by lazy { 2 * s * x }

    // https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
    /**
     * 1 - 2yy - 2zz
     */
    override val m11: Double by lazy { 1 - y2 - z2 }

    /**
     * 2xy - 2sz
     */
    override val m12: Double by lazy { xy - sz }

    /**
     * 2xz + 2sy
     */
    override val m13: Double by lazy { xz + sy }

    /**
     * 2xy + 2sz
     */
    override val m21: Double by lazy { xy + sz }

    /**
     * 1 - 2xx - 2zz
     */
    override val m22: Double by lazy { 1 - x2 - z2 }

    /**
     * 2yz - 2sx
     */
    override val m23: Double by lazy { yz - sx }

    /**
     * 2xz - 2sy
     */
    override val m31: Double by lazy { xz - sy }

    /**
     * 2yz + 2sx
     */
    override val m32: Double by lazy { yz + sx }

    /**
     * 1 - 2xx - 2yy
     */
    override val m33: Double by lazy { 1 - x2 - y2 }

    fun toRotationMatrix(): RotationMatrix =
        RotationMatrix(
            m11 = m11, m12 = m12, m13 = m13,
            m21 = m21, m22 = m22, m23 = m23,
            m31 = m31, m32 = m32, m33 = m33,
        )

    fun toEulerAngles(): EulerAngles =
        Rotation.getEulerAnglesFor(this)

    fun toOrientation(): Orientation =
        Rotation.getOrientationFor(this)

    constructor(x: Double, y: Double, z: Double, s: Double) :
            this(v = Vector(x, y, z), s = s)

    operator fun plus(q: Quaternion): Quaternion =
            Quaternion(v + q.v, s + q.s)

    operator fun minus(q: Quaternion): Quaternion =
            Quaternion(v - q.v, s - q.s)

    operator fun times(f: Double): Quaternion =
            Quaternion(v * f, s * f)

    operator fun times(q: Quaternion): Quaternion =
            hamiltonProduct(this, q)

    operator fun div(f: Double): Quaternion =
            Quaternion(v / f, s / f)

    fun norm(): Double =
            sqrt(normSquare())

    fun normSquare(): Double =
            x * x + y * y + z * z + s * s

    fun conjugate(): Quaternion =
            Quaternion(Vector(-x, -y, -z), s)

    fun inverse(): Quaternion =
        conjugate() * (1 / normSquare())

    fun normalize(): Quaternion =
        this * (1.0 / norm())

    fun normalizePositiveX(): Quaternion =
        // warning: do not use sign(x) as it is 0 for x = 0
        // this method may be difficult for gradient descent debugging...
        when {
            x < 0 -> this * (-1.0 / norm())
            else -> this * (1.0 / norm())
        }

    fun round(): Quaternion {
        val n = norm()
        val xx = (x / n).round(6)
        val yy = (y / n).round(6)
        val zz = (z / n).round(6)
        val ss = (s / n).round(6)
        return Quaternion(xx, yy, zz, ss)
    }

    companion object {
        fun fromFloatArray(v: FloatArray): Quaternion =
                if (v.size >= 4) {
                    // assume v[0], v[1], v[2] build the vector and s = v[3] of a unit quaternion already
                    Quaternion(x = v[0].toDouble(), y = v[1].toDouble(), z = v[2].toDouble(), s = v[3].toDouble())
                } else {
                    // assume v[0], v[1], v[2] build the vector, so that s can be calculated for the unit quaternion
                    val n = 1 - v[0] * v[0] - v[1] * v[1] - v[2] * v[2];
                    val s = if (n > 0) sqrt(n.toDouble()) else 0.0
                    Quaternion(x = v[0].toDouble(), y = v[1].toDouble(), z = v[2].toDouble(), s = s)
                }

        /**
         * Returns a rotation quaternion for Euler angles in degree
         *
         *      v_earth = v_device.rotateBy(q)
         */
        fun forEulerAngles(azimuth: Double, pitch: Double, roll: Double): Quaternion =
            Rotation.eulerAnglesToQuaternion(EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll))

        /**
         * Returns a rotation quaternion for Euler angles given as Vector(pitch,roll,azimuth) in radian
         *
         *      v_earth = v_device.rotateBy(q)
         */
        fun forEulerAngles(eulerAngles: EulerAngles): Quaternion =
            Rotation.eulerAnglesToQuaternion(eulerAngles)


        /**
         * Quaternion for rotating by theta (in radians) around the vector (x, y, z)
         */
        fun forRotation(x: Double, y: Double, z: Double, theta: Double): Quaternion =
            forRotation(u = Vector(x, y, z), theta = theta)

        /**
         * Quaternion for rotating by theta (in radians) around the vector u
         */
        fun forRotation(u: Vector, theta: Double): Quaternion {
            // see: https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
            val thetaOverTwo = theta / 2.0
            val sinThetaOverTwo: Double = sin(thetaOverTwo)
            val cosThetaOverTwo: Double = cos(thetaOverTwo)
            return Quaternion(v = u * sinThetaOverTwo, s = cosThetaOverTwo)
        }

        val default: Quaternion
            get() = Quaternion(0.0, 0.0, 0.0, 1.0)

        val zero: Quaternion
            get() = Quaternion(0.0, 0.0, 0.0, 0.0)
        
        // (r1,v1) * (r2,v2) = (r1 r2 - dot(v1,v2), r1 v2 + r2 v1 + cross(v1, v2)
        private fun hamiltonProduct(a: Quaternion, b: Quaternion): Quaternion {
            val a1 = a.s
            val b1 = a.x
            val c1 = a.y
            val d1 = a.z
            val a2 = b.s
            val b2 = b.x
            val c2 = b.y
            val d2 = b.z
            return Quaternion(
                    x = a1 * b2 + b1 * a2 + c1 * d2 - d1 * c2,
                    y = a1 * c2 + c1 * a2 - b1 * d2 + d1 * b2,
                    z = a1 * d2 + d1 * a2 + b1 * c2 - c1 * b2,
                    s = a1 * a2 - b1 * b2 - c1 * c2 - d1 * d2
            )
        }

        fun fromRotationMatrix(m: RotationMatrix): Quaternion =
                m.toQuaternion()

        fun dot(a: Quaternion, b: Quaternion): Double =
                a.x * b.x + a.y * b.y + a.z * b.z + a.s * b.s

        private const val COS_THETA_THRESHOLD: Double = 0.9995

        /**
         * Q(t) := A sin((1 - t) * θ) / sin(θ) + B sin(t * θ) / sin(θ)
         *      with cos(θ) = dot(A, B)
         *
         *      To ensure -90 <= θ <= 90: use -A when dot(A,B) < 0
         *      Note:
         *          Q(0) = A
         *          Q(1) = B
         */
        fun interpolate(a: Quaternion, b: Quaternion, t: Double): Quaternion {
            // see: https://theory.org/software/qfa/writeup/node12.html
            // see: https://blog.magnum.graphics/backstage/the-unnecessarily-short-ways-to-do-a-quaternion-slerp/

            val cosTheta = dot(a, b)

            return when {
                abs(cosTheta) > COS_THETA_THRESHOLD -> {
                    // If the inputs are too close for comfort, linearly interpolate and normalize the result.
                    val c: Quaternion = a + (a - b) * t
                    return c.normalize()
                }
                cosTheta >= 0 -> {
                    val theta: Double = acos(cosTheta)
                    val sinTheta = sin(theta)
                    val f1 = sin((1 - t) * theta) / sinTheta
                    val f2 = sin(t * theta) / sinTheta
                    a * f1 + b * f2
                }
                else -> {
                    // Use the shorter way for -a ...
                    val theta: Double = acos(-cosTheta)
                    val sinTheta = sin(theta)
                    val f1 = sin((t - 1) * theta) / sinTheta
                    val f2 = sin(t * theta) / sinTheta
                    a * f1 + b * f2
                }
            }
        }

        /**
         * average a number of Quaternions, which need to be normalized or they will be weighted strangely
         */
        fun average(a: Quaternion, vararg array: Quaternion): Quaternion {
            var r: Quaternion = a
            for (b in array) {
                r += if (dot(a, b) > 0) b else b.inverse()
            }
            return r.normalize()
        }

        fun fromEulerAngles(azimuth: Double, pitch: Double, roll: Double): Quaternion =
            EulerAngles.fromAzimuthPitchRoll(
                azimuth,
                pitch,
                roll
            ).toQuaternion()

        private const val EPSILON: Double = 0.000000001
    }
}

/**
 * Rounds a double to the number of digits. It's a kind of slow, but works
 */
fun Double.round(decimals: Int): Double {
    var f = 1.0
    repeat(decimals) { f *= 10 }
    return round(this * f + 0.5) / f
}




