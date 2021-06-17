package com.stho.myorientation

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.stho.myorientation.databinding.FragmentSettingsBinding
import com.stho.myorientation.library.Formatter
import com.stho.myorientation.library.filter.MadgwickFilter
import com.stho.myorientation.library.filter.SeparatedCorrectionFilter
import kotlin.math.sqrt

class SettingsFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var methodAdapter: ArrayAdapter<Method>
    private lateinit var propertyAdapter: ArrayAdapter<Property>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setHasOptionsMenu(true)
    }

    abstract class SeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            // Ignore
        }
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            // Ignore
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)


        binding.buttonDone.setOnClickListener {
            onHome()
        }
        binding.buttonReset.setOnClickListener {
            viewModel.resetDefaultValues()
        }
        binding.seekbarAccelerationFactor.setOnSeekBarChangeListener(object : SeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.accelerationFactor = progressToValue(progress, MAX_ACCELERATION_FACTOR)
            }
        })
        binding.seekbarFilterCoefficient.setOnSeekBarChangeListener(object : SeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.filterCoefficient = progressToValue(progress, MAX_FILTER_COEFFICIENT)
            }
        })
        binding.seekbarVarianceAcceleration.setOnSeekBarChangeListener(object : SeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.varianceAccelerometer = progressToValue(progress, MAX_STANDARD_DEVIATION).square()
            }
        })
        binding.seekbarVarianceMagnetometer.setOnSeekBarChangeListener(object : SeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.varianceMagnetometer = progressToValue(progress, MAX_STANDARD_DEVIATION).square()
            }
        })
        binding.seekbarVarianceGyroscope.setOnSeekBarChangeListener(object : SeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.varianceGyroscope = progressToValue(progress, MAX_STANDARD_DEVIATION).square()
            }
        })
        binding.seekbarUpdateOrientationDelay.setOnSeekBarChangeListener(object : SeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.updateOrientationDelay = progressToValue(progress, MAX_DELAY)
            }
        })
        binding.seekbarUpdateSensorFusionDelay.setOnSeekBarChangeListener(object : SeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.updateSensorFusionDelay = progressToValue(progress, MAX_DELAY)
            }
        })
        val properties: MutableList<Property> = ArrayList()
        properties.add(Property.Azimuth)
        properties.add(Property.Pitch)
        properties.add(Property.Roll)
        properties.add(Property.CenterAzimuth)
        properties.add(Property.CenterAltitude)
        propertyAdapter = ArrayAdapter<Property>(requireContext(), android.R.layout.simple_spinner_item, properties)
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProperty.adapter = propertyAdapter
        binding.spinnerProperty.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as Property
                viewModel.property = item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.property = Property.Azimuth
            }
        }

        val methods: MutableList<Method> = ArrayList()
        methods.add(Method.AccelerometerMagnetometer)
        methods.add(Method.RotationVector)
        methods.add(Method.ComplementaryFilter)
        methods.add(Method.MadgwickFilter)
        methods.add(Method.SeparatedCorrectionFilter)
        methods.add(Method.ExtendedComplementaryFilter)
        methods.add(Method.KalmanFilter)
        methods.add(Method.Composition)

        methodAdapter = ArrayAdapter<Method>(requireContext(), android.R.layout.simple_spinner_item, methods)
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMethod.adapter = methodAdapter
        binding.spinnerMethod.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as Method
                viewModel.method = item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.method = Method.AccelerometerMagnetometer
            }
        }

        binding.radioButtonMadgwickDefault.setOnClickListener { viewModel.setMadgwickFilterMode(MadgwickFilter.Mode.Default) }
        binding.radioButtonMadgwickModified.setOnClickListener { viewModel.setMadgwickFilterMode(MadgwickFilter.Mode.Modified) }
        binding.radioButtonSeparatedCorrectionDefault.setOnClickListener { viewModel.setSeparatedCorrectionFilterMode(SeparatedCorrectionFilter.Mode.SCF) }
        binding.radioButtonSeparatedCorrectionModified.setOnClickListener { viewModel.setSeparatedCorrectionFilterMode(SeparatedCorrectionFilter.Mode.FSCF) }
        binding.switchAccelerometerMagnetometerFilter.setOnCheckedChangeListener { _, isChecked -> viewModel.showFilter(Method.AccelerometerMagnetometer, isChecked) }
        binding.switchRotationVectorFilter.setOnCheckedChangeListener { _, isChecked -> viewModel.showFilter(Method.RotationVector, isChecked) }
        binding.switchComplementaryFilter.setOnCheckedChangeListener { _, isChecked -> viewModel.showFilter(Method.ComplementaryFilter, isChecked) }
        binding.switchMadgwickFilter.setOnCheckedChangeListener { _, isChecked -> viewModel.showFilter(Method.MadgwickFilter, isChecked) }
        binding.switchSeparatedCorrectionFilter.setOnCheckedChangeListener { _, isChecked -> viewModel.showFilter(Method.SeparatedCorrectionFilter, isChecked) }
        binding.switchExtendedComplementaryFilter.setOnCheckedChangeListener { _, isChecked -> viewModel.showFilter(Method.ExtendedComplementaryFilter, isChecked) }
        binding.switchKalmanFilter.setOnCheckedChangeListener { _, isChecked -> viewModel.showFilter(Method.KalmanFilter, isChecked) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.propertyLD.observe(viewLifecycleOwner, { property -> observeProperty(property) })
        viewModel.methodLD.observe(viewLifecycleOwner, { mode -> observeMethod(mode) })
        viewModel.optionsLD.observe(viewLifecycleOwner, { options -> observeOptions(options) })
    }

    override fun onStart() {
        super.onStart()
        updateActionBar()
    }

    override fun onPrepareOptionsMenu(menu: Menu){
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_cube).isVisible = false
        menu.findItem(R.id.action_settings).isVisible = false
        menu.findItem(R.id.action_statistics).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> onHome()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onHome(): Boolean {
        findNavController().popBackStack()
        return true
    }


    private fun observeProperty(property: Property) {
        val pos: Int = propertyAdapter.getPosition(property)
        binding.spinnerProperty.setSelection(pos)
    }

    private fun observeMethod(mode: Method) {
        val pos: Int = methodAdapter.getPosition(mode)
        binding.spinnerMethod.setSelection(pos)
        binding.madgwickFilterOptions.visibility = visibilityFor(mode == Method.MadgwickFilter || mode == Method.Composition)
        binding.separatedCorrectionFilterOptions.visibility = visibilityFor(mode == Method.SeparatedCorrectionFilter || mode == Method.Composition)
        binding.compositionFilterOptions.visibility = visibilityFor(mode == Method.Composition)
    }

    private fun visibilityFor(value: Boolean) =
        if (value) View.VISIBLE else View.GONE


    private fun observeOptions(options: Options) {
        // Madgwick Filter Options
        binding.radioButtonMadgwickDefault.isChecked = (options.madgwickMode == MadgwickFilter.Mode.Default)
        binding.radioButtonMadgwickModified.isChecked = (options.madgwickMode == MadgwickFilter.Mode.Modified)

        // Separated Filter Options
        binding.radioButtonSeparatedCorrectionDefault.isChecked = (options.separatedCorrectionMode == SeparatedCorrectionFilter.Mode.SCF)
        binding.radioButtonSeparatedCorrectionModified.isChecked = (options.separatedCorrectionMode == SeparatedCorrectionFilter.Mode.FSCF)

        // Composition Filter Options
        binding.switchAccelerometerMagnetometerFilter.isChecked = options.showAccelerometerMagnetometerFilter
        binding.switchRotationVectorFilter.isChecked = options.showRotationVectorFilter
        binding.switchComplementaryFilter.isChecked = options.showComplementaryFilter
        binding.switchMadgwickFilter.isChecked = options.showMadgwickFilter
        binding.switchSeparatedCorrectionFilter.isChecked = options.showSeparatedCorrectionFilter
        binding.switchExtendedComplementaryFilter.isChecked = options.showExtendedComplementaryFilter
        binding.switchKalmanFilter.isChecked = options.showKalmanFilter

        // Acceleration Factor
        binding.accelerationFactor.text = Formatter.df2.format(options.accelerationFactor)
        binding.seekbarAccelerationFactor.max = 100
        binding.seekbarAccelerationFactor.progress = valueToProgress(options.accelerationFactor, MAX_ACCELERATION_FACTOR)

        // Filter Coefficient
        binding.filterCoefficient.text = Formatter.df2.format(options.filterCoefficient)
        binding.seekbarFilterCoefficient.max = 100
        binding.seekbarFilterCoefficient.progress = valueToProgress(options.filterCoefficient, MAX_FILTER_COEFFICIENT)

        // Variance
        binding.varianceAcceleration.text = Formatter.df4.format(options.varianceAccelerometer)
        binding.seekbarVarianceAcceleration.max = 100
        binding.seekbarVarianceAcceleration.progress = valueToProgress(options.varianceAccelerometer.squareRoot(), MAX_STANDARD_DEVIATION)

        binding.varianceMagnetometer.text = Formatter.df4.format(options.varianceMagnetometer)
        binding.seekbarVarianceMagnetometer.max = 100
        binding.seekbarVarianceMagnetometer.progress = valueToProgress(options.varianceMagnetometer.squareRoot(), MAX_STANDARD_DEVIATION)

        binding.varianceGyroscope.text = Formatter.df4.format(options.varianceGyroscope)
        binding.seekbarVarianceGyroscope.max = 100
        binding.seekbarVarianceGyroscope.progress = valueToProgress(options.varianceGyroscope.squareRoot(), MAX_STANDARD_DEVIATION)

        // Delay
        binding.updateOrientationDelay.text = options.updateOrientationDelay.toString()
        binding.seekbarUpdateOrientationDelay.max = 100
        binding.seekbarUpdateOrientationDelay.progress = valueToProgress(options.updateOrientationDelay, MAX_DELAY)

        binding.updateSensorFusionDelay.text = options.updateSensorFusionDelay.toString()
        binding.seekbarUpdateSensorFusionDelay.max = 100
        binding.seekbarUpdateSensorFusionDelay.progress = valueToProgress(options.updateSensorFusionDelay, MAX_DELAY)
    }

    private fun updateActionBar() {
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Settings"
        }
    }

    private val actionBar
        get() = (requireActivity() as AppCompatActivity).supportActionBar

    companion object {

        private const val MAX_TIME_CONSTANT: Double = 2.0
        private const val MAX_FILTER_COEFFICIENT: Double = 1.0
        private const val MAX_ACCELERATION_FACTOR: Double = 1.5
        private const val MAX_STANDARD_DEVIATION: Double = 1.0
        private const val MAX_DELAY: Long = 300

        private fun valueToProgress(value: Double, maxValue: Double): Int =
            (100.0 * value / maxValue + 0.5).toInt()

        private fun progressToValue(progress: Int, maxValue: Double): Double =
            maxValue * progress / 100.0

        private fun valueToProgress(value: Long, maxValue: Long): Int =
            (100.0 * value / maxValue + 0.5).toInt()

        private fun progressToValue(progress: Int, maxValue: Long): Long =
            (maxValue * progress / 100.0).toLong()
    }
}

private fun Double.square() = this * this
private fun Double.squareRoot() = sqrt(this)
