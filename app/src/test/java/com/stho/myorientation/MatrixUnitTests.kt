package com.stho.myorientation

import com.stho.myorientation.library.Degree
import com.stho.myorientation.library.algebra.*
import org.junit.Test

import org.junit.Assert.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MatrixUnitTests {

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
        eulerAngles_areCorrect(110.0, -20.0, -30.0)
        eulerAngles_areCorrect(-84.15, 7.79, 6.4)
        eulerAngles_areCorrect(84.15, 7.79, 6.4)
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

    private fun assertEqualAngles(t: String, a: Double, b: Double) {
        assertEquals(t, Degree.normalizeTo180(a), Degree.normalizeTo180(b), EPS)
    }

    @Test
    fun rotation_isCorrect() {
        // sin(30°) = 0.5
        // cos(30°) = 0.8660254
        // rotate a vector from earth frame into device frame

        rotation_isCorrect(Vector(1.0,0.0,0.0), 30.0, 0.0, 0.0, Vector(1.0,0.0, 0.0))
        rotation_isCorrect(Vector(0.0,1.0,0.0), 30.0, 0.0, 0.0, Vector(0.0,0.8660254, 0.5))
        rotation_isCorrect(Vector(0.0,0.0,1.0), 30.0, 0.0, 0.0, Vector(0.0,-0.5, 0.8660254))

        rotation_isCorrect(Vector(1.0,0.0,0.0), 0.0, 30.0, 0.0, Vector(0.8660254,0.0, -0.5))
        rotation_isCorrect(Vector(0.0,1.0,0.0), 0.0, 30.0, 0.0, Vector(0.0,1.0, 0.0))
        rotation_isCorrect(Vector(0.0,0.0,1.0), 0.0, 30.0, 0.0, Vector(0.5,0.0, 0.8660254))

        rotation_isCorrect(Vector(1.0,0.0,0.0), 0.0, 0.0, 30.0, Vector(0.8660254,0.5, 0.0))
        rotation_isCorrect(Vector(0.0,1.0,0.0), 0.0, 0.0, 30.0, Vector(-0.5,0.8660254, 0.0))
        rotation_isCorrect(Vector(0.0,0.0,1.0), 0.0, 0.0, 30.0, Vector(0.0,0.0, 1.0))
    }

    private fun rotation_isCorrect(v: Vector, pitch: Double, roll: Double, azimuth: Double, e: Vector) {
        val r = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val a = v.rotateBy(r)

        assertEquals("x", e.x, a.x, EPS)
        assertEquals("y", e.y, a.y, EPS)
        assertEquals("y", e.z, a.z, EPS)
    }


    private fun assertEquals(e: Matrix, a: Matrix) {
        assertEquals("m11", e.m11, a.m11, EPS)
        assertEquals("m12", e.m12, a.m12, EPS)
        assertEquals("m13", e.m13, a.m13, EPS)
        assertEquals("m21", e.m21, a.m21, EPS)
        assertEquals("m22", e.m22, a.m22, EPS)
        assertEquals("m23", e.m23, a.m23, EPS)
        assertEquals("m31", e.m31, a.m31, EPS)
        assertEquals("m32", e.m32, a.m32, EPS)
        assertEquals("m33", e.m33, a.m33, EPS)

    }

    companion object {
        private const val EPS = 0.0001
    }
}