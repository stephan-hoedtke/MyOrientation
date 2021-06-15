package com.stho.myorientation.library.graphics

import java.nio.FloatBuffer

/**
 * See: https://developer.android.com/training/graphics/opengl/draw
 */
class Pointer : BaseShape() {

    private val vertices = floatArrayOf(
        // Back (z=-1)
        +0.2f, -0.9f, -1.01f,
        -0.2f, -0.9f, -1.01f,
        +0.0f, +0.9f, -1.01f,
        // Right (x=+1)
        +1.01f, -0.9f, +0.2f,
        +1.01f, -0.9f, -0.2f,
        +1.01f, +0.9f, +0.0f,
        // Left (x=-1)
        -1.01f, -0.9f, -0.2f,
        -1.01f, -0.9f, +0.2f,
        -1.01f, +0.9f, +0.0f,
    )

    private val colors = floatArrayOf(
        // orange
        1.0f,   0.369f,  0.08f,  1.0f,
        1.0f,   0.369f,  0.08f,  1.0f,
        0.95f,  0.369f,  0.08f,  1.0f,
        // yellow
        1.0f,   1.0f,  0.0f,  1.0f,
        1.0f,   1.0f,  0.0f,  1.0f,
        0.95f,  1.0f,  0.0f,  1.0f,
        // white -> blue
        1.0f,   1.0f,   1.0f,  1.0f,
        1.0f,   1.0f,   1.0f,  1.0f,
        0.95f,  0.95f,  1.0f,  1.0f,
    )

    private val vertexBuffer: FloatBuffer by lazy {
        BaseShape.asFloatBuffer(vertices.size, vertices)
    }

    private val colorBuffer: FloatBuffer by lazy {
        BaseShape.asFloatBuffer(colors.size, colors)
    }

    fun draw(rotationMatrix: FloatArray) {
         super.draw(9, vertexBuffer, colorBuffer, rotationMatrix)
    }
}

