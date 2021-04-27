package com.stho.myorientation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stho.myorientation.library.Orientation

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val methodLiveData: MutableLiveData<Entries.Method> = MutableLiveData<Entries.Method>().apply { value = Entries.Method.ComplementaryFilter }
    private val zoomLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_ZOOM }
    private val startTimeLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_START_TIME }
    private val propertyLiveData: MutableLiveData<Entries.Property> = MutableLiveData<Entries.Property>().apply { value = Entries.Property.CenterAzimuth}
    private val accelerationFactorLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_ACCELERATION_FACTOR }
    private val timeConstantLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_TIME_CONSTANT }
    private val filterCoefficientLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_FILTER_COEFFICIENT }
    private val orientationLiveData: MutableLiveData<Orientation> = MutableLiveData<Orientation>().apply { value = Orientation(0.0, 0.0, 0.0, 0.0, 0.0) }

    val methodLD: LiveData<Entries.Method>
        get() = methodLiveData

    val zoomLD: LiveData<Double>
        get() = zoomLiveData

    val startTimeLD: LiveData<Double>
        get() = startTimeLiveData

    val propertyLD: LiveData<Entries.Property>
        get() = propertyLiveData

    val accelerationFactorLD: LiveData<Double>
        get() = accelerationFactorLiveData

    val timeConstantLD: LiveData<Double>
        get() = timeConstantLiveData

    val filterCoefficientLD: LiveData<Double>
        get() = filterCoefficientLiveData

    val orientationLD: LiveData<Orientation>
        get() = orientationLiveData

    val versionLD: LiveData<Long>
        get() = Repository.instance.versionLD

    val isActiveLD: LiveData<Boolean>
        get() = Repository.instance.isActiveLD

    fun reset() {
        startTimeLiveData.postValue(DEFAULT_START_TIME)
        zoomLiveData.postValue(DEFAULT_ZOOM)
    }

    fun resetDefaultValues() {
        filterCoefficient = DEFAULT_FILTER_COEFFICIENT
        accelerationFactor = DEFAULT_ACCELERATION_FACTOR
        timeConstant = DEFAULT_TIME_CONSTANT
    }

    var method: Entries.Method
        set(value) {
            methodLiveData.postValue(value)
        }
        get() {
            return methodLiveData.value ?: Entries.Method.AccelerometerMagnetometer
        }

    fun onZoom(f: Double) {
        val zoom: Double = zoomLiveData.value ?: DEFAULT_ZOOM
        zoomLiveData.postValue(zoom * f)
    }

    fun onScroll(dx: Double, dy: Double) {
        val startTime: Double = startTimeLiveData.value ?: DEFAULT_START_TIME
        val zoom: Double = zoomLiveData.value ?: DEFAULT_ZOOM
        startTimeLiveData.postValue(startTime + dx / zoom)
    }

    var property: Entries.Property
        set(value) {
            propertyLiveData.postValue(value)
        }
        get() {
            return propertyLiveData.value ?: Entries.Property.Azimuth
        }

    var filterCoefficient: Double
        set(value) {
            filterCoefficientLiveData.postValue(value)
        }
        get() {
            return filterCoefficientLiveData.value ?: DEFAULT_FILTER_COEFFICIENT
        }

    var timeConstant: Double
        set(value) {
            timeConstantLiveData.postValue(value.coerceIn(0.01, 10.0))
        }
        get() {
            return timeConstantLiveData.value ?: DEFAULT_TIME_CONSTANT
        }

    var accelerationFactor: Double
        set(value) {
            accelerationFactorLiveData.postValue(value.coerceIn(0.01, 10.0))
        }
        get() {
            return accelerationFactorLiveData.value ?: DEFAULT_ACCELERATION_FACTOR
        }

    fun onUpdateOrientation(orientation: Orientation) {
        Repository.instance.recordEntry(Entries.Method.Damped, orientation)
        orientationLiveData.postValue(orientation)
    }

    fun startStop() {
        if (Repository.instance.startStop()) {
            startTimeLiveData.postValue(DEFAULT_START_TIME)
        }
    }

    fun cleanupHistory() {
        Repository.instance.cleanupHistory()
    }

    companion object {
        private const val DEFAULT_ZOOM = 100.0
        private const val DEFAULT_START_TIME = 0.0
        private const val DEFAULT_ACCELERATION_FACTOR = 0.7
        private const val DEFAULT_TIME_CONSTANT = 0.4
        private const val DEFAULT_FILTER_COEFFICIENT = 0.98
    }
}

