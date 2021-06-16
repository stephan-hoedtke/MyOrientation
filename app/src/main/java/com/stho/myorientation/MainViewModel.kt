package com.stho.myorientation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.stho.myorientation.library.Timer
import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.EulerAngles
import com.stho.myorientation.library.algebra.Orientation
import com.stho.myorientation.library.filter.MadgwickFilter
import com.stho.myorientation.library.filter.SeparatedCorrectionFilter
import kotlin.math.abs

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val methodLiveData: MutableLiveData<Method> = MutableLiveData<Method>().apply { value = Method.Composition }
    private val optionsLiveData: MutableLiveData<Options> = MutableLiveData<Options>().apply { value = Options() }
    private val zoomLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_ZOOM }
    private val propertyLiveData: MutableLiveData<Property> = MutableLiveData<Property>().apply { value = Property.CenterAzimuth}
    private val startTimeLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_START_TIME }
    private val orientationLiveData: MutableLiveData<Orientation> = MutableLiveData<Orientation>().apply { value = Orientation.default }
    private val cubeOrientationLiveData: MutableLiveData<EulerAngles> = MutableLiveData<EulerAngles>().apply { value = EulerAngles.default }
    private val processorConsumptionLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = 0.0 }
    private val processorConsumptionTimer = Timer()

    val methodLD: LiveData<Method>
        get() = methodLiveData

    val zoomLD: LiveData<Double>
        get() = zoomLiveData

    val startTimeLD: LiveData<Double>
        get() = startTimeLiveData

    val propertyLD: LiveData<Property>
        get() = propertyLiveData

    val optionsLD: LiveData<Options>
        get() = optionsLiveData

    var options: Options
        get() = optionsLiveData.value ?: Options()
        set(value) { optionsLiveData.postValue(value) }

    val accelerationFactorLD: LiveData<Double>
        get() = Transformations.map(optionsLiveData) { params -> params.accelerationFactor }

    val timeConstantLD: LiveData<Double>
        get() = Transformations.map(optionsLiveData) { params -> params.timeConstant }

    val filterCoefficientLD: LiveData<Double>
        get() = Transformations.map(optionsLiveData) { params -> params.filterCoefficient }

    val orientationLD: LiveData<Orientation>
        get() = orientationLiveData

    val versionLD: LiveData<Long>
        get() = Repository.instance.versionLD

    val isActiveLD: LiveData<Boolean>
        get() = Repository.instance.isActiveLD

    val processorConsumptionLD: LiveData<Double>
        get() = processorConsumptionLiveData

    val cubeOrientationLD: LiveData<EulerAngles>
        get() = cubeOrientationLiveData

    fun rotateCube(alpha: Double, beta: Double) {
        val eulerAngles = cubeOrientationLiveData.value ?: EulerAngles.default
        val newEulerAngles = EulerAngles.fromAzimuthPitchRoll(
            azimuth = Degree.normalizeTo180(eulerAngles.azimuth + alpha),
            pitch = Degree.normalizeTo180(eulerAngles.pitch + beta),
            roll = 0.0)
        cubeOrientationLiveData.postValue(newEulerAngles)
    }

    val isActive: Boolean
        get() = Repository.instance.isActive

    fun setMadgwickFilterMode(mode: MadgwickFilter.Mode) {
        touch(options.apply {
            madgwickMode = mode
        })
    }

    fun setSeparatedCorrectionFilterMode(mode: SeparatedCorrectionFilter.Mode) {
        touch(options.apply {
            separatedCorrectionMode = mode
        })
    }

    fun showFilter(method: Method, value: Boolean) {
        touch(options.apply {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (method) {
                Method.AccelerometerMagnetometer -> showAccelerometerMagnetometerFilter = value
                Method.RotationVector -> showRotationVectorFilter = value
                Method.ComplementaryFilter -> showComplementaryFilter = value
                Method.MadgwickFilter -> showMadgwickFilter = value
                Method.SeparatedCorrectionFilter -> showSeparatedCorrectionFilter = value
                Method.ExtendedComplementaryFilter -> showExtendedComplementaryFilter = value
                Method.KalmanFilter -> showKalmanFilter = value
            }
        })
    }

    internal fun touch(filterParameters: Options) {
        optionsLiveData.value = filterParameters
        touch()
    }

    private fun touch() {
        methodLiveData.postValue(method)
    }

    fun reset() {
        methodLiveData.postValue(method)
        startTimeLiveData.postValue(DEFAULT_START_TIME)
        zoomLiveData.postValue(DEFAULT_ZOOM)
    }

    fun resetDefaultValues() {
        touch(options.apply {
            resetDefaultValues()
        })
    }

    var method: Method
        get() = methodLiveData.value ?: Method.AccelerometerMagnetometer
        set(value) { methodLiveData.postValue(value) }

    fun onZoom(f: Double) {
        val zoom: Double = zoomLiveData.value ?: DEFAULT_ZOOM
        zoomLiveData.postValue(zoom * f)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onScroll(dx: Double, dy: Double) {
        val startTime: Double = startTimeLiveData.value ?: DEFAULT_START_TIME
        val zoom: Double = zoomLiveData.value ?: DEFAULT_ZOOM
        startTimeLiveData.postValue(startTime + dx / zoom)
    }

    var property: Property
        get() = propertyLiveData.value ?: Property.Azimuth
        set(value) { propertyLiveData.postValue(value) }

    var filterCoefficient: Double
        get() = options.filterCoefficient
        set(value) {
            touch(options.apply {
                filterCoefficient = value.coerceIn(0.0, 1.0)
            })
        }

    var timeConstant: Double
        get() = options.timeConstant
        set(value) {
            touch(options.apply {
                timeConstant = value.coerceIn(0.01, 10.0)
            })
        }

    var accelerationFactor: Double
        get() = options.accelerationFactor
        set(value) {
            touch(options.apply {
                accelerationFactor = value.coerceIn(0.01, 10.0)
            })
        }

    var varianceAccelerometer: Double
        get() = options.varianceAccelerometer
        set(value) {
            touch(options.apply {
                varianceAccelerometer = value.coerceIn(0.0, 1.0)
            })
        }

    var varianceMagnetometer: Double
        get() = options.varianceMagnetometer
        set(value) {
            touch(options.apply {
                varianceMagnetometer = value.coerceIn(0.0, 1.0)
            })
        }

    var varianceGyroscope: Double
        get() = options.varianceGyroscope
        set(value) {
            touch(options.apply {
                varianceGyroscope = value.coerceIn(0.0, 1.0)
            })
        }

    fun onUpdateOrientation(orientation: Orientation) {
        Repository.instance.recordEntry(Method.Damped, orientation)
        orientationLiveData.postValue(orientation)
    }

    fun startStop() {
        if (Repository.instance.startStop()) {
            reset()
        }
    }

    fun cleanupHistory() {
        Repository.instance.cleanupHistory()
    }

    fun startProcessorConsumptionMeasurement() {
        processorConsumptionTimer.reset()
    }

    fun stopProcessorConsumption() {
        val consumption = processorConsumptionTimer.getTime()
        val oldValue = processorConsumptionLiveData.value ?: consumption
        val difference = consumption - oldValue
        val percentage = abs(difference) / (consumption + oldValue)
        val gain = when {
            percentage > 0.5 -> 0.01
            percentage > 0.1 -> 0.001
            else -> 0.0001
        }
        val newValue = oldValue + gain * difference
        processorConsumptionLiveData.postValue(newValue)
    }

    companion object {
        private const val DEFAULT_ZOOM = 100.0
        private const val DEFAULT_START_TIME = 0.0
    }
}

