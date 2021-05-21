package com.stho.myorientation

import com.stho.myorientation.library.algebra.EulerAngles
import org.junit.Test

class EulerAnglesUnitTests : BaseUnitTestsHelper() {

    private val list : ArrayList<EulerAngles> = ArrayList<EulerAngles>().apply {
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 0.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 0.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 10.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 0.0, 20.0))
        add(EulerAngles.fromAzimuthPitchRoll(-30.0, 0.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, -10.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 0.0, -20.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 10.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 0.0, 20.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 10.0, 20.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 10.0, 20.0))

        add(EulerAngles.fromAzimuthPitchRoll(0.0, -90.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, -90.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, -90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, -90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(-30.0, -90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, -90.0, -10.0))

        add(EulerAngles.fromAzimuthPitchRoll(0.0, 90.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 90.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(-30.0, 90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 90.0, -10.0))

        add(EulerAngles.fromAzimuthPitchRoll(90.0, 90.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(90.0, 90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(90.0, 90.0, -10.0))
        add(EulerAngles.fromAzimuthPitchRoll(90.0, -90.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(90.0, -90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(90.0, -90.0, -10.0))
        add(EulerAngles.fromAzimuthPitchRoll(-90.0, 90.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(-90.0, 90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(-90.0, 90.0, -10.0))
        add(EulerAngles.fromAzimuthPitchRoll(-90.0, -90.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(-90.0, -90.0, 10.0))
        add(EulerAngles.fromAzimuthPitchRoll(-90.0, -90.0, -10.0))
    }

    @Test
    fun quaternionEqualsRotationMatrix() {
        for (e in list) {
            val q = super.quaternionForEulerAngles(e)
            val m = super.rotationMatrixForEulerAngles(e)
            assertIsEqualRotation(m, q, EPS_E8)
        }
    }

    @Test
    fun quaternionToRotationMatrix_isCorrect() {
        for (e in list) {
            val q = super.quaternionForEulerAngles(e)
            val m = q.toRotationMatrix()
            assertIsEqualRotation(m, q, EPS_E8)
        }
    }

    @Test
    fun rotationMatrixToQuaternion_isCorrect() {
        for (e in list) {
            val m = super.rotationMatrixForEulerAngles(e)
            val q = m.toQuaternion()
            assertIsEqualRotation(m, q, EPS_E8)
        }
    }

    @Test
    fun eulerAnglesToRotationMatrix_isCorrect() {
        for (eulerAngles in list) {
            val e = super.rotationMatrixForEulerAngles(eulerAngles)
            val a = eulerAngles.toRotationMatrix()
            assertEquals(e, a, EPS_E8)
        }
    }

    @Test
    fun eulerAnglesToQuaternion_isCorrect() {
        for (eulerAngles in list) {
            val e = super.quaternionForEulerAngles(eulerAngles)
            val a = eulerAngles.toQuaternion()
            assertEquals(e, a, EPS_E8)
        }
    }

    @Test
    fun rotationMatrixToEulerAngles_isCorrect() {
        for (e in list) {
            val m = super.rotationMatrixForEulerAngles(e)
            val a = m.toEulerAngles()
            assertEquals(e, a, EPS_E8)
        }
    }

    @Test
    fun quaternionToEulerAngles_isCorrect() {
        for (e in list) {
            val q = super.quaternionForEulerAngles(e)
            val a = q.toEulerAngles()
            assertEquals(e, a, EPS_E8)
        }
    }

    @Test
    fun quaternionEqualsRotationMatrixForEulerAngles() {
        for (e in list) {
            val q = e.toQuaternion()
            val m = e.toRotationMatrix()
            assertIsEqualRotation(m, q, EPS_E8)
        }
    }


}
