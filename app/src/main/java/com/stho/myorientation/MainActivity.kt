package com.stho.myorientation

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.stho.myorientation.library.OrientationSensorListener
import com.stho.myorientation.library.ProcessorConsumptionMeter
import com.stho.myorientation.library.filter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var viewModel: MainViewModel
    private lateinit var orientationFilter: IOrientationFilter
    private lateinit var orientationSensorListener: OrientationSensorListener

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { _ ->
            viewModel.startStop()
        }

        handler = Handler(Looper.getMainLooper())
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        orientationFilter = AccelerationMagnetometerFilter(viewModel.options)
        orientationSensorListener = OrientationSensorListener(this, orientationFilter, processorConsumptionMeter)

        viewModel.isActiveLD.observe(this, { isActive -> observeIsActive(isActive) })
        viewModel.accelerationFactorLD.observe(this, { _ -> viewModel.reset() })
        viewModel.timeConstantLD.observe(this, { _ -> viewModel.reset() })
        viewModel.methodLD.observe(this, { method -> observeMethod(method) })

        // TODO: google says its not good to lock the orientation, so why...
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private val processorConsumptionMeter by lazy {
        object : ProcessorConsumptionMeter {
            override fun start() {
                viewModel.startProcessorConsumptionMeasurement()
            }

            override fun stop() {
                viewModel.stopProcessorConsumption()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        OptionsManager(this).load(viewModel)
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

    override fun onStop() {
        super.onStop()
        OptionsManager(this).save(viewModel)
    }

    private fun observeIsActive(isActive: Boolean) {
        val fab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab)
        fab.setImageResource(if (isActive) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
    }

    private fun observeMethod(method: Method) {
        when (method) {
            Method.AccelerometerMagnetometer -> {
                orientationFilter = AccelerationMagnetometerFilter(viewModel.options)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Method.RotationVector -> {
                orientationFilter = RotationVectorFilter(viewModel.options)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Method.ComplementaryFilter -> {
                orientationFilter = ComplementaryFilter(viewModel.options)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Method.MadgwickFilter -> {
                orientationFilter = MadgwickFilter(viewModel.options)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Method.SeparatedCorrectionFilter -> {
                orientationFilter = SeparatedCorrectionFilter(viewModel.options)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Method.ExtendedComplementaryFilter -> {
                orientationFilter = ExtendedComplementaryFilter(viewModel.options)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Method.KalmanFilter -> {
                orientationFilter = KalmanFilter(viewModel.options)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Method.Composition -> {
                orientationFilter = CompositionFilter(viewModel.options)
                orientationSensorListener.setFilter(orientationFilter)
            }
            Method.Damped -> {
                // do nothing...
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

    private fun showSnackbar(message: String) {
        val container: View = findViewById<View>(R.id.toolbar)
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.design_default_color_secondary))
            .setTextColor(getColor(R.color.design_default_color_on_secondary))
            .show()
    }
}

