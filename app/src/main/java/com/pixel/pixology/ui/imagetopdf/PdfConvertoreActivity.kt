package com.pixel.pixology.ui.imagetopdf

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixel.pixology.databinding.ActivityPdfConvertoreBinding
import com.pixel.pixology.databinding.ItemImageBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class PdfConvertoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfConvertoreBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private var imageUris: MutableList<Uri> = mutableListOf()

    private companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_WRITE_STORAGE = 2
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfConvertoreBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        imageAdapter = ImageAdapter()
        recyclerView.adapter = imageAdapter

        binding.btnSelectImages.setOnClickListener {
            openImagePicker()
        }

        binding.btnConvertToPdf.setOnClickListener {

            convertToPdf()

        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun convertToPdf() {
        binding.progressLayout.isVisible = true
        if (imageUris.isNotEmpty()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_STORAGE
                )
            } else {
                generatePdf()
            }
        } else {
            Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun generatePdf() {
        val pdfDocument = PdfDocument()
        var targetWidth = 800
        var targetHeight = 800

        for (uri in imageUris) {
            val bitmap: Bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))

            // Resize the bitmap to a fixed size
            val resizedBitmap = resizeBitmap(bitmap, targetWidth, targetHeight)

            val pageInfo = PdfDocument.PageInfo.Builder(targetWidth, targetHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas: Canvas = page.canvas

            // Calculate the horizontal and vertical position to center align the image
            val horizontalPosition = (canvas.width - resizedBitmap.width) / 2.toFloat()
            val verticalPosition = (canvas.height - resizedBitmap.height) / 2.toFloat()

            canvas.drawBitmap(resizedBitmap, horizontalPosition, verticalPosition, null)

            pdfDocument.finishPage(page)

            resizedBitmap.recycle()
            bitmap.recycle()
        }
        // Save the PDF file using the MediaStore API
        val displayName = "converted.pdf"
        val mimeType = "application/pdf"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val contentResolver = applicationContext.contentResolver
        var outputStream: OutputStream? = null
        var uri: Uri? = null

        try {
            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            uri = contentResolver.insert(collection, values)

            uri?.let {
                outputStream = contentResolver.openOutputStream(it)
                pdfDocument.writeTo(outputStream)
                Toast.makeText(this, "PDF created successfully", Toast.LENGTH_SHORT).show()
                binding.progressLayout.isVisible = false
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share PDF"))
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error creating PDF", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } finally {
            outputStream?.close()
            pdfDocument.close()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scaleX = targetWidth.toFloat() / width.toFloat()
        val scaleY = targetHeight.toFloat() / height.toFloat()
        val scale = if (scaleX < scaleY) scaleX else scaleY

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.let { intent ->
                if (intent.clipData != null) {
                    val clipData = intent.clipData
                    for (i in 0 until clipData?.itemCount!!) {
                        val clipItem = clipData.getItemAt(i)
                        val uri = clipItem.uri
                        imageUris.add(uri)
                    }
                } else if (intent.data != null) {
                    val uri = intent.data
                    uri?.let { imageUris.add(it) }
                }

                imageAdapter.notifyDataSetChanged()
                recyclerView.visibility = RecyclerView.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    generatePdf()
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val itemBinding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ImageViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val uri = imageUris[position]
            holder.bind(uri)
        }

        override fun getItemCount(): Int {
            return imageUris.size
        }

        inner class ImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(uri: Uri) {
                val context = binding.imageView.context
                val bitmap: Bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                binding.imageView.setImageBitmap(bitmap)
            }
        }
    }
}





