package com.stho.myorientation.library.graphics

import java.nio.FloatBuffer

/**
 * See: https://developer.android.com/training/graphics/opengl/draw
 */
class Triangle : BaseShape() {

    private var vertices = floatArrayOf( // in counterclockwise order:
        // @formatter:off
         -0.5f, -0.288675f, 1.01f,
          0.5f, -0.288675f, 1.01f,
          0.0f,  0.577350f, 1.01f,
        // @formatter:on
    )

    private var colors = floatArrayOf(
        // @formatter:off
        0.80f,  0.3f,  0.1f,  0.3f,
        0.80f,  0.1f,  0.3f,  0.3f,
        0.80f,  0.2f,  0.2f,  0.3f,
        // @formatter:on
    )

    private val vertexBuffer: FloatBuffer by lazy {
        BaseShape.asFloatBuffer(vertices)
    }

    private val colorBuffer: FloatBuffer by lazy {
        BaseShape.asFloatBuffer(colors)
    }

    fun draw(rotationMatrix: FloatArray) {
         super.draw(3, vertexBuffer, colorBuffer, rotationMatrix)
    }
}

