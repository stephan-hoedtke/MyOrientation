package com.stho.myorientation.library.algebra;

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception

class Matrix(val rows: Int, val columns: Int) {

    private val values: Array<DoubleArray> = Array<DoubleArray>(rows) { DoubleArray(columns) }

    operator fun times(m: Matrix): Matrix =
        Matrix.multiply(this, m)

    operator fun times(v: DoubleArray): DoubleArray =
        Matrix.multiply(this, v)

    operator fun times(f: Double): Matrix =
        Matrix.multiplyBy(this, f)

    operator fun plus(m: Matrix): Matrix =
        Matrix.add(this, m)

    operator fun minus(m: Matrix): Matrix =
        Matrix.substract(this, m)

    fun transpose(): Matrix =
        Matrix.transpose(this)

    fun invert(): Matrix =
        MatrixInverter.invert(this)

    fun swapRows(i: Int, j: Int) {
        val array = values[i]
        values[i] = values[j]
        values[j] = array
    }

    fun clone(): Matrix {
        val m = Matrix(rows, columns)
        runBlocking(Dispatchers.Default) {
            for (r in 0 until rows) {
                launch {
                    for (c in 0 until columns) {
                        m.values[r][c] = values[r][c]
                    }

                }
            }
        }
        return m
    }

    operator fun get(r: Int, c: Int): Double =
        values[r][c]

    operator fun set(r: Int, c: Int, value: Double) {
        values[r][c] = value
    }

    companion object {

        fun multiply(a: Matrix, b: Matrix): Matrix {
            if (a.columns != b.rows) {
                throw Exception("Invalid columns of a ${a.columns} and rows of b ${b.rows} for matrix product a * b")
            }
            val size = a.columns // = b.rows
            val m = Matrix(a.rows, b.columns)
            runBlocking(Dispatchers.Default) {
                for (r in 0 until a.rows) {
                    launch {
                        for (c in 0 until b.columns) {
                            var sum = 0.0
                            for (n in 0 until size) {
                                sum += a.values[r][n] * b.values[n][c]
                            }
                            m.values[r][c] = sum
                        }
                    }
                }
            }
            return m
        }

        fun multiply(a: Matrix, v: DoubleArray): DoubleArray {
            if (a.columns != v.size) {
                throw Exception("Invalid columns of a ${a.columns} for vector product a * v")
            }
            val size = a.columns // = v.size
            val w = DoubleArray(a.rows)
            runBlocking(Dispatchers.Default) {
                for (r in 0 until a.rows) {
                    launch {
                        var sum = 0.0
                        for (n in 0 until size) {
                            sum += a.values[r][n] * v[n]
                        }
                        w[r] = sum
                    }
                }
            }
            return w
        }

        fun multiply(v: DoubleArray, a: Matrix): DoubleArray {
            if (v.size != a.rows) {
                throw Exception("Invalid rows of a ${a.columns} for vector product v * a")
            }
            val w = DoubleArray(a.columns)
            runBlocking(Dispatchers.Default) {
                for (c in 0 until a.columns) {
                    launch {
                        var sum = 0.0
                        for (n in 0 until a.rows) {
                            sum += a.values[n][c] * v[n]
                        }
                        w[c] = sum
                    }
                }
            }
            return w
        }

        fun multiplyBy(a: Matrix, f: Double): Matrix {
            val m = Matrix(a.rows, a.columns)
            runBlocking(Dispatchers.Default) {
                for (r in 0 until a.rows) {
                    launch {
                        for (c in 0 until a.columns) {
                            m.values[r][c] = a.values[r][c] * f
                        }
                    }
                }
            }
            return m
        }

        fun add(a: Matrix, b: Matrix): Matrix {
            if (a.rows != b.rows || a.columns != b.columns) {
                throw Exception("Invalid rows or columns ${a.columns} for matrix addition")
            }
            val m = Matrix(a.rows, a.columns)
            runBlocking(Dispatchers.Default) {
                for (r in 0 until a.rows) {
                    launch {
                        for (c in 0 until a.columns) {
                            m.values[r][c] = a.values[r][c] + b.values[r][c]
                        }
                    }
                }
            }
            return m
        }

        fun substract(a: Matrix, b: Matrix): Matrix {
            if (a.rows != b.rows || a.columns != b.columns) {
                throw Exception("Invalid rows or columns ${a.columns} for matrix addition")
            }
            val m = Matrix(a.rows, a.columns)
            runBlocking(Dispatchers.Default) {
                for (r in 0 until a.rows) {
                    launch {
                        for (c in 0 until a.columns) {
                            m.values[r][c] = a.values[r][c] - b.values[r][c]
                        }
                    }
                }
            }
            return m
        }

        fun transpose(m: Matrix): Matrix {
            val t = Matrix(m.columns, m.rows)
            runBlocking(Dispatchers.Default) {
                for (r in 0 until m.rows) {
                    launch {
                        for (c in 0 until m.columns) {
                            t.values[c][r] = m.values[r][c]
                        }
                    }
                }
            }
            return t
        }

        @Suppress("FunctionName")
        fun E(size: Int): Matrix {
            val m = Matrix(size, size)
            runBlocking(Dispatchers.Default) {
                for (r in 0 until size) {
                    launch {
                        for (c in 0 until size) {
                            m.values[c][r] = if (r == c) 1.0 else 0.0
                        }
                    }
                }
            }
            return m
        }

        fun create(rows: Int, columns: Int, vararg values: Double): Matrix {
            val m = Matrix(rows, columns)
            for (r in 0 until rows) {
                for (c in 0 until columns) {
                    val n = r * columns + c
                    if (n < values.size) {
                        m.values[r][c] = values[n]
                    }
                }
            }
            return m
        }

        fun create(vararg values: Double): DoubleArray {
            val v = DoubleArray(values.size)
            for (n in values.indices) {
                v[n] = values[n]
            }
            return v
        }
    }
}
