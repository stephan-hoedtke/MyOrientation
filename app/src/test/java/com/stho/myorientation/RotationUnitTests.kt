package com.stho.myorientation

import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.*
import org.junit.Test

import org.junit.Assert.*
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 * See https://github.com/Josef4Sci/AHRS_Filter/blob/master/Filters/MadgwickAHRS3.m
 */
class RotationUnitTests : BaseUnitTestsHelper() {

    @Test
    fun adjustForLookingAtThePhoneFromBelow_isCorrect() {
        adjustForLookingAtThePhoneFromBelow_isCorrect(
            a = Orientation.forEulerAngles(10.0, 30.0, 0.0),10.0, 30.0, 0.0
        )

        adjustForLookingAtThePhoneFromBelow_isCorrect(
            a = Orientation.forEulerAngles(10.0, 30.0, 179.0),190.0, 150.0, -1.0
        )

        adjustForLookingAtThePhoneFromBelow_isCorrect(
            a = Orientation.forEulerAngles(10.0, 30.0, 181.0),190.0, 150.0, 1.0
        )

        adjustForLookingAtThePhoneFromBelow_isCorrect(
            a = Orientation.forEulerAngles(10.0, 30.0, 180.0),190.0, 150.0, 0.0
        )

        adjustForLookingAtThePhoneFromBelow_isCorrect(
            a = Orientation.forEulerAngles(10.0, 30.0, 180.0), 190.0, 150.0, 0.0
        )

        adjustForLookingAtThePhoneFromBelow_isCorrect(
            a = Orientation.forEulerAngles(10.0, 30.0, 160.0),190.0, 150.0, -20.0
        )
    }

    private fun adjustForLookingAtThePhoneFromBelow_isCorrect(a: Orientation, azimuth: Double, pitch: Double, roll: Double) {
        val e = Orientation.forEulerAngles(azimuth, pitch, roll)
        assertEquals("Azimuth", azimuth, e.azimuth, EPS_E8)
        assertEquals("Pitch", pitch, e.pitch, EPS_E8)
        assertEquals("Roll", roll, e.roll, EPS_E8)
        assertEquals(e, a)
    }

    @Test
    fun orientationFromGyro_isCorrect() {
        orientationFromGyro_isCorrect(omega = Vector(1.0, 2.0, 3.0), dt = 0.001)
        orientationFromGyro_isCorrect(omega = Vector(3.0, 4.0, 5.0), dt = 0.002)
    }

    private fun orientationFromGyro_isCorrect(omega: Vector, dt: Double) {
        val p = getRotationMethodA(omega, dt)
        val q = getRotationMethodB(omega, dt)
        val r = getRotationMethodC(omega, dt)
        assertEquals(p, q, EPS_E6)
        assertEquals(p, r, EPS_E6)
    }

    private fun getRotationMethodA(omega: Vector, dt: Double): Quaternion {
        val alpha = omega.x * dt
        val beta = omega.y * dt
        val gamma = omega.z * dt

        val qDotX = sin(alpha / 2)
        val qDotY = sin(beta / 2)
        val qDotZ = sin(gamma / 2)
        val qDotS = sqrt(1 - qDotX * qDotX - qDotY * qDotY - qDotZ * qDotZ)
        return Quaternion(s = qDotS, x = qDotX, y = qDotY, z = qDotZ)
    }

    private fun getRotationMethodB(omega: Vector, dt: Double): Quaternion {
        val alpha = omega.x * dt
        val beta = omega.y * dt
        val gamma = omega.z * dt
        return Quaternion(s = 1.0, x = alpha/2, y = beta/2, z = gamma/2).normalize()
    }


    private fun getRotationMethodC(omega: Vector, dt: Double): Quaternion {
        return Rotation.getRotationFromGyro(omega, dt)
    }

    @Test
    fun rotation_isCorrect() {
        rotation_isCorrect(0.0, 0.0, 0.0)

        rotation_isCorrect(11.0, 0.0, 0.0)
        rotation_isCorrect(0.0, 11.0, 0.0)
        rotation_isCorrect(0.0, 0.0, 11.0)

        rotation_isCorrect(-11.0, 0.0, 0.0)
        rotation_isCorrect(0.0, -11.0, 0.0)
        rotation_isCorrect(0.0, 0.0, -11.0)

        rotation_isCorrect(33.0, 11.0, 0.0)
        rotation_isCorrect(0.0, 33.0, 11.0)
        rotation_isCorrect(11.0, 0.0, 33.0)

        rotation_isCorrect(-33.0, 11.0, 0.0)
        rotation_isCorrect(0.0, -33.0, 11.0)
        rotation_isCorrect(11.0, 0.0, -33.0)

        rotation_isCorrect(0.0, -90.0, 0.0)
        rotation_isCorrect(11.0, -90.0, 0.0)
        rotation_isCorrect(0.0, -90.0, 11.0)

        rotation_isCorrect(33.0, -90.0, 11.0)
        rotation_isCorrect(-33.0, -90.0, 11.0)
        rotation_isCorrect( 33.0, -90.0, -11.0)

        rotation_isCorrect(11.0, -90.0, 33.0)
        rotation_isCorrect(-11.0, -90.0, 33.0)
        rotation_isCorrect( 11.0, -90.0, -33.0)

        rotation_isCorrect(0.0, 90.0, 0.0)
        rotation_isCorrect(11.0, 90.0, 0.0)
        rotation_isCorrect(0.0, 90.0, 11.0)

        rotation_isCorrect(33.0, 90.0, 11.0)
        rotation_isCorrect(-33.0, 90.0, 11.0)
        rotation_isCorrect( 33.0, 90.0, -11.0)

        rotation_isCorrect(33.0, 270.0, 11.0)
        rotation_isCorrect(-33.0, 270.0, 11.0)
        rotation_isCorrect( 33.0, 270.0, -11.0)
    }

