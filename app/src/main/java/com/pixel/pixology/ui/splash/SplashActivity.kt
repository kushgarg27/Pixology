@file:Suppress("DEPRECATION")

package com.pixel.pixology.ui.splash

import android.preference.PreferenceManager
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivitySplashBinding

import com.pixel.pixology.ui.auth.signup.SignupActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")

@Suppress("DEPRECATION")

class SplashActivity : AppCompatActivity() {

    private var binding: ActivitySplashBinding? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater, null, false)
        setContentView(binding?.root)
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(R.color.theme)
        val th: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(5000)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    val intent = Intent(this@SplashActivity, SignupActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        th.start()

    }
}