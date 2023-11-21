package com.example.journalapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.journalapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardUserActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityDashboardUserBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvSubTitle.setPaintFlags(binding.tvSubTitle.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        //set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //handle logout button click, sign out from firebase
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
    }

    private fun checkUser() {
        //check user is logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //user not logged in, go to login activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            //user logged in, change tvSubTitle text to user first name
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