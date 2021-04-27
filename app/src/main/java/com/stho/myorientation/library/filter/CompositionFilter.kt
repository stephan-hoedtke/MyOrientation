package com.stho.myorientation.library.filter

import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.library.Degree
import com.stho.myorientation.library.Orientation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class CompositionFilter(accelerationFactor: Double = 0.7, timeConstant: Double = 0.2, filterCoefficient: Double = 0.98) : AbstractOrientationFilter(Entries.Method.Composition, accelerationFactor), OrientationFilter {

    private val accelerationMagnetometerFilter = AccelerationMagnetometerFilter(accelerationFactor, timeConstant)
    private val rotationVectorFilter = RotationVectorFilter(accelerationFactor)
    private val complementaryFilter = ComplementaryFilter(accelerationFactor, filterCoefficient)
    private val kalmanFilter = KalmanFilter()

    override fun updateReadings(type: Measurements.Type, values: FloatArray) {
        accelerationMagnetometerFilter.updateReadings(type, values)
        rotationVectorFilter.updateReadings(type, values)
        complementaryFilter.updateReadings(type, values)
        kalmanFilter.updateReadings(type, values)
    }

    override val currentOrientation: Orientation
        get() {
            val a = accelerationMagnetometerFilter.currentOrientation
            val b = rotationVectorFilter.currentOrientation
            val c = complementaryFilter.currentOrientation
            //val d = kalmanFilter.currentOrientation

            return Orientation(
                    azimuth = Degree.normalizeTo180(averageForAnglesInDegree(a.azimuth, b.azimuth, c.azimuth)),
                    pitch = Degree.normalizeTo180(averageForAnglesInDegree(a.pitch, b.pitch, c.pitch)),
                    roll = Degree.normalizeTo180(averageForAnglesInDegree(a.roll, b.roll, c.roll)),
                    centerAzimuth = Degree.normalizeTo180(averageForAnglesInDegree(a.centerAzimuth, b.centerAzimuth, c.centerAzimuth)),
                    centerAltitude = Degree.normalizeTo180(averageForAnglesInDegree(a.centerAltitude, b.centerAltitude, c.centerAltitude))
            )
        }

        private fun averageForAnglesInDegree(a: Double, b: Double, c: Double): Double =
            Degree.arcTan2(Degree.sin(a) + Degree.sin(b) + Degree.sin(c), Degree.cos(a) + Degree.cos(b) + Degree.cos(c))
}

