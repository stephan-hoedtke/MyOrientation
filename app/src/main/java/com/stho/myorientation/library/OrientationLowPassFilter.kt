package com.stho.myorientation.library

import com.stho.myorientation.library.algebra.Orientation

/**
 * A low pass filter for a 5-vector of angles in degree
 */
internal class OrientationLowPassFilter(private val timeConstant: Double = 0.2, private val timeSource: TimeSource = SystemClockTimeSource()) {

    private var startTime: Double = 0.0

    var orientation: Orientation = Orientation(0.0, 0.0, 0.0, 0.0, 0.0)
        private set

    /**
     * set the new angles in degrees
     */
    fun onUpdateOrientation(newOrientation: Orientation) {
        val dt: Double = getTimeDifferenceInSeconds()
        if (dt > 0) {
            val alpha = getAlpha(dt)
            orientation += (newOrientation - orientation) * alpha
        } else {
            orientation = newOrientation
        }
    }

    private fun getTimeDifferenceInSeconds(): Double {
        val t0 = startTime
        val t1 = timeSource.elapsedRealtimeSeconds
        startTime = t1
        return t1 - t0
    }

    /**
     * dt / (dt + T)
     *          --> dt << T --> dt/T << 0.5
     *              dt >> T --> 1.0
     *              dt == T --> 50/50
     *
     *
     */
    private fun getAlpha(dt: Double): Double =
        dt / (dt + timeConstant)
}


