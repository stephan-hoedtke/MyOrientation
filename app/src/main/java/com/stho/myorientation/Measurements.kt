package com.stho.myorientation

import kotlin.math.abs

@Suppress("ReplaceWithEnumMap")
class Measurements {

    enum class Type {
        Magnetometer,
        Accelerometer,
        Gyroscope,
        RotationVector,
    }

    enum class Component {
        X,
        Y,
        Z,
    }

    data class Measurement(val time: Double, val x: Double, val y: Double, val z: Double) {

        operator fun get(component: Component): Double =
                when (component) {
                    Component.X -> x
                    Component.Y -> y
                    Component.Z -> z
                }
    }

    private val map: HashMap<Type, MyCollection<Measurement>> = HashMap()
    private val maxValueMap: HashMap<Type, Double> = HashMap()

    operator fun get(type: Type): MyCollection<Measurement> =
            map[type] ?: MyCollection<Measurement>().also {
                map[type] = it
            }

    fun add(time: Double, type: Type, x: Double, y: Double, z: Double) {
        updateMaxValue(type, abs(x), abs(y), abs(z))
        this[type].add(Measurement(time, x, y, z))
    }

    private fun updateMaxValue(type: Type, x: Double, y: Double, z: Double) {
        var m: Double = maxValueMap[type] ?: 0.0
        if (m < x) m = x
        if (m < y) m = y
        if (m < z) m = z
        maxValueMap[type] = m
    }

    internal fun scaleFactor(type: Measurements.Type): Double {
        val m = maxValueMap[type] ?: 0.0
        return if (m < 0.00001) 1.0 else 0.9 / m
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
    companion object {
        internal fun scaleFactor(type: Measurements.Type): Double =
            when (type) {
                Type.Accelerometer -> 0.1 // values [0 .. 10]; g = 9.81
                Type.Magnetometer -> 0.2  // values [0 .. 40]
                Type.Gyroscope -> 0.2     // values ??
                else -> 1.0
            }
    }
}

