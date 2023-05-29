package com.pixel.pixology.ui.imagecompressor

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pixel.pixology.databinding.ActivityImageCompressorBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.IOException

@AndroidEntryPoint
class ImageCompressorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageCompressorBinding
    private lateinit var originalImage: Bitmap
    private lateinit var compressedBytesArray: ByteArray
    private lateinit var saveProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageCompressorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askPermissions()

        binding.btnPickImg.setOnClickListener { openGallery() }

        binding.seekQuality.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.txtQuality.text = "Quality: $progress"
                seekBar.max = 100
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.btnCompress.setOnClickListener { compressImage(binding.seekQuality.progress) }

        binding.saveTxt.setOnClickListener { saveCompressedImage() }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, RESULT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_IMAGE && resultCode == RESULT_OK) {
            binding.btnCompress.visibility = View.VISIBLE
            val imageUri: Uri? = data?.data
            try {
                originalImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                binding.imgOriginal.setImageBitmap(originalImage)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressImage(quality: Int) {
        val outputStream = ByteArrayOutputStream()
        originalImage.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        compressedBytesArray = outputStream.toByteArray()
        val compressedBitmap = BitmapFactory.decodeByteArray(compressedBytesArray, 0, compressedBytesArray.size)
        binding.imgCompressed.setImageBitmap(compressedBitmap)
    }

    private fun saveCompressedImage() {
        if (::compressedBytesArray.isInitialized) {
            saveProgressDialog = ProgressDialog(this)
            saveProgressDialog.setMessage("Saving, Please Wait...")
            saveProgressDialog.setTitle("Saving Image")
            saveProgressDialog.isIndeterminate = false
            saveProgressDialog.setCancelable(false)
            saveProgressDialog.show()

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "${binding.etFileName.text.toString()}.jpeg")
            }

            val resolver = contentResolver
            val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            uri?.let { imageUri ->
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    try {
                        outputStream.write(compressedBytesArray)
                        Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, imageUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Error While Saving Image", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            saveProgressDialog.dismiss()
        } else {
            Toast.makeText(this, "Compressed Image not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {}

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<com.karumi.dexter.listener.PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    companion object {
        private const val RESULT_IMAGE = 1
    }
}
