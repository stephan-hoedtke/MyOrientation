package com.stho.myorientation

import com.stho.myorientation.library.algebra.*
import org.junit.Assert.*
import kotlin.math.sign


open class BaseUnitTestsHelper {

    /**
     * Returns a quaternion which rotates from sensor frame to earth frame
     */
    internal fun quaternionForEulerAngles(eulerAngles: EulerAngles): Quaternion =
        quaternionForRotation(eulerAngles.azimuth, eulerAngles.pitch, eulerAngles.roll)

    /**
     * Returns a quaternion which rotates from sensor frame to earth frame
     */
    private fun quaternionForRotation(azimuth: Double, pitch: Double, roll: Double): Quaternion {

        val mz = quaternionForAzimuth(azimuth)
        val mx = quaternionForPitch(pitch)
        val my = quaternionForRoll(roll)

        return (my * mx * mz).inverse()
    }

    /**
     * Returns a rotation matrix which rotates from sensor frame to earth frame
     */
    internal fun rotationMatrixForEulerAngles(eulerAngles: EulerAngles): RotationMatrix =
        rotationMatrixForRotation(eulerAngles.azimuth, eulerAngles.pitch, eulerAngles.roll)

    /**
     * Returns a rotation matrix which rotates from sensor frame to earth frame
     */
    private fun rotationMatrixForRotation(azimuth: Double, pitch: Double, roll: Double): RotationMatrix {

        val mz = matrixForAzimuth(azimuth)
        val mx = matrixForPitch(pitch)
        val my = matrixForRoll(roll)

        return (my * mx * mz).transpose()
    }

    /**
     * rotation matrix which rotates from earth frame to sensor frame
     */
    private fun matrixForAzimuth(azimuth: Double): RotationMatrix {
        val cosZ = Degree.cos(azimuth)
        val sinZ = Degree.sin(azimuth)
        return RotationMatrix(
            m11 = cosZ, m12 = -sinZ, m13 = 0.0,
            m21 = sinZ, m22 = cosZ,  m23 = 0.0,
            m31 = 0.0,  m32 = 0.0,   m33 = 1.0,
        )
    }

    /**
     * rotation matrix which rotates from earth frame to sensor frame
     */
    private fun matrixForPitch(pitch: Double): RotationMatrix {
        val cosX = Degree.cos(pitch)
        val sinX = Degree.sin(pitch)
        return RotationMatrix(
            m11 = 1.0, m12 = 0.0,  m13 = 0.0,
            m21 = 0.0, m22 = cosX, m23 = -sinX,
            m31 = 0.0, m32 = sinX, m33 = cosX,
        )
    }

    /**
     * rotation matrix which rotates from earth frame to sensor frame
     */
    private fun matrixForRoll(roll: Double): RotationMatrix {
        val cosY = Degree.cos(roll)
        val sinY = Degree.sin(roll)
        return RotationMatrix(
            m11 = cosY,  m12 = 0.0, m13 = sinY,
            m21 = 0.0,   m22 = 1.0, m23 = 0.0,
            m31 = -sinY, m32 = 0.0, m33 = cosY,
        )
    }

    /**
     * quaternion which rotates from earth frame to sensor frame
     */
    private fun quaternionForAzimuth(azimuth: Double): Quaternion =
        Quaternion(
            s = Degree.cos(azimuth / 2),
            x = 0.0,
            y = 0.0,
            z = Degree.sin(azimuth / 2),
        )

    /**
     * quaternion which rotates from earth frame to sensor frame
     */
    private fun quaternionForPitch(pitch: Double): Quaternion =
        Quaternion(
            s = Degree.cos(pitch / 2),
            x = Degree.sin(pitch / 2),
            y = 0.0,
            z = 0.0
        )

    /**
     * quaternion which rotates from earth frame to sensor frame
     */
    private fun quaternionForRoll(roll: Double): Quaternion =
        Quaternion(
            s = Degree.cos(roll / 2),
            x = 0.0,
            y = Degree.sin(roll / 2),
            z = 0.0
        )

    protected fun assertEquals(e: RotationMatrix, a: RotationMatrix, delta: Double = EPS_E8) {
        assertEquals("M11", e.m11, a.m11, delta)
        assertEquals("M12", e.m12, a.m12, delta)
        assertEquals("M13", e.m13, a.m13, delta)
        assertEquals("M21", e.m21, a.m21, delta)
        assertEquals("M22", e.m22, a.m22, delta)
        assertEquals("M23", e.m23, a.m23, delta)
        assertEquals("M31", e.m31, a.m31, delta)
        assertEquals("M32", e.m32, a.m32, delta)
        assertEquals("M33", e.m33, a.m33, delta)
    }

    protected fun assertEquals(e: Quaternion, a: Quaternion, delta: Double = EPS_E8) {
        if (sign(e.x) == sign(a.x)) {
            assertEquals("x", e.x, a.x, delta)
            assertEquals("y", e.y, a.y, delta)
            assertEquals("z", e.z, a.z, delta)
            assertEquals("s", e.s, a.s, delta)
        } else {
            assertEquals("x", -e.x, a.x, delta)
            assertEquals("y", -e.y, a.y, delta)
            assertEquals("z", -e.z, a.z, delta)
            assertEquals("s", -e.s, a.s, delta)
        }
    }

    protected fun assertEquals(e: Vector, a: Vector, delta: Double = EPS_E8) {
        assertEquals("x", e.x, a.x, delta)
        assertEquals("y", e.y, a.y, delta)
        assertEquals("z", e.z, a.z, delta)
    }

    protected fun assertEquals(e: EulerAngles, a: EulerAngles, delta: Double = EPS_E8) {
        when {
            Rotation.isPositiveGimbalLock(e) -> {
                assertEquals("azimuth", 0.0, a.azimuth, delta)
                assertEquals("pitch", e.pitch, a.pitch, delta)
                assertEquals("roll", e.roll - e.azimuth, a.roll, delta)

            }
            Rotation.isNegativeGimbalLock(e) -> {
                assertEquals("azimuth", 0.0, a.azimuth, delta)
                assertEquals("pitch", e.pitch, a.pitch, delta)
                assertEquals("roll", e.roll + e.azimuth, a.roll, delta)

            }
            else -> {
                assertEquals("azimuth", e.azimuth, a.azimuth, delta)
                assertEquals("pitch", e.pitch, a.pitch, delta)
                assertEquals("roll", e.roll, a.roll, delta)
            }
        }
    }

    protected fun assertIsEqualRotation(e: RotationMatrix, a: Quaternion, delta: Double = EPS_E8) {
        assertEquals("M11", e.m11, a.m11, delta)
        assertEquals("M12", e.m12, a.m12, delta)
        assertEquals("M13", e.m13, a.m13, delta)
        assertEquals("M21", e.m21, a.m21, delta)
        assertEquals("M22", e.m22, a.m22, delta)
        assertEquals("M23", e.m23, a.m23, delta)
        assertEquals("M31", e.m31, a.m31, delta)
        assertEquals("M32", e.m32, a.m32, delta)
        assertEquals("M33", e.m33, a.m33, delta)
    }

    companion object {
        internal const val EPS_E8 = 0.00000001
        internal const val EPS_E6 = 0.000001
        internal const val EPS_E4 = 0.0001
        internal const val EPS_E2 = 0.01
        internal const val EPS_E1 = 0.1
        internal const val EPS_E0 = 1.0
    }
}