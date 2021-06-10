package com.stho.myorientation.library.algebra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

/**
 * Wikipedia: LU Decomposition
 * see: https://en.wikipedia.org/wiki/LU_decomposition#C#_code_example
 */
class MatrixInverter(m: Matrix) {

    private val size = m.rows
    private val lu: Matrix = m.clone()
    private val p: IntArray = IntArray(size) { i -> i }
    private var swaps: Int = 0

    init {
        decompose()
    }

    val upperMatrix: Matrix
        get() {
            val m = Matrix(size, size)
            for (i in 0 until size) {
                for (j in 0 until size) {
                    m[i, j] = when {
                        j < i -> 0.0
                        else -> lu[i, j]
                    }
                }
            }
            return m
        }

    val lowerMatrix: Matrix
        get() {
            val m = Matrix(size, size)
            for (i in 0 until size) {
                for (j in 0 until size) {
                    m[i, j] = when {
                        i == j -> 1.0
                        j < i -> lu[i, j]
                        else -> 0.0
                    }
                }
            }
            return m

        }

    val permutationMatrix: Matrix
        get() = p.toPermutationMatrix()

    /**
     * Creates the LU decomposition, and returns both matrices in one as:
     *          LU = (L-E) + U
     *
     * So that P * A = L * U with the permutation matrix P build from the row swaps p
     *
     *           | 1.0   0.0   0.0  |
     * -->  L := | L21   1.0   0.0  |
     *           | L31   L32   1.0  |
     *
     *           | U11   U12   U13  |
     *      U := | 0.0   U22   U23  |
     *           | 0.0   0.0   U33  |
     *
     *            | U11   U12   U13  |     | 0.0   0.0   0.0  |     | U11   U12   U13  |
     *      LU := | L21   U22   U23  |  =  | L21   0.0   0.0  |  +  | 0.0   U22   U23  |
     *            | L31   L32   U33  |     | L31   L32   0.0  |     | 0.0   0.0   U33  |
     *
     */
    private fun decompose() {

        for (i in 0 until size) {

            val iMax = getMaxRow(i)

            if (iMax != i) {
                lu.swapRows(i, iMax)
                p.swap(i, iMax)
                swaps++
            }

            for (j in i + 1 until size) {

                lu[j, i] /= lu[i, i]

                for (k in i + 1 until size) {
                    lu[j, k] -= lu[j, i] * lu[i, k]
                }
            }
        }
    }

    /**
     * Returns the row index with the greatest elements in column i (starting with row i)
     */
    private fun getMaxRow(i: Int): Int {
        var value = abs(lu[i, i])
        var index = i

        for (k in i + 1 until size) {
            val tmp = abs(lu[k, i])
            if (tmp > value) {
                value = tmp
                index = k
            }
        }

        if (value < TOLERANCE) {
            throw Exception("Invalid matrix for LU decomposition")
        }

        return index
    }



    /**
     *  Returns the solution vector x of  A * x = b, calculated from the decomposed lu matrix
     */
    fun solve(b: DoubleArray): DoubleArray {

        val x: DoubleArray = DoubleArray(size)

        for (i in 0 until size) {
            x[i] = b[p[i]];
            for (k in 0 until i) {
                x[i] -= lu[i, k] * x[k]
            }
        }

        for (i in size - 1 downTo 0) {
            for (k in i + 1 until size) {
                x[i] -= lu[i, k] * x[k]
            }
            x[i] /= lu[i, i]
        }

        return x
    }

    /**
     * Returns the inverse matrix, calculated from the decomposed lu matrix
     */
    private fun invert(): Matrix {
        val inverse: Matrix = Matrix(size, size)
        runBlocking(Dispatchers.Default) {
            for (j in 0 until size) {
                launch {
                    for (i in 0 until size) {
                        inverse[i, j] = if (p[i] == j) 1.0 else 0.0
                        for (k in 0 until i) {
                            inverse[i, j] -= lu[i, k] * inverse[k, j]
                        }
                    }
                    for (i in size - 1 downTo 0) {
                        for (k in i + 1 until size) {
                            inverse[i, j] -= lu[i, k] * inverse[k, j]
                        }
                        inverse[i, j] /= lu[i, i]
                    }
                }
            }
        }
        return inverse
    }

    /**
     * Returns the determinant of a give matrix, calculated from the decomposed lu matrix
     */
    private fun determinant(): Double {
        var det = lu[0, 0];
        for (i in 1 until size) {
            det *= lu[i, i];
        }
        return if ((swaps - size) % 2 == 0) det else -det
    }

    companion object {
        private const val TOLERANCE: Double = 0.000000001

        fun invert(m: Matrix): Matrix {
            val inverter = MatrixInverter(m)
            return inverter.invert()
        }
    }
}

// Double Array Extensions (for Matrix operations)

operator fun DoubleArray.times(m: Matrix): DoubleArray =
    Matrix.multiply(this, m)

// Int Array Extensions (for Matrix operations)

fun IntArray.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

fun IntArray.toPermutationMatrix(): Matrix {
    val m = Matrix(size, size)
    for (i in 0 until size) {
        val p = this[i]
        for (j in 0 until size) {
            m[i, j] = if (j == p) 1.0 else 0.0
        }
    }
    return m
}

