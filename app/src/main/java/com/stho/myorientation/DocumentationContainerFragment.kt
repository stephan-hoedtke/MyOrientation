package com.stho.myorientation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stho.myorientation.databinding.FragmentDocumentationContainerBinding


class DocumentationContainerFragment() : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentDocumentationContainerBinding
    private lateinit var adapter: DocumentationViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocumentationContainerBinding.inflate(inflater, container, false)
        adapter = DocumentationViewPagerAdapter(this)
        binding.viewpager.adapter = adapter
        binding.viewpager.currentItem = adapter.positionFor(viewModel.method)
        return binding.root
    }
}