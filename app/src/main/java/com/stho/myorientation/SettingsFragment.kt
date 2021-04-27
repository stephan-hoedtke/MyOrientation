package com.stho.myorientation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.stho.myorientation.databinding.FragmentSettingsBinding
import com.stho.myorientation.library.Formatter
import com.stho.myorientation.library.OrientationSensorListener

class SettingsFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var methodAdapter: ArrayAdapter<Entries.Method>
    private lateinit var propertyAdapter: ArrayAdapter<Entries.Property>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        viewModel.accelerationFactorLD.observe(viewLifecycleOwner, { factor -> observeFactor(factor) })
        viewModel.timeConstantLD.observe(viewLifecycleOwner, { timeConstant -> observeTimeConstant(timeConstant) })
        viewModel.filterCoefficientLD.observe(viewLifecycleOwner, { filterCoefficient -> observeFilterCoefficient(filterCoefficient) })
        viewModel.propertyLD.observe(viewLifecycleOwner, { property -> observeProperty(property) })
        viewModel.methodLD.observe(viewLifecycleOwner, { mode -> observeMethod(mode) })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDone.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsFragment_to_MainFragment)
        }
        binding.buttonReset.setOnClickListener {
            viewModel.resetDefaultValues()
        }
        binding.seekbarAccelerationFactor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.accelerationFactor = progressToValue(progress, MAX_ACCELERATION_FACTOR)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Ignore
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Ignore
            }
        })
        binding.seekbarFilterCoefficient.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.filterCoefficient = progressToValue(progress, MAX_FILTER_COEFFICIENT)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Ignore
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Ignore
            }
        })
        binding.seekbarTimeConstant.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.timeConstant = progressToValue(progress, MAX_TIME_CONSTANT)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Ignore
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Ignore
            }
        })
        val properties: MutableList<Entries.Property> = ArrayList()
        properties.add(Entries.Property.Azimuth)
        properties.add(Entries.Property.Pitch)
        properties.add(Entries.Property.Roll)
        properties.add(Entries.Property.CenterAzimuth)
        properties.add(Entries.Property.CenterAltitude)
        propertyAdapter = ArrayAdapter<Entries.Property>(requireContext(), android.R.layout.simple_spinner_item, properties)
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProperty.adapter = propertyAdapter
        binding.spinnerProperty.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as Entries.Property
                viewModel.property = item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.property = Entries.Property.Azimuth
            }
        }

        val methods: MutableList<Entries.Method> = ArrayList()
        methods.add(Entries.Method.AccelerometerMagnetometer)
        methods.add(Entries.Method.RotationVector)
        methods.add(Entries.Method.ComplementaryFilter)
        methods.add(Entries.Method.KalmanFilter)
        methods.add(Entries.Method.Composition)

        methodAdapter = ArrayAdapter<Entries.Method>(requireContext(), android.R.layout.simple_spinner_item, methods)
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMethod.adapter = methodAdapter
        binding.spinnerMethod.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as Entries.Method
                viewModel.method = item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.method = Entries.Method.AccelerometerMagnetometer
            }
        }
    }

    private fun observeFactor(factor: Double) {
        binding.accelerationFactor.text = Formatter.df2.format(factor)
        binding.seekbarAccelerationFactor.max = 100
        binding.seekbarAccelerationFactor.progress = valueToProgress(factor, MAX_ACCELERATION_FACTOR)
    }

    private fun observeTimeConstant(timeConstant: Double) {
        binding.timeConstant.text = Formatter.df2.format(timeConstant)
        binding.seekbarTimeConstant.max = 100
        binding.seekbarTimeConstant.progress = valueToProgress(timeConstant, MAX_TIME_CONSTANT)
    }

    private fun observeFilterCoefficient(filterCoefficient: Double) {
        binding.filterCoefficient.text = Formatter.df2.format(filterCoefficient)
        binding.seekbarFilterCoefficient.max = 100
        binding.seekbarFilterCoefficient.progress = valueToProgress(filterCoefficient, MAX_FILTER_COEFFICIENT)
    }

    private fun observeProperty(property: Entries.Property) {
        val pos: Int = propertyAdapter.getPosition(property)
        binding.spinnerProperty.setSelection(pos)
    }

    private fun observeMethod(mode: Entries.Method) {
        val pos: Int = methodAdapter.getPosition(mode)
        binding.spinnerMethod.setSelection(pos)
    }

    companion object {

        private const val MAX_TIME_CONSTANT = 2.0
        private const val MAX_FILTER_COEFFICIENT = 1.0
        private const val MAX_ACCELERATION_FACTOR = 1.5

        private fun valueToProgress(value: Double, maxValue: Double): Int =
            (100.0 * value / maxValue + 0.5).toInt()

        private fun progressToValue(progress: Int, maxValue: Double): Double =
            maxValue * progress / 100.0
    }
}

