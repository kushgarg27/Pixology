package com.pixel.pixology.ui.stegnography

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextEncoding
import com.pixel.pixology.databinding.ActivityEncodeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

@AndroidEntryPoint
class EncodeActivity : AppCompatActivity(), TextEncodingCallback {

    private val SELECT_PICTURE = 100
    private val TAG = "EncodeClass"

    private lateinit var filepath: Uri

    lateinit var imageSteganography: ImageSteganography
    lateinit var textEncoding: TextEncoding

    lateinit var save: ProgressDialog

    //Bitmaps
    lateinit var original_image: Bitmap
    lateinit var encoded_image: Bitmap

    private lateinit var binding: ActivityEncodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()


        //Choose image button
        binding.chooseImageButton.setOnClickListener(View.OnClickListener { imageChooser() })

        //Encode Button

        //Encode Button
        binding.encodeButton.setOnClickListener(View.OnClickListener {
            binding.whetherEncoded.setText("")
            if (filepath != null) {
                if (binding.message.getText() != null) {

                    //ImageSteganography Object instantiation
                    imageSteganography = ImageSteganography(
                        binding.message.getText().toString(),
                        binding.secretKey.getText().toString(),
                        original_image
                    )
                    //TextEncoding object Instantiation
                    textEncoding = TextEncoding(this@EncodeActivity, this@EncodeActivity)
                    //Executing the encoding
                    textEncoding.execute(imageSteganography)
                }
            }
        })


        //Save image button
        binding.saveImageButton.setOnClickListener(View.OnClickListener {
            val imgToSave = encoded_image
            val performEncoding = Thread { saveToInternalStorage(imgToSave) }
            save = ProgressDialog(this@EncodeActivity)
            save.setMessage("Saving, Please Wait...")
            save.setTitle("Saving Image")
            save.isIndeterminate = false
            save.setCancelable(false)
            save.show()
            performEncoding.start()
        })


    }

    private fun imageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            SELECT_PICTURE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Image set to imageView
        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filepath = data.data!!
            try {
                original_image = MediaStore.Images.Media.getBitmap(contentResolver, filepath)
                binding.imageview.setImageBitmap(original_image)
            } catch (e: IOException) {
                Log.d(TAG, "Error : $e")
            }
        }
    }

    private fun saveToInternalStorage(imgToSave: Bitmap) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ), "Encoded" + ".PNG"
        )

        try {
            val fOut: OutputStream = FileOutputStream(file)
            imgToSave.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()

            // Add the image to the gallery using FileProvider
            val imageUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = imageUri
            sendBroadcast(mediaScanIntent)

            binding.whetherEncoded.post {
                save.dismiss()
                Toast.makeText(this@EncodeActivity, "Image saved successfully.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            binding.whetherEncoded.post {
                save.dismiss()
                Toast.makeText(this@EncodeActivity, "Failed to save image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStartTextEncoding() {

    }

    override fun onCompleteTextEncoding(result: ImageSteganography?) {
        //By the end of textEncoding
        if (result != null && result.isEncoded()) {
            encoded_image = result.getEncoded_image()
            binding.whetherEncoded.setText("Encoded")
            binding.imageview.setImageBitmap(encoded_image)
            Log.e(TAG, "onCompleteTextEncoding: $encoded_image", )
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionWriteStorage = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val ReadPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (ReadPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 1)
        }
    }

}




