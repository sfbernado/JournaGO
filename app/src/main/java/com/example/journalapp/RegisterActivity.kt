package com.example.journalapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.journalapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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

        //handle register button click
        binding.btnRegister.setOnClickListener {
            //validate data
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        //get data
        name = binding.etName.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        //validate data
        if (name.isEmpty()) {
            //empty name
            binding.etName.error = "Please enter name"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email format
            binding.etEmail.error = "Invalid email format"
        } else if (password.isEmpty()) {
            //empty password
            binding.etPassword.error = "Please enter password"
        }  else if (confirmPassword.isEmpty()) {
            //empty confirm password
            binding.etConfirmPassword.error = "Please confirm password"
        } else if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Password doesn't match"
        } else {
            //data is valid, register user
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //show progress dialog
        progressDialog.setMessage("Creating account...")
        progressDialog.show()

        //create account - firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //account created
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                //failed creating account
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account due to: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        //save data in firebase realtime database
        //show progress dialog
        progressDialog.setMessage("Saving user info...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //get current user id
        val uid = firebaseAuth.currentUser!!.uid

        //create hashmap to store user data
        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = uid
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user" //possible values: admin, user
        hashMap["timestamp"] = timestamp

        //firebase database instance
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        //put data within hashmap in database
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //data stored in database
                //open profile
                progressDialog.dismiss()
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                //failed storing data in database
                progressDialog.dismiss()
                Toast.makeText(this, "Failed saving user info due to: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}