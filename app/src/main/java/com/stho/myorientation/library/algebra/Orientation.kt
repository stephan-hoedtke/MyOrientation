package com.stho.myorientation.library.algebra

/**
 * Orientation: angles in degree (azimuth, pitch, roll)
 *
 * Project the positive y-axis [from the bottom edge to the top edge of the phone] to the sphere
 *      - azimuth: the angle to the geographic north at the horizon plane
 *      - pitch: the angle downwards when the top edge is tilted down
 *      - roll: the angle downwards when the left edge is tilted down
 *
 * Project the negative z-axis [how your eyes look into the phone] to the spheres
 *      - center azimuth: the angle to the geographic north at the horizon plane
 *      - center altitude: the angle upwards or downwards from the horizon
 *
 *
 * see also: EulerAngles for exact definition of azimuth, pitch and roll
 */
data class Orientation(val azimuth: Double, val pitch: Double, val roll: Double, val centerAzimuth: Double, val centerAltitude: Double, val rotation: IRotation) {

    /**
     * Altitude (top edge of the device pointing upwards) is the opposite of pitch (top edge of the device pointing downwards)
     */
    val altitude: Double by lazy { -pitch }

    fun toEulerAngles(): EulerAngles =
        EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)

    companion object {
        val default: Orientation =
            Orientation(
                azimuth = 0.0,
                pitch = 0.0,
                roll = 0.0,
                centerAzimuth = 0.0,
                centerAltitude = -90.0,
                rotation = Quaternion.default
            )
    }
}
