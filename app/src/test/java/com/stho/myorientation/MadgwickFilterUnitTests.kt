package com.stho.myorientation

import com.stho.myorientation.library.algebra.Degree
import com.stho.myorientation.library.algebra.*
import com.stho.myorientation.library.filter.MadgwickFilter
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 * See https://github.com/Josef4Sci/AHRS_Filter/blob/master/Filters/MadgwickAHRS3.m
 */
class MadgwickFilterUnitTests : BaseUnitTestsHelper() {

    //                |  cos(pitch)   -sin(pitch) |
    // (y,z)_Device = |                           | * (y,z)_Earth
    //                |  sin(pitch)    cos(pitch) |
    //
    //
    //                |  cos(pitch)    sin(pitch) |
    // (y,z)_Earth  = |                           | * (y,z)_Device
    //                | -sin(pitch)    cos(pitch) |

    @Test
    fun gradientDescentApproximatesOrientationA() {
        gradientDescent_isCorrect(
            q = Quaternion.forEulerAngles(7.1, -7.1, 6.9).normalize(),
            e = Quaternion.forEulerAngles(7.0, -7.0, 7.0).normalize()
        )
    }

    @Test
    fun gradientDescentApproximatesOrientationB() {
        gradientDescent_isCorrect(
            q = Quaternion.forEulerAngles(13.0, -13.0, 13.0).normalize(),
            e = Quaternion.forEulerAngles(7.0, -7.0, 7.0).normalize()
        )
    }

    @Test
    fun gradientDescent_isCorrect_0_0_0() {
        gradientDescent_isCorrect(
            azimuth = 0.0,
            pitch = 0.0,
            roll = 0.0,
            a = Vector(0.0, 0.0, 9.81),
            m = Vector(0.0, 18.0, -44.0)
        )
    }

    @Test
    fun gradientDescent_isCorrect_P30_0_0() {
        // Device the top edge pointing downwards by 30°
        // sin(30°) = 0.5
        // cos(30°) = 0.8660254
        // y_device = y_earth * cos(x) - z_earth * sin(x)
        // z_device = y_earth * sin(x) + z_earth * cos(x)
        val cos30 = cos(PI * 30 / 180)
        val sin30 = sin(PI * 30 / 180)
        val ay = -9.81 * sin30 // -4.905 = 0 * cos(30) - (9.81) * sin(30)
        val az = 9.81 * cos30 // 8.496 = 0 * cos(30) + (9.81) * cos(30)
        val my = (+18.0) * cos30 - (-44.0) * sin30 // 37.59
        val mz = (+18.0) * sin30 + (-44.0) * cos30 // -29.11
        gradientDescent_isCorrect(
            azimuth = 0.0,
            pitch = 30.0,
            roll = 0.0,
            a = Vector(0.0, ay, az),
            m = Vector(0.0, my, mz)
        )
    }

    @Test
    fun gradientDescent_isCorrect_P60_0_0() {
        // Device the top edge pointing downwards by 60°
        // sin(60°) = 0.8660254
        // cos(60°) = 0.5
        val cos60 = cos(PI * 60 / 180)
        val sin60 = sin(PI * 60 / 180)
        val ay = -9.81 * sin60 // -8.496
        val az = 9.81 * cos60 // 4.905
        val my = (+18.0) * cos60 - (-44.0) * sin60 // 47.11
        val mz = (-44.0) * cos60 + (+18.0) * sin60 // -6.41
        gradientDescent_isCorrect(
            azimuth = 0.0,
            pitch = 60.0,
            roll = 0.0,
            a = Vector(0.0, ay, az),
            m = Vector(0.0, my, mz)
        )
    }

    @Test
    fun gradientDescent_isCorrect_M30_0_0() {
        // Device the top edge pointing downwards by -30°
        // sin(-30°) = -0.5
        // cos(-30°) = 0.8660254
        val cosM30 = cos(PI * (-30) / 180)
        val sinM30 = sin(PI * (-30) / 180)
        val ay = -9.81 * sinM30 // +4.905
        val az = 9.81 * cosM30 // 8.496
        val my = (+18.0) * cosM30 - (-44.0) * sinM30 // -6.41
        val mz = (-44.0) * cosM30 + (+18.0) * sinM30 // -47.11
        gradientDescent_isCorrect(
            azimuth = 0.0,
            pitch = -30.0,
            roll = 0.0,
            a = Vector(0.0, ay, az),
            m = Vector(0.0, my, mz)
        )
    }

