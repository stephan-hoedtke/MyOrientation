package com.stho.myorientation

import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.Orientation


@Suppress("ReplaceWithEnumMap")
class Entries {

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

