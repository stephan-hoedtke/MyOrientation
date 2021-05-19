package com.stho.myorientation

import com.stho.myorientation.library.algebra.EulerAngles
import com.stho.myorientation.library.algebra.Matrix
import com.stho.myorientation.library.algebra.Rotation
import junit.framework.Assert.assertEquals
import org.junit.Test

class OrientationUnitTests {

    @Test
    fun orientation_isCorrect() {
        orientation_isCorrect(0.0, 0.0, 0.0, 0.0, -90.0)
        orientation_isCorrect(30.0, 0.0, 0.0, 0.0, -90.0)
        orientation_isCorrect(0.0, 30.0, 0.0, -180.0, -60.0)
        orientation_isCorrect(0.0, -30.0, 0.0, 0.0, -60.0)
        orientation_isCorrect(0.0, -90.0, 0.0, 0.0, 0.0)
        orientation_isCorrect(0.0, -100.0, 0.0, 0.0, 10.0)
        orientation_isCorrect(0.0, -90.0, 30.0, 30.0, 0.0)
        orientation_isCorrect(0.0, 0.0, 10.0, 90.0, -80.0)
    }

    private fun orientation_isCorrect(azimuth: Double, pitch: Double, roll: Double, centerAzimuth: Double, centerAltitude: Double) {
        val matrix = Matrix.fromEulerAngles(azimuth, pitch, roll)
        val orientationRaw = Rotation.rotationMatrixToOrientation(matrix)

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

    companion object {
        private const val EPS_E8 = 0.000000001
    }
}