    @Test
    fun gradientDescent_isCorrect_M60_0_0() {
        // Device the top edge pointing downwards by -30°
        // sin(-60°) = -0.8660254
        // cos(-60°) = 0.5
        val cosM60 = cos(PI * (-60) / 180)
        val sinM60 = sin(PI * (-60) / 180)
        val ay = -9.81 * sinM60 // 8.496
        val az = 9.81 * cosM60 // 4.905
        val my = (+18.0) * cosM60 - (-44.0) * sinM60 // -29.11
        val mz = (-44.0) * cosM60 + (+18.0) * sinM60 // -37.59
        gradientDescent_isCorrect(
            azimuth = 0.0,
            pitch = -60.0,
            roll = 0.0,
            a = Vector(0.0, ay, az),
            m = Vector(0.0, my, mz)
        )
    }

    @Test
    fun gradientDescent_isCorrect_M90_0_0() {
        // Device the top edge pointing downwards by -30°
        // sin(-90°) = -1
        // cos(-90°) = 0
        gradientDescent_isCorrect(
            azimuth = 0.0,
            pitch = -90.0,
            roll = 0.0,
            a = Vector(0.0, 9.81, 0.0),
            m = Vector(0.0, -44.0, -18.0)
        )
    }

    @Test
    fun gradientDescent_isCorrect_4() {
        // Orientation: z-Rot=6.4, x-Rot=5.85, y-Rot=7.79
        // Accelerometer (m/s2) x=-1.14, y=5.85, z=7.79
        // Magnetometer (microTesla) x=9.06, y=-11.90, z=-45.12
        gradientDescent_isCorrect(
            a = Vector(x = -1.14, y = 5.85, z = 7.79),
            m = Vector(x = 9.06, y = -11.90, z = -45.12)
        )
    }

    @Test
    fun gradientDescent_isCorrect_5() {
        // z-Rot=-6.4, x-Rot=12.9, y-Rot=-17.7
        // Accelerometer (m/s2): -1.73, 9.43, -2.09
        // Magnetometer (μT): 2.90, -46.88, -7.36
        gradientDescent_isCorrect(
            a = Vector(-1.73, y = 9.43, z = -2.09),
            m = Vector(2.90, -46.88, -7.36)
        )
    }

    @Test
    fun gradientDescent_isCorrect_6() {
        // z-Rot=0, x-Rot=12.9, y-Rot=-17.7
        // Accelerometer (m/s2): -0.67, 9.56, -2.09
        // Magnetometer (μT): -2.35, -46.91, -7.36
        gradientDescent_isCorrect(
            a = Vector(-0.67, y = 9.56, z = -2.09),
            m = Vector(2.35, -46.91, -7.36)
        )
    }

    @Test
    fun gradientDescent_isCorrect_7() {
        // z-Rot=-6.4, x-Rot=0.0, y-Rot=0.0
        // Accelerometer (m/s2): -1.09, 9.75, 0.00
        // Magnetometer (μT): 4.90, -43.73, -18.00
        gradientDescent_isCorrect(
            a = Vector(-1.09, y = 9.75, z = 0.0),
            m = Vector(4.90, -43.73, -18.0)
        )
    }

    @Test
    fun gradientDescent_isCorrect_8() {
         // z-Rot=0.0, x-Rot=0.0, y-Rot=-17.7
        //  Accelerometer (m/s2): 0.00, 9.81, 0.00
        //  Magnetometer (μT): -5.47, -44.00, -17.15
        gradientDescent_isCorrect(
            a = Vector(0.0, y = 9.81, z = 0.0),
            m = Vector(-5.47, -44.00, -17.15)
        )
    }

    private fun gradientDescent_isCorrect(a: Vector, m: Vector) {
        val r = Rotation.getRotationMatrixFromAccelerationMagnetometer(a, m, RotationMatrix.E)
        val e = Quaternion.fromRotationMatrix(r)
        val eulerAngles = e.toEulerAngles()
        gradientDescent_isCorrect(eulerAngles.azimuth, eulerAngles.pitch, eulerAngles.roll, a, m)
    }

