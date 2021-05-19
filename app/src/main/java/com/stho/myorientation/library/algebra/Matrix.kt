package com.stho.myorientation.library.algebra

import kotlin.math.*

data class Matrix(
    val m11: Double, val m12: Double, val m13: Double,
    val m21: Double, val m22: Double, val m23: Double,
    val m31: Double, val m32: Double, val m33: Double) {

    operator fun times(m: Matrix): Matrix =
        multiplyBy(m)

    operator fun times(v: Vector): Vector =
        multiplyBy(v)

    /**
     * return a[3x3] times b[3x3] as [3x3]
     */
    private fun multiplyBy(m: Matrix): Matrix =
        Matrix(
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
    fun transpose(): Matrix =
        Matrix (
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


    /**
     * Returns the euler angles in radians as a vector for a rotation matrix m
     */
    fun toEulerAngles(): EulerAngles =
        Rotation.rotationMatrixToEulerAngles(this)

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
        val E: Matrix =
            Matrix(
                1.0, 0.0, 0.0,
                0.0, 1.0, 0.0,
                0.0, 0.0, 1.0,
            )

        fun fromFloatArray(m: FloatArray) =
            Matrix(
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

        /**
         * Returns the rotation matrix for Euler angles in degree
         *
         * @param azimuth rotation about z-axis
         * @param pitch rotation about x-axis
         * @param roll rotation about y-axis
         *
         *      R(alpha, beta, gamma) := E -> Rz(gamma) * Ry(beta) * Rz(gamma) * E
         *
         *      v_earth = R * v_device
         */
        fun fromEulerAngles(azimuth: Double, pitch: Double, roll: Double): Matrix =
            Rotation.eulerAnglesToRotationMatrix(EulerAngles.fromAzimuthPitchRoll(
                azimuth,
                pitch,
                roll
            ))

    }
}
