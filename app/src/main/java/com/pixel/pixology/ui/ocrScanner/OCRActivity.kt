package com.pixel.pixology.ui.ocrScanner

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pixel.pixology.databinding.ActivityOcractivityBinding

@Suppress("DEPRECATION")
class OCRActivity : AppCompatActivity() {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val REQUEST_IMAGE_CAPTURE=1
    private var imageBitmap: Bitmap? =null

    private lateinit var binding: ActivityOcractivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcractivityBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        binding?.captureImage?.setOnClickListener {
            takeImage()

            binding.textView.text = ""

        }

        binding?.detectTextImageBtn?.setOnClickListener {


                processImage()


        }
    }

    private fun processImage() {
        if (imageBitmap!=null) {

            val image = imageBitmap?.let {

                InputImage.fromBitmap(it, 0)

            }

            image?.let {
                recognizer.process(it)
                    .addOnSuccessListener { visionText ->

                        binding.textView.text = visionText.text

                    }
                    .addOnFailureListener { e ->

                    }
            }

        }

        else{

            Toast.makeText(this, "Please select photo", Toast.LENGTH_SHORT).show()

        }



    }

    private fun takeImage() {
        val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {

            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE)

        }
        catch (e:Exception){



        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==REQUEST_IMAGE_CAPTURE && resultCode== RESULT_OK){

            val extras: Bundle? = data?.extras

            imageBitmap= extras?.get("data") as Bitmap

            if (imageBitmap!=null) {

                binding.imageView.setImageBitmap(imageBitmap)

            }



        }


    }
}