    private fun gradientDescent_isCorrect(azimuth: Double, pitch: Double, roll: Double, a: Vector, m: Vector, q: Quaternion = q0) {
        val e = Quaternion.forEulerAngles(azimuth, pitch, roll)
        accelerationMagnetometerGivesOrientation(azimuth, pitch, roll, a, m)
        gradientDescentReducesObjectiveFunction(a.normalize(), m, q, e)
        gradientDescentApproximatesOrientation(azimuth, pitch, roll, a, m, q)
        rotationGivesAccelerationMagnetometer(e, a, m)
    }

    private fun rotationGivesAccelerationMagnetometer(e: Quaternion, a: Vector, m: Vector) {
        val ae = Vector(x = 0.0, y = 0.0, z = 9.81).rotateBy(e.inverse())
        val me = Vector(x = 0.0, y = 18.0, z = -44.0).rotateBy(e.inverse())

        assertEquals("a.x", a.x, ae.x, EPS_E0)
        assertEquals("a.y", a.y, ae.y, EPS_E0)
        assertEquals("a.z", a.z, ae.z, EPS_E0)

        assertEquals("m.x", m.x, me.x, EPS_E0)
        assertEquals("m.y", m.y, me.y, EPS_E0)
        assertEquals("m.z", m.z, me.z, EPS_E0)
    }

    private fun accelerationMagnetometerGivesOrientation(azimuth: Double, pitch: Double, roll: Double, acceleration: Vector, magnetometer: Vector) {
        val eulerAngles = getOrientation(acceleration, magnetometer)
        assertEquals("azimuth", azimuth, eulerAngles.azimuth, EPS_E6)
        assertEquals("pitch", pitch, eulerAngles.pitch, EPS_E6)
        assertEquals("roll", roll, eulerAngles.roll, EPS_E6)
    }

    private fun getOrientation(acceleration: Vector, magnetometer: Vector): EulerAngles {
        val r = Rotation.getRotationMatrixFromAccelerationMagnetometer(acceleration, magnetometer, RotationMatrix.E)
        return r.toEulerAngles()
    }

    private fun gradientDescent_isCorrect(e: Quaternion, q: Quaternion) {
        val a = Vector(0.0, 0.0, 9.81).rotateBy(e.inverse())
        val m = Vector(0.0, 18.0, -44.0).rotateBy(e.inverse())
        gradientDescentReducesObjectiveFunction(a, m, q, e)
    }

    /**
     * calculate the approximation for acceleration a and magnetometer m and compare it with the given angles
     */
    private fun gradientDescentApproximatesOrientation(azimuth: Double, pitch: Double, roll: Double, a: Vector, m: Vector, q: Quaternion) {

        val p = getApproximateOrientationFromGradientDescent(a, m, q)

        val eulerAngles = p.toEulerAngles()

        val p_azimuth = eulerAngles.azimuth
        val p_pitch = eulerAngles.pitch
        val p_roll = eulerAngles.roll

        val eulerAngles2 = p.inverse().toEulerAngles()

        val p_azimuth2 = eulerAngles2.azimuth
        val p_pitch2 = eulerAngles2.pitch
        val p_roll2 = eulerAngles2.roll

        assertEquals("Azimuth", azimuth, p_azimuth, EPS_E3)
        assertEquals("Pitch", pitch, p_pitch, EPS_E3)
        assertEquals("Roll", roll, p_roll, EPS_E3)
    }

    /**
     * calculate the approximation and compare it with the original orientation e
     */
    private fun gradientDescentReducesObjectiveFunction(a: Vector, m: Vector, q: Quaternion, e: Quaternion? = null) {

        val mm = m.normalize()
        val aa = a.normalize()

        val b = MadgwickFilter.flux(a, m)
        val p = getApproximateOrientationFromGradientDescent(a, m, q)

        val n0 = MadgwickFilter.objectiveFunction(q, b.y, b.z, aa, mm).normSquare()
        val n1 = MadgwickFilter.objectiveFunction(p, b.y, b.z, aa, mm).normSquare()

        assertTrue("n1 < n0", n1 < n0 + EPS_E8)
        assertEquals("n1 is about 0", 0.0, n1, EPS_E6)

        e?.also {
            assertEquals("e.x", it.x, p.x, EPS_E5)
            assertEquals("e.y", it.y, p.y, EPS_E5)
            assertEquals("e.z", it.z, p.z, EPS_E5)
            assertEquals("e.s", it.s, p.s, EPS_E5)
        }
    }

