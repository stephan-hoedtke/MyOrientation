package com.stho.myorientation

import com.stho.myorientation.library.algebra.EulerAngles
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 * See https://github.com/Josef4Sci/AHRS_Filter/blob/master/Filters/MadgwickAHRS3.m
 */
class AVDOrientationUnitTests {

    @Test
    fun gradientDescentForAVD() {
        gradientDescentForAVD_0_0_0()
        gradientDescentForAVD_M90_0_0()
        gradientDescentForAVD_30_0_0()
        gradientDescentForAVD_0_M60_0()
        gradientDescentForAVD_0_0_45()
        gradientDescentForAVD_30_M60_0()
        gradientDescentForAVD_30_0_45()
        gradientDescentForAVD_0_M60_45()
        gradientDescentForAVD_30_M60_45()
        gradientDescentForAVD_6_13_18()
    }

    private
    fun gradientDescentForAVD_0_0_0() {
        gradientDescentForAVD(
            zRot=0.0, xRot=0.0, yRot=0.0,
            acceleration = Vector(0.0, y = 9.81, z = 0.0),
            magnetometer = Vector(0.0, -44.0, -18.0))
    }

    private
    fun gradientDescentForAVD_M90_0_0() {
        gradientDescentForAVD(
            zRot = 0.0, xRot = -90.0, yRot = 0.0,
            acceleration = Vector(x = 0.0, y = 0.0, z = 9.81),
            magnetometer = Vector(x = 0.0, y = 18.0, z = -44.0)
        )
    }

    private
    fun gradientDescentForAVD_30_0_0() {
        gradientDescentForAVD(
            zRot = 30.0, xRot = 0.0, yRot = 0.0,
            acceleration = Vector(x = 4.91, y = 8.50, z = 0.00),
            magnetometer = Vector(x = -22.00, y = -38.11, z = -18.00),
        )
    }

    private
    fun gradientDescentForAVD_0_M60_0() {
        gradientDescentForAVD(
            zRot = 0.0, xRot = -60.0, yRot = 0.0,
            acceleration = Vector(x =0.00, y = 4.91, z = 8.50),
            magnetometer = Vector(x = 0.00, y= -6.41, z = -47.11),
        )
    }

    private
    fun gradientDescentForAVD_0_0_45() {
        gradientDescentForAVD(
            zRot = 0.0, xRot = 0.0, yRot = 45.0,
            acceleration = Vector(x = 0.00, y = 9.81, z = 0.00),
            magnetometer = Vector(x = 12.73, y = -44.00, z = -12.73),
        )
    }

    private
    fun gradientDescentForAVD_30_M60_0() {
        gradientDescentForAVD(
            zRot = 30.0, xRot = -60.0, yRot = 0.0,
            acceleration = Vector(x = 2.45, y = 4.25, z = 8.50),
            magnetometer = Vector(x = -3.21, y = -5.55, z = -47.11),
        )
    }

    private
    fun gradientDescentForAVD_30_0_45() {
        gradientDescentForAVD(
            zRot = 30.0, xRot = 0.0, yRot = 45.0,
            acceleration = Vector(x = 4.91, y = 8.50, z = 0.00),
            magnetometer = Vector(x = -10.98, y = -44.47, z =-12.73),
        )
    }

    private
    fun gradientDescentForAVD_0_M60_45() {
        gradientDescentForAVD(
            zRot = 00.0, xRot = -60.0, yRot = 45.0,
            acceleration = Vector(x = -6.01, y = 4.91, z = 6.01),
            magnetometer = Vector(x = 33.31, y = -6.41, z = -33.31),
        )
    }

    private
    fun gradientDescentForAVD_30_M60_45() {
        gradientDescentForAVD(
            zRot = 30.0, xRot = -60.0, yRot = 45.0,
            acceleration = Vector(x = -2.75, y = 7.25, z = 6.01),
            magnetometer = Vector(x = 25.64, y = -22.21, z = -33.31),
        )
    }


    private
    fun gradientDescentForAVD_6_13_18() {
        gradientDescentForAVD(
            zRot=-6.4, xRot=12.9, yRot=-17.7,
            acceleration = Vector(-1.73, y = 9.43, z = -2.09),
            magnetometer = Vector(2.90, -46.88, -7.36),
        )
    }

    private fun gradientDescentForAVD(zRot: Double, xRot: Double, yRot: Double, acceleration: Vector, magnetometer: Vector) {
        val q = getRotationQuaternionForAVD(zRot, xRot, yRot)
        val eulerAngles = q.toEulerAngles()
        rotationMatchesAccelerationAndMagnetometer(q, acceleration, magnetometer)
    }

    private fun rotationMatchesAccelerationAndMagnetometer(q: Quaternion, acceleration: Vector, magnetometer: Vector) {

        val a = Vector(x = 0.0, y = 0.0, z = 9.81).rotateBy(q)
        val m = Vector(x = 0.0, y = 18.0, z = -44.0).rotateBy(q)

        assertEquals(acceleration, a, EPS_E2)
        assertEquals(magnetometer, m, EPS_E2)
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

    private fun assertEquals(e: Vector, a: Vector, delta: Double = EPS_E6) {
        assertEquals("x", e.x, a.x, delta)
        assertEquals("y", e.y, a.y, delta)
        assertEquals("z", e.z, a.z, delta)
    }

    companion object {

        private const val EPS_E8 = 0.00000001
        private const val EPS_E6 = 0.000001
        private const val EPS_E3 = 0.001
        private const val EPS_E2 = 0.01
    }
}

