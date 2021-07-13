package com.stho.myorientation

import com.stho.myorientation.library.algebra.RotationMatrix
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Rotation
import com.stho.myorientation.library.algebra.Vector
import com.stho.myorientation.library.filter.ExtendedComplementaryFilter
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtendedComplementaryFilterUnitTests : BaseUnitTestsHelper() {

    @Test
    fun orthogonalError_isReducing_forCorrectRotation() {
        orthogonalError_isReducing_forCorrectRotation(azimuth = 10.0, pitch = 30.0, roll = 5.0)
    }

    @Test fun orthogonalError_isReducing_forAccelerometerMagnetometer() {
        val accelerometer = Vector(x=0.213333311103986, y=0.763796063555749, z=0.609183446648177)
        val magnetometer = Vector(x=-0.152875289501063, y=0.886600113489289, z=-0.436542534721126)
        val m = Rotation.getRotationMatrixFromAccelerationMagnetometer(accelerometer, magnetometer, RotationMatrix.E)
        val eulerAngles = m.toEulerAngles()
        orthogonalError_isReducing_forCorrectRotation(eulerAngles.azimuth, eulerAngles.pitch, eulerAngles.roll)
    }

    private fun orthogonalError_isReducing_forCorrectRotation(azimuth: Double, pitch: Double, roll: Double) {
        val q = Quaternion.forEulerAngles(azimuth, pitch, roll)
        val q0 = Quaternion.forEulerAngles(azimuth = azimuth + 0.2, pitch = pitch + 0.2, roll = roll + 0.2)
        val q1 = Quaternion.forEulerAngles(azimuth = azimuth + 0.1, pitch = pitch + 0.1, roll = roll + 0.1)
        orthogonalError_isReducing_forCorrectRotation(q, q0, q1)
    }

    private fun orthogonalError_isReducing_forCorrectRotation(q: Quaternion, q0: Quaternion, q1: Quaternion) {
        val a = Vector(0.0, 0.0, 9.81).rotateBy(q.inverse())
        val m = Vector(0.0, 18.0, -44.0).rotateBy(q.inverse())

        val e = error(q, a, m)
        val e0 = error(q0, a, m)
        val e1 = error(q1, a, m)

        val n = e.norm()
        val n0 = e0.norm()
        val n1 = e1.norm()

        assertTrue("N0 > N", n < n0)
        assertTrue("N1 > N", n < n1)
        assertTrue("N0 > N1", n1 < n0)
    }

    @Test
    fun correctionIsReducingTheError() {

        val estimate = Quaternion(s=0.859313490285539, x=0.415710057573015, y=-0.297394999060784, z=-0.017935662228154)
        val accelerometer = Vector(x=0.178994840440616, y=0.763795856921106, z=0.620142512690261)
        val magnetometer = Vector(x=-0.953719265473134, y=0.000000038018953, z=0.275276723037774)

        correctionIsReducingTheError(estimate, accelerometer, magnetometer)
    }

    private fun correctionIsReducingTheError(q: Quaternion, a: Vector, m: Vector, dt: Double = 0.01) {
        val w = Vector(0.0, 0.0, 0.0)

        val error = error(q, a, m)

        // Integrate corrected gyro measurement
        val omega = w + error * GAIN
        val qDot = q * omega.asQuaternion() * 0.5
        val delta = qDot * dt
        val q1 = (q + delta).normalize()

        val aPrediction1 = Vector(
            x = q1.m31,
            y = q1.m32,
            z = q1.m33,
        )

        val error1 = error(q1, a, m)

        val n = error.norm()
        val n1 = error1.norm()

        assertTrue("error",  n1 < n)
    }

    private fun error(q: Quaternion, a: Vector, m: Vector): Vector {
        return ExtendedComplementaryFilter.aError(q, a) + ExtendedComplementaryFilter.eError(q, a, m)
    }

    companion object {
        private const val GAIN = 0.01
    }
}