    private fun getApproximateOrientationFromGradientDescent(a: Vector, m: Vector, q: Quaternion): Quaternion {

        val mm = m.normalize()
        val aa = a.normalize()

        val b = MadgwickFilter.flux(aa, mm)

        var f = 1E-1
        val limit = 1E-8

        var p0 = q
        var n0 = MadgwickFilter.objectiveFunction(q = p0, by = b.y, bz = b.z, a = aa, m = mm).normSquare()

        var gradient = MadgwickFilter.gradientAsJacobianTimesObjectiveFunction(q = p0, by = b.y, bz = b.z, a = aa, m = mm)

        while (f > limit) {

            val p1 = (p0 - gradient.normalize() * f).normalize()
            val n1 = MadgwickFilter.objectiveFunction(q = p1, by = b.y, bz = b.z, a = aa, m = mm).normSquare()

            if (n1 < n0) {
                // another step with same step size f
                p0 = p1
                n0 = n1
                gradient = MadgwickFilter.gradientAsJacobianTimesObjectiveFunction(p0, b.y, b.z, aa, mm)
            }
            else {
                // reduce step size
                f /= 10
            }
        }

        return p0
    }


//    @Test
//    fun orthogonalGradientDescent_isCorrect() {
//        // Orientation: z-Rot=6.4, x-Rot=5.85, y-Rot=7.79
//        // Accelerometer (m/s2) x=-1.14, y=5.85, z=7.79
//        // Magnetometer (microTesla) x=9.06, y=-11.90, z=-45.12
//        orthogonalGradientDescent_isCorrect(
//            a = Vector(x = -1.14, y = 5.85, z = 7.79),
//            m = Vector(x = 9.06, y = -11.90, z = -45.12)
//        )
//    }
//
//    private fun orthogonalGradientDescent_isCorrect(a: Vector, m: Vector) {
//        val r = Rotation.getRotationMatrixFromAccelerationMagnetometer(a, m, RotationMatrix.E)
//        val e = Quaternion.fromRotationMatrix(r)
//        val eulerAngles = e.toEulerAngles()
//        orthogonalGradientDescent_isCorrect(eulerAngles.azimuth, eulerAngles.pitch, eulerAngles.roll, a, m)
//    }
//
//    private fun orthogonalGradientDescent_isCorrect(azimuth: Double, pitch: Double, roll: Double, a: Vector, m: Vector, q: Quaternion = q0) {
//        orthogonalGradientDescentApproximatesOrientation(azimuth, pitch, roll, a, m, q)
//    }
//
//    /**
//     * calculate the approximation for acceleration a and magnetometer m and compare it with the given angles
//     */
//    private fun orthogonalGradientDescentApproximatesOrientation(azimuth: Double, pitch: Double, roll: Double, a: Vector, m: Vector, q: Quaternion) {
//
//        val e = Vector.cross(a, m)
//        val p = getApproximateOrientationFromOrthogonalGradientDescent(a, e, q)
//
//        val eulerAngles = p.toEulerAngles()
//
//        val p_azimuth = eulerAngles.azimuth
//        val p_pitch = eulerAngles.pitch
//        val p_roll = eulerAngles.roll
//
//        val eulerAngles2 = p.inverse().toEulerAngles()
//
//        val p_azimuth2 = eulerAngles2.azimuth
//        val p_pitch2 = eulerAngles2.pitch
//        val p_roll2 = eulerAngles2.roll
//
//        assertEquals("Azimuth", azimuth, p_azimuth, EPS_E3)
//        assertEquals("Pitch", pitch, p_pitch, EPS_E3)
//        assertEquals("Roll", roll, p_roll, EPS_E3)
//    }
//
//    private fun getApproximateOrientationFromOrthogonalGradientDescent(a: Vector, e: Vector, q: Quaternion): Quaternion {
//
//        val ee = e.normalize()
//        val aa = a.normalize()
//
//        var f = 1E-1
//        val limit = 1E-8
//
//        var p0 = q
//        var n0 = MadgwickFilter.orthogonalObjectiveFunction(q = p0, a = aa, e = ee).normSquare()
//
//        var gradient = MadgwickFilter.gradientAsJacobianTimesOrthogonalObjectiveFunction(q = p0, aa, ee)
//
//        while (f > limit) {
//
//            val p1 = (p0 - gradient.normalize() * f).normalize()
//            val n1 = MadgwickFilter.orthogonalObjectiveFunction(q = p1, a = aa, e = ee).normSquare()
//
//            if (n1 < n0) {
//                // another step with same step size f
//                p0 = p1
//                n0 = n1
//                gradient = MadgwickFilter.gradientAsJacobianTimesOrthogonalObjectiveFunction(p0, aa, ee)
//            }
//            else {
//                // reduce step size
//                f /= 10
//            }
//        }
//
//        return p0
//    }
//


