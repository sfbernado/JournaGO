package com.example.journalapp

import android.app.ProgressDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.journalapp.databinding.ActivityAddCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddCategoryActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityAddCategoryBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
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

        //handle back button click, navigate to dashboard admin activity
        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle add category button click
        binding.btnSubmit.setOnClickListener {
            //validate data
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        //get data
        category = binding.etCategory.text.toString().trim()

        //validate data
        if (category.isEmpty()) {
            //category name is empty
            binding.etCategory.error = "Please enter category title"
        }
        else {
            //data is valid, add category to db
            addCategoryToDb()
        }
    }

    private fun addCategoryToDb() {
        //show progress
        progressDialog.setMessage("Adding category...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //setup data to add in db
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add to db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //category added
                progressDialog.dismiss()
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                //clear data
                binding.etCategory.setText("")
            }
            .addOnFailureListener { e ->
                //failed adding category
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}