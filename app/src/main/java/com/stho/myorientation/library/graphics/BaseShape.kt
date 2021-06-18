package com.stho.myorientation.library.graphics

import android.opengl.GLES20
import android.util.Log
import java.nio.*

/**
 * read: http://www.opengl-tutorial.org/beginners-tutorials/tutorial-2-the-first-triangle/
 *      GLES30
 *      Triangle
 *
 * read: https://developer.android.com/training/graphics/opengl/draw
 *      GLES20
 *      Triangle
 *      Square
 */
@Suppress("SameParameterValue")
abstract class BaseShape {

    /**
     * This matrix member variable provides a hook to manipulate
     * the coordinates of the objects that use this vertex shader
     * the matrix must be included as a modifier of gl_Position
     * Note that the "rotationMatrix" factor *must be first* in order
     * for the matrix multiplication product to be correct.
     */
    private val vertexShaderCode =
        // @formatter:off
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec4 vColor;" +
        "varying vec4 fragmentColor;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  fragmentColor = vColor;" +
        "}"
        // @formatter:on

    private val fragmentShaderCode =
        // @formatter:off
        "precision mediump float;" +
        "varying vec4 fragmentColor;" +
        "void main() {" +
        "  gl_FragColor = fragmentColor;" +
        "}"
        // @formatter:on

    // Set color with red, green, blue and alpha (opacity) values
    private val fixedColor = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private val program: Int =
        loadProgram(vertexShaderCode, fragmentShaderCode)