    @Test
    fun objectiveFunction_isCorrect() {
        val e = Quaternion.forEulerAngles(7.0, -7.0, 7.0).normalize()
        val q = Quaternion.forEulerAngles(7.1, -7.1, 6.9).normalize()

        val ae = g0.rotateBy(e.inverse())
        val me = b0.rotateBy(e.inverse())

        val aq = g0.rotateBy(q.inverse())
        val mq = b0.rotateBy(q.inverse())

        objectiveFunction_isCorrect(e, ae, me)
        objectiveFunction_isCorrect(q, ae, me)
        objectiveFunction_isCorrect(q, aq, mq)
    }

    private fun objectiveFunction_isCorrect(q: Quaternion, a: Vector, m: Vector) {
        val f = MadgwickFilter.objectiveFunction(q, by = b0.y, bz = b0.z, a = a, m = m)

        val fa = g0.rotateBy(q.inverse()) - a
        val fm = b0.rotateBy(q.inverse()) - m

        assertEquals("f1", fa.x, f.f1, EPS_E6)
        assertEquals("f2", fa.y, f.f2, EPS_E6)
        assertEquals("f3", fa.z, f.f3, EPS_E6)
        assertEquals("f4", fm.x, f.f4, EPS_E6)
        assertEquals("f5", fm.y, f.f5, EPS_E6)
        assertEquals("f6", fm.z, f.f6, EPS_E6)
    }


    @Test
    fun objectiveFunction_isMeasuringTheError() {
        val e0 = Quaternion.forEulerAngles(7.0, -7.0, 7.0).normalize()
        val e1 = Quaternion.forEulerAngles(7.1, -7.1, 6.9).normalize()
        val e2 = Quaternion.forEulerAngles(7.01, -7.01, 6.99).normalize()

        val a0 = g0.rotateBy(e0.inverse())
        val m0 = b0.rotateBy(e0.inverse())
        val n0 = MadgwickFilter.objectiveFunction(e0, b0.y, b0.z, a0, m0).normSquare()

        assertEquals("error for e0 is zero", 0.0, n0, EPS_E8)

        val a1 = g0.rotateBy(e1.inverse())
        val m1 = b0.rotateBy(e1.inverse())
        val n01 = MadgwickFilter.objectiveFunction(e0, b0.y, b0.z, a0, m1).normSquare()
        val n10 = MadgwickFilter.objectiveFunction(e1, b0.y, b0.z, a1, m0).normSquare()
        val n11 = MadgwickFilter.objectiveFunction(e1, b0.y, b0.z, a1, m1).normSquare()

        val a2 = g0.rotateBy(e2.inverse())
        val m2 = b0.rotateBy(e2.inverse())
        val n02 = MadgwickFilter.objectiveFunction(e0, b0.y, b0.z, a0, m2).normSquare()
        val n20 = MadgwickFilter.objectiveFunction(e2, b0.y, b0.z, a2, m0).normSquare()
        val n22 = MadgwickFilter.objectiveFunction(e2, b0.y, b0.z, a2, m2).normSquare()

        assertTrue("error for e2, a0, m0 is less than for e1", n20 < n10)
        assertTrue("error for e0, a2, m2 is less than for e1", n02 < n01)

        assertEquals("error for e1, a1, m1 is zero", 0.0, n11, EPS_E8)
        assertEquals("error for e2, a2, m2 is zero", 0.0, n22, EPS_E8)
    }

    private val g0: Vector = Vector(0.0, 0.0, 9.81).normalize()
    private val b0: Vector = Vector(0.0, 10 * Degree.cos(60.0), -10.0).normalize()
    private val q0: Quaternion = Quaternion.forEulerAngles(0.0, 0.0, 0.0).normalize()

