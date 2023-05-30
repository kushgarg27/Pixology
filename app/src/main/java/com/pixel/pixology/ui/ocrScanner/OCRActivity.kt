package com.pixel.pixology.ui.ocrScanner
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.provider.MediaStore
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pixel.pixology.databinding.ActivityOcractivityBinding
import java.io.IOException

class OCRActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var binding: ActivityOcractivityBinding
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val REQUEST_IMAGE_CAPTURE = 1
    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var isPreviewing = false
    private var imageBitmap: Bitmap? = null

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.surfaceView.holder.addCallback(this)

        binding.detectTextImageBtn.setOnClickListener {
            openCamera()
        }

        binding.sendToWhatsappButton.setOnClickListener {
            sendTextToWhatsApp()
        }
    }

    private fun sendTextToWhatsApp() {
        val text = binding.textView.text.toString().trim()
        if (text.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("OCR Text", text)
            clipboard.setPrimaryClip(clip)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } else {
                Toast.makeText(this, "No apps available to share", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No text to share", Toast.LENGTH_SHORT).show()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceHolder = holder
        checkCameraPermission()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (isPreviewing) {
            camera?.stopPreview()
            isPreviewing = false
        }

        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
            isPreviewing = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()
        camera = null
        isPreviewing = false
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        try {
            camera = Camera.open()
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(binding.surfaceView.holder)
            setAutoFocus()
            camera?.startPreview()
            isPreviewing = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAutoFocus() {
        val params = camera?.parameters
        val supportedFocusModes = params?.supportedFocusModes
        if (supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        } else if (supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_AUTO) == true) {
            params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        }
        camera?.parameters = params
    }

    private fun captureImage() {
        camera?.takePicture(null, null, Camera.PictureCallback { data, _ ->
            imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            processImage()
            camera?.startPreview()
        })
    }

    private fun processImage() {
        imageBitmap?.let {
            val image = InputImage.fromBitmap(it, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    binding.textView.text = visionText.text
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Text detection failed", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
