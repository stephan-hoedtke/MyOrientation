package com.stho.myorientation

import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import org.junit.Test

import org.junit.Assert.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

    companion object {
        private const val EPS = 0.00000001
    }
}