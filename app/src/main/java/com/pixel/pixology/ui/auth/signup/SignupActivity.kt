package com.pixel.pixology.ui.auth.signup

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
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
import com.pixel.pixology.R
import com.pixel.pixology.databinding.ActivitySignupBinding
import com.pixel.pixology.ui.auth.login.LoginActivity
import com.pixel.pixology.ui.auth.login.LoginWthPhoneActivity
import com.pixel.pixology.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 100
    private var mGoogleSignInClient: GoogleSignInClient? = null

    var mAuth: FirebaseAuth? = null

    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)


        binding.signupWithPhone.setOnClickListener(View.OnClickListener {
            val i = Intent(this@SignupActivity, LoginWthPhoneActivity::class.java)
            startActivity(i)
        })

        binding.login.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
        })

        requestGoogleSignin()
        binding.signupWithGoogle.setOnClickListener(View.OnClickListener { signIn() })

        binding.signUpBtn.setOnClickListener(View.OnClickListener { createUser() })

    }

    private fun sendEmailVerification(user: FirebaseUser?) {

        user!!.sendEmailVerification()
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    // The verification email was sent successfully
                    Toast.makeText(this@SignupActivity, "Verification email sent", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // There was an error sending the verification email
                    Toast.makeText(
                        this@SignupActivity,
                        "Failed to send verification email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        mAuth!!.addAuthStateListener { firebaseAuth: FirebaseAuth ->
            val updatedUser = firebaseAuth.currentUser
            if (updatedUser != null && updatedUser.isEmailVerified) {
                // The user's email has been verified, so you can redirect them to the main activity
            }
        }

    }

    private fun createUser() {
        val em: String = binding.etEmail.getText().toString()
        val nm: String = binding.etUsername.getText().toString()
        val pas: String = binding.etPassword.getText().toString()
        val cp: String = binding.etConfirmPassword.getText().toString()

        if (TextUtils.isEmpty(nm)) {
            binding.etUsername.setError("username cant be empty")
            binding.etUsername.requestFocus()
        } else if (TextUtils.isEmpty(em)) {
            binding.etEmail.setError("email cant be empty")
            binding.etEmail.requestFocus()
        } else if (TextUtils.isEmpty(pas)) {
            binding.etPassword.setError("password  cant be empty")
            binding.etPassword.requestFocus()
        } else if (cp != pas) {
            Toast.makeText(this@SignupActivity, "password do not match try again !", Toast.LENGTH_SHORT)
                .show()
        } else {
//
//        }
            mAuth!!.createUserWithEmailAndPassword(em, pas)
                .addOnCompleteListener{task ->
                        if (task.isSuccessful) {
                            val user = mAuth!!.currentUser
                            sendEmailVerification(user)
                            //                        Toast.makeText(Signup.this, "SignUp Successfull", Toast.LENGTH_SHORT).show();
//
//                        Intent intent = new Intent(Signup.this, Login.class);
//                        startActivity(intent);
                        } else {
                            Toast.makeText(this@SignupActivity, "Sing Up error ", Toast.LENGTH_SHORT).show()
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
                    val intent = Intent(this@SignupActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@SignupActivity, "User authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth!!.currentUser
        if (user != null) {
            startActivity(Intent(this@SignupActivity, HomeActivity::class.java))
        }
    }


}