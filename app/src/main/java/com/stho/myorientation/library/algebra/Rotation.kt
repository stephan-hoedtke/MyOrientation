package com.stho.myorientation.library.algebra

import kotlin.math.*


object Rotation {

    /**
     * Returns the rotation matrix for given Euler angles, rotating a vector in sensor frame to earth frame
     *      v_earth = M(x,y,z) * v_sensor
     */
    fun eulerAnglesToRotationMatrix(omega: EulerAngles): Matrix {
        val cosX = cos(omega.x)
        val sinX = sin(omega.x)
        val cosY = cos(omega.y)
        val sinY = sin(omega.y)
        val cosZ = cos(omega.z)
        val sinZ = sin(omega.z)

        // calculate as m.transpose() of m = My * Mx * Mz with
        //
        //       |  cos(Y)   0   sin(Y) |
        //  My = |  0        1   0      |
        //       | -sin(Y)   0   cos(Y) |
        //
        //       |  1   0        0      |
        //  Mx = |  0   cos(X)  -sin(X) |
        //       |  0   sin(X)   cos(X) |
        //
        //       |  cos(Z)  -sin(Z)   0 |
        //  Mz = |  sin(Z)   cos(Z)   0 |
        //       |  0        0        1 |

        return Matrix(
            m11 = sinX * sinY * sinZ + cosY * cosZ,
            m12 = cosX * sinZ,
            m13 = sinX * cosY * sinZ - sinY * cosZ,
            m21 = sinX * sinY * cosZ - cosY * sinZ,
            m22 = cosX * cosZ,
            m23 = sinX * cosY * cosZ + sinY * sinZ,
            m31 = cosX * sinY,
            m32 = -sinX,
            m33 = cosX * cosY,
        )
    }

    /**
     * Returns the rotation matrix for given Euler angles rotating a vector in sensor frame to earth frame
     *      v_earth = q # v_sensor # q*
     */
    fun eulerAnglesToQuaternion(omega: EulerAngles): Quaternion {
        val cosX = cos(omega.x / 2)
        val sinX = sin(omega.x / 2)
        val cosY = cos(omega.y / 2)
        val sinY = sin(omega.y / 2)
        val cosZ = cos(omega.z / 2)
        val sinZ = sin(omega.z / 2)

        // calculate as q.inverse() of q = qy * qx * qz with
        //  qy = (cos(Y/2) + sin(Y/2) * j)
        //  qx = (cos(X/2) + sin(X/2) * i)
        //  qz = (cos(Z/2) + sin(Z/2) * k)

        return Quaternion(
            s = cosY * cosX * cosZ + sinY * sinX * sinZ,
            x = cosY * sinX * cosZ + sinY * cosX * sinZ,
            y = sinY * cosX * cosZ - cosY * sinX * sinZ,
            z = cosY * cosX * sinZ - sinY * sinX * cosZ,
        ).conjugate()
    }

