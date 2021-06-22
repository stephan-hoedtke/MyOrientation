package com.stho.myorientation

import com.stho.myorientation.library.filter.MadgwickFilter
import com.stho.myorientation.library.filter.SeparatedCorrectionFilter
import kotlin.math.PI

interface IFilterOptions {
    val accelerationFactor: Double
}

interface IAccelerationMagnetometerFilterOptions : IFilterOptions {
}

interface IComplementaryFilterOptions : IFilterOptions {
    val filterCoefficient: Double
}

interface IMadgwickFilterOptions : IFilterOptions {
    val madgwickMode: MadgwickFilter.Mode
    val gyroscopeMeanError: Double
    val gyroscopeDrift: Double

}

interface ISeparatedCorrectionFilterOptions : IFilterOptions {
    val separatedCorrectionMode: SeparatedCorrectionFilter.Mode
    val lambda1: Double
    val lambda2: Double
}

interface IExtendedComplementaryFilterOptions : IFilterOptions {
    val mMin: Double
    val mMax: Double
    val kNorm: Double
    val kInit: Double
    val tInit: Double

}

interface IKalmanFilterOptions : IFilterOptions {
    val varianceAccelerometer: Double
    val varianceMagnetometer: Double
    val varianceGyroscope: Double
}

interface ICompositionFilterOptions :
        IFilterOptions,
        IMadgwickFilterOptions,
        ISeparatedCorrectionFilterOptions,
        IExtendedComplementaryFilterOptions,
        IKalmanFilterOptions,
        IAccelerationMagnetometerFilterOptions,
        IComplementaryFilterOptions {
    var showAccelerometerMagnetometerFilter: Boolean
    var showRotationVectorFilter: Boolean
    var showMadgwickFilter: Boolean
    var showComplementaryFilter: Boolean
    var showSeparatedCorrectionFilter: Boolean
    var showExtendedComplementaryFilter: Boolean
    var showKalmanFilter: Boolean
}

interface ISettings {
    var updateSensorFusionDelay: Long
    var updateOrientationDelay: Long
}

data class Options(
    override var accelerationFactor: Double = DEFAULT_ACCELERATION_FACTOR,
    override var updateSensorFusionDelay: Long = 150,
    override var updateOrientationDelay: Long = 200,
        // Composition Filter
    override var showAccelerometerMagnetometerFilter: Boolean = false,
    override var showRotationVectorFilter: Boolean = true,
    override var showMadgwickFilter: Boolean = true,
    override var showComplementaryFilter: Boolean = false,
    override var showSeparatedCorrectionFilter: Boolean = false,
    override var showExtendedComplementaryFilter: Boolean = true,
    override var showKalmanFilter: Boolean = false,
        // Complementary Filter
    override var filterCoefficient: Double = DEFAULT_FILTER_COEFFICIENT,
        // MadgwickFilter
    override var madgwickMode: MadgwickFilter.Mode = MadgwickFilter.Mode.Default,
    override var gyroscopeMeanError: Double = DEFAULT_GYROSCOPE_MEAN_ERROR,
    override var gyroscopeDrift: Double = DEFAULT_GYROSCOPE_DRIFT,
        // Separated Correction Filter
    override var separatedCorrectionMode: SeparatedCorrectionFilter.Mode = SeparatedCorrectionFilter.Mode.SCF,
    override var lambda1: Double = DEFAULT_LAMBDA1,
    override var lambda2: Double = DEFAULT_LAMBDA2,
        // Extended Complementary Filter
    override var kInit: Double = DEFAULT_K_INIT,
    override var kNorm: Double = DEFAULT_K_NORM,
    override var tInit: Double = DEFAULT_T_INIT,
    override val mMax: Double = DEFAULT_M_MAX,
    override val mMin: Double = DEFAULT_M_MIN,
        // Kalman Filter
    override var varianceAccelerometer: Double = DEFAULT_VARIANCE_ACCELEROMETER,
    override var varianceMagnetometer: Double = DEFAULT_VARIANCE_MAGNETOMETER,
    override var varianceGyroscope: Double = DEFAULT_VARIANCE_GYROSCOPE,
    ) : IAccelerationMagnetometerFilterOptions,
        IComplementaryFilterOptions,
        IMadgwickFilterOptions,
        ISeparatedCorrectionFilterOptions,
        IExtendedComplementaryFilterOptions,
        IKalmanFilterOptions,
        ICompositionFilterOptions,
        IFilterOptions,
        ISettings {

    fun resetDefaultValues() {
        accelerationFactor = DEFAULT_ACCELERATION_FACTOR
        separatedCorrectionMode = SeparatedCorrectionFilter.Mode.SCF
        madgwickMode = MadgwickFilter.Mode.Default
        gyroscopeMeanError = DEFAULT_GYROSCOPE_MEAN_ERROR
        gyroscopeDrift = DEFAULT_GYROSCOPE_DRIFT
        filterCoefficient = DEFAULT_FILTER_COEFFICIENT
        varianceAccelerometer = DEFAULT_VARIANCE_ACCELEROMETER
        varianceMagnetometer = DEFAULT_VARIANCE_MAGNETOMETER
        varianceGyroscope = DEFAULT_VARIANCE_GYROSCOPE
        lambda1 = DEFAULT_LAMBDA1
        lambda2 = DEFAULT_LAMBDA2
        kInit = DEFAULT_K_INIT
        kNorm = DEFAULT_K_NORM
        tInit = DEFAULT_T_INIT
    }

    companion object {
        private const val DEFAULT_ACCELERATION_FACTOR = 0.7
        private const val DEFAULT_FILTER_COEFFICIENT = 0.98
        private const val DEFAULT_VARIANCE_GYROSCOPE = 0.3 * 0.3
        private const val DEFAULT_VARIANCE_ACCELEROMETER = 0.5 * 0.5
        private const val DEFAULT_VARIANCE_MAGNETOMETER = 0.8 * 0.8
        private const val DEFAULT_GYROSCOPE_MEAN_ERROR = PI * (5.0f / 180.0f) // 5 deg/s
        private const val DEFAULT_GYROSCOPE_DRIFT = PI * (0.2f / 180.0f) // 0.2 deg/s^2
        private const val DEFAULT_LAMBDA1 = 0.1
        private const val DEFAULT_LAMBDA2 = 0.7
        private const val DEFAULT_K_INIT = 10.0
        private const val DEFAULT_K_NORM = 0.5
        private const val DEFAULT_T_INIT = 3.0 // sec
        private const val DEFAULT_M_MAX = 65.0
        private const val DEFAULT_M_MIN = 25.0
    }
}

