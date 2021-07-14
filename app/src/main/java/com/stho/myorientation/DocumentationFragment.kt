package com.stho.myorientation

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stho.myorientation.databinding.FragmentDocumentationBinding
import com.stho.myorientation.library.filter.IOrientationFilter


class DocumentationFragment(private val method: Method) : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentDocumentationBinding
    private lateinit var adapter: DocumentationFileAdapter
    private lateinit var filter: IOrientationFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        filter = viewModel.createFilter(method)
        adapter = DocumentationFileAdapter(requireContext())
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocumentationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayDocumentation()
    }

    override fun onStart() {
        super.onStart()
        updateActionBar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_documentation, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pdf -> onPdf()
            R.id.action_web -> onWeb()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayDocumentation() {
        try {
            val bitmap = adapter.getBitmapFromPdf(filter)
            binding.image.setImageBitmap(bitmap)
        }
        catch (ex: Exception) {
            mainActivity.showSnackbar("Can't display PDF: ${ex.message}")
        }
    }

    private fun onPdf(): Boolean {
        try {
            adapter.openPdf(filter)
        } catch (ex: Exception) {
            mainActivity.showSnackbar("No PDF viewer: ${ex.message}")
        }
        return true
    }

    private fun onWeb(): Boolean {
        try {
            adapter.openLink(filter)
        } catch (ex: Exception) {
            mainActivity.showSnackbar("No Web browser: ${ex.message}")
        }
        return true
    }

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    private fun updateActionBar() {
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Documentation"
        }
    }

    private val actionBar
        get() = (requireActivity() as AppCompatActivity).supportActionBar


}