package com.stho.myorientation.library.algebra

import kotlin.math.*

/**
 * https://mathworld.wolfram.com/Quaternion.html
 * https://www.ashwinnarayan.com/post/how-to-integrate-quaternions/
 */
data class Quaternion(val v: Vector, val s: Double) {
    val x: Double = v.x
    val y: Double = v.y
    val z: Double = v.z

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

    fun toRotationMatrix(): Matrix =
        Rotation.quaternionToRotationMatrix(this)

    fun toEulerAngles(): EulerAngles =
        Rotation.quaternionToEulerAngles(this)

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
        fun forRotation(x: Double, y: Double, z: Double, theta: Double): Quaternion {
            // see: https://developer.android.com/reference/android/hardware/SensorEvent
            val thetaOverTwo = theta / 2.0
            val sinThetaOverTwo: Double = sin(thetaOverTwo)
            val cosThetaOverTwo: Double = cos(thetaOverTwo)
            return Quaternion(x = x * sinThetaOverTwo, y = y * sinThetaOverTwo, z = z * sinThetaOverTwo, s = cosThetaOverTwo)
        }

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

        fun fromRotationMatrix(m: Matrix): Quaternion =
            Rotation.rotationMatrixToQuaternion(m)

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




