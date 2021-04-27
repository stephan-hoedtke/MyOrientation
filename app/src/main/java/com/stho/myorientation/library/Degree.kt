package com.stho.myorientation.library

import java.security.InvalidParameterException
import java.util.regex.Pattern
import kotlin.math.*

/**
 * Created by shoedtke on 30.08.2016.
 */
class Degree {
    val angleInDegree: Double
    private val sign: Int
    private val degree: Int
    private val minute: Int
    private val seconds: Double

    private constructor(angleInDegree: Double) {
        this.angleInDegree = normalizeTo180(angleInDegree)
        sign = sign(this.angleInDegree).toInt()
        val degrees = abs(this.angleInDegree)
        degree = degrees.toInt()
        val minutes = 60 * (degrees - degree)
        minute = minutes.toInt()
        seconds = 60 * (minutes - minute)
    }

    private constructor(sign: Int, degree: Int, minute: Int, seconds: Double) {
        this.sign = sign
        this.degree = degree
        this.minute = minute
        this.seconds = seconds
        angleInDegree = this.sign * (this.degree + this.minute / 60.0 + this.seconds / 3600.0)
    }

    override fun toString(): String =
        (if (sign < 0) "-" else "") + degree + "° " + minute + "' " + Formatter.df0.format(seconds) + "''"

    fun toShortString(): String =
        (if (sign < 0) "-" else "") + degree + "° " + minute + "'"

    companion object {
        fun fromDegree(angleInDegree: Double): Degree =
            Degree(angleInDegree)

        fun fromPositive(degree: Int, minute: Int, seconds: Double): Degree =
            Degree(1, abs(degree), minute, seconds)

        fun fromNegative(degree: Int, minute: Int, seconds: Double): Degree =
            Degree(-1, abs(degree), minute, seconds)

        private val pattern = Pattern.compile("^([+|−|-|–|-])(\\d+)[°]\\s(\\d+)[′|']\\s(\\d+[.]*\\d*)$") // for:  −11° 09′ 40.5

        fun fromDegree(str: String): Degree {
            val m = pattern.matcher(str)
            if (m.find() && m.groupCount() == 4) {
                val degree: Int= m.group(2)?.toInt() ?: 0
                val minute: Int= m.group(3)?.toInt() ?: 0
                val seconds: Double = m.group(4)?.toDouble() ?: 0.0
                return if (m.group(1) == "+") {
                    fromPositive(degree, minute, seconds)
                } else {
                    fromNegative(degree, minute, seconds)
                }
            }
            throw InvalidParameterException("Invalid degree $str")
        }

        fun fromRadian(angleInRadian: Double): Degree =
            Degree(Math.toDegrees(angleInRadian))

        fun toRadian(angleInDegree: Double): Double =
            Math.toRadians(angleInDegree)

        /* convert from degree to radian */
        internal const val DEGRAD = Math.PI / 180.0

        /* convert from radian to degree */
        internal const val RADEG = 180.0 / Math.PI

        fun sin(degree: Double): Double {
            return kotlin.math.sin(degree * DEGRAD)
        }

        fun tan(degree: Double): Double {
            return kotlin.math.tan(degree * DEGRAD)
        }

        fun cos(degree: Double): Double {
            return kotlin.math.cos(degree * DEGRAD)
        }

        fun arcTan2(y: Double, x: Double): Double {
            return RADEG * kotlin.math.atan2(y, x)
        }

        fun arcTan(x: Double): Double {
            return RADEG * kotlin.math.atan(x)
        }

        fun arcSin(x: Double): Double {
            return RADEG * kotlin.math.asin(x)
        }

        fun arcCos(x: Double): Double {
            return RADEG * kotlin.math.acos(x)
        }

        fun normalize(degree: Double): Double =
            degree.IEEErem(360.0).let {
                when {
                    it < 0 -> it + 360.0
                    else -> it
                }
            }

        fun normalizeTo180(degree: Double): Double =
            degree.IEEErem(360.0).let {
                when {
                    it > 180 -> it - 360.0
                    it < -180 -> it + 360.0
                    else -> it
                }
            }

        /**
         * difference x - y of two angles x and y in degree from -180° to 180°
         */
        fun difference(x: Double, y: Double): Double =
            normalizeTo180(x - y)

    }
}