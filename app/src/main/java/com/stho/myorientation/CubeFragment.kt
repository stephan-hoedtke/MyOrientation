package com.stho.myorientation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.methodLD.observe(viewLifecycleOwner, { method -> observeMethod(method) })
        viewModel.orientationLD.observe(viewLifecycleOwner, { orientation -> observeOrientation(orientation) })
        viewModel.cubeOrientationLD.observe(viewLifecycleOwner, { eulerAngles -> observeCubeOrientation(eulerAngles) })
    }

    private fun observeMethod(method: Entries.Method) {
        binding.method.text = method.toString()
    }

    private fun observeOrientation(orientation: Orientation) {
        binding.cubeView.setOrientation(orientation)
    }

    private fun observeCubeOrientation(eulerAngles: EulerAngles) {
        val alpha = eulerAngles.azimuth
        val beta = eulerAngles.pitch
        binding.alpha.text = alpha.f0()
        binding.beta.text = beta.f0()
        binding.cubeView.setAngles(alpha, beta)
    }
}