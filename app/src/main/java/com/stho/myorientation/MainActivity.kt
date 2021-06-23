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
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
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

    override fun onResume() {
        super.onResume()

        try {
            executeHandlerToUpdateOrientation(viewModel.options.updateOrientationDelay)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_statistics -> onStatistics()
            R.id.action_settings -> onSettings()
            R.id.action_cube -> onCube()
            R.id.action_documentation -> onDocumentation()
            android.R.id.home -> onHome()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onHome(): Boolean {
        findNavController().popBackStack()
        return true
    }


    private fun onStatistics(): Boolean {
        findNavController().navigate(R.id.action_global_StatisticsFragment)
        return true
    }

    private fun onSettings(): Boolean {
        findNavController().navigate(R.id.action_global_SettingsFragment)
        return true
    }

    private fun onCube(): Boolean {
        findNavController().navigate(R.id.action_global_CubeFragment)
        return true
    }

    private fun onDocumentation(): Boolean {
        findNavController().navigate(R.id.action_global_DocumentationFragment)
        return true
    }

    private fun findNavController() =
        Navigation.findNavController(this, R.id.nav_host_fragment)


    private fun observeIsActive(isActive: Boolean) {
        val fab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab)
        fab.setImageResource(if (isActive) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun observeMethod(method: Method) {
        orientationFilter = viewModel.createFilter()
        orientationSensorListener.setFilter(orientationFilter)
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

    internal fun showSnackbar(message: String) {
        val container: View = findViewById<View>(R.id.toolbar)
        Snackbar.make(container, message, 7000)
            .setBackgroundTint(getColor(R.color.design_default_color_secondary))
            .setTextColor(getColor(R.color.design_default_color_on_secondary))
            .show()
    }
}

