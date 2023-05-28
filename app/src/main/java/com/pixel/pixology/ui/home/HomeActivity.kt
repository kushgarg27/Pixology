package com.pixel.pixology.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivityHomeBinding
import com.pixel.pixology.ui.auth.login.LoginActivity
import com.pixel.pixology.ui.imagecompressor.ImageCompressorActivity
import com.pixel.pixology.ui.ocrScanner.OCRActivity

import com.pixel.pixology.ui.stegnography.ImageStegnographyActivity

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null

    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.theme)
            mAuth = FirebaseAuth.getInstance()

            binding.signout.setOnClickListener(View.OnClickListener {
                mAuth!!.signOut()
                requestgooglesignout()
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                Toast.makeText(this@HomeActivity, "Log out successful", Toast.LENGTH_SHORT).show()
            })
        }

        manageFunctions()




        binding.imageCompressorBtn.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@HomeActivity,
                    ImageCompressorActivity::class.java
                )
            )
        })


    }

    private fun manageFunctions() {
        movedNext()
    }

    private fun movedNext() {
        binding.imgStegnographyBtn.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@HomeActivity,
                    ImageStegnographyActivity::class.java
                )
            )
        })

        binding.ocrScannerBtn.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@HomeActivity, OCRActivity::class.java
                
                )
            )
        })

    }

    private fun requestgooglesignout() {
        val googleSignInClient: GoogleSignInClient =
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.revokeAccess()
            .addOnCompleteListener {
                if (it.isSuccessful()) {
                    // User has been signed out and access revoked
                    // Clear any user data stored in your app
                }


            }
    }
}