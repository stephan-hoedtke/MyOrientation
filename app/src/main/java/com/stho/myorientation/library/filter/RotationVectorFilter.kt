package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import com.stho.myorientation.Entries
import com.stho.myorientation.Measurements
import com.stho.myorientation.Repository
import com.stho.myorientation.library.*
import com.stho.myorientation.library.Acceleration


class RotationVectorFilter(accelerationFactor: Double = 0.7): AbstractOrientationFilter(Entries.Method.RotationVector, accelerationFactor), OrientationFilter {

    private val rotationVectorReading = FloatArray(5)

    override fun updateReadings(type: Measurements.Type, values: FloatArray) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (type) {
            Measurements.Type.RotationVector -> {
                System.arraycopy(values, 0, rotationVectorReading, 0, rotationVectorReading.size)
                updateOrientationAnglesFromRotationVector()
            }
        }
    }

    /**
     * Compute the three orientation angles based on the most recent readings from rotation vector
     */
    private fun updateOrientationAnglesFromRotationVector() {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVectorReading)
        val adjustedRotationMatrix = getAdjustedRotationMatrix(rotationMatrix)
        val angles = getOrientationAnglesFromRotationMatrix(adjustedRotationMatrix)
        onOrientationAnglesChanged(angles)
    }
}


