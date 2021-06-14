package com.stho.myorientation.library.graphics

import com.stho.myorientation.library.algebra.RotationMatrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10



class Cube : BaseShape() {

    // Coordinates:
    // TOP+LEFT+FRONT --> X=1, Y=1, Z=-1
    // TOP+LEFT-BACK --> X=1, Y=1, Z=1
    // BOTTOM+RIGHT+FRONT --> X=-1, Y=-1, Z=-1

    /**
     * The eight edges of the cube
     */
    private val vertices = floatArrayOf(
        // Front (z=-1) --> red
        +1f, +1f, -1f,
        +1f, -1f, -1f,
        -1f, -1f, -1f,
        -1f, +1f, -1f,
        // Right (x=-1)
        -1f, -1f, -1f,
        -1f, -1f, +1f,
        -1f, +1f, +1f,
        -1f, +1f, -1f,
        // Top (y=+1)
        +1f, +1f, +1f,
        +1f, +1f, -1f,
        -1f, +1f, -1f,
        -1f, +1f, +1f,
        // Bottom (y=-1)
        +1f, -1f, +1f,
        -1f, -1f, +1f,
        -1f, -1f, -1f,
        +1f, -1f, -1f,
        // Left (x=+1)
        +1f, +1f, +1f,
        +1f, -1f, +1f,
        +1f, -1f, -1f,
        +1f, +1f, -1f,
        // Back (z=+1)
        +1f, +1f, +1f,
        -1f, +1f, +1f,
        -1f, -1f, +1f,
        +1f, -1f, +1f,
    )

    private val colors = floatArrayOf(
        // Front --> red
        0.8f,  0.0f,  0.0f,  1.0f,
        0.8f,  0.1f,  0.1f,  1.0f,
        0.8f,  0.2f,  0.2f,  1.0f,
        0.8f,  0.3f,  0.3f,  1.0f,
        // Right --> yellow
        1.0f,  1.0f,  0.0f,  1.0f,
        1.0f,  1.0f,  0.0f,  1.0f,
        1.0f,  1.0f,  0.0f,  1.0f,
        1.0f,  1.0f,  0.0f,  1.0f,
        // Top --> blue
        0.0f,  0.0f,  0.8f,  1.0f,
        0.0f,  0.0f,  0.8f,  1.0f,
        0.0f,  0.0f,  0.8f,  1.0f,
        0.0f,  0.0f,  0.8f,  1.0f,
        // Bottom -> green
        0.0f,  0.6f,  0.0f,  1.0f,
        0.0f,  0.6f,  0.0f,  1.0f,
        0.0f,  0.6f,  0.0f,  1.0f,
        0.0f,  0.6f,  0.0f,  1.0f,
        // Left --> white
        1.0f,  1.0f,  1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,  1.0f,
        // Back --> orange: RGB=255,94,19
        1.0f,  0.369f,  0.08f,  1.0f,
        1.0f,  0.369f,  0.18f,  1.0f,
        1.0f,  0.469f,  0.08f,  1.0f,
        1.0f,  0.469f,  0.18f,  1.0f,
    )

    private val indices = shortArrayOf(
        // front
        0, 1, 2, 0, 2, 3,
        // right
        4, 5, 6, 4, 6, 7,
        // top
        8, 9, 10, 8, 10, 11,
        // bottom
        12, 13, 14, 12, 14, 15,
        // left
        16, 17, 18, 16, 18, 19,
        // back
        20, 21, 22, 20, 22, 23,
     )

    private val vertexBuffer: FloatBuffer by lazy {
        BaseShape.asFloatBuffer(vertices.size, vertices)
    }

    private val colorBuffer: FloatBuffer by lazy {
        BaseShape.asFloatBuffer(colors.size, colors)
    }

    private val indexBuffer: ShortBuffer by lazy {
        BaseShape.asShortBuffer(indices.size, indices)
    }

    fun draw(rotationMatrix: FloatArray) {
        super.draw(36, vertexBuffer, colorBuffer, indexBuffer, rotationMatrix)
    }
}