    private fun rotation_isCorrect(azimuth: Double, pitch: Double, roll: Double) {
        val m = matrixFromEulerAngles_withValidation(azimuth, pitch, roll)
        val q = quaternionFromEulerAngles_withValidation(azimuth, pitch, roll)
        quaternionRotation_equals_matrixRotation(m, q)
        matrixToEulerAnglesIsCorrect(azimuth, pitch, roll, m)
        quaternionToEulerAnglesIsCorrect(azimuth, pitch, roll, q)
    }

    private fun matrixFromEulerAngles_withValidation(azimuth: Double, pitch: Double, roll: Double): RotationMatrix {
        val eulerAngles = EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)
        val m1 = eulerAngles.toRotationMatrix()
        val m2 = super.rotationMatrixForEulerAngles(eulerAngles)
        val m3 = eulerAngles.toQuaternion().toRotationMatrix()
        super.assertEquals(m1, m2, EPS_E8)
        super.assertEquals(m1, m3, EPS_E8)
        return m1
    }

    private fun quaternionFromEulerAngles_withValidation(azimuth: Double, pitch: Double, roll: Double): Quaternion {
        val eulerAngles = EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll)
        val q1 = eulerAngles.toQuaternion()
        val q2 = super.quaternionForEulerAngles(eulerAngles)
        val q3 = eulerAngles.toRotationMatrix().toQuaternion()
        super.assertEquals(q1, q2, EPS_E8)
        super.assertEquals(q1, q3, EPS_E8)
        return q1
    }

    private fun quaternionRotation_equals_matrixRotation(m: RotationMatrix, q: Quaternion) {

        for (earth in vectors) {
            val e = earth.rotateBy(m)
            val a = earth.rotateBy(q)
            super.assertEquals(e, a, EPS_E6)
        }
    }

    private fun matrixToEulerAnglesIsCorrect(azimuth: Double, pitch: Double, roll: Double, m: RotationMatrix) {
        val eulerAngles = m.toEulerAngles()
        eulerAngles_areCorrect(azimuth, pitch, roll, eulerAngles)
    }

    private fun quaternionToEulerAnglesIsCorrect(azimuth: Double, pitch: Double, roll: Double, q: Quaternion) {
        val eulerAngles = q.toEulerAngles()
        eulerAngles_areCorrect(azimuth, pitch, roll, eulerAngles)
    }

    private fun eulerAngles_areCorrect(azimuth: Double, pitch: Double, roll: Double, eulerAngles: EulerAngles) {
        if (Rotation.isGimbalLock(eulerAngles)) {

            if (Rotation.isPositiveGimbalLock(eulerAngles)) {
                // only Y - Z is defined: z = 0, y = roll - azimuth
                assertEqualAngleInDegree("azimuth", 0.0, eulerAngles.azimuth)
                assertEqualAngleInDegree("pitch", pitch, eulerAngles.pitch)
                assertEqualAngleInDegree("roll", roll - azimuth, eulerAngles.roll)
            } else {
                // only Y + Z is defined: z = 0, y = roll + azimuth
                assertEqualAngleInDegree("azimuth", 0.0, eulerAngles.azimuth)
                assertEqualAngleInDegree("pitch", pitch, eulerAngles.pitch)
                assertEqualAngleInDegree("roll", roll + azimuth, eulerAngles.roll)
            }
        } else {
            assertEqualAngleInDegree("azimuth", azimuth, eulerAngles.azimuth)
            assertEqualAngleInDegree("pitch", pitch, eulerAngles.pitch)
            assertEqualAngleInDegree("roll", roll, eulerAngles.roll)
        }
    }

    private fun assertEqualAngleInDegree(message: String, e: Double, a: Double) {
        assertEquals(message, Degree.normalizeTo180(e), Degree.normalizeTo180(a), EPS_E8)
    }

    private fun assertEquals(e: Orientation, a: Orientation) {
        assertEquals("x", e.azimuth, a.azimuth, EPS_E8)
        assertEquals("y", e.pitch, a.pitch, EPS_E8)
        assertEquals("z", e.roll, a.roll, EPS_E8)
        assertEquals("z", e.centerAzimuth, a.centerAzimuth, EPS_E8)
        assertEquals("z", e.centerAltitude, a.centerAltitude, EPS_E8)
    }


    private val vectors: ArrayList<Vector> = ArrayList<Vector>().apply {
        this.add(Vector(x = 1.0, y = 0.0, z = 0.0))
        this.add(Vector(x = 0.0, y = 1.0, z = 0.0))
        this.add(Vector(x = 0.0, y = 0.0, z = 1.0))
        this.add(Vector(x = 2.0, y = 1.0, z = 0.0))
        this.add(Vector(x = 0.0, y = 2.0, z = 1.0))
        this.add(Vector(x = 1.0, y = 0.0, z = 2.0))
        this.add(Vector(x = 3.0, y = 2.0, z = 1.0))
    }

    companion object {

        private const val EPS_E8 = 0.00000001
    }
}

