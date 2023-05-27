package com.pixel.pixology.ui.auth.signup

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pixel.pixology.databinding.ActivitySignupBinding
import com.pixel.pixology.ui.auth.login.LoginActivity
import com.pixel.pixology.ui.auth.login.LoginWthPhoneActivity
import com.pixel.pixology.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 100
    private var mGoogleSignInClient: GoogleSignInClient? = null

    val auth = Firebase.auth

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manageFunctions()
    }

    private fun manageFunctions() {
        allIntent()
        requestGoogleSignin()
    }

    private fun allIntent() {
        binding.signupWithPhone.setOnClickListener{
            val i = Intent(this@SignupActivity, LoginWthPhoneActivity::class.java)
            startActivity(i)
        }

        binding.login.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signupWithGoogle.setOnClickListener { signIn() }

        binding.signUpBtn.setOnClickListener { createUser() }
    }

    private fun sendEmailVerification(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    // The verification email was sent successfully
                    Toast.makeText(this@SignupActivity, "Verification email sent", Toast.LENGTH_SHORT).show()
                    waitForEmailVerification(user)
                } else {
                    // There was an error sending the verification email
                    Toast.makeText(this@SignupActivity, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

    private fun waitForEmailVerification(user: FirebaseUser) {
        auth.addAuthStateListener { firebaseAuth: FirebaseAuth ->
            val updatedUser = firebaseAuth.currentUser
            if (updatedUser != null && updatedUser.isEmailVerified) {
                val i = Intent(this@SignupActivity, HomeActivity::class.java)
                startActivity(i)
                finish()
            }
        }

    }

    private fun createUser() {
        val em: String = binding.etEmail.text.toString()
        val nm: String = binding.etUsername.text.toString()
        val pas: String = binding.etPassword.text.toString()
        val cp: String = binding.etConfirmPassword.text.toString()

        if (TextUtils.isEmpty(nm)) {
            binding.etUsername.error = "username cant be empty"
            binding.etUsername.requestFocus()
        } else if (TextUtils.isEmpty(em)) {
            binding.etEmail.error = "email cant be empty"
            binding.etEmail.requestFocus()
        } else if (!isValidEmail(em)) {
            binding.etEmail.error = "Invalid email address"
            binding.etEmail.requestFocus()
        } else if (TextUtils.isEmpty(pas)) {
            binding.etPassword.error = "password  cant be empty"
            binding.etPassword.requestFocus()
        } else if (cp != pas) {
            Toast.makeText(this@SignupActivity, "passwords do not match, please try again!", Toast.LENGTH_SHORT).show()
        } else {
            binding.progressBar.visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(em, pas)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        sendEmailVerification(user)

                    } else {
                        Toast.makeText(this@SignupActivity, "Sign Up error", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun requestGoogleSignin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)

                // Authenticating user with Firebase using received token id
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        binding.progressBar.visibility = View.VISIBLE

        // Getting user credentials with the help of AuthCredential method and also passing user Token Id.
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Trying to sign in user using signInWithCredential and passing above credentials of user.
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate user to Profile Activity
                    val intent = Intent(this@SignupActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@SignupActivity, "User authentication failed", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
            }
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            startActivity(Intent(this@SignupActivity, HomeActivity::class.java))
            finish()
        }
    }


}