    @Test
    fun estimation_change_by_gyro_measurement_is_correct() {
        // D/Estimate: error=1.079659156230738 b=(0, 0.64, 0.77, 0) estimate x=-0.668094683661823, y=0.310937913799505, z=-0.348775798505734, s=0.686522161129934
        //D/Gyro: omega x=0.000000000000000, y=0.705973029136658, z=0.000000000000000, s=0.000000000000000
        //    qDotGyro x=0.123113153480325, y=0.242333064831172, z=-0.235828413787417, s=-0.109756890439235
        //    dt=0.027295600000798 delta x=0.003360447392236, y=0.006614626404599, z=-0.006437078051564, s=-0.002995880178761 n1=1.103359658707653
        //D/Final: error=1.103359658707653 estimate x=-0.664734236269588, y=0.317552540204104, z=-0.355212876557298, s=0.683526280951173
        estimation_change_by_gyro_measurement_is_correct(
            q = Quaternion(x=-0.668094683661823, y=0.310937913799505, z=-0.348775798505734, s=0.686522161129934),
            e = Quaternion(x=-0.664734236269588, y=0.317552540204104, z=-0.355212876557298, s=0.683526280951173),
            w = Vector(x=0.000000000000000, y=0.705973029136658, z=0.000000000000000),
            dt=0.027295600000798,
        )
        // D/Estimate: error=1.105764430673372 b=(0, 0.63, 0.78, 0) estimate x=-0.664734236269588, y=0.317552540204104, z=-0.355212876557298, s=0.683526280951173
        //D/Gyro: omega x=0.000000000000000, y=0.284803986549377, z=0.000000000000000, s=0.000000000000000
        //    qDotGyro x=0.050583021658595, y=0.097335504863082, z=-0.094659480242717, s=-0.045220114694505
        //D/Gyro: dt=0.012692799999058 delta x=0.000642040177261, y=0.001235460096034, z=-0.001201493850736, s=-0.000573969871752 n1=1.110238877338978
        //D/Final: error=1.110238877338978 estimate x=-0.664092196092327, y=0.318788000300139, z=-0.356414370408033, s=0.682952311079421
        estimation_change_by_gyro_measurement_is_correct(
            q = Quaternion(x=-0.664734236269588, y=0.317552540204104, z=-0.355212876557298, s=0.683526280951173),
            e = Quaternion(x=-0.664092196092327, y=0.318788000300139, z=-0.356414370408033, s=0.682952311079421),
            w = Vector(x=0.000000000000000, y=0.284803986549377, z=0.000000000000000),
            dt=0.012692799999058
        )
        // D/Estimate: error=0.06635839280 b=(0, 0.97, 0.22, 0) estimate x=-0.73196898093, y=-0.18622635396, z=-0.04018231830, s=0.65778926251
        //D/Gyro: omega x=0.00000000000, y=-2.64406991005, z=0.00000000000, s=0.00000000000
        //    dt=0.01304940000 delta x=-0.00069321583, y=-0.01134802442, z=0.01262775534, s=-0.00321273291 n1=0.08131263415
        //D/Final: error=0.08131263415 estimate x=-0.73266219676, y=-0.19757437838, z=-0.02755456296, s=0.65457652959
        estimation_change_by_gyro_measurement_is_correct(
            q = Quaternion(x=-0.73196898093, y=-0.18622635396, z=-0.04018231830, s=0.65778926251),
            e = Quaternion(x=-0.73266219676, y=-0.19757437838, z=-0.02755456296, s=0.65457652959),
            w = Vector(x=0.00000000000, y=-2.64406991005, z=0.00000000000),
            dt=0.01304940000
        )


        // D/Estimate: error=0.09444551363 b=(0, 0.97, 0.24, 0) estimate x=-0.73266219676, y=-0.19757437838, z=-0.02755456296, s=0.65457652959
        //D/Gyro: omega x=0.00000000000, y=-2.88807988167, z=0.00000000000, s=0.00000000000
        //    dt=0.02162300000 delta x=-0.00086037678, y=-0.02043880890, z=0.02287699292, s=-0.00616915637 n1=0.12615274127
        //D/Final: error=0.12615274127 estimate x=-0.73352257354, y=-0.21801318729, z=-0.00467757004, s=0.64840737323
        estimation_change_by_gyro_measurement_is_correct(
            q = Quaternion(x=-0.73266219676, y=-0.19757437838, z=-0.02755456296, s=0.65457652959),
            e = Quaternion(x=-0.73352257354, y=-0.21801318729, z=-0.00467757004, s=0.64840737323),
            w = Vector(x=0.00000000000, y=-2.88807988167, z=0.00000000000),
            dt=0.02162300000
        )
    }

