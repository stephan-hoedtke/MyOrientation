package com.stho.myorientation.library

import android.os.SystemClock


/*
    Testability: the internal call to SystemClock can be mocked

    Hints for Mockito:
    - Using 'org.mockito:mockito-core:3.4.0' the both the class and the internal private function elapsedRealtimeNanos() must be internal and open.

                internal open class SystemClockTimeSource: TimeSource {
                    override val elapsedRealtimeSeconds: Double
                        get() = SECONDS_PER_NANOSECOND * elapsedRealtimeNanos()

                    internal open fun elapsedRealtimeNanos(): Long {
                        return SystemClock.elapsedRealtimeNanos()
                    }

                    companion object {
                        private const val SECONDS_PER_NANOSECOND: Double = 1.0 / 1000000000.0
                    }
                }

            val timeSource = Mockito.mock(SystemClockTimeSource::class.java)
            Mockito.doAnswer { _ -> fakeClock.elapsedRealtimeNanos}.`when`(timeSource).elapsedRealtimeNanos()
            Mockito.doCallRealMethod().`when`(timeSource).elapsedRealtimeSeconds
            (in 1.3 seconds)
        or
            val timeSource = Mockito.mock(SystemClockTimeSource::class.java)
            Mockito.`when`(timeSource.elapsedRealtimeNanos()).then { _ -> fakeClock.elapsedRealtimeNanos}
            Mockito.`when`(timeSource.elapsedRealtimeSeconds).thenCallRealMethod()
            (in 1.3 seconds)

      Otherwise the mocked method elapsedRealtimeNanos() would never be called, but the original method in the based class.
      Which fails with exception: "SystemClock.elapsedRealtimeNanos not mocked"

    - using 'org.mockito:mockito-inline:3.4.0' we may mock the final function in the final class...

                internal class SystemClockTimeSource: TimeSource {
                    override val elapsedRealtimeSeconds: Double
                        get() = SECONDS_PER_NANOSECOND * elapsedRealtimeNanos()

                    internal fun elapsedRealtimeNanos(): Long {
                        return SystemClock.elapsedRealtimeNanos()
                    }

                    companion object {
                        private const val SECONDS_PER_NANOSECOND: Double = 1.0 / 1000000000.0
                    }
                }

            val timeSource = Mockito.mock(SystemClockTimeSource::class.java)
            Mockito.doAnswer { _ -> fakeClock.elapsedRealtimeNanos}.`when`(timeSource).elapsedRealtimeNanos()
            Mockito.doCallRealMethod().`when`(timeSource).elapsedRealtimeSeconds
            (in 1.8 seconds)

      ... or even SystemClock.elapsedRealtimeNanos() directly

                internal class SystemClockTimeSource: TimeSource {
                    override val elapsedRealtimeSeconds: Double
                        get() = SECONDS_PER_NANOSECOND * SystemClock.elapsedRealtimeNanos()

                    companion object {
                        private const val SECONDS_PER_NANOSECOND: Double = 1.0 / 1000000000.0
                    }
                }

            val timeSource = SystemClockTimeSource()
            val systemClock = Mockito.mockStatic(SystemClock::class.java)
            systemClock.`when`<Any>(SystemClock::elapsedRealtimeNanos).then { _ -> fakeClock.elapsedRealtimeNanos }
            (in 1.8 seconds)

 */
internal class SystemClockTimeSource: TimeSource {

    override val elapsedRealtimeSeconds: Double
        get() = SECONDS_PER_NANOSECOND * SystemClock.elapsedRealtimeNanos()

    companion object {
        private const val SECONDS_PER_NANOSECOND: Double = 1.0 / 1000000000.0
    }
}

