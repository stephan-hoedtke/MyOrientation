package com.stho.myorientation.library

import com.stho.myorientation.library.algebra.Degree
import kotlin.math.exp

internal class Acceleration(factorInSeconds: Double = 0.8, private val timeSource: TimeSource = SystemClockTimeSource()) {

/*
   Use critical damped oscillation
        x(0) = x0
        v(0) = v0
        x(t) = (x0 + (v0 + x0 * delta) * t) * EXP(-delta * t)
        v(t) = (v0 - (v0 + s0 * delta) * delta * t) * EXP(-delta * t)
  */

    private var v0: Double = 0.0
    private var x0: Double = 0.0
    private val delta: Double = 5.0
    private var alpha: Double = 0.0
    private var t0: Double = timeSource.elapsedRealtimeSeconds
    private val factor = 1 / factorInSeconds

    val position: Double
        get() {
            val t = getTime(timeSource.elapsedRealtimeSeconds)
            return getPosition(t)
        }

    fun rotateTo(targetAngle: Double) {
        // current 10°, new target = 30° --> o.k.
        // current 350°, new target = 330° --> o.k.
        // current 10°, new target = 350° --> rotate in negative direction to -20
        // current 350°, new target = 10° --> rotate in positive direction to 370
        // the trick:
        //      instead of 350° to 10° we will move from -10° to 10°
        //      instead of 10° to 350° we will move from 10° to -20°

        val t1 = timeSource.elapsedRealtimeSeconds
        val t = getTime(t1)
        val v = getSpeed(t)
        val s = getPosition(t)
        x0 = Degree.difference(targetAngle, s)
        v0 = v
        t0 = t1
        alpha = targetAngle
     }

    private fun getTime(t1: Double): Double =
        factor * (t1 - t0)

    private fun getPosition(t: Double): Double =
        Degree.normalize(alpha - x(t))

    private fun getSpeed(t: Double): Double =
        v(t)

    private fun x(t: Double): Double =
        when {
            t < 0 -> x0;
            t > 2 -> 0.0;
            else -> (x0 + (x0 * delta + v0) * t) * exp(-delta * t);
        }

    private fun v(t: Double): Double =
        when {
            t < 0 -> v0
            t > 2 -> 0.0
            else -> (v0 - (v0 + x0 * delta) * delta * t) * exp(-delta * t)
        }
}

