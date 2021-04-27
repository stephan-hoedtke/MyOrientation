package com.stho.myorientation

import com.stho.myorientation.library.Degree
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Vector
import com.stho.myorientation.library.algebra.matrixMultiplyBy
import com.stho.myorientation.library.algebra.toEulerAngles
import org.junit.Test

import org.junit.Assert.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MatrixUnitTest {

    @Test
    fun eulerAngles_areCorrect() {
        eulerAngles_areCorrect(0.0, 0.0, 0.0)
        eulerAngles_areCorrect(10.0, 20.0, 30.0)
        eulerAngles_areCorrect(10.0, 90.0, 0.0)
        eulerAngles_areCorrect(10.0, -20.0, 30.0)
        eulerAngles_areCorrect(10.0, -20.0, -30.0)
        eulerAngles_areCorrect(110.0, 20.0, 30.0)
        eulerAngles_areCorrect(110.0, -20.0, 30.0)
        eulerAngles_areCorrect(110.0, -20.0, -30.0)
    }

    private fun eulerAngles_areCorrect(pitch: Double, roll: Double, azimuth: Double) {
        val m = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val v = m.toEulerAngles()

        if (-90 <= pitch && pitch <= 90) {
            assertEqualAngles("x ", pitch, Math.toDegrees(v.x))
            assertEqualAngles("y ", roll, Math.toDegrees(v.y))
            assertEqualAngles("z ", azimuth, Math.toDegrees(v.z))
        }
        else {
            val newPitch = 180 - pitch
            val newRoll = roll - 180
            val newAzimuth = 180 + azimuth
            assertEqualAngles("x ", newPitch, Math.toDegrees(v.x))
            assertEqualAngles("y ", newRoll, Math.toDegrees(v.y))
            assertEqualAngles("z ", newAzimuth, Math.toDegrees(v.z))
        }
    }

    @Test
    fun gimbalLock_isCorrect() {
        gimbalLock_isCorrect(90.0, 0.0, 0.0)
        gimbalLock_isCorrect(90.0, 11.0, 0.0)
        gimbalLock_isCorrect(90.0, 11.0, 33.0)
        gimbalLock_isCorrect(90.0, -11.0, 33.0)
        gimbalLock_isCorrect(270.0, 11.0, 33.0)
    }

    private fun gimbalLock_isCorrect(pitch: Double, roll: Double, azimuth: Double) {

        assertEqualAngles("cos(pitch) ", 0.0, cos(Math.toRadians(pitch)))

        val m = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val v = m.toEulerAngles()

        val wx = Math.toDegrees(v.x)
        val wy = Math.toDegrees(v.y)
        val wz = Math.toDegrees(v.z)


        if (-90 <= pitch && pitch <= 90) {
            assertEqualAngles("x ", pitch, wx)
            assertEqualAngles("y ", roll + azimuth, wy)
            assertEqualAngles("z ", 0.0, wz)
        }
        else {
            assertEqualAngles("x ", pitch, wx)
            assertEqualAngles("y ", azimuth - roll, wy)
            assertEqualAngles("z ", 0.0, wz)
        }
    }

    private fun assertEqualAngles(t: String, a: Double, b: Double) {
        assertEquals(t, Degree.normalizeTo180(a), Degree.normalizeTo180(b), EPS)
    }

    @Test
    fun rotation_isCorrect() {
        // sin(30) = 0.5
        // cos(30) = 0.8660254
        rotation_isCorrect(Vector(1.0,0.0,0.0), 30.0, 0.0, 0.0, Vector(1.0,0.0, 0.0))
        rotation_isCorrect(Vector(0.0,1.0,0.0), 30.0, 0.0, 0.0, Vector(0.0,0.8660254, -0.5))
        rotation_isCorrect(Vector(0.0,0.0,1.0), 30.0, 0.0, 0.0, Vector(0.0,0.5, 0.8660254))

        rotation_isCorrect(Vector(1.0,0.0,0.0), 0.0, 30.0, 0.0, Vector(0.8660254,0.0, 0.5))
        rotation_isCorrect(Vector(0.0,1.0,0.0), 0.0, 30.0, 0.0, Vector(0.0,1.0, 0.0))
        rotation_isCorrect(Vector(0.0,0.0,1.0), 0.0, 30.0, 0.0, Vector(-0.5,0.0, 0.8660254))

        rotation_isCorrect(Vector(1.0,0.0,0.0), 0.0, 0.0, 30.0, Vector(0.8660254,-0.5, 0.0))
        rotation_isCorrect(Vector(0.0,1.0,0.0), 0.0, 0.0, 30.0, Vector(0.5,0.8660254, 0.0))
        rotation_isCorrect(Vector(0.0,0.0,1.0), 0.0, 0.0, 30.0, Vector(0.0,0.0, 1.0))
    }

    private fun rotation_isCorrect(v: Vector, pitch: Double, roll: Double, azimuth: Double, e: Vector) {
        val r = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val a = v.rotateBy(r)

        assertEquals("x", e.x, a.x, EPS)
        assertEquals("y", e.y, a.y, EPS)
        assertEquals("y", e.z, a.z, EPS)

    }

    @Test
    fun rotationMatrix_isCorrect() {
        rotationMatrix_isCorrect(0.0, 0.0, 0.0)
        rotationMatrix_isCorrect(10.0, 0.0, 0.0)
        rotationMatrix_isCorrect(0.0, 20.0, 0.0)
        rotationMatrix_isCorrect(0.0, 0.0, 30.0)
        rotationMatrix_isCorrect(10.0, 20.0, 30.0)
    }

    private fun rotationMatrix_isCorrect(pitch: Double, roll: Double, azimuth: Double) {
        val e = Matrix.E
        val r = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val sinX = sin(Math.toRadians(pitch)).toFloat()
        val cosX = cos(Math.toRadians(pitch)).toFloat()
        val sinY = sin(Math.toRadians(roll)).toFloat()
        val cosY = cos(Math.toRadians(roll)).toFloat()
        val sinZ = sin(Math.toRadians(azimuth)).toFloat()
        val cosZ = cos(Math.toRadians(azimuth)).toFloat()
        val rx = FloatArray(9).apply {
            this[0] = 1f
            this[1] = 0f
            this[2] = 0f
            this[3] = 0f
            this[4] = cosX
            this[5] = sinX
            this[6] = 0f
            this[7] = -sinX
            this[8] = cosX
        }
        val ry = FloatArray(9).apply {
            this[0] = cosY
            this[1] = 0f
            this[2] = -sinY
            this[3] = 0f
            this[4] = 1f
            this[5] = 0f
            this[6] = sinY
            this[7] = 0f
            this[8] = cosY
        }
        val rz = FloatArray(9).apply {
            this[0] = cosZ
            this[1] = sinZ
            this[2] = 0f
            this[3] = -sinZ
            this[4] = cosZ
            this[5] = 0f
            this[6] = 0f
            this[7] = 0f
            this[8] = 1f
        }

        val a = ry.matrixMultiplyBy(rx.matrixMultiplyBy(rz.matrixMultiplyBy(e)))

        for (i in 0..8) {
            assertEquals("index " + i.toString(), a[i].toDouble(), r[i].toDouble(), EPS)
        }
    }

    companion object {
        private const val EPS = 0.0001
    }
}