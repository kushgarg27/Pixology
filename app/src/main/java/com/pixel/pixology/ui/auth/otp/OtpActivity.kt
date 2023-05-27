package com.pixel.pixology.ui.auth.otp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivityOtpBinding
import com.pixel.pixology.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OtpActivity : AppCompatActivity() {

    var VerficationID: String? = null


    private lateinit var binding: ActivityOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.theme)

            binding.phoneNUm.setText("+91" + intent.getStringExtra("phone"))
            VerficationID = intent.getStringExtra("verficationId")
            binding.tvResend.setOnClickListener(View.OnClickListener {
                Toast.makeText(
                    this@OtpActivity,
                    "otp resend successfully ",
                    Toast.LENGTH_SHORT
                ).show()
            })
            binding.btnVerify.setOnClickListener(View.OnClickListener {
                binding.progressbar.setVisibility(View.VISIBLE)
                binding.btnVerify.setVisibility(View.INVISIBLE)
                if (binding.etOtp.getText().toString().trim { it <= ' ' }.isEmpty()) {
                    binding.etOtp.setError("otp ccant be empty")
                    binding.etOtp.requestFocus()
                } else {
                    if (VerficationID != null) {
                        val code: String = binding.etOtp.getText().toString().trim { it <= ' ' }
                        val credential = PhoneAuthProvider.getCredential(VerficationID!!, code)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this@OtpActivity, HomeActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    binding.progressbar.setVisibility(View.VISIBLE)
                                    binding.btnVerify.setVisibility(View.INVISIBLE)
                                    startActivity(intent)
                                } else {
                                    binding.progressbar.setVisibility(View.GONE)
                                    binding.btnVerify.setVisibility(View.VISIBLE)
                                    Toast.makeText(this@OtpActivity, "otp not valid", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                    }
                }
            })
        }

    }
}