    private fun estimation_change_by_gyro_measurement_is_correct(q: Quaternion, e: Quaternion, w: Vector, dt: Double) {

        val dotOmega = q * w.asQuaternion() * 0.5
        val delta1 = dotOmega * dt
        val estimation1 = q + delta1

        val delta2 = Rotation.getRotationFromGyro(omega = w, dt = dt)
        val estimation2 = q * delta2

        assertEquals(e, estimation1, EPS_E6)
        assertEquals(e, estimation2, EPS_E3)
    }

    @Test
    fun flux_is_correct() {
        val q = super.quaternionForEulerAngles(EulerAngles.fromAzimuthPitchRoll(30.0, 10.0, 20.0))
        val a = Vector(0.0, 0.0, 9.81).rotateBy(q.inverse())
        val m = Vector(0.0, 18.0, -44.0).rotateBy(q.inverse())

        val b = MadgwickFilter.flux(a, m)

        val x = 0.0
        val z = -0.925546
        val y = 0.378632

        assertEquals("b.x", x, b.x, EPS_E6)
        assertEquals("b.y", y, b.y, EPS_E6)
        assertEquals("b.z", z, b.z, EPS_E6)

    }

    companion object {

        private const val EPS_E8 = 0.00000001
        private const val EPS_E6 = 0.000001
        private const val EPS_E5 = 0.00001
        private const val EPS_E4 = 0.0001
        private const val EPS_E3 = 0.001
        private const val EPS_E2 = 0.01
        private const val EPS_E1 = 0.1
        private const val EPS_E0 = 1.0
    }
}

//         /**
//         * Return the normalized gradient as the product of the Jacobian matrix with the objective function
//         *      fa = q* # (0, 0, -1) # q - |a| for gravity
//         *      fb = q* # (1, 0, 0) # q* - |a x m| for a vector e = |gravity x magnetic field|
//          */
//        internal fun gradientAsJacobianTimesOrthogonalObjectiveFunction(q: Quaternion, a: Vector, m: Vector): Quaternion {
//            val f: ObjectiveFunction = orthogonalObjectiveFunction(q, a, m)
//            val J: Jacobian = orthogonalJacobian(q)
//            return J * f
//        }
//
//         /**
//         * return the objective function for the current estimate and the normalized acceleration
//         *      fa = q* # ( 0, 0, 1) # q  - |a|      for gravity
//         *      fb = q* # (-1, 0, 0) # q* - |a x m|  for e = |gravity x magnetic field|
//         */
//        @Suppress("SpellCheckingInspection")
//        internal fun orthogonalObjectiveFunction(q: Quaternion, a: Vector, m: Vector): ObjectiveFunction {
//            val e = Vector.cross(a, m).normalize()
//            return ObjectiveFunction(
//                f1 = q.m31 - a.x,
//                f2 = q.m32 - a.y,
//                f3 = q.m33 - a.z,
//                f4 = -q.m11 - e.x,
//                f5 = -q.m12 - e.y,
//                f6 = -q.m13 - e.z,
//            )
//        }
//
//        @Suppress("SpellCheckingInspection")
//        internal fun orthogonalJacobian(q: Quaternion): Jacobian {
//            val qx2 = 2 * q.x
//            val qy2 = 2 * q.y
//            val qz2 = 2 * q.z
//            val qs2 = 2 * q.s
//            val qx4 = 4 * q.x
//            val qy4 = 4 * q.y
//            val qz4 = 4 * q.z
//
//            return Jacobian(
//                df1ds = -qy2,
//                df1dx = qz2,
//                df1dy = -qs2,
//                df1dz = qx2,
//                df2ds = qx2,
//                df2dx = qs2,
//                df2dy = qz2,
//                df2dz = qy2,
//                df3ds = 0.0,
//                df3dx = -qx4,
//                df3dy = -qy4,
//                df3dz = 0.0,
//                df4ds = 0.0,
//                df4dx = 0.0,
//                df4dy = qy4,
//                df4dz = qz4,
//                df5ds = qz2,
//                df5dx = +qy2,
//                df5dy = +qx2,
//                df5dz = qs2,
//                df6ds = +qy2,
//                df6dx = +qz2,
//                df6dy = +qx2,
//                df6dz = qs2,
//            )
//        }
//
