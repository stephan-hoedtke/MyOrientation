package com.stho.myorientation

import com.stho.myorientation.library.Degree
import com.stho.myorientation.library.algebra.*
import com.stho.myorientation.library.algebra.Rotation.adjustForLookingAtThePhoneFromBelow
import org.junit.Test

import org.junit.Assert.*
import java.lang.Exception
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 * See https://github.com/Josef4Sci/AHRS_Filter/blob/master/Filters/MadgwickAHRS3.m
 */
class RotationUnitTests {

    @Test
    fun adjustForLookingAtThePhoneFromBelow_isCorrect() {
        adjustForLookingAtThePhoneFromBelow_isCorrect(
            o = Orientation(10.0, 30.0, 0.0, 10.0, -60.0),
            e = Orientation(10.0, 30.0, 0.0, 10.0, -60.0),
        )

        adjustForLookingAtThePhoneFromBelow_isCorrect(
            o = Orientation(10.0, 30.0, 180.0, 190.0, 60.0),
            e = Orientation(190.0, 150.0, 0.0, 190.0, 60.0),
        )

        adjustForLookingAtThePhoneFromBelow_isCorrect(
            o = Orientation(10.0, 30.0, 160.0, 205.0, 55.0),
            e = Orientation(190.0, 150.0, 20.0, 205.0, 55.0),
        )
    }

    private fun adjustForLookingAtThePhoneFromBelow_isCorrect(o: Orientation, e: Orientation) {
        val a = if (Rotation.requireAdjustmentForLookingAtThePhoneFromBelow(o))
            Rotation.adjustForLookingAtThePhoneFromBelow(o)
        else
            o

        assertEquals(e, a)
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

    private fun matrixFromEulerAngles_withValidation(azimuth: Double, pitch: Double, roll: Double): Matrix {
        val m1 = Matrix.fromEulerAngles(azimuth, pitch, roll)

        val mz = matrixForAzimuth(azimuth)
        val mx = matrixForPitch(pitch)
        val my = matrixForRoll(roll)

        val m2 = my * mx * mz
        val m3 = (my * mx) * mz
        val m4 = my * (mx * mz)

        assertEquals(m1, m2)
        assertEquals(m1, m3)
        assertEquals(m1, m4)

        return m1
    }

    private fun quaternionFromEulerAngles_withValidation(azimuth: Double, pitch: Double, roll: Double): Quaternion {
        val q1 = Quaternion.forEulerAngles(azimuth, pitch, roll)

        val qz = quaternionForAzimuth(azimuth)
        val qx = quaternionForPitch(pitch)
        val qy = quaternionForRoll(roll)

        val q2 = qy * qx * qz
        val q3 = (qy * qx) * qz
        val q4 = qy * (qx * qz)

        val m = Rotation.eulerAnglesToRotationMatrix(EulerAngles.fromAzimuthPitchRoll(azimuth, pitch, roll))
        val q5 = Rotation.rotationMatrixToQuaternion(m)

        assertEquals(q1, q2)
        assertEquals(q1, q3)
        assertEquals(q1, q4)
        assertEquals(q1, q5)

        return q1
    }

    private fun quaternionRotation_equals_matrixRotation(m: Matrix, q: Quaternion) {

        for (earth in vectors) {
            val e = earth.rotateBy(m)
            val a = earth.rotateBy(q)
            assertEquals(e, a)
        }
    }

    private fun matrixToEulerAnglesIsCorrect(azimuth: Double, pitch: Double, roll: Double, m: Matrix) {
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

    private fun assertEquals(e: Matrix, a: Matrix) {
        assertEquals("m11", e.m11, a.m11, EPS_E8)
        assertEquals("m12", e.m12, a.m12, EPS_E8)
        assertEquals("m13", e.m13, a.m13, EPS_E8)
        assertEquals("m21", e.m21, a.m21, EPS_E8)
        assertEquals("m22", e.m22, a.m22, EPS_E8)
        assertEquals("m23", e.m23, a.m23, EPS_E8)
        assertEquals("m31", e.m31, a.m31, EPS_E8)
        assertEquals("m32", e.m32, a.m32, EPS_E8)
        assertEquals("m33", e.m33, a.m33, EPS_E8)
    }

    private fun assertEquals(e: Quaternion, a: Quaternion) {
        if (sign(e.x) == sign(a.x)) {
            assertEquals("x", e.x, a.x, EPS_E8)
            assertEquals("y", e.y, a.y, EPS_E8)
            assertEquals("z", e.z, a.z, EPS_E8)
            assertEquals("s", e.s, a.s, EPS_E8)
        } else {
            assertEquals("x", -e.x, a.x, EPS_E8)
            assertEquals("y", -e.y, a.y, EPS_E8)
            assertEquals("z", -e.z, a.z, EPS_E8)
            assertEquals("s", -e.s, a.s, EPS_E8)

        }
    }

    private fun assertEquals(e: Vector, a: Vector) {
        assertEquals("x", e.x, a.x, EPS_E8)
        assertEquals("y", e.y, a.y, EPS_E8)
        assertEquals("z", e.z, a.z, EPS_E8)
    }

    private fun assertEquals(e: Orientation, a: Orientation) {
        assertEquals("x", e.azimuth, a.azimuth, EPS_E8)
        assertEquals("y", e.pitch, a.pitch, EPS_E8)
        assertEquals("z", e.roll, a.roll, EPS_E8)
        assertEquals("z", e.centerAzimuth, a.centerAzimuth, EPS_E8)
        assertEquals("z", e.centerAltitude, a.centerAltitude, EPS_E8)
    }

    private fun matrixForAzimuth(azimuth: Double): Matrix {
        val cosZ = Degree.cos(azimuth)
        val sinZ = Degree.sin(azimuth)
        return Matrix(
            m11 = cosZ, m12 = -sinZ, m13 = 0.0,
            m21 = sinZ, m22 = cosZ,  m23 = 0.0,
            m31 = 0.0,  m32 = 0.0,   m33 = 1.0,
        )
    }

    private fun matrixForPitch(pitch: Double): Matrix {
        val cosX = Degree.cos(pitch)
        val sinX = Degree.sin(pitch)
        return Matrix(
            m11 = 1.0, m12 = 0.0,  m13 = 0.0,
            m21 = 0.0, m22 = cosX, m23 = -sinX,
            m31 = 0.0, m32 = sinX, m33 = cosX,
        )
    }

    private fun matrixForRoll(roll: Double): Matrix {
        val cosY = Degree.cos(roll)
        val sinY = Degree.sin(roll)
        return Matrix(
            m11 = cosY,  m12 = 0.0, m13 = sinY,
            m21 = 0.0,   m22 = 1.0, m23 = 0.0,
            m31 = -sinY, m32 = 0.0, m33 = cosY,
        )
    }

    private fun quaternionForAzimuth(azimuth: Double): Quaternion =
        Quaternion(
            s = Degree.cos(azimuth / 2),
            x = 0.0,
            y = 0.0,
            z = Degree.sin(azimuth / 2),
        )

    private fun quaternionForPitch(pitch: Double): Quaternion =
        Quaternion(
            s = Degree.cos(pitch / 2),
            x = Degree.sin(pitch / 2),
            y = 0.0,
            z = 0.0
        )

    private fun quaternionForRoll(roll: Double): Quaternion =
        Quaternion(
            s = Degree.cos(roll / 2),
            x = 0.0,
            y = Degree.sin(roll / 2),
            z = 0.0
        )

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

