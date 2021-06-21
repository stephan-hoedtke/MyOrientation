package com.stho.myorientation



import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.stho.myorientation.databinding.FragmentMainBinding
import com.stho.myorientation.library.algebra.Orientation
import com.stho.myorientation.library.f0
import com.stho.myorientation.library.f2
import com.stho.myorientation.library.f4
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
        binding.version.text = BuildConfig.VERSION_NAME
        binding.gyroscopeView.setType(Measurements.Type.Gyroscope)
        binding.accelerometerView.setType(Measurements.Type.Accelerometer)
        binding.magnetometerView.setType(Measurements.Type.Magnetometer)
        binding.property.setOnClickListener { onSettings() }
        binding.method.setOnClickListener { onSettings() }
        binding.azimuth.setOnClickListener { onChangeProperty(Property.Azimuth) }
        binding.pitch.setOnClickListener { onChangeProperty(Property.Pitch) }
        binding.roll.setOnClickListener { onChangeProperty(Property.Roll) }
        binding.centerAzimuth.setOnClickListener { onChangeProperty(Property.CenterAzimuth) }
        binding.centerAltitude.setOnClickListener { onChangeProperty(Property.CenterAltitude) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.versionLD.observe(viewLifecycleOwner, { _ -> observeVersion() })
        viewModel.zoomLD.observe(viewLifecycleOwner, { zoom -> observeZoom(zoom) })
        viewModel.startTimeLD.observe(viewLifecycleOwner, { startTime -> observeStartTime(startTime) })
        viewModel.propertyLD.observe(viewLifecycleOwner, { property -> observeProperty(property) })
        viewModel.filterCoefficientLD.observe(viewLifecycleOwner, { filterCoefficient -> observeFilterCoefficient(filterCoefficient) })
        viewModel.accelerationFactorLD.observe(viewLifecycleOwner, { accelerationFactor -> observeFactor(accelerationFactor) })
        viewModel.methodLD.observe(viewLifecycleOwner, { method -> observeMethod(method) })
        viewModel.orientationLD.observe(viewLifecycleOwner, { orientation -> observeOrientation(orientation) })
        viewModel.processorConsumptionLD.observe(viewLifecycleOwner, { consumption -> observeProcessorConsumption(consumption) })
    }

    override fun onStart() {
        super.onStart()
        updateActionBar()
    }

    private fun onSettings() {
        findNavController().navigate(R.id.action_global_SettingsFragment)
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

    private fun observeProperty(property: Property) {
        binding.mainView.setProperty(property)
        binding.property.text = property.toString()
        binding.property.setTextColor(getColorForProperty(property))
    }

    private fun getColorForProperty(property: Property): Int =
            resources.getColor(getColorResourceIdForProperty(property), null)

    private fun getColorResourceIdForProperty(property: Property): Int =
        when (property) {
            Property.Azimuth -> android.R.color.holo_red_light
            Property.Pitch -> android.R.color.holo_blue_bright
            Property.Roll -> android.R.color.holo_green_light
            Property.CenterAzimuth -> android.R.color.holo_orange_dark
            Property.CenterAltitude -> android.R.color.holo_blue_dark
        }

    private fun observeFilterCoefficient(filterCoefficient: Double) {
        binding.filterCoefficient.text = getString(R.string.label_name_value, "f", filterCoefficient.f2())
    }
    private fun observeFactor(accelerationFactor: Double) {
        binding.accelerationFactor.text = getString(R.string.label_name_value, "a", accelerationFactor.f2())
    }

    private fun observeMethod(method: Method) {
        binding.method.text = method.toString()
        binding.method.setTextColor(getColorForMethod(method))
    }

    private fun observeProcessorConsumption(consumption: Double) {
        binding.processorConsumption.text = consumption.f4()
    }

    private fun getColorForMethod(method: Method): Int =
            when (method) {
                Method.AccelerometerMagnetometer -> Color.RED
                Method.RotationVector -> Color.YELLOW
                Method.KalmanFilter -> Color.rgb(0xFC, 0x0F, 0xC0)
                Method.ComplementaryFilter -> Color.CYAN
                Method.MadgwickFilter -> Color.GREEN
                Method.SeparatedCorrectionFilter -> Color.MAGENTA
                Method.ExtendedComplementaryFilter -> Color.BLUE
                Method.Composition -> Color.WHITE
                Method.Damped -> Color.GRAY
            }


    private fun observeOrientation(orientation: Orientation) {
        binding.azimuth.text = getString(R.string.label_value_degree, orientation.azimuth.f0())
        binding.pitch.text = getString(R.string.label_value_degree, orientation.pitch.f0())
        binding.roll.text = getString(R.string.label_value_degree, orientation.roll.f0())
        binding.centerAzimuth.text = getString(R.string.label_value_degree, orientation.centerAzimuth.f0())
        binding.centerAltitude.text = getString(R.string.label_value_degree, orientation.centerAltitude.f0())
    }

    private fun onChangeProperty(newProperty: Property) {
        viewModel.property = newProperty
    }

    /**
     * Mind: MainFragment.onViewCreated() is called from inside MainActivity.onCreate(), hence before the actionBar is set
     */
    private fun updateActionBar() {
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(false)
            title = "Sensor Fusion"
        }
    }

    private val actionBar
        get() = (requireActivity() as AppCompatActivity).supportActionBar

}

