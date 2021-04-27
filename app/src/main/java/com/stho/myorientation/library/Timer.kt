package com.stho.myorientation.library


class Timer {
    private val timeSource: SystemClockTimeSource = SystemClockTimeSource()
    private var startTime = timeSource.elapsedRealtimeSeconds

    /**
     * reset start time
     */
    fun reset() {
        startTime = timeSource.elapsedRealtimeSeconds
    }

    /**
     * get elapsed time since last start time in seconds
     */
    fun getTime(): Double {
        return timeSource.elapsedRealtimeSeconds - startTime
    }

    /**
     * getTime() + reset()
     */
    fun getNextTime(): Double {
        val previousStartTime = startTime
        startTime = timeSource.elapsedRealtimeSeconds
        return startTime - previousStartTime
    }
}

