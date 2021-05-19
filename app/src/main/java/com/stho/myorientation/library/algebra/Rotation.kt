package com.stho.myorientation.library.algebra

import com.stho.myorientation.library.Degree
import kotlin.math.*

object Rotation {

    /**
     * Returns the euler angles in radians as a vector for a rotation matrix m
     */
    fun rotationMatrixToEulerAngles(m: Matrix): EulerAngles {
        return Rotation.getEulerAnglesFor(
            m12 = m.m12,
            m13 = m.m13,
            m21 = m.m21,
            m22 = m.m22,
            m23 = m.m23,
            m33 = m.m33)
    }

   /**
     * Return the rotation matrix for Euler angles given as Vector(pitch, roll, azimuth) in radian
     *
     * The rotation matrix is rotating a vector in earth frame to a vector in device frame
     *      v_device = M(x,y,z) * v_earth
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
     * References:
     *   https://www.cdiweb.com/datasheets/invensense/sensor-introduction.pdf
     *   SensorManager.getOrientation(float[] R, ...)
     *
     * Note:
     *   M(x, y, z) = M(y) * M(x) * M(z)
     *   M(y, y, z) * vector = M(y) * (M(x) * (M(z) * vector))
     *       M-inverse = M-transpose
     *       --> M(x, y, z)  = M-transpose(-x, -y, -z)
     *
     */
    fun eulerAnglesToRotationMatrix(omega: EulerAngles): Matrix {
        val cosX = cos(omega.x)
        val sinX = sin(omega.x)
        val cosY = cos(omega.y)
        val sinY = sin(omega.y)
        val cosZ = cos(omega.z)
        val sinZ = sin(omega.z)
        //
        // rotation: first by z (azimuth), then by x (pitch), then by y (roll), left hand (thumb into vector direction, bending fingers show rotation)
        //
        // zero-position: phone is flat on the table surface upwards, looked at from above
        //   x -> to the right
        //   y -> forward
        //   z -> upwards
        //
        // 1) azimuth:
        //          in earth frame: turn y to x around minus-z-axis -> move positive y to the right
        //          for a vector in device frame: turn x to y around positive-z-axis
        //
        //      |  cos(z)  -sin(z)   0 |   --->  x is decreased by y * sin(z)
        // Mz = |  sin(z)   cos(z)   0 |   --->  y is increased by x * sin(z)
        //      |  0        0        1 |
        //
        // 2) pitch:
        //          in earth frame: turn z to y around x-axis -> move positive y downwards
        //          for a vector in device frame: turn y to z around x-axis
        //
        //      |  1   0        0      |
        // Mx = |  0   cos(x)  -sin(x) |  ---> y is decreased by z * sin(x)
        //      |  0   sin(x)   cos(x) |  ---> z is increased by y * sin(x)
        //
        // 3) roll:
        //          in earth frame: turn x to z around y-axis -> move left downwards and positive x upwards
        //          for a vector in device frame: turn z to x around y-axis
        //
        //      |  cos(y)  0  sin(y) |  ---> x is increased by z * sin(y)
        // My = |  0       1  0      |
        //      | -sin(y)  0  cos(y) |  ---> z is decreased by x * sin(y)
        //
        //                  |  sin(x)*sin(y)*sin(z)+cos(y)*cos(z)   sin(x)*sin(y)*cos(z)-cos(y)*sin(z)   cos(x)*sin(y)  |
        // My x (Mx * Mz) = |  cos(x)*sin(z)                        cos(x)*cos(z)                       -sin(x)         |
        //                  |  sin(x)*cos(y)*sin(z)-sin(y)*cos(z)   sin(x)*cos(y)*cos(z)+sin(y)*sin(z)   cos(x)*cos(y)  |
        //
        return Matrix(
            m11 = sinX * sinY * sinZ + cosY * cosZ,
            m12 = sinX * sinY * cosZ - cosY * sinZ,
            m13 = cosX * sinY,
            m21 = cosX * sinZ,
            m22 = cosX * cosZ,
            m23 = -sinX,
            m31 = sinX * cosY * sinZ - sinY * cosZ,
            m32 = sinX * cosY * cosZ + sinY * sinZ,
            m33 = cosX * cosY,
        )
    }

