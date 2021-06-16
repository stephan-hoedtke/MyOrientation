package com.stho.myorientation

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.stho.myorientation.library.filter.MadgwickFilter
import com.stho.myorientation.library.filter.SeparatedCorrectionFilter

class OptionsManager(val context: Context) {

    fun save(viewModel: MainViewModel) {
        val preferences = context.getSharedPreferences("Options", MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString(METHOD, viewModel.method.toString())

        viewModel.options.apply {
            editor.putString(MADGWICK_MODE, madgwickMode.toString())
            editor.putString(SEPARATED_CORRECTION_MODE, separatedCorrectionMode.toString())
            editor.putDouble(ACCELERATION_FACTOR, accelerationFactor)
            editor.putDouble(TIME_CONSTANT, timeConstant)
            editor.putDouble(FILTER_COEFFICIENT, filterCoefficient)
            editor.putDouble(VARIANCE_ACCELERATION, varianceAccelerometer)
            editor.putDouble(VARIANCE_MAGNETOMETER, varianceMagnetometer)
            editor.putDouble(VARIANCE_GYROSCOPE, varianceGyroscope)
            editor.putBoolean(SHOW_ACCELEROMETER_MAGNETOMETER_FILTER, showAccelerometerMagnetometerFilter)
            editor.putBoolean(SHOW_ROTATION_VECTOR_FILTER, showRotationVectorFilter)
            editor.putBoolean(SHOW_MADGWICK_FILTER, showMadgwickFilter)
            editor.putBoolean(SHOW_COMPLEMENTARY_FILTER, showComplementaryFilter)
            editor.putBoolean(SHOW_SEPARATED_CORRECTION_FILTER, showSeparatedCorrectionFilter)
            editor.putBoolean(SHOW_EXTENDED_COMPLEMENTARY_FILTER, showExtendedComplementaryFilter)
            editor.putBoolean(SHOW_KALMAN_FILTER, showKalmanFilter)
        }
        editor.apply()
    }

    fun load(viewModel: MainViewModel) {
        val preferences = context.getSharedPreferences("Options", MODE_PRIVATE)

        viewModel.method = preferences.parseMethod(METHOD, viewModel.method)
        viewModel.touch(viewModel.options.apply {
            madgwickMode =
                preferences.parseMadgwickMode(MADGWICK_MODE, madgwickMode)
            separatedCorrectionMode =
                preferences.parseSeparatedCorrectionMode(SEPARATED_CORRECTION_MODE, separatedCorrectionMode)
            accelerationFactor =
                preferences.getDouble(ACCELERATION_FACTOR, accelerationFactor)
            timeConstant =
                preferences.getDouble(TIME_CONSTANT, timeConstant)
            filterCoefficient =
                preferences.getDouble(FILTER_COEFFICIENT, filterCoefficient)
            varianceAccelerometer =
                preferences.getDouble(VARIANCE_ACCELERATION, varianceAccelerometer)
            varianceMagnetometer =
                preferences.getDouble(VARIANCE_MAGNETOMETER, varianceMagnetometer)
            varianceGyroscope =
                preferences.getDouble(VARIANCE_GYROSCOPE, varianceGyroscope)
            showAccelerometerMagnetometerFilter =
                preferences.getBoolean(SHOW_ACCELEROMETER_MAGNETOMETER_FILTER, showAccelerometerMagnetometerFilter)
            showRotationVectorFilter =
                preferences.getBoolean(SHOW_ROTATION_VECTOR_FILTER, showRotationVectorFilter)
            showMadgwickFilter =
                preferences.getBoolean(SHOW_MADGWICK_FILTER, showMadgwickFilter)
            showComplementaryFilter =
                preferences.getBoolean(SHOW_COMPLEMENTARY_FILTER, showComplementaryFilter)
            showSeparatedCorrectionFilter =
                preferences.getBoolean(SHOW_SEPARATED_CORRECTION_FILTER, showSeparatedCorrectionFilter)
            showExtendedComplementaryFilter =
                preferences.getBoolean(SHOW_EXTENDED_COMPLEMENTARY_FILTER, showExtendedComplementaryFilter)
            showKalmanFilter =
                preferences.getBoolean(SHOW_KALMAN_FILTER, showKalmanFilter)
        })
    }


    companion object {
        private const val METHOD = "Method"
        private const val MADGWICK_MODE = "MadgwickMode"
        private const val SEPARATED_CORRECTION_MODE = "SeparatedCorrectionMode"
        private const val ACCELERATION_FACTOR = "AccelerationFactor"
        private const val TIME_CONSTANT = "TimeConstant"
        private const val FILTER_COEFFICIENT = "FilterCoefficient"
        private const val VARIANCE_ACCELERATION = "VarianceAccelerometer"
        private const val VARIANCE_MAGNETOMETER = "VarianceMagnetometer"
        private const val VARIANCE_GYROSCOPE = "VarianceGyroscope"
        private const val SHOW_ACCELEROMETER_MAGNETOMETER_FILTER = "ShowAccelerometerMagnetometerFilter"
        private const val SHOW_ROTATION_VECTOR_FILTER = "ShowRotationVectorFilter"
        private const val SHOW_MADGWICK_FILTER = "ShowMadgwickFilter"
        private const val SHOW_COMPLEMENTARY_FILTER = "ShowComplementaryFilter"
        private const val SHOW_SEPARATED_CORRECTION_FILTER = "ShowSeparatedCorrectionFilter"
        private const val SHOW_EXTENDED_COMPLEMENTARY_FILTER = "ShowExtendedComplementaryFilter"
        private const val SHOW_KALMAN_FILTER = "ShowKalmanFilter"

        private fun parseMethod(str: String?, defaultValue: Method): Method =
            try {
                if (str.isNullOrBlank()) {
                    defaultValue
                } else {
                    Method.valueOf(str)
                }
            } catch (ex: Exception) {
                defaultValue
            }

        private fun parseMadgwickMode(str: String?, defaultValue: MadgwickFilter.Mode): MadgwickFilter.Mode =
            try {
                if (str.isNullOrBlank()) {
                    defaultValue
                } else {
                    MadgwickFilter.Mode.valueOf(str)
                }
            } catch (ex: Exception) {
                defaultValue
            }

        private fun parseSeparatedCorrectionMode(str: String?, defaultValue: SeparatedCorrectionFilter.Mode): SeparatedCorrectionFilter.Mode =
            try {
                if (str.isNullOrBlank()) {
                    defaultValue
                } else {
                    SeparatedCorrectionFilter.Mode.valueOf(str)
                }
            } catch (ex: Exception) {
                defaultValue
            }
    }
}

fun SharedPreferences.Editor.putDouble(key: String, value: Double) {
    this.putFloat(key, value.toFloat())
}

fun SharedPreferences.getDouble(key: String, defaultValue: Double): Double {
    return getFloat(key, defaultValue.toFloat()).toDouble()
}

fun SharedPreferences.parseMethod(key: String, defaultValue: Method): Method {
    val value = this.getString(key, "")
    return try {
        if (value.isNullOrBlank()) {
            defaultValue
        } else {
            Method.valueOf(value)
        }
    } catch (ex: Exception) {
        defaultValue
    }
}

fun SharedPreferences.parseMadgwickMode(key: String, defaultValue: MadgwickFilter.Mode): MadgwickFilter.Mode {
    val value = getString(key, "")
    return try {
        if (value.isNullOrBlank()) {
            defaultValue
        } else {
            MadgwickFilter.Mode.valueOf(value)
        }
    } catch (ex: Exception) {
        defaultValue
    }
}

fun SharedPreferences.parseSeparatedCorrectionMode(key: String, defaultValue: SeparatedCorrectionFilter.Mode): SeparatedCorrectionFilter.Mode {
    val value = getString(key, "")
    return try {
        if (value.isNullOrBlank()) {
            defaultValue
        } else {
            SeparatedCorrectionFilter.Mode.valueOf(value)
        }
    } catch (ex: Exception) {
        defaultValue
    }
}

