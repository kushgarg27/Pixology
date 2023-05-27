package com.pixel.pixology.ui.auth.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivityLoginBinding
import com.pixel.pixology.ui.auth.signup.SignupActivity
import com.pixel.pixology.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class LoginActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null
    private val RC_SIGN_IN = 100
    private var mGoogleSignInClient: GoogleSignInClient? = null

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.theme)
        }

        mAuth = FirebaseAuth.getInstance()

        binding.loginWithPhone.setOnClickListener(View.OnClickListener {
            val i = Intent(this@LoginActivity, LoginWthPhoneActivity::class.java)
            startActivity(i)
        })

        binding.newUserSignup.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        })

        requestGoogleSignin()

        binding.loginWithGoogle.setOnClickListener(View.OnClickListener { signIn() })

        binding.loginBtn.setOnClickListener(View.OnClickListener { logInUser() })


    }

    private fun logInUser() {

        val em: String = binding.etEmail.getText().toString()

        val pas: String = binding.etPassword.getText().toString()

        if (TextUtils.isEmpty(em)) {
            binding.etEmail.setError("email cant be empty")
            binding.etEmail.requestFocus()
        } else if (TextUtils.isEmpty(pas)) {
            binding.etPassword.setError("password cant be empty")
            binding.etPassword.requestFocus()
        } else {
            mAuth!!.signInWithEmailAndPassword(em, pas)
                .addOnCompleteListener{task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@LoginActivity, "Log in Successfull", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        } else {
                            Toast.makeText(this@LoginActivity, "Sing in Error", Toast.LENGTH_SHORT).show()
                        }
                }
        }

    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    private fun requestGoogleSignin() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
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

                //authenticating user with firebase using received token id
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {

        //getting user credentials with the help of AuthCredential method and also passing user Token Id.
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        //trying to sign in user using signInWithCredential and passing above credentials of user.
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate user to Profile Activity
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@LoginActivity, "User authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth!!.currentUser
        if (user != null) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        }
    }

}