package com.stho.myorientation.library.algebra

import kotlin.math.*


data class RotationMatrix (
    override val m11: Double,
    override val m12: Double,
    override val m13: Double,
    override val m21: Double,
    override val m22: Double,
    override val m23: Double,
    override val m31: Double,
    override val m32: Double,
    override val m33: Double) : IRotation {

    operator fun times(m: RotationMatrix): RotationMatrix =
        multiplyBy(m)

    operator fun times(v: Vector): Vector =
        multiplyBy(v)

    /**
     * return a[3x3] times b[3x3] as [3x3]
     */
    private fun multiplyBy(m: RotationMatrix): RotationMatrix =
        RotationMatrix(
            m11 = m11 * m.m11 + m12 * m.m21 + m13 * m.m31,
            m12 = m11 * m.m12 + m12 * m.m22 + m13 * m.m32,
            m13 = m11 * m.m13 + m12 * m.m23 + m13 * m.m33,

            m21 = m21 * m.m11 + m22 * m.m21 + m23 * m.m31,
            m22 = m21 * m.m12 + m22 * m.m22 + m23 * m.m32,
            m23 = m21 * m.m13 + m22 * m.m23 + m23 * m.m33,

            m31 = m31 * m.m11 + m32 * m.m21 + m33 * m.m31,
            m32 = m31 * m.m12 + m32 * m.m22 + m33 * m.m32,
            m33 = m31 * m.m13 + m32 * m.m23 + m33 * m.m33,
        )

    /**
     * v[3x3] := a[3x3] x b[3]
     */
    private fun multiplyBy(v: Vector): Vector =
        Vector(
            x = m11 * v.x + m12 * v.y + m13 * v.z,
            y = m21 * v.x + m22 * v.y + m23 * v.z,
            z = m31 * v.x + m32 * v.y + m33 * v.z,
        )


    /**
     * m[3x3] := m[3x3]T note, for rotation matrix m.inverse() == m.transpose()
     */
    fun transpose(): RotationMatrix =
        RotationMatrix (
            m11 = this.m11,
            m12 = this.m21,
            m13 = this.m31,
            m21 = this.m12,
            m22 = this.m22,
            m23 = this.m32,
            m31 = this.m13,
            m32 = this.m23,
            m33 = this.m33,
        )


    fun toQuaternion(): Quaternion {
        // see: https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
        // mind, as both q and -q define the same rotation you may get q or -q, respectively

        when {
            m11 + m22 + m33 > 0 -> {
                val fourS = 2.0 * sqrt(1.0 + m11 + m22 + m33) // 4s = 4 * q.s
                return Quaternion(
                    x = (m32 - m23) / fourS,
                    y = (m13 - m31) / fourS,
                    z = (m21 - m12) / fourS,
                    s = 0.25 * fourS
                )
            }
            m11 > m22 && m11 > m33 -> {
                val fourX = 2.0 * sqrt(1.0 + m11 - m22 - m33) // 4x = 4 * q.x
                return Quaternion(
                    x = 0.25 * fourX,
                    y = (m12 + m21) / fourX,
                    z = (m13 + m31) / fourX,
                    s = (m32 - m23) / fourX,
                )
            }
            m22 > m33 -> {
                val fourY = 2.0 * sqrt(1.0 + m22 - m11 - m33) // 4y = 4*q.y
                return Quaternion(
                    x = (m12 + m21) / fourY,
                    y = 0.25 * fourY,
                    z = (m23 + m32) / fourY,
                    s = (m13 - m31) / fourY
                )
            }
            else -> {
                val fourZ = 2.0 * sqrt(1.0 + m33 - m11 - m22) // 4z = 4 * q.z
                return Quaternion(
                    x = (m13 + m31) / fourZ,
                    y = (m23 + m32) / fourZ,
                    z = 0.25 * fourZ,
                    s = (m21 - m12) / fourZ
                )
            }
        }
    }


    /**
     * Returns the euler angles in radians as a vector for a rotation matrix m
     */
    fun toEulerAngles(): EulerAngles =
        Rotation.getEulerAnglesFor(this)

    fun toOrientation(): Orientation =
        Rotation.getOrientationFor(this)

    fun toFloatArray(): FloatArray =
        FloatArray(9).apply {
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

    companion object {

        @Suppress("PropertyName")
        val E: RotationMatrix =
            RotationMatrix(
                1.0, 0.0, 0.0,
                0.0, 1.0, 0.0,
                0.0, 0.0, 1.0,
            )

        fun fromFloatArray(m: FloatArray) =
            RotationMatrix(
                m11 = m[0].toDouble(),
                m12 = m[1].toDouble(),
                m13 = m[2].toDouble(),
                m21 = m[3].toDouble(),
                m22 = m[4].toDouble(),
                m23 = m[5].toDouble(),
                m31 = m[6].toDouble(),
                m32 = m[7].toDouble(),
                m33 = m[8].toDouble(),
            )

        fun fromQuaternion(q: Quaternion): RotationMatrix =
                q.toRotationMatrix()

        fun fromEulerAngles(azimuth: Double, pitch: Double, roll: Double): RotationMatrix =
            EulerAngles.fromAzimuthPitchRoll(
                azimuth,
                pitch,
                roll
            ).toRotationMatrix()

    }
}
