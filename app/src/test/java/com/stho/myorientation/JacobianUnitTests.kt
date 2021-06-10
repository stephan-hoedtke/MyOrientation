package com.stho.myorientation

import com.stho.myorientation.library.algebra.EulerAngles
import com.stho.myorientation.library.algebra.Quaternion
import com.stho.myorientation.library.algebra.Vector
import com.stho.myorientation.library.f2
import com.stho.myorientation.library.filter.MadgwickFilter
import org.junit.Test
import org.junit.Assert.assertEquals

class JacobianUnitTests : BaseUnitTestsHelper() {

    @Test
    fun objectiveFunction_isCorrect() {
        for (eulerAngles in list) {
            val q = super.quaternionForEulerAngles(eulerAngles)
            val i = q.inverse()
            val a = Vector(0.0, 0.0, 1.0).rotateBy(i).normalize()
            val m = Vector(0.0, 18.0, -44.0).rotateBy(i).normalize()
            val b = MadgwickFilter.flux(a, m)
            val f = MadgwickFilter.objectiveFunction(q, b.y, b.z, a, m)
            assertEquals("f1", 0.0, f.f1, EPS_E6)
            assertEquals("f2", 0.0, f.f2, EPS_E6)
            assertEquals("f3", 0.0, f.f3, EPS_E6)
            assertEquals("f4", 0.0, f.f4, EPS_E6)
            assertEquals("f5", 0.0, f.f5, EPS_E6)
            assertEquals("f6", 0.0, f.f6, EPS_E6)
        }
    }

//    @Test
//    fun orthogonalObjectiveFunction_isCorrect() {
//        for (eulerAngles in list) {
//            val q = super.quaternionForEulerAngles(eulerAngles)
//            val i = q.inverse()
//            val a = Vector(0.0, 0.0, 1.0).rotateBy(i).normalize()
//            val m = Vector(0.0, 18.0, -44.0).rotateBy(i).normalize()
//            val e = Vector.cross(a, m).normalize()
//            val f = MadgwickFilter.orthogonalObjectiveFunction(q, a, e)
//            assertEquals("f1", 0.0, f.f1, EPS_E6)
//            assertEquals("f2", 0.0, f.f2, EPS_E6)
//            assertEquals("f3", 0.0, f.f3, EPS_E6)
//            assertEquals("f4", 0.0, f.f4, EPS_E6)
//            assertEquals("f5", 0.0, f.f5, EPS_E6)
//            assertEquals("f6", 0.0, f.f6, EPS_E6)
//        }
//    }


    @Test
    fun jacobianMatrix_withoutDelta_isCorrect() {
        for (eulerAngles in list) {
            val q = super.quaternionForEulerAngles(eulerAngles)
            val i = q.inverse()
            val a = Vector(0.0, 0.0, 1.0).rotateBy(i)
            val m = Vector(0.0, 18.0, -44.0).rotateBy(i)
            jacobianMatrix_isCorrect(q, a.normalize(), m.normalize())
        }
    }

    @Test
    fun jacobianMatrix_withDelta_isCorrect() {
        for (eulerAngles in list) {
            val q = super.quaternionForEulerAngles(eulerAngles)
            val delta = Vector(0.1, 0.1, 0.1)
            val i = q.inverse()
            val a = Vector(0.0, 0.0, 1.0).rotateBy(i) + delta
            val m = Vector(0.0, 18.0, -44.0).rotateBy(i) + delta
            jacobianMatrix_isCorrect(q, a.normalize(), m.normalize())
        }
    }


    @Test
    fun gradient_withoutDelta_isCorrect() {
        for (eulerAngles in list) {
            val q = super.quaternionForEulerAngles(eulerAngles)
            val delta = Vector(0.1, 0.1, 0.1)
            val i = q.inverse()
            val a = Vector(0.0, 0.0, 1.0).rotateBy(i)
            val m = Vector(0.0, 18.0, -44.0).rotateBy(i)
            gradient_isCorrect(q, a.normalize(), m.normalize())
        }
    }

    @Test
    fun gradient_withDelta_isCorrect() {
        for (eulerAngles in list) {
            println("Angles: azimuth=${eulerAngles.azimuth.f2()} pitch=${eulerAngles.pitch.f2()} roll=${eulerAngles.roll.f2()}")
            val q = super.quaternionForEulerAngles(eulerAngles)
            val delta = Vector(0.1, 0.1, 0.1)
            val i = q.inverse()
            val a = Vector(0.0, 0.0, 1.0).rotateBy(i) + delta
            val m = Vector(0.0, 18.0, -44.0).rotateBy(i) + delta
            gradient_isCorrect(q, a.normalize(), m.normalize())
        }
    }

    private fun gradient_isCorrect(q: Quaternion, a: Vector, m: Vector) {
        val b = MadgwickFilter.flux(a, m)
        val jacobian = MadgwickFilter.jacobian(q, b.y, b.z)
        val f = MadgwickFilter.objectiveFunction(q, b.y, b.z, a, m)
        val gs = 2 * (f.f1 * jacobian.df1ds + f.f2 * jacobian.df2ds + f.f3 * jacobian.df3ds + f.f4 * jacobian.df4ds + f.f5 * jacobian.df5ds + f.f6 * jacobian.df6ds)
        val gx = 2 * (f.f1 * jacobian.df1dx + f.f2 * jacobian.df2dx + f.f3 * jacobian.df3dx + f.f4 * jacobian.df4dx + f.f5 * jacobian.df5dx + f.f6 * jacobian.df6dx)
        val gy = 2 * (f.f1 * jacobian.df1dy + f.f2 * jacobian.df2dy + f.f3 * jacobian.df3dy + f.f4 * jacobian.df4dy + f.f5 * jacobian.df5dy + f.f6 * jacobian.df6dy)
        val gz = 2 * (f.f1 * jacobian.df1dz + f.f2 * jacobian.df2dz + f.f3 * jacobian.df3dz + f.f4 * jacobian.df4dz + f.f5 * jacobian.df5dz + f.f6 * jacobian.df6dz)


        val gradient = MadgwickFilter.gradientAsJacobianTimesObjectiveFunction(q, b.y, b.z, a, m)
        assertEquals("jacobian.s", gs, gradient.s, EPS_E4)
        assertEquals("jacobian.s", gx, gradient.x, EPS_E4)
        assertEquals("jacobian.s", gy, gradient.y, EPS_E4)
        assertEquals("jacobian.s", gz, gradient.z, EPS_E4)
    }

