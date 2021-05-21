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
data class Orientation(val azimuth: Double, val pitch: Double, val roll: Double, val centerAzimuth: Double, val centerAltitude: Double) {

    /**
     * Altitude (top edge of the device pointing upwards) is the opposite of pitch (top edge of the device pointing downwards)
     */
    val altitude: Double by lazy { -pitch }

    fun toEulerAngles(): EulerAngles =
        EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)

    operator fun times(f: Double): Orientation =
        Orientation(
            azimuth = azimuth * f,
            pitch = pitch * f,
            roll = roll * f,
            centerAzimuth = centerAzimuth * f,
            centerAltitude = centerAltitude * f,
        )

    operator fun plus(orientation: Orientation) =
        Orientation(
            azimuth = Degree.normalizeTo180(azimuth + orientation.azimuth),
            pitch = Degree.normalizeTo180(pitch + orientation.pitch),
            roll = Degree.normalizeTo180(roll + orientation.roll),
            centerAzimuth = Degree.normalizeTo180(centerAzimuth + orientation.centerAzimuth),
            centerAltitude = Degree.normalizeTo180(centerAltitude + orientation.centerAltitude),
        )

    operator fun minus(orientation: Orientation) =
        Orientation(
            azimuth = Degree.normalizeTo180(azimuth - orientation.azimuth),
            pitch = Degree.normalizeTo180(pitch - orientation.pitch),
            roll = Degree.normalizeTo180(roll - orientation.roll),
            centerAzimuth = Degree.normalizeTo180(centerAzimuth - orientation.centerAzimuth),
            centerAltitude = Degree.normalizeTo180(centerAltitude - orientation.centerAltitude),
        )
}