    /**
     * Returns the orientation (azimuth, pitch, roll) of the device
     *
     *      (A) for the normal case:
     *                  cos(x) <> 0:
     *
     *         -->      m12 = cosX * sinZ,
     *                  m22 = cosX * cosZ,
     *                  m31 = cosX * sinY,
     *                  m32 = -sinX,
     *                  m33 = cosX * cosY
     *
     *          -->     X = asin(-m32)
     *                  Y = atan(m31 / m33)
     *                  Z = atan(m12 / m22)
     *
     *      (B) for the gimbal lock when:
     *                  cos(x) = 0
     *                  sin(x) = +/-1 (X=+/-90°)
     *
     *              m11 = sinX * sinY * sinZ + cosY * cosZ,
     *              m13 = sinX * cosY * sinZ - sinY * cosZ,
     *              m21 = sinX * sinY * cosZ - cosY * sinZ,
     *              m23 = sinX * cosY * cosZ + sinY * sinZ,
     *
     *
     *          --> only (Y - Z) is defined, as cosY * cosZ + sinY * sinZ = cos(Y - Z) etc.
     *          --> assume z = 0
     *                  cos(z) = 1
     *                  sin(z) = 0
     *
     *          (B.1) when sin(X) = -1 (X=-90°):
     *                  m21 = - sinY * cosZ - cosY * sinZ = - sin(Y + Z)
     *                  m23 = - cosY * cosZ + sinY * sinZ = - cos(Y + Z)
     *
     *          -->     Y = atan2(-m21, -m23) and Y = Y' + Z' for any other Y' and Z'
     *
     *          (B.2) when sin(X) = -1 (X=-90°)
     *                  m21 = sinY * cosZ - cosY * sinZ = sin(Y - Z) = sinY
     *                  m23 = cosY * cosZ + sinY * sinZ = cos(Y - Z) = cosY
     *
     *          -->     Y = atan2(m21, m23) and Y = Y' - Z' for any other Y' and Z'
     *
     * see also: SensorManager.getOrientation()
     */
    internal fun getEulerAnglesFor(r: IRotation): EulerAngles =
        //
        // SensorManager.getOrientation()
        //            values[0] = (float) Math.atan2(R[1], R[4]);   // azimuth: atan2(m12, m22)
        //            values[1] = (float) Math.asin(-R[7]);         // pitch: asin(-m32)
        //            values[2] = (float) Math.atan2(-R[6], R[8]);  // roll: atan2(-m31, m33)
        //
        if (isGimbalLockForSinus(r.m32)) {
            if (r.m32 < 0) { // pitch 90°
                EulerAngles(
                    x = PI_OVER_TWO,
                    y = atan2(r.m21, r.m23),
                    z = 0.0,
                )
            } else { // pitch -90°
                EulerAngles(
                    x = -PI_OVER_TWO,
                    y = atan2(-r.m21, -r.m23),
                    z = 0.0,
                )
            }
        } else {
            EulerAngles(
                x = asin(-r.m32),
                y = atan2(r.m31, r.m33),
                z = atan2(r.m12, r.m22),
            )
        }

    /**
     * Returns the orientation (azimuth, pitch, roll, center azimuth, center altitude) of the device
     *      for a rotation from sensor frame into earth frame
     *
     *      (C) The center "pointer" vector is defined by
     *              C = M * (0, 0, -1)
     *
     *          --> C = (-m13, -m23, -m33)
     *
     *              center azimuth  = atan2(-m13, -m23)
     *              center azimuth = asin(-m33) // opposite of pitch
     */
    internal fun getOrientationFor(r: IRotation): Orientation =
        if (isGimbalLockForSinus(r.m32)) {
            if (r.m32 < 0) { // pitch 90°
                val roll = Degree.arcTan2(r.m21, r.m23)
                Orientation(
                    azimuth = 0.0,
                    pitch = 90.0,
                    roll = roll,
                    centerAzimuth = 180 - roll,
                    centerAltitude = 0.0,
                )
            } else { // pitch -90°
                val roll = Degree.arcTan2(-r.m21, -r.m23)
                Orientation(
                    azimuth = 0.0,
                    pitch = -90.0,
                    roll = roll,
                    centerAzimuth = roll,
                    centerAltitude = 0.0,
                )
            }
        } else {
            if (isGimbalLockForCenter(r.m13, r.m23)) { // pitch 0°
                val azimuth = Degree.arcTan2(r.m12, r.m22)
                val roll = Degree.arcTan2(r.m31, r.m33)
                Orientation(
                    azimuth = azimuth,
                    pitch = Degree.arcSin(-r.m32),
                    roll = roll,
                    centerAzimuth = azimuth,
                    centerAltitude = roll - 90
                )
            }
            else {
                Orientation(
                    azimuth = Degree.arcTan2(r.m12, r.m22),
                    pitch = Degree.arcSin(-r.m32),
                    roll = Degree.arcTan2(r.m31, r.m33),
                    centerAzimuth = Degree.arcTan2(-r.m13, -r.m23),
                    centerAltitude = Degree.arcSin(-r.m33)
                )
            }
        }

    private const val FREE_FALL_GRAVITY_SQUARED = 0.01 * 9.81 * 9.81
    private const val FREE_FALL_MAGNETOMETER_SQUARED = 0.01

    internal fun getRotationMatrixFromAccelerationMagnetometer(acceleration: Vector, magnetometer: Vector, defaultValue: Matrix): Matrix =
        getRotationMatrixFromAccelerationMagnetometer(acceleration, magnetometer) ?: defaultValue

