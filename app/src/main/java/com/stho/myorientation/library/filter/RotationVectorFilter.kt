package com.stho.myorientation.library.filter

import android.hardware.SensorManager
import com.stho.myorientation.IFilterOptions
import com.stho.myorientation.Measurements
import com.stho.myorientation.Method
import com.stho.myorientation.library.algebra.RotationMatrix


class RotationVectorFilter(options: IFilterOptions): AbstractOrientationFilter(Method.RotationVector, options), IOrientationFilter {

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

    override fun reset() {
        // nothing to do
    }

    /**
     * Compute the three orientation angles based on the most recent readings from rotation vector
     */
    private fun updateOrientationAnglesFromRotationVector() {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVectorReading)
        val adjustedRotationMatrix = RotationMatrix.fromFloatArray(getAdjustedRotationMatrix(rotationMatrix))
        val orientation = adjustedRotationMatrix.toOrientation()
        super.onOrientationAnglesChanged(orientation)
    }
}


