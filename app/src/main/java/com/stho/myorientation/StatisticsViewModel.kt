package com.stho.myorientation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.filter.MadgwickFilter
import com.stho.myorientation.library.filter.SeparatedCorrectionFilter
import kotlin.math.sqrt

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val accelerometerLiveData: MutableLiveData<Statistic> = MutableLiveData<Statistic>()
    private val magnetometerLiveData: MutableLiveData<Statistic> = MutableLiveData<Statistic>()
    private val gyroscopeLiveData: MutableLiveData<Statistic> = MutableLiveData<Statistic>()

    class Statistic(
        val meanX: Double,
        val meanY: Double,
        val meanZ: Double,
        val varianceX: Double,
        val varianceY: Double,
        val varianceZ: Double,
    ) {
        val standardDeviationX: Double = sqrt(varianceX)
        val standardDeviationY: Double = sqrt(varianceY)
        val standardDeviationZ: Double = sqrt(varianceZ)
    }

    private val timer: Timer = Timer()

    val accelerometerLD: LiveData<Statistic>
        get() = accelerometerLiveData

    val magnetometerLD: LiveData<Statistic>
        get() = magnetometerLiveData

    val gyroscopeLD: LiveData<Statistic>
        get() = gyroscopeLiveData

    fun updateStatistics() {
        val repository = Repository.instance
        val startTime = timer.getTime() - TIME_RANGE_IN_SECONDS
        accelerometerLiveData.postValue(calculate(repository, Measurements.Type.Accelerometer, startTime))
        magnetometerLiveData.postValue(calculate(repository, Measurements.Type.Magnetometer, startTime))
        gyroscopeLiveData.postValue(calculate(repository, Measurements.Type.Gyroscope, startTime))
    }

    companion object {
        private const val TIME_RANGE_IN_SECONDS = 2

        private fun calculate(repository: Repository, type: Measurements.Type, startTime: Double): Statistic {

            val entries = repository.measurements[type].elements().filter { x -> x.time > startTime }

            val n: Int = entries.size
            if (n > 2) {

                var sumX = 0.0
                var sumY = 0.0
                var sumZ = 0.0

                for (entry in entries) {
                    sumX += entry.x
                    sumY += entry.y
                    sumZ += entry.z
                }
                val meanX = sumX / n
                val meanY = sumY / n
                val meanZ = sumZ / n

                var sumSquareDX = 0.0
                var sumSquareDY = 0.0
                var sumSquareDZ = 0.0

                for (entry in entries) {
                    sumSquareDX = (entry.x - meanX).let { it * it }
                    sumSquareDY = (entry.y - meanY).let { it * it }
                    sumSquareDZ = (entry.z - meanZ).let { it * it }
                }

                return Statistic(
                    meanX = meanX,
                    meanY = meanY,
                    meanZ = meanZ,
                    varianceX = sumSquareDX / n,
                    varianceY = sumSquareDY / n,
                    varianceZ = sumSquareDZ / n,
                )
            }
            else {
                return Statistic(
                    meanX = 0.0,
                    meanY = 0.0,
                    meanZ = 0.0,
                    varianceX = 0.0,
                    varianceY = 0.0,
                    varianceZ = 0.0,
                )
            }
        }
    }
}

