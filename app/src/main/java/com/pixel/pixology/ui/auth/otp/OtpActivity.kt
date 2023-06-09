@file:Suppress("DEPRECATION")

package com.pixel.pixology.ui.auth.otp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goodiebag.pinview.Pinview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivityOtpBinding
import com.pixel.pixology.ui.auth.login.LoginActivity
import com.pixel.pixology.ui.auth.login.LoginWthPhoneActivity
import com.pixel.pixology.ui.auth.signup.SignupActivity
import com.pixel.pixology.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpActivity : AppCompatActivity() {

    private var verificationId: String? = null
    private lateinit var binding: ActivityOtpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)


        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = resources.getColor(R.color.theme)

        verificationId = intent.getStringExtra("verificationId")

        binding.tvResend.setOnClickListener {
           val i = Intent (this@OtpActivity,LoginWthPhoneActivity::class.java)
            startActivity(i)
            finish()
            Toast.makeText(this@OtpActivity, "OTP Resent", Toast.LENGTH_SHORT).show()
        }

        binding.etOtp.setPinViewEventListener(object : Pinview.PinViewEventListener {
            override fun onDataEntered(pinview: Pinview?, fromUser: Boolean) {
                val otp = pinview?.value
                if (!otp.isNullOrEmpty() && otp.length == 6) {
                    verifyOtp(otp)
                }
            }
        })
    }

    private fun verifyOtp(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val loaderView = layoutInflater.inflate(R.layout.loader, null)

                    // Add the loader view to the activity's view
                    addContentView(loaderView, RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT))

                    // Delay starting the new activity for 2 seconds
                    Handler().postDelayed({
                        // Remove the loader view
                        (loaderView.parent as? ViewGroup)?.removeView(loaderView)


                        // Start the new activity
                        val intent = Intent(this@OtpActivity, HomeActivity::class.java)
                        startActivity(intent)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK},2000)



                } else {
                    Toast.makeText(this@OtpActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
