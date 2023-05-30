package com.pixel.pixology.ui.stegnography
import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextEncoding
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivityEncodeBinding
import java.io.*

@Suppress("DEPRECATION")
class EncodeActivity : AppCompatActivity(), TextEncodingCallback {
    private var filePath: Uri? = null
    private var originalImage: Bitmap? = null
    private var encodedImage: Bitmap? = null
    private val saveProgressDialog: ProgressDialog? = null
    private var binding: ActivityEncodeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncodeBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        checkAndRequestPermissions()
        binding!!.chooseImageButton.setOnClickListener { imageChooser() }
        binding!!.encodeButton.setOnClickListener { encodeMessage() }
        binding!!.saveImageButton.setOnClickListener { saveEncodedImage() }
    }

    private fun imageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    private fun encodeMessage() {
        val message = binding!!.messageEditText.text.toString()
        val secretKey = binding!!.secretKeyEditText.text.toString()
        if (filePath != null && !message.isEmpty() && !secretKey.isEmpty()) {
            try {
                originalImage = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                val imageSteganography = ImageSteganography(
                    message,
                    secretKey,
                    originalImage
                )
                val textEncoding = TextEncoding(this@EncodeActivity, this@EncodeActivity)
                textEncoding.execute(imageSteganography)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Please fill in all the details.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveEncodedImage() {
        if (encodedImage != null) {
            val outputStream: OutputStream
            try {
                val file = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ), "Encoded.png"
                )
                outputStream = FileOutputStream(file)
                encodedImage!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                Toast.makeText(this, "Encoded image saved successfully.", Toast.LENGTH_SHORT).show()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "No encoded image found.", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data?.data
            try {
                originalImage = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding!!.imageView.setImageBitmap(originalImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        var allPermissionsGranted = true
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                allPermissionsGranted = false
                break
            }
        }
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    override fun onStartTextEncoding() {
        // Show progress dialog or perform any UI operation
    }

    override fun onCompleteTextEncoding(result: ImageSteganography) {
        if (result != null && result.isEncoded) {
            encodedImage = result.encoded_image
            binding!!.imageView.setImageBitmap(encodedImage)
            Toast.makeText(this, "Message encoded successfully.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Message encoding failed.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val SELECT_PICTURE = 100
    }
}