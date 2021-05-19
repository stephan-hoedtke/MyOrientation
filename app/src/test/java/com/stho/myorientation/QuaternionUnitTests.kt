package com.stho.myorientation

import com.stho.myorientation.library.Degree
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import org.junit.Test

import org.junit.Assert.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class QuaternionUnitTests {

    @Test
    fun quaternion_rotation_isCorrect() {
        // Note:
        // the quaternion rotation is directly against the android azimuth, pitch, roll

        val cos30 = cos(PI * 30 / 180)
        val sin30 = 0.5

        // rotation by 90° around z-axis: moves x to y and y to -x and keeps z
        quaternion_rotation_isCorrect(Vector(1.0, 0.0, 0.0), Quaternion.forRotation(0.0, 0.0, 1.0, PI/2), Vector(0.0, 1.0, 0.0))
        quaternion_rotation_isCorrect(Vector(0.0, 1.0, 0.0), Quaternion.forRotation(0.0, 0.0, 1.0, PI/2), Vector(-1.0, 0.0, 0.0))
        quaternion_rotation_isCorrect(Vector(0.0, 0.0, 1.0), Quaternion.forRotation(0.0, 0.0, 1.0, PI/2), Vector(0.0, 0.0, 1.0))

        // rotation by 90° around x-axis: moves y to z and z to -y and keeps x
        quaternion_rotation_isCorrect(Vector(1.0, 0.0, 0.0), Quaternion.forRotation(1.0, 0.0, 0.0, PI/2), Vector(1.0, 0.0, 0.0))
        quaternion_rotation_isCorrect(Vector(0.0, 1.0, 0.0), Quaternion.forRotation(1.0, 0.0, 0.0, PI/2), Vector(0.0, 0.0, 1.0))
        quaternion_rotation_isCorrect(Vector(0.0, 0.0, 1.0), Quaternion.forRotation(1.0, 0.0, 0.0, PI/2), Vector(0.0, -1.0, 0.0))

        // rotation by 30° around x-axis: moves y to z and z to -y and keeps x
        quaternion_rotation_isCorrect(Vector(1.0, 0.0, 0.0), Quaternion.forRotation(1.0, 0.0, 0.0, PI/6), Vector(1.0, 0.0, 0.0))
        quaternion_rotation_isCorrect(Vector(0.0, 1.0, 0.0), Quaternion.forRotation(1.0, 0.0, 0.0, PI/6), Vector(0.0, cos30, sin30))
        quaternion_rotation_isCorrect(Vector(0.0, 0.0, 1.0), Quaternion.forRotation(1.0, 0.0, 0.0, PI/6), Vector(0.0, -sin30, cos30))

        // rotation by 90° around y-axis: moves x to -z and z to x and keeps y
        quaternion_rotation_isCorrect(Vector(1.0, 0.0, 0.0), Quaternion.forRotation(0.0, 1.0, 0.0, PI/2), Vector(0.0, 0.0, -1.0))
        quaternion_rotation_isCorrect(Vector(0.0, 1.0, 0.0), Quaternion.forRotation(0.0, 1.0, 0.0, PI/2), Vector(0.0, 1.0, 0.0))
        quaternion_rotation_isCorrect(Vector(0.0, 0.0, 1.0), Quaternion.forRotation(0.0, 1.0, 0.0, PI/2), Vector(1.0, 0.0, 0.0))
    }


    private fun quaternion_rotation_isCorrect(v: Vector, q: Quaternion, e: Vector) {
        val a = v.rotateBy(q)

        assertEquals("||q||", 1.0, q.norm(), EPS_E5)
        assertEquals("x", e.x, a.x, EPS_E5)
        assertEquals("y", e.y, a.y, EPS_E5)
        assertEquals("z", e.z, a.z, EPS_E5)
    }

    @Test
    fun quaternion_hamilton_product_isCorrect() {
        quaternion_hamilton_product_isCorrect(Quaternion(1.0, 1.0, 0.0, 0.5).normalize(), Quaternion(2.0, 0.0, 0.0, 0.7).normalize())
        quaternion_hamilton_product_isCorrect(Quaternion(0.1, 0.2, 0.3, 0.4).normalize(), Quaternion(1.3, 2.4, 2.5, 2.6).normalize())
    }

    private fun quaternion_hamilton_product_isCorrect(p: Quaternion, q: Quaternion) {

        assertEquals("norm p", 1.0, p.norm(), EPS_E5)
        assertEquals("norm q", 1.0, q.norm(), EPS_E5)

        val r = p.times(q)
        val t = Quaternion(
                v = p.v * q.s + q.v * p.s + Vector.cross(p.v, q.v),
                s = p.s * q.s - Vector.dot(p.v, q.v)
        )

        assertEquals("X", t.x, r.x, EPS_E5)
        assertEquals("Y", t.y, r.y, EPS_E5)
        assertEquals("Z", t.z, r.z, EPS_E5)
        assertEquals("S", t.s, r.s, EPS_E5)
    }

    @Test
    fun quaternion_to_rotation_from_rotation_isCorrect() {

        val q = Quaternion.forRotation(2.0, 3.0, 4.0, 0.5)
        val e = q.normalize()
        val m = e.toRotationMatrix()
        val p = Quaternion.fromRotationMatrix(m)

        assertEquals("ex'.x", e.x, p.x, EPS_E5)
        assertEquals("ex'.y", e.y, p.y, EPS_E5)
        assertEquals("ex'.z", e.z, p.z, EPS_E5)
    }


    @Test
    fun quaternion_rotation_equals_matrix_rotation() {
        quaternion_rotation_equals_matrix_rotation(Vector(1.0,2.0,3.0), 11.1, 55.5, 33.3)
        quaternion_rotation_equals_matrix_rotation(Vector(1.0,2.0,3.0), -11.1, 55.5, 33.3)
        quaternion_rotation_equals_matrix_rotation(Vector(1.0,2.0,3.0), 11.1, -55.5, 33.3)
        quaternion_rotation_equals_matrix_rotation(Vector(1.0,2.0,3.0), 11.1, 55.5, -33.3)
    }

    private fun quaternion_rotation_equals_matrix_rotation(v: Vector, azimuth: Double, pitch: Double, roll: Double) {

        val m = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val e = v.rotateBy(m)

        val q = Quaternion.fromRotationMatrix(m)
        val a = v.rotateBy(q)

        assertEquals("x", e.x, a.x, EPS_E5)
        assertEquals("y", e.y, a.y, EPS_E5)
        assertEquals("z", e.z, a.z, EPS_E5)
    }

    @Test
    fun default_isCorrect() {
        val a = Quaternion.default
        val e = Quaternion.fromRotationMatrix(Matrix.E)

        assertEquals("ex.x", e.x, a.x, EPS_E5)
        assertEquals("ex.y", e.y, a.y, EPS_E5)
        assertEquals("ex.z", e.z, a.z, EPS_E5)

    }

    @Test
    fun normalize_isCorrect() {
        normalize_isCorrect(Quaternion(1.0, 2.0, 3.0, 4.0))
        normalize_isCorrect(Quaternion(-1.0, 2.0, -3.0, 4.0))
        normalize_isCorrect(Quaternion(0.0, 2.0, -3.0, 4.0))
    }

    private fun normalize_isCorrect(q: Quaternion) {
        val a = q.normalize()
        val f = 1.0 / sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.s * q.s)
        val e = q.times(f)

        assertEquals("ex.x", e.x, a.x, EPS_E5)
        assertEquals("ex.y", e.y, a.y, EPS_E5)
        assertEquals("ex.z", e.z, a.z, EPS_E5)
    }

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
        val q = Quaternion.forEulerAngles(azimuth, pitch, roll)
        val m = q.toRotationMatrix()
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
        assertEquals(t, Degree.normalizeTo180(a), Degree.normalizeTo180(b), EPS_E4)
    }

    companion object {
        private const val EPS_E5 = 0.00001
        private const val EPS_E4 = 0.0001
    }
}