        /**
     * Returns the rotation matrix M for a device defined by the gravity and the geomagnetic vector
     *      in case of free fall or close to magnetic pole it returns null
     *      as the rotation matrix cannot be calculated
     *
     * @param acceleration acceleration (a vector pointing upwards in default position)
     * @param magnetometer magnetic (a vector pointing down to the magnetic north)
     *
     * see also: SensorManager.getRotationMatrix()
     */
    internal fun getRotationMatrixFromAccelerationMagnetometer(acceleration: Vector, magnetometer: Vector): Matrix? {
        val normSquareA = acceleration.normSquare()
        if (normSquareA < FREE_FALL_GRAVITY_SQUARED) {
            // free fall detected, typical values of gravity should be 9.81
            return null
        }

        val hh = Vector.cross(magnetometer, acceleration)
        val normSquareH = hh.normSquare()
        if (normSquareH < FREE_FALL_MAGNETOMETER_SQUARED) {
            // free fall or in space or close to magnetic north pole, typical values of magnetometer should be more than 100
            return null
        }

        val h = hh / sqrt(normSquareH)
        val a = acceleration / sqrt(normSquareA)
        val m = Vector.cross(a, h)
        return Matrix(
            m11 = h.x, m12 = h.y, m13 = h.z,
            m21 = m.x, m22 = m.y, m23 = m.z,
            m31 = a.x, m32 = a.y, m33 = a.z,
        )
    }

    /**
     * Returns the rotation matrix as integration of angle velocity from gyroscope of a time period
     *
     * @param omega angle velocity around x, y, z, in radians/second
     * @param dt time period in seconds
     */
    internal fun getRotationFromGyro(omega: Vector, dt: Double): Quaternion {
        // Calculate the angular speed of the sample
        val omegaMagnitude: Double = omega.norm()

        // Normalize the rotation vector if it's big enough to get the axis
        // (that is, EPSILON should represent your maximum allowable margin of error)
        val w = if (omegaMagnitude > OMEGA_THRESHOLD)
            omega * (1 / omegaMagnitude)
        else
            omega

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
        Rotation.isGimbalLockForRadians(eulerAngles.x) && eulerAngles.x > 0

    internal fun isNegativeGimbalLock(eulerAngles: EulerAngles) =
        Rotation.isGimbalLockForRadians(eulerAngles.x) && eulerAngles.x < 0

    internal fun isGimbalLock(eulerAngles: EulerAngles) =
        Rotation.isGimbalLockForRadians(eulerAngles.x)

    /**
     * Returns if x is about +/- PI/2
     */
    private fun isGimbalLockForRadians(x: Double): Boolean =
        x < GIMBAL_LOCK_RADIANS_MINIMUM || x > GIMBAL_LOCK_RADIANS_MAXIMUM

    /**
     * Returns if sin(x) is about +/- 1.0
     */
    private fun isGimbalLockForSinus(sinX: Double): Boolean =
        sinX < GIMBAL_LOCK_SINUS_MINIMUM || sinX > GIMBAL_LOCK_SINUS_MAXIMUM

    /**
     * Returns if x^2 +y^2 is too small to calculate the atan2
     */
    private fun isGimbalLockForCenter(sinX: Double, cosX: Double): Boolean =
        abs(sinX) < GIMBAL_LOCK_SINUS_TOLERANCE && abs(cosX) < GIMBAL_LOCK_SINUS_TOLERANCE

    /**
     * When the pitch is about 90° (Gimbal lock) the rounding errors of x, y, z produce unstable azimuth and roll
     *      pitch = +/- 90°
     *      --> z = +/- 1.0
     *          x = +/- 0.0
     *          y = +/- 0.0
     *      --> atan2(...,...) can be anything.
     *
     * Tolerance estimation:
     *      x,y < 0.001 --> z > sqrt(1 - x * x - y * y) = sqrt(0.999998) = 0.999999 --> 89.92°
     *          pitch = +/- (90° +/- 0.08°) or
     *          pitch = +/- (PI/2 +/- 0.001414) or
     *          sin(x) = +/- (1.0 +/- 0.000001)
     *
     */
    private const val PI_OVER_TWO = PI / 2
    private const val GIMBAL_LOCK_RADIANS_MINIMUM: Double = 0.001414 - PI_OVER_TWO
    private const val GIMBAL_LOCK_RADIANS_MAXIMUM: Double = PI_OVER_TWO - 0.001414
    private const val GIMBAL_LOCK_SINUS_TOLERANCE: Double = 0.000001
    private const val GIMBAL_LOCK_SINUS_MINIMUM: Double = -0.999999
    private const val GIMBAL_LOCK_SINUS_MAXIMUM: Double = 0.999999
    private const val OMEGA_THRESHOLD: Double = 0.0000001
}