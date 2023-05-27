package com.pixel.pixology.ui.stegnography

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextDecoding
import com.pixel.pixology.databinding.ActivityDecodeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class DecodeActivity : AppCompatActivity(), TextDecodingCallback {

    private val SELECT_PICTURE = 100
    private val TAG = "Decode Class"

    private var filepath: Uri? = null

    //Bitmap
    private var original_image: Bitmap? = null

    private lateinit var binding: ActivityDecodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecodeBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)


        //Choose Image Button
        binding.chooseImageButton.setOnClickListener(View.OnClickListener { ImageChooser() })

        //Decode Button

        //Decode Button
        binding.decodeButton.setOnClickListener(View.OnClickListener {
            if (filepath != null) {

                //Making the ImageSteganography object
                val imageSteganography = ImageSteganography(
                    binding.secretKey.getText().toString(),
                    original_image
                )

                //Making the TextDecoding object
                val textDecoding = TextDecoding(this@DecodeActivity, this@DecodeActivity)

                //Execute Task
                textDecoding.execute(imageSteganography)
            }
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
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.data != null) {
            filepath = data.data
            try {
                original_image = MediaStore.Images.Media.getBitmap(contentResolver, filepath)
                binding.imageview.setImageBitmap(original_image)
            } catch (e: IOException) {
                Log.d(TAG, "Error : $e")
            }
        }
    }

    override fun onStartTextEncoding() {
        //Whatever you want to do by the start of textDecoding
    }

    @SuppressLint("SetTextI18n")
    override fun onCompleteTextEncoding(result: ImageSteganography?) {
        //By the end of textDecoding

        if (result != null) {
            if (!result.isDecoded) binding.whetherDecoded.text = "No message found" else {
                if (!result.isSecretKeyWrong) {
                    binding.whetherDecoded.text = "Decoded"
                    binding.message.setText(/* text = */ "" + result.message)
                } else {
                    binding.whetherDecoded.text = "Wrong secret key"
                }
            }
        } else {
            binding.whetherDecoded.text = "Select Image First"
        }


    }
}