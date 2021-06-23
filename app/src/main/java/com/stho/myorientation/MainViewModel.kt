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
import com.stho.myorientation.library.filter.*
import kotlin.math.abs

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val optionsLiveData: MutableLiveData<Options> = MutableLiveData<Options>().apply { value = Options() }
    private val zoomLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_ZOOM }
    private val startTimeLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = DEFAULT_START_TIME }
    private val orientationLiveData: MutableLiveData<Orientation> = MutableLiveData<Orientation>().apply { value = Orientation.default }
    private val cubeOrientationLiveData: MutableLiveData<EulerAngles> = MutableLiveData<EulerAngles>().apply { value = EulerAngles.default }
    private val processorConsumptionLiveData: MutableLiveData<Double> = MutableLiveData<Double>().apply { value = 0.0 }
    private val processorConsumptionTimer = Timer()

    init {
        loadOptions()
    }

    val methodLD: LiveData<Method>
        get() =  Transformations.map(optionsLiveData) { options -> options.method }

    val propertyLD: LiveData<Property>
        get() = Transformations.map(optionsLiveData) { options -> options.property }

    val zoomLD: LiveData<Double>
        get() = zoomLiveData

    val startTimeLD: LiveData<Double>
        get() = startTimeLiveData

    val optionsLD: LiveData<Options>
        get() = optionsLiveData

    val options: Options
        get() = optionsLiveData.value ?: Options()

    val accelerationFactorLD: LiveData<Double>
        get() = Transformations.map(optionsLiveData) { params -> params.accelerationFactor }

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

    fun setUpdateOrientationDelay(newDelay: Long) {
        touch(options.apply {
            updateOrientationDelay = newDelay
        })
    }

    fun setMethod(newMethod: Method) {
        touch(options.apply {
            method = newMethod
        })
    }

    fun setProperty(newProperty: Property) {
        touch(options.apply {
            property = newProperty
        })
    }

    fun setAccelerationFactor(newAccelerationFactor: Double) {
        touch(options.apply {
            accelerationFactor = newAccelerationFactor
        })
    }

    fun setVarianceAccelerometer(newVariance: Double) {
        touch(options.apply {
            varianceAccelerometer = newVariance
        })
    }

    fun setVarianceMagnetometer(newVariance: Double) {
        touch(options.apply {
            varianceMagnetometer = newVariance
        })
    }

    fun setVarianceGyroscope(newVariance: Double) {
        touch(options.apply {
            varianceGyroscope = newVariance
        })
    }

    fun setOptions(
        newFilterCoefficient: Double,
        newLambda1: Double,
        newLambda2: Double,
        newKNorm: Double,
        newGyroscopeMeanError: Double,
        newGyroscopeDrift: Double) {
        touch(options.apply {
            filterCoefficient = newFilterCoefficient
            lambda1 = newLambda1
            lambda2 = newLambda2
            kNorm = newKNorm
            gyroscopeMeanError = newGyroscopeMeanError
            gyroscopeDrift = newGyroscopeDrift
        })
    }

    fun resetCubeOrientation() {
        cubeOrientationLiveData.postValue(EulerAngles.default)
    }

    fun reset() {
        startTimeLiveData.postValue(DEFAULT_START_TIME)
        zoomLiveData.postValue(DEFAULT_ZOOM)
    }

    fun resetDefaultOptions() {
        touch(options.apply {
            resetDefaultOptions()
        })
    }

    val method: Method
        get() = options.method

    val property: Property
        get() = options.property

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
        // apply a linear low pass filter ...
        val percentage = abs(difference) / (consumption + oldValue)
        val gain = when {
            percentage > 0.5 -> 0.01
            percentage > 0.1 -> 0.001
            else -> 0.0001
        }
        val newValue = oldValue + gain * difference
        processorConsumptionLiveData.postValue(newValue)
    }

    internal fun createFilter(): IOrientationFilter =
        when (method) {
            Method.AccelerometerMagnetometer -> AccelerationMagnetometerFilter(options)
            Method.RotationVector -> RotationVectorFilter(options)
            Method.ComplementaryFilter -> ComplementaryFilter(options)
            Method.MadgwickFilter -> MadgwickFilter(options)
            Method.SeparatedCorrectionFilter -> SeparatedCorrectionFilter(options)
            Method.ExtendedComplementaryFilter -> ExtendedComplementaryFilter(options)
            Method.KalmanFilter -> KalmanFilter(options)
            Method.Composition -> CompositionFilter(options)
            Method.Damped -> CompositionFilter(options)
        }

    private fun loadOptions() {
        touch(options.apply {
            OptionsManager(getApplication()).load(this)
        })
    }

    private fun saveOptions(options: Options) {
        OptionsManager(getApplication()).save(options)
    }

    private fun touch(options: Options) {
        saveOptions(options)
        optionsLiveData.postValue(options)
    }

    companion object {
        private const val DEFAULT_ZOOM = 100.0
        private const val DEFAULT_START_TIME = 0.0
    }
}

