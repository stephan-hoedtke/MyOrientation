package com.stho.myorientation.library.algebra

import kotlin.math.*

object Matrix {

    /**
     * return a[3x3] times b[3x3] as [3x3]
     */
    fun matrixMultiply(a: FloatArray, b: FloatArray): FloatArray {
        val m: FloatArray = FloatArray(9)
        matrixMultiplyInto(a, b, m)
        return m
    }

    /**
     * m[3x3] := m[3x3] x r[3x3]
     */
    fun matrixMultiplyBy(m: FloatArray, r: FloatArray) {
        val a: FloatArray = m.copyOf()
        Matrix.matrixMultiplyInto(a, r, m)
    }

    /**
     * m[3x3] := a[3x3] x b[3x3]
     */
    private fun matrixMultiplyInto(a: FloatArray, b: FloatArray, m: FloatArray) {
        if (a.size != 9 || b.size != 9 || m.size != 9)
            throw Exception("Invalid matrix size")

        m[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6]
        m[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7]
        m[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8]
        m[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6]
        m[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7]
        m[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8]
        m[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6]
        m[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7]
        m[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8]
    }

    /**
     * m[3x3] := m[3x3]T note, for rotation matrix m.inverse() == m.transpose()
     */
    fun transpose(m: FloatArray): FloatArray {
        return FloatArray(9).apply {
            this[0] = m[0]
            this[1] = m[3]
            this[2] = m[6]
            this[3] = m[1]
            this[4] = m[4]
            this[5] = m[7]
            this[6] = m[2]
            this[7] = m[5]
            this[8] = m[8]
        }
    }


    /**
     * Returns the rotation matrix from Euler angles in degree
     * @param azimuth rotation about z-axis
     * @param pitch rotation about x-axis
     * @param roll rotation about y-axis
     *
     * R(alpha, beta, gamma) := E -> Rz(gamma) * Ry(beta) * Rz(gamma) * E
     */
    fun fromEulerAngles(azimuth: Double, pitch: Double, roll: Double): FloatArray =
        fromEulerAngles(
            Vector(
                x = Math.toRadians(pitch),
                y = Math.toRadians(roll),
                z = Math.toRadians(azimuth)
            )
        )

