package com.stho.myorientation.library

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.stho.myorientation.Measurements
import com.stho.myorientation.Repository
import com.stho.myorientation.library.filter.OrientationFilter


class OrientationSensorListener(private val context: Context, private var filter: OrientationFilter, private val processorConsumptionMeter: ProcessorConsumptionMeter) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    internal fun setFilter(filter: OrientationFilter) {
        context.display?.let { filter.deviceRotation = it.rotation }
        this.filter = filter
    }

    internal fun onResume() {
        filter.reset()
        context.display?.let {
            filter.deviceRotation = it.rotation
        }
        initializeRotationVectorSensor()
        initializeMagneticFieldSensor()
        initializeAccelerationSensor()
        initializeGyroscopeSensor()
    }

    internal fun onPause() {
        removeSensorListeners()
    }

    private fun initializeMagneticFieldSensor() {
        val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun initializeAccelerationSensor() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun initializeRotationVectorSensor() {
        val rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotation != null) {
            sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    private fun initializeGyroscopeSensor() {
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (gyro != null) {
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME)
        }
    }


    private fun removeSensorListeners() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // We don't care
    }

    val proximitySensorListener: SensorEventListener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                // More code goes here
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
        }
    }


    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor?.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                Repository.instance.recordMeasurement(Measurements.Type.Magnetometer, event.values)
                filter.updateReadings(Measurements.Type.Magnetometer, event.values)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                Repository.instance.recordMeasurement(Measurements.Type.Accelerometer, event.values)
                filter.updateReadings(Measurements.Type.Accelerometer, event.values)
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                filter.updateReadings(Measurements.Type.RotationVector, event.values)
            }
            Sensor.TYPE_GYROSCOPE -> {
                processorConsumptionMeter.start()
                Repository.instance.recordMeasurement(Measurements.Type.Gyroscope, event.values)
                filter.updateReadings(Measurements.Type.Gyroscope, event.values)
                processorConsumptionMeter.stop()
            }
        }
    }
}