package com.stho.myorientation.library.graphics

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.stho.myorientation.library.algebra.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Render a cube with OpenGL
 *
 * see:
 *      https://developer.android.com/guide/topics/graphics/opengl
 *      http://www.intransitione.com/blog/create-a-spinning-cube-with-opengl-es-and-android/
 *
 */
class CubeRenderer : GLSurfaceView.Renderer {

    private lateinit var triangle: Triangle
    private lateinit var pointer: Pointer
    private lateinit var cube: Cube

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)


    @Volatile
    var orientation: Quaternion = Quaternion.default

    @Volatile
    var alpha: Float = 0f

    @Volatile
    var beta: Float = 0f

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {

        // Set the background frame color: transparent 0, otherwise 1
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Define a camera view
        // Set the camera position (View matrix)
        android.opengl.Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -7f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        triangle = Triangle()
        pointer = Pointer()
        cube = Cube()

        /*
           * By default, OpenGL enables features that improve quality
           * but reduce performance. One might want to tweak that
           * especially on software renderer.
           */
        //GLES20.glDisable(GL10.GL_DITHER);

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        //GLES20.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        // Define a projection
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        // Mind, because eye distance is set to z=-7 using Matrix.setLookAtM(..., -7, ...) ...
        // ... all edges will be visible: their distance 7 + edge.z ranges between 6 and 8
        val ratio: Float = 2 * width.toFloat() / height.toFloat()
        android.opengl.Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -2f, 2f, 5f, 9f)
    }

    override fun onDrawFrame(gl: GL10) {

       gl.apply {

           //glShadeModel(GL10.GL_SMOOTH);

           // enable face culling feature
           glEnable(GL10.GL_CULL_FACE)

           // specify which faces to not draw
           glCullFace(GL10.GL_BACK)

           // Enable depth test
           glEnable(GLES20.GL_DEPTH_TEST);

           // Accept fragment if it closer to the camera than the former one
           glDepthFunc(GLES20.GL_LESS);
       }


        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val m1 = matrixMultiply(orientation.toRotationMatrixOpenGL(), getRotationMatrixFor(alpha, beta))
        val m2 = matrixMultiply(viewMatrix, m1)
        val scratch = matrixMultiply(projectionMatrix, m2)

        cube.draw(scratch)
        pointer.draw(scratch)
        triangle.draw(scratch)
    }

    companion object {
        private fun getRotationMatrixFor(alpha: Float, beta: Float): FloatArray =
            FloatArray(16).also { matrix ->
                android.opengl.Matrix.setIdentityM(matrix, 0)
                android.opengl.Matrix.rotateM(matrix, 0, alpha, 0f, 1f, 0f)
                android.opengl.Matrix.rotateM(matrix, 0, beta, 1f, 0f, 0f)
            }

        private fun matrixMultiply(lhs: FloatArray, rhs: FloatArray): FloatArray =
            FloatArray(16).also { matrix ->
                android.opengl.Matrix.multiplyMM(matrix, 0, lhs, 0, rhs, 0)
            }
    }
}

/**
 * Returns M2 * M1 with
 *  M1 = M(q0).transpose()
 *  M2 = M(OpenGL -> Device) = (-1, 1, -1, 0) : x -> -x, y -> y, z -> -z for x and z directions are opposite
 *  q0 := q(y -> -y) for rotation angle around y is opposite
 *
 *  v_earth = M(q) * v_device
 *  v_device = M
 *  The rotation around y is in opposite direction:
 */
private fun Quaternion.toRotationMatrixOpenGL(): FloatArray =
    Quaternion(s = s, x = x, y = -y, z = z).run {
        FloatArray(16).also {
            it[0] = -m11.toFloat()
            it[1] = -m21.toFloat()
            it[2] = -m31.toFloat()
            it[3] = 0.0f
            it[4] = m12.toFloat()
            it[5] = m22.toFloat()
            it[6] = m32.toFloat()
            it[7] = 0.0f
            it[8] = -m13.toFloat()
            it[9] = -m23.toFloat()
            it[10] = -m33.toFloat()
            it[11] = 0.0f
            it[12] = 0.0f
            it[13] = 0.0f
            it[14] = 0.0f
            it[15] = 1.0f
        }
    }