    /**
     * Pitch (around X axis):
     *   When the device is placed face up on a table, the pitch value is 0.
     *   When the positive Z axis begins to tilt towards the positive Y axis, the pitch angle becomes positive.
     *   The value of Pitch ranges from -180 degrees to 180 degrees.
     *
     * Roll (around Y axis):
     *   When the device is placed face up on a table, the roll value is 0.
     *   When the positive X axis begins to tilt towards the positive Y axis, the roll angle becomes positive.
     *   The value of Roll ranges from -90 degrees to 90 degrees.
     *
     * Azimuth (around Z axis):
     *   The following table shows the value of Azimuth when the positive Y axis of the device is aligned to the magnetic north, south, east, and west
     *      North -> 0
     *      East -> 90
     *      South -> 180
     *      West -> 270
     *
     * References:
     *   https://www.cdiweb.com/datasheets/invensense/sensor-introduction.pdf
     *   SensorManager.getOrientation(float[] R, ...)
     *
     * Note:
     *   M(x, y, z) = M(y) * M(x) * M(z)
     *       M-inverse = M-transpose
     *       --> M(x, y, z)  = M-transpose(-x, -y, -z)
     */
    fun fromEulerAngles(omega: Vector): FloatArray {
        val cosX = cos(omega.x).toFloat()
        val sinX = sin(omega.x).toFloat()
        val cosY = cos(omega.y).toFloat()
        val sinY = sin(omega.y).toFloat()
        val cosZ = cos(omega.z).toFloat()
        val sinZ = sin(omega.z).toFloat()
        //
        // rotation: first by z (azimuth), then by x (pitch), then by y (roll), left hand (thumb into vector direction, bending fingers show rotation)
        //
        // zero-position: phone is flat on the table surface upwards, looked at from above
        //   x -> to the right
        //   y -> forward
        //   z -> upwards
        //
        // 1) azimuth: turn y to x around minus-z-axis -> move positive y to the right
        //
        //      |  cos(z)   sin(z)   0 |   --->  x is increased by y * sin(z)
        // Mz = | -sin(z)   cos(z)   0 |   --->  y is decreased by x * sin(z)
        //      |  0        0        1 |
        //
        // 2) pitch: turn z to y around x-axis -> move positive y downwards
        //
        //      |  1   0        0      |
        // Mx = |  0   cos(x)   sin(x) |  ---> y is increased by z * sin(x)
        //      |  0  -sin(x)   cos(x) |  ---> z is decreased by y * sin(x)
        //
        // 3) roll: turn x to z around y-axis -> move left downwards and positive x upwards
        //
        //      |  cos(y)  0 -sin(y) |  ---> x is decreased by z * sin(y)
        // My = |  0       1  0      |
        //      |  sin(y)  0  cos(y) |  ---> z is increased by x * sin(y)
        //
        //           | 1   0       0      |   |  cos(z)  sin(z)  0 |
        // Mx * Mz = | 0   cos(x)  sin(x) | x | -sin(z)  cos(z)  0 |
        //           | 0  -sin(x)  cos(x) |   |  0       0       1 |
        //
        //           |  cos(z)          sin(z)           0       |
        //         = | -cos(x)*sin(z)   cos(x)*cos(z)    sin(x)  |
        //           |  sin(x)*sin(z)  -sin(x)*cos(z)    cos(x)  |
        //
        //                  |  cos(y)  0 -sin(y) |   |  cos(z)          sin(z)           0       |
        // My x (Mx * Mz) = |  0       1  0      | x | -cos(x)*sin(z)   cos(x)*cos(z)    sin(x)  |
        //                  |  sin(y)  0  cos(y) |   |  sin(x)*sin(z)  -sin(x)*cos(z)    cos(x)  |
        //
        //                  |  cos(y)*cos(z)-sin(y)*sin(x)*sin(z)   cos(y)*sin(z)+sin(y)*sin(x)*cos(z)  -sin(y)*cos(x)  |
        //                = | -cos(x)*sin(z)                        cos(x)*cos(z)                        sin(x)         |
        //                  |  sin(y)*cos(z)+cos(y)*sin(x)*sin(z)   sin(y)*sin(z)-cos(y)*sin(x)*cos(z)   cos(y)* cos(x) |
        //
        //
        //      (B) For the gimbal lock when cos(x) = 0 and hence sin(x)= 1
        //          assume z = 0 --> cos(z) = 1 hence cos(y) = M11
        //          -->
        //          M11 = cosY * cosZ - sinY * sinZ = cos(Y + Z)
        //          M12 = cosY * sinZ + sinY * cosZ = sin(Y + Z)
        //          M31 = sinY * cosZ + cosY * sinZ = sin(Y + Z)
        //          M32 = sinY * sinZ - cosY * cosZ = -cos(Y + Z)
        //          --> only Y + Z is defined. we assume Z = 0

        return FloatArray(9).apply {
            this[0] = cosY * cosZ - sinX * sinY * sinZ      // M11
            this[1] = cosY * sinZ + sinX * sinY * cosZ      // M12
            this[2] = -cosX * sinY                          // M13
            this[3] = -cosX * sinZ                          // M21
            this[4] = cosX * cosZ                           // M22
            this[5] = sinX                                  // M23
            this[6] = sinY * cosZ + sinX * cosY * sinZ      // M31
            this[7] = sinY * sinZ - sinX * cosY * cosZ      // M32
            this[8] = cosX * cosY                           // M33
        }
    }

    /**
     * Returns the euler angles in radians as a vector for a rotation matrix m
     */
    fun toEulerAngles(m: FloatArray): Vector {
        // Note, the sensor manager uses a different calculation:
        // SensorManager.getOrientation() --> azimuth, pitch, roll
        val x = asin(m[5].toDouble())
        val cosX = cos(x)
        return if (abs(cosX) > EPSILON) {
            val y = atan2(-m[2].toDouble(), m[8].toDouble())
            val z = atan2(-m[3].toDouble(), m[4].toDouble())
            Vector(x, y, z)
        } else {
            val y = acos(m[0].toDouble())
            val z = 0.0
            Vector(x, y, z)
        }
    }

    val E: FloatArray =
            FloatArray(9).apply {
                this[0] = 1f
                this[1] = 0f
                this[2] = 0f
                this[3] = 0f
                this[4] = 1f
                this[5] = 0f
                this[6] = 0f
                this[7] = 0f
                this[8] = 1f
            }


    private const val EPSILON: Double = 0.000000001
}

/**
 * Return this[3,3] x r[3,3]
 */
fun FloatArray.matrixMultiplyBy(r: FloatArray): FloatArray {
    Matrix.matrixMultiplyBy(this, r)
    return this
}

fun FloatArray.transpose(): FloatArray {
    return Matrix.transpose(this)
}

/**
 * Returns a vector representing the Euler angles of the rotation matrix in radians
 */
fun FloatArray.toEulerAngles(): Vector {
    return Matrix.toEulerAngles(this)
}


