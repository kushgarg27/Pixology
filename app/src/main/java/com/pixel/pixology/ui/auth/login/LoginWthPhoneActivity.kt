package com.pixel.pixology.ui.auth.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivityLoginWthPhoneBinding
import com.pixel.pixology.ui.auth.otp.OtpActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginWthPhoneActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mCallbacks: OnVerificationStateChangedCallbacks? = null

    private lateinit var binding: ActivityLoginWthPhoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginWthPhoneBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.theme)
            mAuth = FirebaseAuth.getInstance()
            val gifImageView = findViewById<ImageView>(R.id.gifImageView)
            Glide.with(this).load(R.drawable.otpanim).into(gifImageView)
            binding.loginBtn.setOnClickListener(View.OnClickListener {
                if (binding.etPhone.getText().toString().trim { it <= ' ' }.isEmpty()) {
                    binding.etPhone.setError(" Phone number Invalid")
                    binding.etPhone.requestFocus()
                } else if (binding.etPhone.getText().toString().trim { it <= ' ' }.length < 10) {
                    binding.etPhone.setError(" Phone number Invalid")
                    binding.etPhone.requestFocus()
                } else {
                    sendOtp()
                }
            })
        }

    }

    private fun sendOtp() {

        binding.progressbar.setVisibility(View.VISIBLE)
        binding.loginBtn.setVisibility(View.INVISIBLE)


        mCallbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
            override fun onVerificationFailed(e: FirebaseException) {
                binding.progressbar.setVisibility(View.GONE)
                binding.loginBtn.setVisibility(View.VISIBLE)
                Toast.makeText(this@LoginWthPhoneActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            ) {
                binding.progressbar.setVisibility(View.GONE)
                binding.loginBtn.setVisibility(View.VISIBLE)
                val i = Intent(this@LoginWthPhoneActivity, OtpActivity::class.java)
                i.putExtra("phone", binding.etPhone.getText().toString().trim { it <= ' ' })
                i.putExtra("verficationId", verificationId)
                startActivity(i)
            }
        }
        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber("+91" + binding.etPhone.getText().toString()) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallbacks as OnVerificationStateChangedCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }
}