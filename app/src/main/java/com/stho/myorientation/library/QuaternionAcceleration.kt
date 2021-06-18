package com.stho.myorientation.library

import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.IRotation
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Rotation
import kotlin.math.exp

internal class QuaternionAcceleration(factorInSeconds: Double = 0.8, private val timeSource: TimeSource = SystemClockTimeSource()) {

/*
   Use critical damped oscillation
        x(0) = x0
        v(0) = v0
        x(t) = (x0 + (v0 + x0 * delta) * t) * EXP(-delta * t)
        v(t) = (v0 - (v0 + s0 * delta) * delta * t) * EXP(-delta * t)

   Here we use it for interpolation of quaternions
        x0 = 0
        x1 = 1
        t --> x(t) between 0 and 1 --> interpolate(q0, q1, x(t)) := q1 + (q0 - q1) * x(t)
                                            where x(t) >= 1 --> q1
                                                  x(t) <= 0 --> q0
  */

    private var v0: Double = 0.0
    private var x0: Double = 0.0
    private val delta: Double = 5.0
    private var x1: Double = 0.0
    private var t0: Double = timeSource.elapsedRealtimeSeconds
    private var q0: Quaternion = Quaternion.default
    private var q1: Quaternion = Quaternion.default
    private val factor = 1 / factorInSeconds

    val position: Quaternion
        get() {
            val t = getTime(timeSource.elapsedRealtimeSeconds)
            return getPosition(t)
        }

    fun rotateTo(targetPosition: IRotation) {
        rotateTo(Quaternion.fromRotationMatrix(targetPosition))
    }

    fun rotateTo(targetQuaternion: Quaternion) {
        val t1 = timeSource.elapsedRealtimeSeconds
        val t = getTime(t1)
        val v = getSpeed(t)
        q0 = getPosition(t)
        q1 = targetQuaternion
        x0 = 1.0
        v0 = v
        t0 = t1
    }

    private fun getTime(t1: Double): Double =
        factor * (t1 - t0)

    /**
     * Interpolation of two quaternions so that:
     *      t = 0 --> x(t) = 1 --> q0
     *      t > 2 --> x(t) = 0 --> q1
     *
     * Do not use the simple linear interpolation, as it fails for dot(q0, q1) < 0
     *      q := q1 + (q0 - q1) * x(t)
     */
    private fun getPosition(t: Double): Quaternion =
        Quaternion.interpolate(q1, q0, x(t))

    private fun getSpeed(t: Double): Double =
        v(t)

    /**
     * moves the position x from x0 at time 0 to 0 at time 2
     */
    private fun x(t: Double): Double =
        when {
            t < 0 -> x0
            t > 2 -> 0.0
            else -> (x0 + (x0 * delta + v0) * t) * exp(-delta * t)
        }

    /**
     * reduces the speed from v0 at time 0 to 0 at time 2
     */
    private fun v(t: Double): Double =
        when {
            t < 0 -> v0
            t > 2 -> 0.0
            else -> (v0 - (v0 + x0 * delta) * delta * t) * exp(-delta * t)
        }
}
