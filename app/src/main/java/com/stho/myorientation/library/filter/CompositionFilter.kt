package com.stho.myorientation.library.filter

import com.stho.myorientation.Entries
import com.stho.myorientation.MainViewModel
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.Orientation


class CompositionFilter(options: MainViewModel.Options, accelerationFactor: Double = 0.7, timeConstant: Double = 0.2, filterCoefficient: Double = 0.98) : AbstractOrientationFilter(Entries.Method.Composition, accelerationFactor), OrientationFilter {

    private val accelerationMagnetometerFilter = AccelerationMagnetometerFilter(accelerationFactor, timeConstant)
    private val rotationVectorFilter = RotationVectorFilter(accelerationFactor)
    private val complementaryFilter = ComplementaryFilter(accelerationFactor, filterCoefficient)
    private val madgwickFilter = MadgwickFilter(options.madgwickMode, accelerationFactor)
    private val separatedCorrectionFilter = SeparatedCorrectionFilter(options.separatedCorrectionMode, accelerationFactor)
    private val kalmanFilter = KalmanFilter(accelerationFactor)

    override fun updateReadings(type: Measurements.Type, values: FloatArray) {
        accelerationMagnetometerFilter.updateReadings(type, values)
        rotationVectorFilter.updateReadings(type, values)
        complementaryFilter.updateReadings(type, values)
        madgwickFilter.updateReadings(type, values)
        separatedCorrectionFilter.updateReadings(type, values)
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


