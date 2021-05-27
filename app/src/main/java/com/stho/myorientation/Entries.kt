package com.stho.myorientation

import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.Orientation


@Suppress("ReplaceWithEnumMap")
class Entries {

    enum class Method(private val description: String) {
        AccelerometerMagnetometer("Accelerometer & Magnetometer"),
        RotationVector("Rotation Vector"),
        ComplementaryFilter("Complementary Fusion Filter"),
        KalmanFilter("Kalman Filter"),
        MadgwickFilter("Madgwick Filter"),
        SeparatedCorrectionFilter("Separated Correction Filter"),
        Composition("Composition Filter"),
        Damped("(Damped)");

        override fun toString(): String =
                description
    }

    enum class Property(private val description: String) {
        Azimuth("Azimuth"),
        Pitch("Pitch"),
        Roll("Roll"),
        CenterAzimuth("Center Azimuth"),
        CenterAltitude("Center Altitude");

        override fun toString(): String =
                description
    }

    data class Entry(val time: Double, val orientation: Orientation) {

        operator fun get(property: Property): Double =
                when (property) {
                    Property.Azimuth -> Degree.normalizeTo180(orientation.azimuth)
                    Property.Pitch -> Degree.normalizeTo180(orientation.pitch)
                    Property.Roll -> Degree.normalizeTo180(orientation.roll)
                    Property.CenterAzimuth -> Degree.normalizeTo180(orientation.centerAzimuth)
                    Property.CenterAltitude -> Degree.normalizeTo180(orientation.centerAltitude)
                }
    }

    private val map: HashMap<Method, MyCollection<Entry>> = HashMap()

    operator fun get(method: Method): MyCollection<Entry> =
            map[method] ?: MyCollection<Entry>().also {
                map[method] = it
            }

    fun add(time: Double, method: Method, orientation: Orientation) {
        this[method].add(Entry(time, orientation))
    }

    fun clear() {
        map.clear()
    }

    fun cleanupHistory(time: Double) {
        // Warning: removeIf { x -> x.time < time } may fail with ConcurrentModificationException as the sensor adds new records while the loop is running

        map.values.forEach {
            array -> array.removeIf { x -> x.time < time }
        }
    }
}

