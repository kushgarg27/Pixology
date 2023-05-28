package com.pixel.pixology.ui.imagecompressor

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pixel.pixology.databinding.ActivityImageCompressorBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class ImageCompressorActivity : AppCompatActivity() {

    //    File originalImg, compressedImg;
    val RESULT_IMAGE = 1

    private lateinit var storageVolume: StorageVolume

    lateinit var originalImage: Bitmap

    var quality: Int = 0

    lateinit var bytesArray: ByteArray

    var save: ProgressDialog? = null

    private lateinit var binding: ActivityImageCompressorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageCompressorBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        askPermissions()


//        filePath = path.getAbsolutePath();
//
//        if(!path.exists()){
//            path.mkdir();
//        }
        binding.btnPickImg.setOnClickListener { openGallery() }

        binding.seekQuality.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                binding.txtQuality.text = "Quality: $i"
                seekBar.max = 100
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.btnCompress.setOnClickListener {
            quality = binding.seekQuality.progress
            /*
                            int width = Integer.parseInt(binding.etWidth.getText().toString());
                int height = Integer.parseInt(binding.etHeight.getText().toString());

                try {
                    compressedImg = new Compressor(ImgCompressor.this)
                            .setMaxWidth(width)
                            .setMaxHeight(height)
                            .setQuality(quality)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(filePath)
                            .compressToFile(originalImg);
                    File finalFile = new File(filePath, originalImg.getName());
                    Bitmap finalBitmap = BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                    binding.imgCompressed.setImageBitmap(finalBitmap);
                    binding.txtCompressed.setText("Size: " + Formatter.formatShortFileSize(ImgCompressor.this, finalFile.length()));
                    Toast.makeText(ImgCompressor.this, "Image Compressed and Saved!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ImgCompressor.this, "Error While Compressing!", Toast.LENGTH_SHORT).show();
                }
                ---------------------

                final Bitmap imgToSave = originalImage;
                Thread PerformEncoding = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveToInternalStorage(imgToSave);
                    }
                });
                save = new ProgressDialog(ImgCompressor.this);
                save.setMessage("Saving, Please Wait...");
                save.setTitle("Saving Image");
                save.setIndeterminate(false);
                save.setCancelable(false);
                save.show();
                PerformEncoding.start();
            */
            val storageManager = getSystemService(STORAGE_SERVICE) as StorageManager
            storageVolume = storageManager.storageVolumes[0] // internal storage
            val byteArrayOutputStream = ByteArrayOutputStream()
            originalImage.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                byteArrayOutputStream
            )
            bytesArray = byteArrayOutputStream.toByteArray()
            val bitmapCompressedImage =
                BitmapFactory.decodeByteArray(bytesArray, 0, bytesArray.size)
            binding.imgCompressed.setImageBitmap(bitmapCompressedImage)
        }


        binding.saveTxt.setOnClickListener {
            if (binding.imgCompressed != null) {
                save = ProgressDialog(this@ImageCompressorActivity)
                save!!.setMessage("Saving, Please Wait...")
                save!!.setTitle("Saving Image")
                save!!.isIndeterminate = false
                save!!.setCancelable(false)
                save!!.show()
                val progressRunnable = Runnable { save!!.cancel() }
                val pdCanceller = Handler()
                pdCanceller.postDelayed(progressRunnable, 3000)
                val Name = binding.etFileName.text.toString()
                var fileOutput: File? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    fileOutput =
                        File(storageVolume.directory!!.path + "/Download/" + Name + ".jpeg")
                }
                try {
                    val fileOutputStream = FileOutputStream(fileOutput)
                    fileOutputStream.write(bytesArray)
                    fileOutputStream.close()
                } catch (e: FileNotFoundException) {
                    throw RuntimeException(e)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Compressed Image not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, RESULT_IMAGE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_IMAGE && resultCode == RESULT_OK) {
            binding.btnCompress.visibility = View.VISIBLE
            val imageUri = data!!.data
            //
//            try {
//                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                binding.imgOriginal.setImageBitmap(selectedImage);
//                originalImg = new File(imageUri.getPath().replace("raw/", ""));
//                binding.txtOriginal.setText("Size: " + Formatter.formatShortFileSize(this, originalImg.length()));
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
//            }
            try {
                originalImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                binding.imgOriginal.setImageBitmap(originalImage)
                val oriImageSize = imageUri!!.path?.let { File(it) }
                //                binding.txtOriginal.setText("Size: " + Formatter.formatShortFileSize(this, oriImageSize.length()));
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("TAG", "Error : $e")
            }
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {}
                override fun onPermissionRationaleShouldBeShown(
                    list: List<com.karumi.dexter.listener.PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()

    }
}