    @Suppress("unused", "UNUSED_VARIABLE")
    protected fun draw(count: Int, vertexes: FloatBuffer, rotationMatrix: FloatArray) {
        try {
            GLES20.glUseProgram(program)

            val positionHandle = assignVertexBuffer(program, vertexes)
            val colorHandle = assignColor(program, fixedColor)
            val matrixHandle = assignMatrix(program, rotationMatrix)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count)

            GLES20.glDisableVertexAttribArray(positionHandle)

        } catch (ex: Exception) {
            Log.d("ERROR", ex.toString())
        }
    }

    @Suppress("UNUSED_VARIABLE")
    protected fun draw(count: Int, vertexes: FloatBuffer, colors: FloatBuffer, rotationMatrix: FloatArray) {

        try {
            // Add program to OpenGL ES environment
            GLES20.glUseProgram(program)

            val positionHandle = assignVertexBuffer(program, vertexes)
            val colorHandle = assignColorBuffer(program, colors)
            val matrixHandle = assignMatrix(program, rotationMatrix)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count)

            GLES20.glDisableVertexAttribArray(colorHandle)
            GLES20.glDisableVertexAttribArray(positionHandle)
        }
        catch (ex: Exception) {
             Log.d("ERROR", ex.toString())
        }
    }

    @Suppress("UNUSED_VARIABLE")
    protected fun draw(count: Int, vertexes: FloatBuffer, colors: FloatBuffer, indices: ShortBuffer, rotationMatrix: FloatArray) {
        try {
            GLES20.glUseProgram(program)

            val positionHandle = assignVertexBuffer(program, vertexes)
            val colorHandle = assignColorBuffer(program, colors)
            val matrixHandle = assignMatrix(program, rotationMatrix)

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, count, GLES20.GL_UNSIGNED_SHORT, indices)

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(colorHandle)
            GLES20.glDisableVertexAttribArray(positionHandle)

        } catch (ex: Exception) {
            Log.d("ERROR", ex.toString())
        }
    }


    companion object {

        private const val COORDINATES_PER_VERTEX = 3 // 3 floats
        private const val COMPONENTS_PER_COLOR = 4 // 4 floats
        private const val VERTEX_STRIDE = COORDINATES_PER_VERTEX * Float.SIZE_BYTES // vec4 = 4 floats
        private const val COLOR_STRIDE = COMPONENTS_PER_COLOR * Float.SIZE_BYTES // vec4 = 4 floats

        /**
         * Loads the vertex and fragment shader, compiles them and links the program
         * Returns the program handle
         */
        private fun loadProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
            val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

            // create empty OpenGL ES Program
            return GLES20.glCreateProgram().also { program ->

                // add the vertex shader to program
                GLES20.glAttachShader(program, vertexShader)

                // add the fragment shader to program
                GLES20.glAttachShader(program, fragmentShader)

                // creates OpenGL ES program executables
                GLES20.glLinkProgram(program)

                verifyProgram(program)
            }
        }

        /**
         * Converts the vertexPositionBuffer (3 floats per vertex) into a an attribute pointer (vec4 per vertex)
         * And assigns it to the attribute vec4-variable "vPosition"
         * Returns the positionHandle
         */
        private fun assignVertexBuffer(program: Int, vertexBuffer: FloatBuffer): Int {
            // get handle to vertex shader's vPosition member
            return GLES20.glGetAttribLocation(program, "vPosition").also { positionHandle ->

                // Enable a handle to the triangle vertices
                GLES20.glEnableVertexAttribArray(positionHandle)

                // Prepare the triangle coordinate data: convert from 3 floats per vertex to vec4
                GLES20.glVertexAttribPointer(
                    positionHandle,
                    COORDINATES_PER_VERTEX,
                    GLES20.GL_FLOAT,
                    false,
                    VERTEX_STRIDE,
                    vertexBuffer
                )
            }
        }

        /**
         * Converts the colorBuffer (4 floats per vertex) into a an attribute pointer (vec4 per vertex)
         * And assigns it to the attribute vec4-variable "vColor"
         * Returns the colorHandle
         */
        private fun assignColorBuffer(program: Int, colorBuffer: FloatBuffer): Int {
            return GLES20.glGetAttribLocation(program, "vColor").also { colorHandle ->

                // Enable a handle to the triangle vertices
                GLES20.glEnableVertexAttribArray(colorHandle)

                // Prepare the triangle coordinate data: convert from 3 floats per vertex to vec4
                GLES20.glVertexAttribPointer(
                    colorHandle,
                    COMPONENTS_PER_COLOR,
                    GLES20.GL_FLOAT,
                    false,
                    COLOR_STRIDE,
                    colorBuffer
                )
            }
        }

        /**
         * Assigns a fixed RGBA-color value to the uniform vec4-variable: "vColor"
         * Returns the colorHandle
         */
        private fun assignColor(program: Int, color: FloatArray): Int {
            // get handle to fragment shader's vColor member
            return GLES20.glGetUniformLocation(program, "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }
        }

        /**
         * Assigns a rotation matrix to the uniform mat4-variable: "uMVPMatrix"
         */
        private fun assignMatrix(program: Int, rotationMatrix: FloatArray): Int {
            // get handle to shape's transformation matrix
            return GLES20.glGetUniformLocation(program, "uMVPMatrix").also { matrixHandle ->

                // Pass the projection and view transformation to the shader
                GLES20.glUniformMatrix4fv(matrixHandle, 1, false, rotationMatrix, 0)
            }
        }


        /**
         * create a vertex shader type (GLES20.GL_VERTEX_SHADER)
         * or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
         */
        private fun loadShader(type: Int, shaderCode: String): Int =
            GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
                verifyShader(shader)
            }

        internal fun asFloatBuffer(array: FloatArray): FloatBuffer =
            ByteBuffer.allocateDirect(array.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(array)
                    position(0)
                }
            }

        internal fun asShortBuffer(array: ShortArray): ShortBuffer =
            // (# of coordinate values * 2 bytes per short)
            ByteBuffer.allocateDirect(array.size * Short.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    put(array)
                    position(0)
                }
            }

        internal fun asByteBuffer(array: ByteArray): ByteBuffer =
            ByteBuffer.allocateDirect(array.size).apply {
                put(array)
                position(0)
            }

        private fun verifyShader(shader: Int) {
            val isShader = GLES20.glIsShader(shader)
            if (!isShader) {
                throw java.lang.Exception("Invalid shader")
            }
            val result = IntBuffer.allocate(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, result)
            val log = GLES20.glGetShaderInfoLog(shader)
            if (result.isNotTrue() || log.isNotBlank()) {
                throw Exception("Compile shader failed: $result $log")
            }
        }

        private fun verifyProgram(program: Int) {
            val isProgram = GLES20.glIsProgram(program)
            if (!isProgram) {
                throw Exception("Invalid program")
            }
            val result = IntBuffer.allocate(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,  result)
            val log = GLES20.glGetProgramInfoLog(program)
            if (result.isNotTrue() ||log.isNotBlank()) {
                throw Exception("Program link failed: $result $log")
            }
        }
    }
}

private fun IntBuffer.isNotTrue(): Boolean =
    this.get(0) != GLES20.GL_TRUE


