package com.stho.myorientation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.Orientation
import com.stho.myorientation.library.filter.MadgwickFilter
import com.stho.myorientation.library.filter.SeparatedCorrectionFilter

class Repository private constructor() {

    private val timer: Timer = Timer()

    private val isActiveLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = true }

    val isActive: Boolean
        get() = isActiveLiveData.value ?: true

    val isActiveLD: LiveData<Boolean>
        get() = isActiveLiveData

    fun startStop(): Boolean =
        if (isActive) {
            stop()
            false
        } else {
            start()
            true
        }

    private fun start() {
        measurements.clear()
        entries.clear()
        timer.reset()
        touch()
        isActiveLiveData.postValue(true)
    }

    private fun stop() {
        isActiveLiveData.postValue(false)
        touch()
    }

    private val versionLiveData: MutableLiveData<Long> = MutableLiveData<Long>().apply { value = 1 }

    val versionLD: LiveData<Long>
        get() = versionLiveData

    private fun touch() {
        val version = versionLiveData.value ?: 1
        versionLiveData.postValue(version)
    }

    val currentTime: Double
        get() = timer.getTime()

    val entries: Entries = Entries()
    val measurements: Measurements = Measurements()

    internal fun recordMeasurement(type: Measurements.Type, r: FloatArray) {
        // see: https://developer.android.com/reference/android/hardware/SensorEvent#values
        // Magnetometer:  micro-Tesla: typical values expected from 0.01 to 0.2
        // Accelerometer: meter per square second, typical value g = 9.81
        // Gyroscope: radian per second: typical values expected from 0 to 2 PI
        if (isActive) {
            measurements.add(timer.getTime(), type, r[0].toDouble(), r[1].toDouble(), r[2].toDouble())
            touch()
        }
    }

    internal fun recordEntry(method: Method, orientation: Orientation) {
        if (isActive) {
            entries.add(timer.getTime(), method, orientation)
            touch()
        }
    }

    internal fun cleanupHistory() {
        val time = timer.getTime() - FIVE_MINUTES
        entries.cleanupHistory(time)
        measurements.cleanupHistory(time)
    }

    companion object {
        val instance: Repository by lazy { Repository() }
        private const val FIVE_MINUTES: Double = 300.0
    }
}

