package com.stho.myorientation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.stho.myorientation.databinding.FragmentStatisticsBinding
import com.stho.myorientation.library.f4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        viewModel = ViewModelProvider(requireActivity()).get(StatisticsViewModel::class.java)
        handler = Handler(Looper.getMainLooper())
        setHasOptionsMenu(true)
     }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.methodLD.observe(viewLifecycleOwner) { method -> observeMethod(method) }
        viewModel.accelerometerLD.observe(viewLifecycleOwner) { statistics -> observeAccelerometer(statistics) }
        viewModel.magnetometerLD.observe(viewLifecycleOwner) { statistics -> observeMagnetometer(statistics) }
        viewModel.gyroscopeLD.observe(viewLifecycleOwner) { statistics -> observeGyroscope(statistics) }
    }

    override fun onStart() {
        super.onStart()
        updateActionBar()
    }

    override fun onResume() {
        super.onResume()
        executeHandlerToUpdateStatistics()
    }

    override fun onPause() {
        super.onPause()
        stopHandler()
    }

    override fun onPrepareOptionsMenu(menu: Menu){
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_statistics).isVisible = false
    }

    private fun executeHandlerToUpdateStatistics(delayMillis: Long = 1000) {
        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.Default).launch {
                    viewModel.updateStatistics()
                }
                handler.postDelayed(this, delayMillis)
            }
        }
        handler.postDelayed(runnableCode, 100)
    }

    private fun stopHandler() =
        handler.removeCallbacksAndMessages(null)

    private fun observeMethod(method: Method) {
        binding.method.text = method.toString()
    }

    private fun observeAccelerometer(statistic: StatisticsViewModel.Statistic) {
        binding.accelerometerMeanX.text = statistic.meanX.f4()
        binding.accelerometerMeanY.text = statistic.meanY.f4()
        binding.accelerometerMeanZ.text = statistic.meanZ.f4()
        binding.accelerometerVarianceX.text = statistic.varianceX.f4()
        binding.accelerometerVarianceY.text = statistic.varianceY.f4()
        binding.accelerometerVarianceZ.text = statistic.varianceZ.f4()
        binding.accelerometerDeviationX.text = statistic.standardDeviationX.f4()
        binding.accelerometerDeviationY.text = statistic.standardDeviationY.f4()
        binding.accelerometerDeviationZ.text = statistic.standardDeviationZ.f4()
    }

    private fun observeMagnetometer(statistic: StatisticsViewModel.Statistic) {
        binding.magnetometerMeanX.text = statistic.meanX.f4()
        binding.magnetometerMeanY.text = statistic.meanY.f4()
        binding.magnetometerMeanZ.text = statistic.meanZ.f4()
        binding.magnetometerVarianceX.text = statistic.varianceX.f4()
        binding.magnetometerVarianceY.text = statistic.varianceY.f4()
        binding.magnetometerVarianceZ.text = statistic.varianceZ.f4()
        binding.magnetometerDeviationX.text = statistic.standardDeviationX.f4()
        binding.magnetometerDeviationY.text = statistic.standardDeviationY.f4()
        binding.magnetometerDeviationZ.text = statistic.standardDeviationZ.f4()
    }

    private fun observeGyroscope(statistic: StatisticsViewModel.Statistic) {
        binding.gyroscopeMeanX.text = statistic.meanX.f4()
        binding.gyroscopeMeanY.text = statistic.meanY.f4()
        binding.gyroscopeMeanZ.text = statistic.meanZ.f4()
        binding.gyroscopeVarianceX.text = statistic.varianceX.f4()
        binding.gyroscopeVarianceY.text = statistic.varianceY.f4()
        binding.gyroscopeVarianceZ.text = statistic.varianceZ.f4()
        binding.gyroscopeDeviationX.text = statistic.standardDeviationX.f4()
        binding.gyroscopeDeviationY.text = statistic.standardDeviationY.f4()
        binding.gyroscopeDeviationZ.text = statistic.standardDeviationZ.f4()
    }


    private fun updateActionBar() {
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Statistics"
        }
    }

    private val actionBar
        get() = (requireActivity() as AppCompatActivity).supportActionBar

}