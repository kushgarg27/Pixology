package com.pixel.pixology.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
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
        binding = ActivityLoginWthPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.loginBtn.setOnClickListener {
            val phoneNumber = binding.etPhone.text.toString().trim()

            if (phoneNumber.isEmpty()) {
                binding.etPhone.error = "Phone number is required"
                binding.etPhone.requestFocus()
            } else if (phoneNumber.length < 10) {
                binding.etPhone.error = "Invalid phone number"
                binding.etPhone.requestFocus()
            } else {
                verifyWithSafetyNet(phoneNumber)
            }
        }
    }

    private fun verifyWithSafetyNet(phoneNumber: String) {
        binding.progressbar.visibility = View.VISIBLE
        binding.loginBtn.visibility = View.INVISIBLE
        sendOtp(phoneNumber)

//        SafetyNet.getClient(this).verifyWithRecaptcha(getString(R.string.recaptcha_site_key))
//            .addOnSuccessListener(this) { response ->
//                val userResponseToken = response.tokenResult
//                if (userResponseToken.isNotEmpty()) {
//                    sendOtp(phoneNumber)
//                }
//            }
//            .addOnFailureListener(this) { e ->
//                if (e is ApiException) {
//                    val statusCode = e.statusCode
//                    Toast.makeText(this@LoginWthPhoneActivity, "Error: $statusCode", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this@LoginWthPhoneActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//                binding.progressbar.visibility = View.GONE
//                binding.loginBtn.visibility = View.VISIBLE
//            }
    }

    private fun sendOtp(phoneNumber: String) {
        mCallbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval of verification code is done by Firebase
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.progressbar.visibility = View.GONE
                binding.loginBtn.visibility = View.VISIBLE
                Toast.makeText(this@LoginWthPhoneActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            ) {
                binding.progressbar.visibility = View.GONE
                binding.loginBtn.visibility = View.VISIBLE
                val intent = Intent(this@LoginWthPhoneActivity, OtpActivity::class.java)
                intent.putExtra("phoneNumber", phoneNumber)
                intent.putExtra("verificationId", verificationId)
                startActivity(intent)
            }
        }

        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber("+91$phoneNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallbacks as OnVerificationStateChangedCallbacks) // OnVerificationStateChangedCallbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
