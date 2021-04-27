package com.stho.myorientation.library.algebra

data class Vector(val x: Double, val y: Double, val z: Double) {

    operator fun plus(v: Vector): Vector =
            Vector(x + v.x, y + v.y, z + v.z)

    operator fun minus(v: Vector): Vector =
            Vector(x - v.x, y - v.y, z - v.z)

    operator fun times(f: Double): Vector =
            Vector(x * f, y * f, z * f)

    fun rotateBy(q: Quaternion): Vector =
            rotate(this, u = q.v, s = q.s)

    fun rotateBy(m: FloatArray): Vector =
            multiply(m, this)

    fun inverseRotateBy(q: Quaternion): Vector =
            rotateBy(q.conjugate())

    companion object {
        fun fromEventValues(v: FloatArray): Vector {
            if (v.size < 3) {
                throw Exception("Invalid event values")
            }
            return Vector(x = v[0].toDouble(), y = v[1].toDouble(), z = v[2].toDouble())
        }

        fun dot(a: Vector, b: Vector): Double =
                a.x * b.x + a.y * b.y + a.z * b.z

        fun cross(a: Vector, b: Vector): Vector =
                Vector(
                        x = a.y * b.z - a.z * b.y,
                        y = a.z * b.x - a.x * b.z,
                        z = a.x * b.y - a.y * b.x
                )


        private fun rotate(v: Vector, q: Quaternion): Vector =
                rotate(v, u = q.v, s = q.s)

        /**
         * 2 dot(u,v) u + (s*s - dot(u,u)) v + 2s cross(u,v)
         */
        private fun rotate(v: Vector, u: Vector, s: Double): Vector {
            // see: https://gamedev.stackexchange.com/questions/28395/rotating-vector3-by-a-quaternion
            val f1: Double = 2.0 * dot(u, v)
            val f2: Double = s * s - dot(u, u)
            val f3: Double = 2.0 * s
            val v1: Vector = u * f1
            val v2: Vector = v * f2
            val v3: Vector = cross(u, v) * f3
            return v1 + v2 + v3
        }

        private fun multiply(m: FloatArray, v: Vector): Vector =
                Vector(
                        x = m[0] * v.x + m[1] * v.y + m[2] * v.z,
                        y = m[3] * v.x + m[4] * v.y + m[5] * v.z,
                        z = m[6] * v.x + m[7] * v.y + m[8] * v.z
                )
    }
}
