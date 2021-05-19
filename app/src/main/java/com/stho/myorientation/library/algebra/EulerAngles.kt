package com.stho.myorientation.library.algebra

/**
 * Euler angles in degree
 */
data class EulerAngles(val x: Double, val y: Double, val z: Double) {

    val azimuth: Double
        get()= Math.toDegrees(z)

    val pitch: Double
        get() = Math.toDegrees(x)

    val roll: Double
        get() = Math.toDegrees(y)

    companion object {
        fun fromAzimuthPitchRoll(azimuth: Double, pitch: Double, roll: Double): EulerAngles =
            EulerAngles(
                x = Math.toRadians(pitch),
                y = Math.toRadians(roll),
                z = Math.toRadians(azimuth),
            )
    }
}

