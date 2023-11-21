package com.example.journalapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.journalapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //initialize progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back button click, go back to previous screen
        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle have no account textview click, start register activity
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //handle login button click
        binding.btnLogin.setOnClickListener {
            //validate data
            validateData()
        }

        //handle forgot password textview click, start forgot password activity
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //get data
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email format
            binding.etEmail.error = "Invalid email format"
        } else if (password.isEmpty()) {
            //no password entered
            binding.etPassword.error = "Please enter password"
        }  else {
            //data is valid, login user
            this.email = email
            this.password = password
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        //show progress dialog
        progressDialog.setMessage("Logging In...")
        progressDialog.show()

        //login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //login success
                checkUser()
            }
            .addOnFailureListener { e ->
                //failed login
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed due to: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        //show progress dialog
        progressDialog.setMessage("Checking user...")
        progressDialog.show()

        //check if user has already setup profile
        val firebaseUser = firebaseAuth.currentUser

        //firebase database instance
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //check user
                    progressDialog.dismiss()
                    //get user type
                    val userType = snapshot.child("userType").value
                    if (userType == "user") {
                        //user is normal user, open user dashboard
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    } else if (userType == "admin") {
                        //user is admin, open admin dashboard
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    //failed getting user info
                }
            })
    }
}