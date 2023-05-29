package com.pixel.pixology.ui.stegnography

import com.pixel.pixology.ui.stegnography.EncodeActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pixel.pixology.databinding.ActivityImageStegnographyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageStegnographyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageStegnographyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageStegnographyBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)


        binding.encodeButton.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@ImageStegnographyActivity,
                    EncodeActivity::class.java
                )
            )
        })

        binding.decodeButton.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@ImageStegnographyActivity,
                    DecodeActivity::class.java
                )
            )
        })

    }
}