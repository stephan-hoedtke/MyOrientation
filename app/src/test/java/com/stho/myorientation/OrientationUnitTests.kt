package com.stho.myorientation

import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Rotation
import junit.framework.Assert.assertEquals
import org.junit.Test

class OrientationUnitTests {

    @Test
    fun orientation_isCorrect() {
        orientation_isCorrect(0.0, 0.0, 0.0, 0.0, -90.0)
        orientation_isCorrect(30.0, 0.0, 0.0, 30.0, -90.0)
        orientation_isCorrect(0.0, 30.0, 0.0, -180.0, -60.0)
        orientation_isCorrect(0.0, -30.0, 0.0, 0.0, -60.0)
        orientation_isCorrect(0.0, -90.0, 0.0, 0.0, 0.0)
        orientation_isCorrect(0.0, -100.0, 0.0, 0.0, 10.0)
        orientation_isCorrect(0.0, -90.0, 30.0, 30.0, 0.0)
        orientation_isCorrect(0.0, 0.0, 10.0, 90.0, -80.0)
    }

    private fun orientation_isCorrect(azimuth: Double, pitch: Double, roll: Double, centerAzimuth: Double, centerAltitude: Double) {
        val matrix = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val orientationRaw = matrix.toOrientation()

        val orientation = if (Rotation.requireAdjustmentForLookingAtThePhoneFromBelow(orientationRaw)) {
            Rotation.adjustForLookingAtThePhoneFromBelow(orientationRaw)
        } else {
            orientationRaw
        }

        assertEquals("azimuth", azimuth, orientation.azimuth, EPS_E8)
        assertEquals("pitch", pitch, orientation.pitch, EPS_E8)
        assertEquals("roll", roll, orientation.roll, EPS_E8)
        assertEquals("centerAzimuth", centerAzimuth, orientation.centerAzimuth, EPS_E8)
        assertEquals("centerAltitude", centerAltitude, orientation.centerAltitude, EPS_E8)
    }


    @Test
    fun calculationFromRotationVector_isCorrect_1() {
        calculationFromRotationVector_isCorrect(
            xRot = 0.0,
            yRot = 0.0,
            zRot = -104.5,
            rotationVector = FloatArray(5).apply {
                this[0] = 0.43290147f
                this[1] = -0.5591015f
                this[2] = -0.5591019f
                this[3] = 0.4329053f
                this[4] = 0.0f
            }
        )
    }

    @Test
    fun calculationFromRotationVector_isCorrect_2() {
        calculationFromRotationVector_isCorrect(
            xRot = 0.0,
            yRot = 0.0,
            zRot = -104.5,
            rotationVector = FloatArray(5).apply {
                this[0] = 0.43290302f
                this[1] = -0.5591042f
                this[2] = -0.5590959f
                this[3] = 0.4329079f
                this[4] = 0.0f
            }
        )
    }

    @Suppress("SameParameterValue")
    private fun calculationFromRotationVector_isCorrect(xRot: Double, yRot: Double, zRot: Double, rotationVector: FloatArray) {
        val q = getRotationQuaternionForAVD(xRot, yRot, zRot)
        val e = q.toOrientation()
        val r = Quaternion.fromFloatArray(rotationVector).inverse()
        val m = r.toRotationMatrix()
        val orientation1 = m.toOrientation()
        val orientation2 = r.toOrientation()

        assertEquals("azimuth", e.azimuth, orientation1.azimuth, EPS_E3)
        assertEquals("pitch", e.pitch, orientation1.pitch, EPS_E3)
        assertEquals("roll", e.roll, orientation1.roll, EPS_E3)
        assertEquals("centerAzimuth", e.centerAzimuth, orientation1.centerAzimuth, EPS_E3)
        assertEquals("centerAltitude", e.centerAltitude, orientation1.centerAltitude, EPS_E3)

        assertEquals("azimuth", e.azimuth, orientation2.azimuth, EPS_E3)
        assertEquals("pitch", e.pitch, orientation2.pitch, EPS_E3)
        assertEquals("roll", e.roll, orientation2.roll, EPS_E3)
        assertEquals("centerAzimuth", e.centerAzimuth, orientation2.centerAzimuth, EPS_E3)
        assertEquals("centerAltitude", e.centerAltitude, orientation2.centerAltitude, EPS_E3)
    }

    /**
     * Returns Euler Angles for the rotation defined by zRot, xRot, yRot in the device manager
     *
     *      zRot: rotation around the device's z-axis, positive: tilting x to y, 0 is x pointing to the right
     *      xRot: rotation around the device's x-axis, positive: tilting y to z, 0 is y pointing upwards
     *      yRot: rotation around the device's y-axis, positive: tilting x to -z, 0 is x pointing to the right
     */
    private fun getRotationQuaternionForAVD(zRot: Double, xRot: Double, yRot: Double): Quaternion {
        val m0 = Quaternion.forRotation(1.0, 0.0, 0.0, Math.toRadians(-90.0))
        val mx = Quaternion.forRotation(1.0, 0.0, 0.0, Math.toRadians(-xRot))
        val my = Quaternion.forRotation(0.0, 1.0, 0.0, Math.toRadians(-yRot))
        val mz = Quaternion.forRotation(0.0, 0.0, 1.0, Math.toRadians(-zRot))
        return (mz * my * mx * m0)
    }

    companion object {
        private const val EPS_E8 = 0.000000001
        private const val EPS_E3 = 0.001
    }
}