
package com.pixel.pixology.ui.scanandsearch
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pixel.pixology.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GoogleLensActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: TextureView
    private lateinit var captureButton: Button
    private lateinit var capturedImage: ImageView
    private lateinit var resultText: TextView

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_lens)

        cameraExecutor = Executors.newSingleThreadExecutor()
        previewView = findViewById(R.id.preview_view)
        captureButton = findViewById(R.id.capture_button)
        capturedImage = findViewById(R.id.captured_image)
        resultText = findViewById(R.id.result_text)

        // Set click listener for the capture button
        captureButton.setOnClickListener {
            captureImage()
        }

        // Request camera permission if not granted
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
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()

        val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                val surface = Surface(surfaceTexture)
                val previewSize = Size(width, height)
                val rotation = previewView.display.rotation
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
//                    cameraProvider.bindToLifecycle(this@GoogleLensActivity, cameraSelector, preview.setSurfaceProvider(surface), imageCapture)
                } catch (e: Exception) {
                    Log.e("CameraX", "Error: ${e.message}")
                }
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
        }

        previewView.surfaceTextureListener = surfaceTextureListener
    }




    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        val file = createImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    val bitmap = savedUri?.let { getBitmapFromUri(it) }
                    bitmap?.let { showCapturedImage(it) }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Error capturing image: ${exception.message}", exception)
                }
            }
        )
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = filesDir
        return File.createTempFile("IMG_$timestamp", ".jpg", storageDir)
    }

    private fun getBitmapFromUri(uri: android.net.Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val byteArray = inputStream?.readBytes()
            byteArray?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        } catch (e: Exception) {
            Log.e("CameraX", "Error loading bitmap: ${e.message}", e)
            null
        }
    }

    private fun showCapturedImage(bitmap: Bitmap) {
        capturedImage.setImageBitmap(bitmap)
        capturedImage.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                // Camera permission denied
            }
        }
    }
}