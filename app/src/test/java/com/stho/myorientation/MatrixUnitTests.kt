package com.stho.myorientation

import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.MatrixInverter
import org.junit.Assert.assertEquals
import org.junit.Test

class MatrixUnitTests : BaseUnitTestsHelper() {

    @Test
    fun matrixTimesMatrixProduct_isCorrect() {

        val a = Matrix.create(rows = 3, columns = 2,
            1.0, 2.0,
            2.0, 3.0,
            3.0, 4.0)

        val b = Matrix.create(rows = 2, columns = 4,
            1.0, 2.0, 3.0, 4.0,
            2.0, 3.0, 4.0, 5.0)

        val c = a * b

        assertEquals("rows: ", a.rows, c.rows)
        assertEquals("columns: ", b.columns, c.columns)
        assertEquals("values[3,4]", 32.0, c[2, 3], EPS_E8)
    }

    @Test
    fun matrixClone_isCorrect() {

        val a = Matrix.create(rows = 3, columns = 3,
            1.0, 2.0, 3.0,
            2.0, 3.0, 4.0,
            3.0, 4.0, 7.0)

        val b = a.clone()

        b[0, 1] = 0.23456789
        b[1, 2] = 1.23456789
        b[2, 0] = 2.23456789

        assertEquals("a[0,1]", 2.0, a[0,1], EPS_E8)
        assertEquals("a[1,2]", 4.0, a[1,2], EPS_E8)
        assertEquals("a[2,0]", 3.0, a[2,0], EPS_E8)

        assertEquals("b[0,1]", 0.23456789, b[0,1], EPS_E8)
        assertEquals("b[1,2]", 1.23456789, b[1,2], EPS_E8)
        assertEquals("b[2,0]", 2.23456789, b[2,0], EPS_E8)
    }

    @Test
    fun matrixInverter_isCorrect() {

        val a = Matrix.create(
            rows = 3, columns = 3,
            1.0, 2.0, 3.0,
            2.0, 3.0, 4.0,
            3.0, 4.0, 7.0
        )

        val inverter = MatrixInverter(a)

        val l = inverter.lowerMatrix
        val u = inverter.upperMatrix
        val p = inverter.permutationMatrix

        val e = p * a
        val c = l * u

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                assertEquals("$i $j", e[i, j], c[i, j], EPS_E8)
            }
        }
    }


    @Test
    fun matrixInverse_3x3_isCorrect() {

        val a = Matrix.create(rows = 3, columns = 3,
            1.0, 2.0, 3.0,
            2.0, 3.0, 4.0,
            3.0, 4.0, 7.0)

        val b = a.invert()
        val c = a * b
        val e = Matrix.E(3)

        assertEquals("rows: ", 3, c.rows)
        assertEquals("columns: ", 3, c.columns)

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                assertEquals("$i $j", e[i, j], c[i, j], EPS_E8)
            }
        }
    }

    @Test
    fun matrixInverse_7x7_isCorrect() {

        val a = Matrix.create(rows = 7, columns = 7,
            1.0, 2.0, 3.6, 4.0, 5.7, 6.0, 7.9,
            1.1, 2.2, 3.5, 4.3, 5.6, 5.5, 7.8,
            1.2, 7.7, 3.4, 4.9, 5.5, 6.8, 7.7,
            1.3, 2.6, 3.3, 4.2, 5.4, 6.2, 7.6,
            1.4, 2.8, 8.8, 4.5, 5.3, 6.6, 7.5,
            1.5, 2.0, 3.1, 4.8, 6.6, 6.0, 7.4,
            1.6, 2.2, 3.0, 9.9, 5.1, 6.4, 7.3,
        )

        val b = a.invert()
        val c = a * b
        val e = Matrix.E(7)

        for (i in 0 until 7) {
            for (j in 0 until 7) {
                assertEquals("$i $j", e[i, j], c[i, j], EPS_E8)
            }
        }
    }

}