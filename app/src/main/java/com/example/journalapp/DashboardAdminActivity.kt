package com.example.journalapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.journalapp.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //adapter
    private lateinit var adapterCategory: AdapterCategory

    private companion object {
        const val TAG = "DASHBOARD_ADMIN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //handle search
        binding.etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //before search
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //while searching
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {
                    Log.d("TAG", "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //after search
            }
        })

        //handle logout button click, logout user
        binding.ibLogout.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener() { _, _ ->
                    firebaseAuth.signOut()
                    checkUser()
                })
                .setNegativeButton("No") { _, _ ->

                }
                .show()
        }

        //handle add category button click, start AddCategoryActivity
        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
        }

        //handle add journal button click, start AddJournalActivity
        binding.fabAddJournal.setOnClickListener {
            startActivity(Intent(this, AddJournalActivity::class.java))
        }
    }

    private fun loadCategories() {
        //init arraylist
        categoryArrayList = ArrayList()

        //get all categories from firebase
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to list
                    categoryArrayList.add(model!!)
                }

                //setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity,
                    categoryArrayList
                )

                //set adapter to recyclerview
                binding.rvCategory.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                //in case of error
                Toast.makeText(this@DashboardAdminActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkUser() {
        //check user is logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //user not logged in, go to login activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener() { _, _ ->
                firebaseAuth.signOut()
                checkUser()
                super.onBackPressed()
            })
            .setNegativeButton("No") { _, _ ->

            }
            .show()
    }
}