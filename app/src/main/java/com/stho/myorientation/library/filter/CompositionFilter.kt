package com.stho.myorientation.library.filter

import com.stho.myorientation.Entries
import com.stho.myorientation.MainViewModel
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.Orientation


class CompositionFilter(val options: MainViewModel.Options, accelerationFactor: Double = 0.7, timeConstant: Double = 0.2, filterCoefficient: Double = 0.98) : AbstractOrientationFilter(Entries.Method.Composition, accelerationFactor), OrientationFilter {

    private val accelerationMagnetometerFilter = AccelerationMagnetometerFilter(accelerationFactor, timeConstant)
    private val rotationVectorFilter = RotationVectorFilter(accelerationFactor)
    private val complementaryFilter = ComplementaryFilter(accelerationFactor, filterCoefficient)
    private val madgwickFilter = MadgwickFilter(options.madgwickMode, accelerationFactor)
    private val separatedCorrectionFilter = SeparatedCorrectionFilter(options.separatedCorrectionMode, accelerationFactor)
    private val extendedComplementaryFilter = ExtendedComplementaryFilter(accelerationFactor)
    private val kalmanFilter = KalmanFilter(accelerationFactor)

    override fun updateReadings(type: Measurements.Type, values: FloatArray) {
        if (options.showAccelerometerMagnetometerFilter)
            accelerationMagnetometerFilter.updateReadings(type, values)

        if (options.showRotationVectorFilter)
            rotationVectorFilter.updateReadings(type, values)

        if (options.showComplementaryFilter)
            complementaryFilter.updateReadings(type, values)

        if (options.showMadgwickFilter)
            madgwickFilter.updateReadings(type, values)

        if (options.showSeparatedCorrectionFilter)
            separatedCorrectionFilter.updateReadings(type, values)

        if (options.showExtendedComplementaryFilter)
            extendedComplementaryFilter.updateReadings(type, values)

        if (options.showKalmanFilter)
            kalmanFilter.updateReadings(type, values)
    }

    override val currentOrientation: Orientation
        get() = rotationVectorFilter.currentOrientation

    override fun reset() {
        accelerationMagnetometerFilter.reset()
        rotationVectorFilter.reset()
        complementaryFilter.reset()
        madgwickFilter.reset()
        separatedCorrectionFilter.reset()
        extendedComplementaryFilter.reset()
        kalmanFilter.reset()
    }

    private fun averageForAnglesInDegree(a: Double, b: Double): Double =
        Degree.arcTan2(
            Degree.sin(a) + Degree.sin(b),
            Degree.cos(a) + Degree.cos(b),
        )

    private fun averageForAnglesInDegree(a: Double, b: Double, c: Double): Double =
        Degree.arcTan2(
            Degree.sin(a) + Degree.sin(b) + Degree.sin(c),
            Degree.cos(a) + Degree.cos(b) + Degree.cos(c),
        )

    private fun averageForAnglesInDegree(a: Double, b: Double, c: Double, d: Double): Double =
        Degree.arcTan2(
            Degree.sin(a) + Degree.sin(b) + Degree.sin(c) + Degree.sin(d),
            Degree.cos(a) + Degree.cos(b) + Degree.cos(c) + Degree.cos(d),
        )
}


