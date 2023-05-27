package com.pixel.pixology.ui.stegnography

import android.Manifest
import android.annotation.SuppressLint
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
    private val TAG = "Encode Class"

    private var filepath: Uri? = null

    private var imageSteganography: ImageSteganography? = null
    private var textEncoding: TextEncoding? = null

    private var save: ProgressDialog? = null

    //Bitmaps
    private var original_image: Bitmap? = null
    private var encoded_image: Bitmap? = null

    private lateinit var binding: ActivityEncodeBinding
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncodeBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        checkAndRequestPermissions()


        //Choose image button
        binding.chooseImageButton.setOnClickListener(View.OnClickListener { ImageChooser() })

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
                    textEncoding!!.execute(imageSteganography)
                }
            }
        })


        //Save image button
        binding.saveImageButton.setOnClickListener(View.OnClickListener {
            val imgToSave = encoded_image!!
            var PerformEncoding = Thread { saveToInternalStorage(imgToSave) }
            save = ProgressDialog(this@EncodeActivity)
                save!!.setMessage("Saving, Please Wait...")
                save!!.setTitle("Saving Image")
                save!!.setIndeterminate(false)
                save!!.setCancelable(false)
                save!!.show()
            PerformEncoding.start()

//            saveToInternalStorage(imgToSave)


        })

    }

    private fun ImageChooser() {
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
            filepath = data.data
            try {
                original_image = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath)
                binding.imageview.setImageBitmap(original_image)
            } catch (e: IOException) {
                Log.d(TAG, "Error : $e")
            }
        }
    }

    private fun saveToInternalStorage(imgToSave: Bitmap) {
        val fOut: OutputStream
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ), "Encoded" + ".PNG"
        ) // the File to save ,

        try {
            fOut = FileOutputStream(file)
            imgToSave.compress(
                Bitmap.CompressFormat.PNG,
                100,
                fOut
            ) // saving the Bitmap to a file
            fOut.flush() // Not really required
            fOut.close() // do not forget to close the stream
//            binding.whetherEncoded.post(Runnable { save?.dismiss() })
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
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




