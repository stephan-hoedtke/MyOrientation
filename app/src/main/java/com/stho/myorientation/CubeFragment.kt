package com.stho.myorientation

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.stho.myorientation.databinding.FragmentCubeBinding
import com.stho.myorientation.library.algebra.EulerAngles
import com.stho.myorientation.library.algebra.Orientation
import com.stho.myorientation.library.f0
import com.stho.myorientation.views.CubeView

class CubeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentCubeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCubeBinding.inflate(inflater, container, false)
        binding.cubeView.setEventListener(object: CubeView.IEventListener {
            override fun rotateBy(alpha: Double, beta: Double) {
                viewModel.rotateCube(alpha, beta)
            }
        })
        binding.method.setOnClickListener {
            onSettings()
        }
        binding.alpha.setOnLongClickListener {
            onResetCubeOrientation()
        }
        binding.beta.setOnLongClickListener {
            onResetCubeOrientation()
        }
        binding.buttonSensors.setOnClickListener { onSensors() }
        binding.buttonDocumentation.setOnClickListener { onDocumentation() }
        binding.buttonStatistics.setOnClickListener { onStatistics() }
        return binding.root
    }

    private fun onSensors() {
        findNavController().navigate(R.id.action_global_SensorsFragment)
    }

    private fun onSettings() {
        findNavController().navigate(R.id.action_global_SettingsFragment)
    }

    private fun onStatistics() {
        findNavController().navigate(R.id.action_global_StatisticsFragment)
    }

    private fun onDocumentation() {
        findNavController().navigate(R.id.action_global_DocumentationContainerFragment)
    }

    private fun onResetCubeOrientation(): Boolean {
        viewModel.resetCubeOrientation()
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.methodLD.observe(viewLifecycleOwner) { method -> observeMethod(method) }
        viewModel.orientationLD.observe(viewLifecycleOwner) { orientation -> observeOrientation(orientation) }
        viewModel.cubeOrientationLD.observe(viewLifecycleOwner) { eulerAngles -> observeCubeOrientation(eulerAngles) }
    }

    override fun onStart() {
        super.onStart()
        updateActionBar()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        // We have buttons instead ...s
        menu.findItem(R.id.action_sensors).isVisible = false
        menu.findItem(R.id.action_statistics).isVisible = false
        menu.findItem(R.id.action_documentation).isVisible = false
    }

    private fun observeMethod(method: Method) {
        binding.method.text = method.toString()
    }

    private fun observeOrientation(orientation: Orientation) {
        if (viewModel.isActive) {
            binding.cubeView.setOrientation(orientation)
        }
    }

    private fun observeCubeOrientation(eulerAngles: EulerAngles) {
        val alpha = eulerAngles.azimuth
        val beta = eulerAngles.pitch
        binding.alpha.text = getString(R.string.label_angle_param, alpha.f0())
        binding.beta.text = getString(R.string.label_angle_param, beta.f0())
        binding.cubeView.setAngles(alpha, beta)
    }

    private fun updateActionBar() {
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(false)
            title = "Orientation"
        }
    }

    private val actionBar
        get() = (requireActivity() as AppCompatActivity).supportActionBar
}
