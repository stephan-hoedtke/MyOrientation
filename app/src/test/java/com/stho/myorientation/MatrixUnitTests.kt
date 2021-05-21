package com.stho.myorientation

import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.*
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MatrixUnitTests {

    @Test
    fun eulerAngles_areCorrect() {
        eulerAngles_areCorrect(0.0, 0.0, 0.0)
        eulerAngles_areCorrect(30.0, 0.0, 0.0)
        eulerAngles_areCorrect(0.0, 10.0, 0.0)
        eulerAngles_areCorrect(0.0, 0.0, 20.0)
        eulerAngles_areCorrect(30.0, 10.0, 20.0)
        eulerAngles_areCorrect(0.0, 10.0, 90.0)
        eulerAngles_areCorrect(30.0, 10.0, -20.0)
        eulerAngles_areCorrect(-30.0, 10.0, -20.0)
        eulerAngles_areCorrect(30.0, 110.0, 20.0)
        eulerAngles_areCorrect(30.0, 110.0, -20.0)
        eulerAngles_areCorrect(-30.0, 110.0, -20.0)
        eulerAngles_areCorrect(-30.0, 110.0, -20.0)
        eulerAngles_areCorrect(6.4, -84.15, 7.79)
        eulerAngles_areCorrect(6.4, 84.15, 7.79)
    }

    private fun eulerAngles_areCorrect(azimuth: Double, pitch: Double, roll: Double) {

        val m = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val a = m.toEulerAngles()

        val x = m * Vector(1.0, 0.0, 0.0)
        val y = m * Vector(0.0, 1.0, 0.0)
        val z = m * Vector(0.0, 0.0, 1.0)

        val q = Quaternion.forEulerAngles(azimuth, pitch, roll)
        val b = q.toEulerAngles()
        val c = q.toRotationMatrix().toEulerAngles()
        val d = m.toQuaternion().toRotationMatrix().toEulerAngles()

        eulerAngles_areCorrect(azimuth, pitch, roll, a)
        eulerAngles_areCorrect(azimuth, pitch, roll, b)
        eulerAngles_areCorrect(azimuth, pitch, roll, c)
        eulerAngles_areCorrect(azimuth, pitch, roll, d)
    }

    private fun eulerAngles_areCorrect(azimuth: Double, pitch: Double, roll: Double, eulerAngles: EulerAngles) {

        if (-90 <= pitch && pitch <= 90) {
            assertEqualAngles("azimuth ", azimuth, eulerAngles.azimuth)
            assertEqualAngles("pitch ", pitch, eulerAngles.pitch)
            assertEqualAngles("roll ", roll, eulerAngles.roll)
        }
        else {
            val newAzimuth = 180 + azimuth
            val newPitch = 180 - pitch
            val newRoll = roll - 180
            assertEqualAngles("azimuth ", newAzimuth, eulerAngles.azimuth)
            assertEqualAngles("pitch ", newPitch, eulerAngles.pitch)
            assertEqualAngles("roll ", newRoll, eulerAngles.roll)
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
        val r = Matrix.fromEulerAngles(azimuth, pitch, roll).transpose()
        val a = v.rotateBy(r)

        assertEquals("x", e.x, a.x, EPS)
        assertEquals("y", e.y, a.y, EPS)
        assertEquals("y", e.z, a.z, EPS)
    }

    companion object {
        private const val EPS = 0.0001
    }
}