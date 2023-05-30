package com.pixel.pixology.ui.qrscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivityQrScannerBinding

@Suppress("DEPRECATION")
class QrScannerActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var surfaceHolder: SurfaceHolder
    private var camera: Camera? = null
    private lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        surfaceHolder = binding.surfaceView.holder
        surfaceHolder.addCallback(this)

        barcodeScanner = BarcodeScanning.getClient()

        binding.ScanBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera = Camera.open()
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
            startAutoFocus()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun startCamera() {
        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
            startAutoFocus()
            captureCameraFrame()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start camera preview", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun startAutoFocus() {
        val parameters = camera?.parameters
        parameters?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        camera?.parameters = parameters
        camera?.autoFocus { _, _ -> }
    }

    private fun captureCameraFrame() {
        camera?.takePicture(null, null) { data, camera ->
            val bitmap = data.toBitmap()
            processImage(bitmap)
            camera.startPreview()
        }
    }

    private fun ByteArray.toBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(this, 0, size)
    }

    private fun processImage(imageBitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(imageBitmap, 0)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    for (barcode in barcodes) {
                        val barcodeValue = barcode.rawValue
                        if (barcodeValue != null) {
                            if (barcode.url != null) {
                                // Detected barcode is a URL, open it in browser
                                val url = barcode.url!!.url
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                                return@addOnSuccessListener
                            }
                            // Detected barcode is not a URL, display its raw value
                            Toast.makeText(
                                this,
                                "Scanned Text: $barcodeValue",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@addOnSuccessListener
                        }
                    }
                }
                // No barcode detected
                Toast.makeText(this, "No barcode detected", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to scan barcode", Toast.LENGTH_SHORT).show()
                it.printStackTrace()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
