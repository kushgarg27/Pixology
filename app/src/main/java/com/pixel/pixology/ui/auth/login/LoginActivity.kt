package com.pixel.pixology.ui.auth.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivityLoginBinding
import com.pixel.pixology.ui.auth.signup.SignupActivity
import com.pixel.pixology.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityLoginBinding

    private companion object {
        private const val RC_SIGN_IN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.theme)
        }

        mAuth = FirebaseAuth.getInstance()

        setupGoogleSignIn()
        setupClickListeners()






    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        binding.loginWithPhone.setOnClickListener {

            val loaderView = layoutInflater.inflate(R.layout.loader, null)

            // Add the loader view to the activity's view
            addContentView(loaderView, RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT))

            // Delay starting the new activity for 2 seconds
            Handler().postDelayed({
                // Remove the loader view
                (loaderView.parent as? ViewGroup)?.removeView(loaderView)

                // Start the new activity
                val intent = Intent(this@LoginActivity, LoginWthPhoneActivity::class.java)
                startActivity(intent)
            }, 2000)
        }

        binding.newUserSignup.setOnClickListener {

            val loaderView = layoutInflater.inflate(R.layout.loader, null)

            // Add the loader view to the activity's view
            addContentView(loaderView, RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT))

            // Delay starting the new activity for 2 seconds
            Handler().postDelayed({
                // Remove the loader view
                (loaderView.parent as? ViewGroup)?.removeView(loaderView)

                // Start the new activity
                val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(intent)},2000)

        }

        binding.loginWithGoogle.setOnClickListener {
            signIn()
        }

        binding.loginBtn.setOnClickListener {
            logInUser()
        }
    }



    private fun logInUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.error = "Email cannot be empty"
            binding.etEmail.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.error = "Password cannot be empty"
            binding.etPassword.requestFocus()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "Log in Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                } else {
                    Toast.makeText(this@LoginActivity, "Sign in Error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        binding.progressBar.visibility = View.VISIBLE

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, "User authentication failed", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
            }

    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        if (user != null) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        }
    }
}