package com.stho.myorientation.library.algebra

/**
 * Euler angles in radians (x,y,z) and degree (azimuth, pitch, roll)
 *
 * Pitch (around X axis):
 *   When the device is placed face up on a table, the pitch value is 0.
 *   When the positive Z axis begins to tilt towards the positive Y axis, the pitch angle becomes positive.
 *   (This is when the top edge of the device is moving downwards)
 *   The value of Pitch ranges from -180 degrees to 180 degrees.
 *
 * Roll (around Y axis):
 *   When the device is placed face up on a table, the roll value is 0.
 *   When the positive X axis begins to tilt towards the positive Z axis, the roll angle becomes positive.
 *   (This is when the left edge of the device is moving downwards)
 *   The value of Roll ranges from -90 degrees to 90 degrees.
 *
 * Azimuth (around Z axis):
 *   The following table shows the value of Azimuth when the positive Y axis of the device is aligned to the magnetic north, south, east, and west
 *      North -> 0
 *      East -> 90
 *      South -> 180
 *      West -> 270
 *
 */
data class EulerAngles(val x: Double, val y: Double, val z: Double) {

    val azimuth: Double by lazy { Math.toDegrees(z) }

    val pitch: Double by lazy { Math.toDegrees(x) }

    val roll: Double by lazy { Math.toDegrees(y) }

    fun toQuaternion(): Quaternion =
        Rotation.eulerAnglesToQuaternion(this)

    fun toRotationMatrix(): RotationMatrix =
        Rotation.eulerAnglesToRotationMatrix(this)

    companion object {
        fun fromAzimuthPitchRoll(azimuth: Double, pitch: Double, roll: Double): EulerAngles =
            EulerAngles(
                x = Math.toRadians(pitch),
                y = Math.toRadians(roll),
                z = Math.toRadians(azimuth),
            )
    }
}