    fun eulerAnglesToQuaternion(azimuth: Double, pitch: Double, roll: Double): Quaternion =
        eulerAnglesToQuaternion(EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll))

    fun eulerAnglesToQuaternion(eulerAngles: EulerAngles): Quaternion {
        val cx = cos(eulerAngles.x / 2)
        val sx = sin(eulerAngles.x / 2)
        val cy = cos(eulerAngles.y / 2)
        val sy = sin(eulerAngles.y / 2)
        val cz = cos(eulerAngles.z / 2)
        val sz = sin(eulerAngles.z / 2)
        return Quaternion(
            s = cy * cx * cz + sy * sx * sz,
            x = cy * sx * cz + sy * cx * sz,
            y = sy * cx * cz - cy * sx * sz,
            z = cy * cx * sz - sy * sx * cz,
        )
    }

    fun rotationMatrixToQuaternion(m: Matrix): Quaternion {
        // see: https://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
        // mind, as both q and -q define the same rotation you may get q or -q, respectively

        val trace: Double = m.m11 + m.m22 + m.m33

        when {
            trace > 0 -> {
                val fourS = 2.0 * sqrt(1.0 + trace) // 4s = 4 * q.s
                return Quaternion(
                    x = (m.m32 - m.m23) / fourS,
                    y = (m.m13 - m.m31) / fourS,
                    z = (m.m21 - m.m12) / fourS,
                    s = 0.25 * fourS
                )
            }
            m.m11 > m.m22 && m.m11 > m.m33 -> {
                val fourX = 2.0 * sqrt(1.0 + m.m11 - m.m22 - m.m33) // 4x = 4 * q.x
                return Quaternion(
                    x = 0.25 * fourX,
                    y = (m.m12 + m.m21) / fourX,
                    z = (m.m13 + m.m31) / fourX,
                    s = (m.m32 - m.m23) / fourX,
                )
            }
            m.m22 > m.m33 -> {
                val fourY = 2.0 * sqrt(1.0 + m.m22 - m.m11 - m.m33) // 4y = 4*q.y
                return Quaternion(
                    x = (m.m12 + m.m21) / fourY,
                    y = 0.25 * fourY,
                    z = (m.m23 + m.m32) / fourY,
                    s = (m.m13 - m.m31) / fourY
                )
            }
            else -> {
                val fourZ = 2.0 * sqrt(1.0 + m.m33 - m.m11 - m.m22) // 4z = 4 * q.z
                return Quaternion(
                    x = (m.m13 + m.m31) / fourZ,
                    y = (m.m23 + m.m32) / fourZ,
                    z = 0.25 * fourZ,
                    s = (m.m21 - m.m12) / fourZ
                )
            }
        }
    }


    fun quaternionToEulerAngles(q: Quaternion): EulerAngles {
        val x2 = 2 * q.x * q.x
        val y2 = 2 * q.y * q.y
        val z2 = 2 * q.z * q.z
        val xy = 2 * q.x * q.y
        val xz = 2 * q.x * q.z
        val yz = 2 * q.y * q.z
        val sz = 2 * q.s * q.z
        val sy = 2 * q.s * q.y
        val sx = 2 * q.s * q.x

        return Rotation.getEulerAnglesFor(
            m12 = xy - sz,
            m13 = sy + xz,
            m21 = xy + sz,
            m22 = 1 - x2 - z2,
            m23 = yz - sx,
            m33 = 1 - x2 - y2,
        )
    }

    /**
     * Note, the sensor manager uses a different calculation:
     * SensorManager.getOrientation() --> azimuth, pitch, roll
     *
     *      (A) For the normal case
     *          M23 = -sinX,
     *          M13 = cosX * sinY,
     *          M21 = cosX * sinZ,
     *          M22 = cosX * cosZ,
     *          M33 = cosX * cosY
     *          -->
     *              X = -asin(M23)
     *              Y = atan(M13 / M33)
     *              Z = atan(M21 / M22)
     *
     *      (B) For the gimbal lock when
     *                  cos(x) = 0
     *                  sin(x) = +/-1
     *
     *          --> only Y - Z is defined:
     *
     *          for sin(X) = 1 (X=90°)
     *          M11 = cosY * cosZ + sinY * sinZ = cos(Y - Z)
     *          M12 = sinY * cosZ - cosY * sinZ = sin(Y - Z)
     *          M31 = cosY * sinZ - sinY * cosZ = sin(Z - Y) = -sin(Y - Z)
     *          M32 = cosY * cosZ + sinY * sinZ = cos(Y - Z)
     *
     *          for sin(X) = -1 (X=-90°)
     *          M12 = - sinY * cosZ - cosY * sinZ = -sin(Y + Z)
     *
     *          assume z = 0
     *                  cos(z) = 1
     *                  sin(z) = 0
     *
     *          for sin(X) = 1 (X=90°)
     *          --> Y = asin(M12)
     *
     *          for sin(X) = -1 (X=-90°)
     *          --> Y = -sin(M12)
     *
     */
    private fun getEulerAnglesFor(m12: Double, m13: Double, m21: Double, m22: Double, m23: Double, m33: Double): EulerAngles =
        if (isGimbalLockForSinX(m23)) {
            if (m23 < 0) {
                EulerAngles(
                    x = PI_OVER_TWO,
                    y = asin(m12),
                    z = 0.0,
                )
            } else {
                EulerAngles(
                    x = -PI_OVER_TWO,
                    y = asin(-m12),
                    z = 0.0,
                )
            }
        } else {
            EulerAngles(
                x = asin(-m23),
                y = atan2(m13, m33),
                z = atan2(m21, m22),
            )
        }

    /**
     * Returns the orientation angles (EulerAngles plus center azimuth and altitude) for a rotation matrix
     *      where the rotation matrix a vector in earth frame into device frame:
     *
     *          v_device = M * v_earth
     */
    internal fun rotationMatrixToOrientation(r: Matrix): Orientation =
        Rotation.getOrientationFor(r.m12, r.m13, r.m21, r.m22, r.m23, r.m31, r.m32, r.m33)

    /**
     * Returns the orientation angles (EulerAngles plus center azimuth and altitude) for a quaternion
     *      where the quaternion rotates a vector in earth frame into device frame:
     *
     *          v_device = q # v_earth # q*
     */
    internal fun quaternionToOrientation(q: Quaternion): Orientation {
        val x2 = 2 * q.x * q.x
        val y2 = 2 * q.y * q.y
        val z2 = 2 * q.z * q.z
        val xy = 2 * q.x * q.y
        val xz = 2 * q.x * q.z
        val yz = 2 * q.y * q.z
        val sz = 2 * q.s * q.z
        val sy = 2 * q.s * q.y
        val sx = 2 * q.s * q.x

        return Rotation.getOrientationFor(
            m12 = xy - sz,
            m13 = sy + xz,
            m21 = xy + sz,
            m22 = 1 - x2 - z2,
            m23 = yz - sx,
            m31 = xz - sy,
            m32 = yz + sx,
            m33 = 1 - x2 - y2,
        )
    }

    /**
     * see SensorManager -->
     *      values[0] = (float) Math.atan2(R[1], R[4]);
     *      values[1] = (float) Math.asin(-R[7]);
     *      values[2] = (float) Math.atan2(-R[6], R[8]);
     *
     *      Mind, here the rotation matrix converts from earth to device frame,
     *      while in the sensor manager the matrix converts from device to earth frame:
     *
     *          getOrientationAnglesFor(m) = SensorManager.getOrientation(m.transpose())
     *
     *
     *        val angles: DoubleArray = doubleArrayOf(
     *               Degree.arcTan2(R[1].toDouble(), R[4].toDouble()),   -- azimuth
     *               Degree.arcSin(R[7].toDouble()),                     -- altitude
     *               Degree.arcTan2(-R[6].toDouble(), R[8].toDouble()),  -- roll
     *               Degree.arcTan2(-R[2].toDouble(), -R[5].toDouble()), -- centerAzimuth
     *               Degree.arcSin(-R[8].toDouble()))                    -- centerAltitude
     */
    private fun getOrientationFor(m12: Double, m13: Double, m21: Double, m22: Double, m23: Double, m31: Double, m32: Double, m33: Double): Orientation =
        if (isGimbalLockForSinX(m23)) {
            if (m23 < 0) {
                Orientation(
                    azimuth = 0.0,
                    pitch = 90.0,
                    roll = Degree.arcSin(m12),
                    centerAzimuth = Degree.arcSin(m12),
                    centerAltitude = 0.0,
                )
            } else {
                Orientation(
                    azimuth = 0.0,
                    pitch = -90.0,
                    roll = Degree.arcSin(-m12),
                    centerAzimuth = Degree.arcSin(-m12),
                    centerAltitude = 0.0,
                )
            }
        } else {
            Orientation(
                azimuth = Degree.arcTan2(m21, m22),
                pitch = Degree.arcSin(-m23),
                roll = Degree.arcTan2(m13, m33),
                centerAzimuth = if (abs(m31) < EPSILON && abs(m32) < EPSILON) 0.0 else Degree.arcTan2(-m31, -m32),
                centerAltitude = Degree.arcSin(-m33)
            )
        }


    fun quaternionToRotationMatrix(q: Quaternion): Matrix {
        val x2 = 2 * q.x * q.x
        val y2 = 2 * q.y * q.y
        val z2 = 2 * q.z * q.z
        val xy = 2 * q.x * q.y
        val xz = 2 * q.x * q.z
        val yz = 2 * q.y * q.z
        val sz = 2 * q.s * q.z
        val sy = 2 * q.s * q.y
        val sx = 2 * q.s * q.x
        return Matrix(
            m11 = 1 - y2 - z2,
            m12 = xy - sz,
            m13 = xz + sy,
            m21 = xy + sz,
            m22 = 1 - x2 - z2,
            m23 = yz - sx,
            m31 = xz - sy,
            m32 = yz + sx,
            m33 = 1 - x2 - y2,
        )
    }

    /**
     * Returns the rotation matrix M for a device defined by the gravity and the geomagnetic vector
     *
     *      v_earth = M * v_device
     *
     *      see also: SensorManager.getRotationMatrix()
     */
    internal fun getRotationMatrix(acceleration: Vector, magnetometer: Vector): Matrix {
        val a = acceleration.normalize()
        val h = Vector.cross(magnetometer, acceleration).normalize()
        val m = Vector.cross(a, h).normalize()
        return Matrix(
            m11 = h.x, m12 = h.y, m13 = h.z,
            m21 = m.x, m22 = m.y, m23 = m.z,
            m31 = a.x, m32 = a.y, m33 = a.z,
        )
    }

    internal fun getRotationFromGyro(omega: Vector, dt: Double): Quaternion {
        // Calculate the angular speed of the sample
        val omegaMagnitude: Double = omega.norm()

        // Normalize the rotation vector if it's big enough to get the axis
        // (that is, EPSILON should represent your maximum allowable margin of error)
        val w = if (omegaMagnitude > EPSILON)
            omega * (1 / omegaMagnitude)
        else
            omega

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        //
        // Quaternion integration:
        // ds/dt = omega x s
        // with s = q # s0 # q* follows
        //      dq/dt = 0.5 * omega # q
        //      q(t) = exp(0.5 * omega * (t - t0)) # q0
        //      q(t) = cos(|v|) + v / |v| * sin(|v|) # q0 with v = 0.5 * omega * (t - t0)
        //      this is equivalent to a rotation by theta around the rotation vector omega/|omega| with theta = |omega| * (t - t0)
        val theta: Double = omegaMagnitude * dt
        return Quaternion.forRotation(w, theta)
    }

    //    if (lookAtThePhoneFromAbove(angles[2])) {
    //        lowPassFilter.setAngles(angles)
    //    } else {
    //        lowPassFilter.setAngles(adjustForLookingAtThePhoneFromBelow(angles))
    //    }
    //
    //    private fun lookAtThePhoneFromAbove(roll: Double) =
    //        -90 < roll && roll < 90
    //
    //    private fun adjustForLookingAtThePhoneFromBelow(angles: DoubleArray): DoubleArray =
    //        doubleArrayOf(
    //            -angles[0],           azimuth
    //            180 - angles[1],      altitude
    //            180 - angles[2],      roll
    //            angles[3],            centerAzimuth
    //            angles[4])            centerAltitude
    //
    internal fun requireAdjustmentForLookingAtThePhoneFromBelow(orientation: Orientation) =
        orientation.roll <= -90 || 90 <= orientation.roll

    internal fun adjustForLookingAtThePhoneFromBelow(orientation: Orientation): Orientation =
        Orientation(
            azimuth = Degree.normalize(180 + orientation.azimuth),
            pitch = Degree.normalizeTo180(180 - orientation.pitch),
            roll = Degree.normalizeTo180(180 - orientation.roll),
            centerAzimuth = orientation.centerAzimuth,
            centerAltitude = orientation.centerAltitude,
        )


    internal fun isPositiveGimbalLock(eulerAngles: EulerAngles) =
        Rotation.isGimbalLockForX(eulerAngles.x) && eulerAngles.x > 0

    internal fun isNegativeGimbalLock(eulerAngles: EulerAngles) =
        Rotation.isGimbalLockForX(eulerAngles.x) && eulerAngles.x < 0

    internal fun isGimbalLock(eulerAngles: EulerAngles) =
        Rotation.isGimbalLockForX(eulerAngles.x)

    /**
     * Returns if x is about +/- PI/2
     */
    private fun isGimbalLockForX(x: Double): Boolean =
        abs(x - PI_OVER_TWO) < EPSILON || abs(x + Rotation.PI_OVER_TWO) < Rotation.EPSILON

    /**
     * Returns if sin(x) is about +/- 1
     */
    private fun isGimbalLockForSinX(sinX: Double): Boolean =
        abs(sinX - 1.0) < EPSILON || abs(sinX + 1.0) < EPSILON

    private const val EPSILON: Double = 0.000000001
    private const val PI_OVER_TWO = PI / 2.0
}