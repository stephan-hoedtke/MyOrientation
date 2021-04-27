package com.stho.myorientation.library

/**
 * A low pass filter for a 5-vector of angles in degree
 */
internal class LowPassFilterAnglesInDegree(private val timeConstant: Double = 0.2, private val timeSource: TimeSource = SystemClockTimeSource()) {

    private var startTime: Double = 0.0

    val angles: DoubleArray = DoubleArray(5)

    /**
     * set the new angles in degrees
     */
    fun setAngles(newAngles: DoubleArray) {

        val dt: Double = getTimeDifferenceInSeconds()
        if (dt > 0) {
            val alpha = getAlpha(dt)
            angles[0] = Degree.normalizeTo180(angles[0] + alpha * Degree.normalizeTo180(newAngles[0] - angles[0]))
            angles[1] = Degree.normalizeTo180(angles[1] + alpha * Degree.normalizeTo180(newAngles[1] - angles[1]))
            angles[2] = Degree.normalizeTo180(angles[2] + alpha * Degree.normalizeTo180(newAngles[2] - angles[2]))
            angles[3] = Degree.normalizeTo180(angles[3] + alpha * Degree.normalizeTo180(newAngles[3] - angles[3]))
            angles[4] = Degree.normalizeTo180(angles[4] + alpha * Degree.normalizeTo180(newAngles[4] - angles[4]))
        } else {
            angles[0] = newAngles[0]
            angles[1] = newAngles[1]
            angles[2] = newAngles[2]
            angles[3] = newAngles[3]
            angles[4] = newAngles[4]
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


