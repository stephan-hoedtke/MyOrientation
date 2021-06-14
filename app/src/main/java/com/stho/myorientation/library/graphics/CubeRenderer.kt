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

         // Calculate the projection and view transformation
        val scratch = FloatArray(16)

        android.opengl.Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // calculate a rotation matrix for the current orientation, given by the quaternion, which rotates from body to earth frame

        val q = orientation.inverse()
        val phi = 2 * Degree.arcCos(q.s)
        val x = q.x
        val y = q.y
        val z = q.z

        // additionally rotate by the angles defined by touch events
        android.opengl.Matrix.setRotateM(rotationMatrix, 0, alpha, 0f, 1f, 0f)
        android.opengl.Matrix.rotateM(rotationMatrix, 0, beta, 1f, 0f, 0f)

        if (x > 0.001 || y > 0.001 || z > 0.001) {
            android.opengl.Matrix.rotateM(
                rotationMatrix,
                0,
                phi.toFloat(),
                x.toFloat(),
                y.toFloat(),
                z.toFloat()
            )
        }

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        android.opengl.Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        cube.draw(scratch)
        triangle.draw(scratch)
    }
}