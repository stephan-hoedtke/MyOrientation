package com.stho.myorientation.library.filter

import com.stho.myorientation.*
import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.Orientation


class CompositionFilter(private val options: ICompositionFilterOptions) :
    AbstractOrientationFilter(Method.Composition, options) {

    private val accelerationMagnetometerFilter = AccelerationMagnetometerFilter(options)
    private val rotationVectorFilter = RotationVectorFilter(options)
    private val complementaryFilter = ComplementaryFilter(options)
    private val madgwickFilter = MadgwickFilter(options)
    private val separatedCorrectionFilter = SeparatedCorrectionFilter(options)
    private val extendedComplementaryFilter = ExtendedComplementaryFilter(options)
    private val kalmanFilter = KalmanFilter(options)

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
        get() = rotationVectorFilter.currentOrientation // TODO: use average?

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


