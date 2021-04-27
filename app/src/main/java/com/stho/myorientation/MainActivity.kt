package com.stho.myorientation

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.stho.myorientation.library.OrientationSensorListener
import com.stho.myorientation.library.filter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var viewModel: MainViewModel
    private lateinit var orientationFilter: OrientationFilter
    private lateinit var orientationSensorListener: OrientationSensorListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { _ ->
            viewModel.startStop()
        }

        // TODO: replace Handler()
        handler = Handler()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        orientationFilter = AccelerationMagnetometerFilter(viewModel.accelerationFactor, viewModel.timeConstant)
        orientationSensorListener = OrientationSensorListener(this, orientationFilter)

        viewModel.isActiveLD.observe(this, { isActive -> observeIsActive(isActive) })
        viewModel.accelerationFactorLD.observe(this, { _ -> reset() })
        viewModel.timeConstantLD.observe(this, { _ -> reset() })
        viewModel.methodLD.observe(this, { method -> observeMethod(method) })

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onResume() {
        super.onResume()

        try {
            executeHandlerToUpdateOrientation()
            executeHandlerToFuseSensors()
            executeHandlerToCleanupHistory()
            orientationSensorListener.onResume()
        } catch (ex: Exception) {
            showSnackbar(ex.toString())
        }
    }

    override fun onPause() {
        super.onPause()
        stopHandler()
        orientationSensorListener.onPause()
    }

    private fun observeIsActive(isActive: Boolean) {
        val fab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab)
        fab.setImageResource(if (isActive) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun observeMethod(method: Entries.Method) {
        when (method) {
            Entries.Method.AccelerometerMagnetometer -> {
                orientationFilter = AccelerationMagnetometerFilter(viewModel.accelerationFactor, viewModel.timeConstant)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Entries.Method.RotationVector -> {
                orientationFilter = RotationVectorFilter(viewModel.accelerationFactor)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Entries.Method.ComplementaryFilter -> {
                orientationFilter = ComplementaryFilter(viewModel.accelerationFactor, viewModel.filterCoefficient)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Entries.Method.KalmanFilter -> {
                orientationFilter = KalmanFilter(viewModel.accelerationFactor)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Entries.Method.Composition -> {
                orientationFilter = CompositionFilter(viewModel.accelerationFactor, viewModel.timeConstant, viewModel.filterCoefficient)
                orientationSensorListener.setFilter(orientationFilter)
            }
        }
    }

    private fun executeHandlerToUpdateOrientation(delayMillis: Long = 200) {
        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.Default).launch {
                    viewModel.onUpdateOrientation(orientationFilter.currentOrientation)
                }
                handler.postDelayed(this, delayMillis)
            }
        }
        handler.postDelayed(runnableCode, 100)
    }

    private fun executeHandlerToFuseSensors(delayMillis: Long = 100) {
        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.Default).launch {
                    orientationFilter.fuseSensors()
                }
                handler.postDelayed(this, delayMillis)
            }
        }
        handler.postDelayed(runnableCode, 100)
    }

    private fun executeHandlerToCleanupHistory(delayMillis: Long = 30000) {
        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.Default).launch {
                    viewModel.cleanupHistory()
                }
                handler.postDelayed(this, delayMillis)
            }
        }
        handler.postDelayed(runnableCode, 100)
    }

    private fun stopHandler() =
            handler.removeCallbacksAndMessages(null)

    private fun reset() {
        orientationFilter = AccelerationMagnetometerFilter(viewModel.accelerationFactor, viewModel.timeConstant)
        orientationSensorListener.setFilter(orientationFilter)
        viewModel.reset()
    }

    private fun showSnackbar(message: String) {
        val container: View = findViewById<View>(R.id.toolbar)
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.design_default_color_secondary))
                .setTextColor(getColor(R.color.design_default_color_on_secondary))
                .show()
    }
}

