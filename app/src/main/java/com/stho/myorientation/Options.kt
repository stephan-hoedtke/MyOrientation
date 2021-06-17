package com.stho.myorientation

import com.stho.myorientation.library.filter.MadgwickFilter
import com.stho.myorientation.library.filter.SeparatedCorrectionFilter

interface IFilterOptions {
    val accelerationFactor: Double
}

interface IAccelerationMagnetometerFilterOptions : IFilterOptions {
}

interface IComplementaryFilterOptions : IFilterOptions {
    val filterCoefficient: Double
}

interface IMadgwickFilterOptions : IFilterOptions {
    var madgwickMode: MadgwickFilter.Mode
}

interface ISeparatedCorrectionOptions : IFilterOptions {
    var separatedCorrectionMode: SeparatedCorrectionFilter.Mode
}

interface IKalmanFilterOptions : IFilterOptions {
    val varianceAccelerometer: Double
    val varianceMagnetometer: Double
    val varianceGyroscope: Double
}

interface ICompositionFilterOptions :
        IFilterOptions,
        IMadgwickFilterOptions,
        ISeparatedCorrectionOptions,
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
    override var madgwickMode: MadgwickFilter.Mode = MadgwickFilter.Mode.Default,
    override var separatedCorrectionMode: SeparatedCorrectionFilter.Mode = SeparatedCorrectionFilter.Mode.SCF,
    override var accelerationFactor: Double = DEFAULT_ACCELERATION_FACTOR,
    override var filterCoefficient: Double = DEFAULT_FILTER_COEFFICIENT,
    override var varianceAccelerometer: Double = DEFAULT_VARIANCE_ACCELEROMETER,
    override var varianceMagnetometer: Double = DEFAULT_VARIANCE_MAGNETOMETER,
    override var varianceGyroscope: Double = DEFAULT_VARIANCE_GYROSCOPE,
    override var showAccelerometerMagnetometerFilter: Boolean = false,
    override var showRotationVectorFilter: Boolean = true,
    override var showMadgwickFilter: Boolean = true,
    override var showComplementaryFilter: Boolean = false,
    override var showSeparatedCorrectionFilter: Boolean = false,
    override var showExtendedComplementaryFilter: Boolean = true,
    override var showKalmanFilter: Boolean = false,
    override var updateSensorFusionDelay: Long = 150,
    override var updateOrientationDelay: Long = 200,
    ) : IAccelerationMagnetometerFilterOptions,
        IComplementaryFilterOptions,
        IMadgwickFilterOptions,
        ISeparatedCorrectionOptions,
        IKalmanFilterOptions,
        ICompositionFilterOptions,
        IFilterOptions,
        ISettings {

    fun resetDefaultValues() {
        filterCoefficient = DEFAULT_FILTER_COEFFICIENT
        accelerationFactor = DEFAULT_ACCELERATION_FACTOR
        varianceAccelerometer = DEFAULT_VARIANCE_ACCELEROMETER
        varianceMagnetometer = DEFAULT_VARIANCE_MAGNETOMETER
        varianceGyroscope = DEFAULT_VARIANCE_GYROSCOPE
    }

    companion object {
        private const val DEFAULT_ACCELERATION_FACTOR = 0.7
        private const val DEFAULT_FILTER_COEFFICIENT = 0.98
        private const val DEFAULT_VARIANCE_GYROSCOPE = 0.3 * 0.3
        private const val DEFAULT_VARIANCE_ACCELEROMETER = 0.5 * 0.5
        private const val DEFAULT_VARIANCE_MAGNETOMETER = 0.8 * 0.8
    }
}

