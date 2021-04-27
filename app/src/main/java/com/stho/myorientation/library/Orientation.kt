package com.stho.myorientation.library

/**
 * Orientation: angles in Degree
 *
 * Project the positive y-axis - from the botton edge to the top edge of the phone - to the sphere
 *      - Azimuth: the angle to the geographic north at the horizon plane
 *      - Pitch: the angle downwards when the top edge is tilted down
 *      - Roll: the angle downwards when the left edge is tilted down
 *  Project the negative z-axis - how your eyes look into the phone - to the sphere
 *     - Center Azimuth: the angle to the geographic north at the horizon plane
 *      - Center Altitude: the angle upwards or downwards from the horizon
 */
data class Orientation(val azimuth: Double, val pitch: Double, val roll: Double, val centerAzimuth: Double, val centerAltitude: Double) {
}


