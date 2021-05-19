package com.stho.myorientation.library


class Timer {
    private val timeSource: SystemClockTimeSource = SystemClockTimeSource()
    private var startTime = timeSource.elapsedRealtimeSeconds

    /**
     * Reset start time
     */
    fun reset() {
        startTime = timeSource.elapsedRealtimeSeconds
    }

    /**
     * Return elapsed time (since last start time) in seconds
     */
    fun getTime(): Double {
        return timeSource.elapsedRealtimeSeconds - startTime
    }

    /**
     * Return elapsed time (since last start time) in seconds and reset start time
     *
     *      getTime() + reset()
     */
    fun getNextTime(): Double {
        val previousStartTime = startTime
        startTime = timeSource.elapsedRealtimeSeconds
        return startTime - previousStartTime
    }
}

