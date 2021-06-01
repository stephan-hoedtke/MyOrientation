package com.stho.myorientation

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.stho.myorientation.databinding.FragmentMainBinding
import com.stho.myorientation.library.Formatter
import com.stho.myorientation.library.algebra.Orientation
import com.stho.myorientation.library.f0
import com.stho.myorientation.library.f2
import com.stho.myorientation.views.AbstractPlotView

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.mainView.setListener(object : AbstractPlotView.Listener {
            override fun onDoubleTap() {
                viewModel.reset()
            }

            override fun onSingleTap() {
                // ignore
            }

            override fun onZoom(f: Double) {
                viewModel.onZoom(f)
            }

            override fun onScroll(dx: Double, dy: Double) {
                viewModel.onScroll(dx, dy)
            }
        })
        binding.version.text = BuildConfig.VERSION_NAME;
        binding.gyroscopeView.setType(Measurements.Type.Gyroscope)
        binding.accelerometerView.setType(Measurements.Type.Accelerometer)
        binding.magnetometerView.setType(Measurements.Type.Magnetometer)
        binding.property.setOnClickListener { onSettings() }
        binding.method.setOnClickListener { onSettings() }
        binding.azimuth.setOnClickListener { onChangeProperty(Entries.Property.Azimuth) }
        binding.pitch.setOnClickListener { onChangeProperty(Entries.Property.Pitch) }
        binding.roll.setOnClickListener { onChangeProperty(Entries.Property.Roll) }
        binding.centerAzimuth.setOnClickListener { onChangeProperty(Entries.Property.CenterAzimuth) }
        binding.centerAltitude.setOnClickListener { onChangeProperty(Entries.Property.CenterAltitude) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.versionLD.observe(viewLifecycleOwner, { _ -> observeVersion() })
        viewModel.zoomLD.observe(viewLifecycleOwner, { zoom -> observeZoom(zoom) })
        viewModel.startTimeLD.observe(viewLifecycleOwner, { startTime -> observeStartTime(startTime) })
        viewModel.propertyLD.observe(viewLifecycleOwner, { property -> observeProperty(property) })
        viewModel.timeConstantLD.observe(viewLifecycleOwner, { timeConstant -> observeTimeConstant(timeConstant) })
        viewModel.filterCoefficientLD.observe(viewLifecycleOwner, { filterCoefficient -> observeFilterCoefficient(filterCoefficient) })
        viewModel.accelerationFactorLD.observe(viewLifecycleOwner, { accelerationFactor -> observeFactor(accelerationFactor) })
        viewModel.methodLD.observe(viewLifecycleOwner, { method -> observeMethod(method) })
        viewModel.orientationLD.observe(viewLifecycleOwner, { orientation -> observeOrientation(orientation) })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> onSettings();
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onSettings(): Boolean {
        findNavController().navigate(R.id.action_MainFragment_to_SettingsFragment)
        return true
    }

    private fun observeVersion() {
        binding.mainView.invalidate()
        binding.gyroscopeView.invalidate()
        binding.magnetometerView.invalidate()
        binding.accelerometerView.invalidate()
    }

    private fun observeZoom(zoom: Double) {
        binding.mainView.setZoom(zoom)
        binding.gyroscopeView.setZoom(zoom)
        binding.magnetometerView.setZoom(zoom)
        binding.accelerometerView.setZoom(zoom)
    }

    private fun observeStartTime(startTime: Double) {
        binding.mainView.setStartTime(startTime)
        binding.gyroscopeView.setStartTime(startTime)
        binding.magnetometerView.setStartTime(startTime)
        binding.accelerometerView.setStartTime(startTime)
    }

    private fun observeProperty(property: Entries.Property) {
        binding.mainView.setProperty(property);
        binding.property.text = property.toString()
        binding.property.setTextColor(getColorForProperty(property))
    }

    private fun getColorForProperty(property: Entries.Property): Int =
            resources.getColor(getColorResourceIdForProperty(property), null)

    private fun getColorResourceIdForProperty(property: Entries.Property): Int =
        when (property) {
            Entries.Property.Azimuth -> android.R.color.holo_red_light
            Entries.Property.Pitch -> android.R.color.holo_blue_bright
            Entries.Property.Roll -> android.R.color.holo_green_light
            Entries.Property.CenterAzimuth -> android.R.color.holo_orange_dark
            Entries.Property.CenterAltitude -> android.R.color.holo_blue_dark
        }

    private fun observeTimeConstant(timeConstant: Double) {
        binding.timeConstant.text = getString(R.string.label_name_value, "t", timeConstant.f2())
    }

    private fun observeFilterCoefficient(filterCoefficient: Double) {
        binding.filterCoefficient.text = getString(R.string.label_name_value, "f", filterCoefficient.f2())
    }
    private fun observeFactor(accelerationFactor: Double) {
        binding.accelerationFactor.text = getString(R.string.label_name_value, "a", accelerationFactor.f2())
    }

    private fun observeMethod(method: Entries.Method) {
        binding.method.text = method.toString()
        binding.method.setTextColor(getColorForMethod(method))
    }

    private fun getColorForMethod(method: Entries.Method): Int =
            when (method) {
                Entries.Method.AccelerometerMagnetometer -> Color.RED
                Entries.Method.RotationVector -> Color.YELLOW
                Entries.Method.KalmanFilter -> Color.rgb(0xFC, 0x0F, 0xC0);
                Entries.Method.ComplementaryFilter -> Color.CYAN
                Entries.Method.MadgwickFilter -> Color.GREEN
                Entries.Method.SeparatedCorrectionFilter -> Color.MAGENTA
                Entries.Method.ExtendedComplementaryFilter -> Color.BLUE
                Entries.Method.Composition -> Color.WHITE
                Entries.Method.Damped -> Color.GRAY
            }


    private fun observeOrientation(orientation: Orientation) {
        binding.azimuth.text = getString(R.string.label_value_degree, orientation.azimuth.f0())
        binding.pitch.text = getString(R.string.label_value_degree, orientation.pitch.f0())
        binding.roll.text = getString(R.string.label_value_degree, orientation.roll.f0())
        binding.centerAzimuth.text = getString(R.string.label_value_degree, orientation.centerAzimuth.f0())
        binding.centerAltitude.text = getString(R.string.label_value_degree, orientation.centerAltitude.f0())
    }

    private fun onChangeProperty(newProperty: Entries.Property) {
        viewModel.property = newProperty
    }
}

