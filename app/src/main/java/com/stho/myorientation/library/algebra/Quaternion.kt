package com.stho.myorientation.library.algebra

import kotlin.math.*

/**
 * see: https://mathworld.wolfram.com/Quaternion.html
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
        this * (sign(x) / norm())

    fun toRotationMatrix(): FloatArray {
        return FloatArray(9).apply {
            val x2 = 2 * x * x
            val y2 = 2 * y * y
            val z2 = 2 * z * z
            val xy = 2 * x * y
            val xz = 2 * x * z
            val yz = 2 * y * z
            val sz = 2 * s * z
            val sy = 2 * s * y
            val sx = 2 * s * x
            val m11 = 1 - y2 - z2
            val m12 = xy - sz
            val m13 = sy + xz
            val m21 = xy + sz
            val m22 = 1 - x2 - z2
            val m23 = yz - sx
            val m31 = xz - sy
            val m32 = sx + yz
            val m33 = 1 - x2 - y2
            this[0] = m11.toFloat()
            this[1] = m12.toFloat()
            this[2] = m13.toFloat()
            this[3] = m21.toFloat()
            this[4] = m22.toFloat()
            this[5] = m23.toFloat()
            this[6] = m31.toFloat()
            this[7] = m32.toFloat()
            this[8] = m33.toFloat()
        }
    }

    companion object {
        fun fromEventValues(v: FloatArray): Quaternion =
                if (v.size >= 4) {
                    Quaternion(v[0].toDouble(), v[1].toDouble(), v[2].toDouble(), v[3].toDouble())
                } else {
                    val n = 1 - v[0] * v[0] - v[1] * v[1] - v[2] * v[2];
                    val s = if (n > 0) sqrt(n.toDouble()) else 0.0
                    Quaternion(v[0].toDouble(), v[1].toDouble(), v[2].toDouble(), s)
                }

        fun forEulerAngles(azimuth: Double, pitch: Double, roll: Double): Quaternion {
            val m = Matrix.fromEulerAngles(azimuth, pitch, roll)
            return Quaternion.fromRotationMatrix(m)
        }

        fun forRotation(x: Double, y: Double, z: Double, theta: Double): Quaternion {
            // see: https://developer.android.com/reference/android/hardware/SensorEvent
            val thetaOverTwo = theta / 2.0
            val sinThetaOverTwo: Double = sin(thetaOverTwo)
            val cosThetaOverTwo: Double = cos(thetaOverTwo)
            return Quaternion(x = x * sinThetaOverTwo, y = y * sinThetaOverTwo, z = z * sinThetaOverTwo, s = cosThetaOverTwo)
        }

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

        fun fromRotationMatrix(m: FloatArray): Quaternion {
            // see: https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
            if (m.size != 9) {
                throw Exception("Invalid rotation matrix")
            }
            val m00 = m[0].toDouble()
            val m01 = m[1].toDouble()
            val m02 = m[2].toDouble()
            val m10 = m[3].toDouble()
            val m11 = m[4].toDouble()
            val m12 = m[5].toDouble()
            val m20 = m[6].toDouble()
            val m21 = m[7].toDouble()
            val m22 = m[8].toDouble()

            val trace: Double = m00 + m11 + m22

            when {
                trace > 0 -> {
                    val fourS = 2.0 * sqrt(1.0 + trace) // 4s = 4 * q.s
                    return Quaternion(
                            x = (m21 - m12) / fourS,
                            y = (m02 - m20) / fourS,
                            z = (m10 - m01) / fourS,
                            s = 0.25 * fourS
                    )
                }
                m00 > m11 && m00 > m22 -> {
                    val fourX = 2.0 * sqrt(1.0 + m00 - m11 - m22) // 4x = 4 * q.x
                    return Quaternion(
                            x = 0.25 * fourX,
                            y = (m01 + m10) / fourX,
                            z = (m02 + m20) / fourX,
                            s = (m21 - m12) / fourX,
                    )
                }
                m11 > m22 -> {
                    val fourY = 2.0 * sqrt(1.0 + m11 - m00 - m22) // 4y = 4*q.y
                    return Quaternion(
                            x = (m01 + m10) / fourY,
                            y = 0.25 * fourY,
                            z = (m12 + m21) / fourY,
                            s = (m02 - m20) / fourY
                    )
                }
                else -> {
                    val fourZ = 2.0 * sqrt(1.0 + m22 - m00 - m11) // 4z = 4 * q.z
                    return Quaternion(
                            x = (m02 + m20) / fourZ,
                            y = (m12 + m21) / fourZ,
                            z = 0.25 * fourZ,
                            s = (m10 - m01) / fourZ
                    )
                }
            }
        }

        fun dot(a: Quaternion, b: Quaternion): Double =
                a.x * b.x + a.y * b.y + a.z * b.z + a.s * b.s

        private const val COS_THETA_THRESHOLD: Double = 0.9995

        /**
         * Q(t) := A sin((1 - t) * θ) / sin(θ) + B sin(t * θ) / sin(θ)
         * with cos(θ) = dot(A, B)
         * mind to ensure -90 <= θ <= 90, use -A when dot(A,B) < 0
         * θ := tθ0
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
    }
}



