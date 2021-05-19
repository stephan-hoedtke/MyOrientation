package com.stho.myorientation

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
class VectorUnitTest {

    @Test
    fun vector_plus_isCorrect() {
        vector_plus_isCorrect(Vector(1.0, 2.0, 3.0), Vector(0.1, 0.2, 0.3), Vector(1.1, 2.2, 3.3))
    }

    private fun vector_plus_isCorrect(a: Vector, b: Vector, e: Vector) {
        val c = a + b
        assertEquals("x ", e.x, c.x, EPS)
        assertEquals("y ", e.y, c.y, EPS)
        assertEquals("z ", e.z, c.z, EPS)
    }

    @Test
    fun vector_minus_isCorrect() {
        vector_minus_isCorrect(Vector(1.0, 2.0, 3.0), Vector(0.1, 0.2, 0.3), Vector(0.9, 1.8, 2.7))
    }

    private fun vector_minus_isCorrect(a: Vector, b: Vector, e: Vector) {
        val c = a - b
        assertEquals("x ", e.x, c.x, EPS)
        assertEquals("y ", e.y, c.y, EPS)
        assertEquals("z ", e.z, c.z, EPS)
    }

    @Test
    fun vector_times_isCorrect() {
        vector_times_isCorrect(Vector(1.0, 2.0, 3.0), 2.0, Vector(2.0, 4.0, 6.0))
    }

    private fun vector_times_isCorrect(a: Vector, f: Double, e: Vector) {
        val c = a * f
        assertEquals("x ", e.x, c.x, EPS)
        assertEquals("y ", e.y, c.y, EPS)
        assertEquals("z ", e.z, c.z, EPS)
    }

    @Test
    fun vector_cross_isCorrect() {
        vector_cross_isCorrect(Vector(1.0, 0.0, 0.0), Vector(0.0, 1.0, 0.0), Vector(0.0, 0.0, 1.0))
        vector_cross_isCorrect(Vector(0.0, 1.0, 0.0), Vector(0.0, 0.0, 1.0), Vector(1.0, 0.0, 0.0))
        vector_cross_isCorrect(Vector(0.0, 0.0, 1.0), Vector(1.0, 0.0, 0.0), Vector(0.0, 1.0, 0.0))
    }

    private fun vector_cross_isCorrect(a: Vector, b: Vector, e: Vector) {
        val c = Vector.cross(a , b)
        assertEquals("cross.x ", e.x, c.x, EPS)
        assertEquals("cross.y ", e.y, c.y, EPS)
        assertEquals("cross.z ", e.z, c.z, EPS)
    }

    @Test
    fun vector_dot_isCorrect() {
        vector_dot_isCorrect(Vector(1.0, 0.0, 0.0), Vector(0.0, 1.0, 0.0), 0.0)
        vector_dot_isCorrect(Vector(1.0, 2.0, 3.0), Vector(0.1, 0.2, 0.3), 1.4)
    }

    private fun vector_dot_isCorrect(a: Vector, b: Vector, e: Double) {
        val c = Vector.dot(a, b)
        assertEquals("dot", e, c, EPS)
    }

    @Test
    fun vector_rotateBy_isCorrect() {
        vector_rotateBy_isCorrect(Vector(1.0, 0.0, 0.0),  Quaternion.forRotation(0.0, 0.0, 1.0, PI/2).normalize())
        vector_rotateBy_isCorrect(Vector(1.0, 2.0, 3.0),  Quaternion.forRotation(0.0, 0.0, 1.0, PI/2).normalize())
        vector_rotateBy_isCorrect(Vector(-1.0, 2.0, 3.0),  Quaternion.forRotation(0.1, 0.2, 0.3, PI/4).normalize())

        val q = Quaternion.forEulerAngles(7.0, -7.0, 7.0).normalize()
        val v = Vector(-0.3, 0.45, -0.76)
        vector_rotateBy_isCorrect(v, q)
    }

    private fun vector_rotateBy_isCorrect(v: Vector, q: Quaternion) {
        val a = v.rotateBy(q)
        val e = q.times(v.asQuaternion()).times(q.inverse())

        assertEquals("s", 0.0, e.s, EPS)
        assertEquals(e.v, a)

        val a1 = rotateByFormulaOne(v, q)
        val a2 = rotateByFormulaTwo(v, q)
        val a3 = rotateByFormulaThree(v, q)
        val a4 = rotateByFormulaFour(v, q)

        assertEquals(e.v, a1)
        assertEquals(e.v, a2)
        assertEquals(e.v, a3)
        assertEquals(e.v, a4)
    }

    private fun assertEquals(e: Vector, a: Vector) {
        assertEquals("x", e.x, a.x, EPS)
        assertEquals("y", e.y, a.y, EPS)
        assertEquals("z", e.z, a.z, EPS)
    }

    /**
     * dot=5
     * cross=9
     * --> 38 Operations
     */
    private fun rotateByFormulaOne(v: Vector, q: Quaternion): Vector {
        // see: https://gamedev.stackexchange.com/questions/28395/rotating-vector3-by-a-quaternion
        val f1 = 2 * Vector.dot(v, q.v)                                         // 1+5 = 6
        val f2 = q.s * q.s - Vector.dot(q.v, q.v)                               // 2+5 = 7
        val f3 = 2 * q.s                                                        // 1
        return q.v * f1 + v * f2 + Vector.cross(q.v, v) * f3                    // 3 + (3+3) + (9+3+3) = 24
    }

    /**
     * dot=5
     * cross=9
     * --> 40 Operations
     */
    private fun rotateByFormulaTwo(v: Vector, q: Quaternion): Vector {
        val f1 = 2 * Vector.dot(v, q.v)                                         // 1+5 = 6
        val f2 = 2 * Vector.dot(q.v, q.v)                                       // 1+5 = 6
        val f3 = 2 * q.s                                                        // 1
        return v + q.v * f1 - v * f2 + Vector.cross(q.v, v) * f3                // (3+3) + (3+3) + (9+3+3) = 27
    }

    /**
     * cross=9
     * --> 30 Operations
     */
    private fun rotateByFormulaThree(v: Vector, q: Quaternion): Vector {
        val t = Vector.cross(q.v, v) * 2.0                                      // 9 + 3
        return v + t * q.s + Vector.cross(q.v, t)                               // (3+3) + (9+3) = 18
    }

    /**
     * --> 45 Operations
     */
    private fun rotateByFormulaFour(v: Vector, q: Quaternion): Vector {
        val qxx = 2 * q.x * q.x                                                         // 9 x 2 = 18
        val qyy = 2 * q.y * q.y
        val qzz = 2 * q.z * q.z
        val qxy = 2 * q.x * q.y
        val qxz = 2 * q.x * q.z
        val qyz = 2 * q.y * q.z
        val qsx = 2 * q.s * q.x
        val qsy = 2 * q.s * q.y
        val qsz = 2 * q.s * q.z
        return Vector(
                x = v.x * (1 - qyy - qzz) + v.y * (qxy - qsz) + v.z * (qxz + qsy),   // 9
                y = v.x * (qxy + qsz) + v.y * (1 - qxx - qzz) + v.z * (qyz - qsx),   // 9
                z = v.x * (qxz - qsy) + v.y * (qsx + qyz) + v.z * (1 - qxx - qyy)    // 9
        )
    }


    companion object {
        private const val EPS = 0.00000001
    }
}

