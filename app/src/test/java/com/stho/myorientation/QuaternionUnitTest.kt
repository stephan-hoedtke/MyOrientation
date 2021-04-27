package com.stho.myorientation

import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import org.junit.Test

import org.junit.Assert.*
import kotlin.math.PI

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class QuaternionUnitTest {

    @Test
    fun quaternion_rotation_isCorrect() {
        // rotation by 90° around z-axis: moves x to y and y to -x --> theta=90
        val q = Quaternion.forRotation(0.0, 0.0, 1.0, PI/2)
        val ex = Vector(1.0, 0.0, 0.0)
        val ey = Vector(0.0, 1.0, 0.0)
        val rx = ex.rotateBy(q)
        val ry = ey.rotateBy(q)

        assertEquals("||q||", 1.0, q.norm(), EPS)

        assertEquals("ex'.x", 0.0, rx.x, EPS)
        assertEquals("ex'.y", 1.0, rx.y, EPS)
        assertEquals("ex'.z", 0.0, rx.z, EPS)

        assertEquals("ey'.x", -1.0, ry.x, EPS)
        assertEquals("ey'.y", 0.0, ry.y, EPS)
        assertEquals("ey'.z", 0.0, ry.z, EPS)
    }

    @Test
    fun quaternion_hamilton_product_isCorrect() {
        val p = Quaternion(1.0, 1.0, 0.0, 0.5).normalize()
        val q = Quaternion(2.0, 0.0, 0.0, 0.7).normalize()

        assertEquals("norm p", 1.0, p.norm(), EPS)
        assertEquals("norm q", 1.0, q.norm(), EPS)

        val r = p.times(q)
        val t = Quaternion(
                v = p.v * q.s + q.v * p.s + Vector.cross(p.v, q.v),
                s = p.s * q.s - Vector.dot(p.v, q.v)
        )

        assertEquals("X", t.x, r.x, EPS)
        assertEquals("Y", t.y, r.y, EPS)
        assertEquals("Z", t.z, r.z, EPS)
        assertEquals("S", t.s, r.s, EPS)
    }

    @Test
    fun quaternion_to_rotation_from_rotation_isCorrect() {

        val q = Quaternion.forRotation(2.0, 3.0, 4.0, 0.5)
        val e = q.normalize()
        val m = e.toRotationMatrix()
        val p = Quaternion.fromRotationMatrix(m)

        assertEquals("ex'.x", e.x, p.x, EPS)
        assertEquals("ex'.y", e.y, p.y, EPS)
        assertEquals("ex'.z", e.z, p.z, EPS)
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

        assertEquals("x", e.x, a.x, EPS)
        assertEquals("y", e.y, a.y, EPS)
        assertEquals("z", e.z, a.z, EPS)
    }

    @Test fun default_isCorrect() {
        val a = Quaternion.default
        val e = Quaternion.fromRotationMatrix(Matrix.E)

        assertEquals("ex.x", e.x, a.x, EPS)
        assertEquals("ex.y", e.y, a.y, EPS)
        assertEquals("ex.z", e.z, a.z, EPS)

    }


    companion object {
        private const val EPS = 0.00001
    }
}