    private fun jacobianMatrix_isCorrect(q: Quaternion, a: Vector, m: Vector) {
        val b = MadgwickFilter.flux(a, m)
        val jacobian = MadgwickFilter.jacobian(q, b.y, b.z)
        val h = 0.000000001
        val ds = difference(q, a, m, Quaternion(s = h, x = 0.0, y = 0.0, z = 0.0))
        val df1ds = ds.f1 / h
        val df2ds = ds.f2 / h
        val df3ds = ds.f3 / h
        val df4ds = ds.f4 / h
        val df5ds = ds.f5 / h
        val df6ds = ds.f6 / h

        val dx = difference(q, a, m, Quaternion(s = 0.0, x = h, y = 0.0, z = 0.0))
        val df1dx = dx.f1 / h
        val df2dx = dx.f2 / h
        val df3dx = dx.f3 / h
        val df4dx = dx.f4 / h
        val df5dx = dx.f5 / h
        val df6dx = dx.f6 / h

        val dy = difference(q, a, m, Quaternion(s = 0.0, x = 0.0, y = h, z = 0.0))
        val df1dy = dy.f1 / h
        val df2dy = dy.f2 / h
        val df3dy = dy.f3 / h
        val df4dy = dy.f4 / h
        val df5dy = dy.f5 / h
        val df6dy = dy.f6 / h

        val dz = difference(q, a, m, Quaternion(s = 0.0, x = 0.0, y = 0.0, z = h))
        val df1dz = dz.f1 / h
        val df2dz = dz.f2 / h
        val df3dz = dz.f3 / h
        val df4dz = dz.f4 / h
        val df5dz = dz.f5 / h
        val df6dz = dz.f6 / h

        assertEquals("df1/ds", df1ds, jacobian.df1ds, EPS_E6)
        assertEquals("df2/ds", df2ds, jacobian.df2ds, EPS_E6)
        assertEquals("df3/ds", df3ds, jacobian.df3ds, EPS_E6)
        assertEquals("df4/ds", df4ds, jacobian.df4ds, EPS_E6)
        assertEquals("df5/ds", df5ds, jacobian.df5ds, EPS_E6)
        assertEquals("df6/ds", df6ds, jacobian.df6ds, EPS_E6)

        assertEquals("df1/dx", df1dx, jacobian.df1dx, EPS_E6)
        assertEquals("df2/dx", df2dx, jacobian.df2dx, EPS_E6)
        assertEquals("df3/dx", df3dx, jacobian.df3dx, EPS_E6)
        assertEquals("df4/dx", df4dx, jacobian.df4dx, EPS_E6)
        assertEquals("df5/dx", df5dx, jacobian.df5dx, EPS_E6)
        assertEquals("df6/dx", df6dx, jacobian.df6dx, EPS_E6)

        assertEquals("df1/dy", df1dy, jacobian.df1dy, EPS_E6)
        assertEquals("df2/dy", df2dy, jacobian.df2dy, EPS_E6)
        assertEquals("df3/dy", df3dy, jacobian.df3dy, EPS_E6)
        assertEquals("df4/dy", df4dy, jacobian.df4dy, EPS_E6)
        assertEquals("df5/dy", df5dy, jacobian.df5dy, EPS_E6)
        assertEquals("df6/dy", df6dy, jacobian.df6dy, EPS_E6)

        assertEquals("df1/dz", df1dz, jacobian.df1dz, EPS_E6)
        assertEquals("df2/dz", df2dz, jacobian.df2dz, EPS_E6)
        assertEquals("df3/dz", df3dz, jacobian.df3dz, EPS_E6)
        assertEquals("df4/dz", df4dz, jacobian.df4dz, EPS_E6)
        assertEquals("df5/dz", df5dz, jacobian.df5dz, EPS_E6)
        assertEquals("df6/dz", df6dz, jacobian.df6dz, EPS_E6)
    }

    private fun difference(q: Quaternion, a: Vector, m: Vector, delta: Quaternion): MadgwickFilter.ObjectiveFunction {
        val b = MadgwickFilter.flux(a, m)
        val p = q + delta
        val fq = MadgwickFilter.objectiveFunction(q, b.y, b.z, a, m)
        val fp = MadgwickFilter.objectiveFunction(p, b.y, b.z, a, m)
        return MadgwickFilter.ObjectiveFunction(
            fp.f1 - fq.f1,
            fp.f2 - fq.f2,
            fp.f3 - fq.f3,
            fp.f4 - fq.f4,
            fp.f5 - fq.f5,
            fp.f6 - fq.f6,
        )
    }

    private val list =ArrayList<EulerAngles>().apply {
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 0.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 0.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 10.0, 0.0))
        add(EulerAngles.fromAzimuthPitchRoll(0.0, 0.0, 20.0))
        add(EulerAngles.fromAzimuthPitchRoll(30.0, 10.0, 20.0))
        add(EulerAngles.fromAzimuthPitchRoll(-30.0, 60.0, -70.0))
    }
}