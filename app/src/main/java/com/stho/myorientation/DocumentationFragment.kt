package com.stho.myorientation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stho.myorientation.databinding.FragmentDocumentationBinding
import com.stho.myorientation.library.filter.IOrientationFilter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class DocumentationFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentDocumentationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
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
        viewModel.methodLD.observe(viewLifecycleOwner, { method -> observeMethod(method) })
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

    private val filter: IOrientationFilter
        get() = viewModel.createFilter()

    @Suppress("UNUSED_PARAMETER")
    private fun observeMethod(method: Method) {
        try {
            val pdf = filter.pdf
            val file = loadFile(pdf, false)
            val bitmap = openPDF(file, 0)
            if (bitmap != null) {
                binding.image.setImageBitmap(bitmap)
            }
        }
        catch (ex: Exception) {
            mainActivity.showSnackbar("Can't display PDF: ${ex.message}")
        }
    }

    private fun onPdf(): Boolean {
        try {
            val pdf = filter.pdf
            val file = loadFile(pdf, true)
            val authority = BuildConfig.APPLICATION_ID + ".provider"
            val uri: Uri = FileProvider.getUriForFile(
                requireContext(),
                authority,
                file)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            requireContext().startActivity(intent)
        } catch (ex: Exception) {
            mainActivity.showSnackbar("No PDF viewer: ${ex.message}")
        }
        return true
    }

    private fun onWeb(): Boolean {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(filter.link)))
        } catch (ex: Exception) {
            mainActivity.showSnackbar("No Web browser: ${ex.message}")
        }
        return true
    }

    private fun loadFile(fileName: String, overwrite: Boolean = true): File {
        val context = requireContext()
        val file = File(rootDir, fileName)
        if (file.exists() && overwrite) {
            file.delete()
        }
        if (!file.exists()) {
            createFileFromAssets(context, file)
        }
        return file
    }

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    private val rootDir
        get() = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    private fun updateActionBar() {
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Documentation"
        }
    }

    private val actionBar
        get() = (requireActivity() as AppCompatActivity).supportActionBar


    companion object {

        private fun createFileFromAssets(context: Context, file: File) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            val asset: InputStream = context.assets.open(file.name)
            val outputStream = FileOutputStream(file)
            val size: Int = asset.available()
            val buffer = ByteArray(size)
            asset.read(buffer)
            asset.close()
            outputStream.write(buffer, 0, size)
            outputStream.close()
        }

        internal fun openPDF(file: File, pageIndex: Int = 0): Bitmap? {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            val page = renderer.openPage(pageIndex)
            val bitmap = Bitmap.createBitmap(3 * page.width, 3 * page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            page.close()
            renderer.close()
            return bitmap
        }
    }
}