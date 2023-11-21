package com.example.journalapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.journalapp.databinding.ActivityViewJournalBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ViewJournalActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityViewJournalBinding

    //TAG
    private companion object {
        private const val TAG = "JOURNAL_VIEW_TAG"
    }

    //journal id
    var journalId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //get data from intent
        journalId = intent.getStringExtra("journalId")!!
        loadJournalDetails()

        //handle back button click, go back
        binding.ibBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadJournalDetails() {
        Log.d(TAG, "loadJournalDetails: Getting journal url from database")

        //get journal details
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.child(journalId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get url
                    val journalUrl = "${snapshot.child("journalUrl").value}"
                    Log.d(TAG, "onDataChange: Journal url: $journalUrl")

                    loadJournalFromUrl(journalUrl)
                }

                override fun onCancelled(error: DatabaseError) {
                    //failed to get data
                    Log.d(TAG, "onCancelled: Failed to get journal details: ${error.message}")
                }
            })
    }

    private fun loadJournalFromUrl(journalUrl: String) {
        Log.d(TAG, "loadJournalFromUrl: Loading journal from url")

        var storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(journalUrl)
        storageRef.getBytes(Constants.MAX_BYTES_JOURNAL)
            .addOnSuccessListener { bytes ->
                //load journal
                Log.d(TAG, "onSuccess: Journal loaded")
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange{page, pageCount ->
                        val currentPage = page + 1
                        binding.tvPage.text = "$currentPage/$pageCount"
                        Log.d(TAG, "onPageChange: Page: $currentPage/$pageCount")
                    }
                    .onError{t ->
                        //failed to load journal
                        Log.d(TAG, "onError: Failed to load journal: ${t.message}")
                        Toast.makeText(this, "Failed to load journal due to: ${t.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                    .onPageError { page, t ->
                        //failed to load page
                        Log.d(TAG, "onPageError: Failed to load page: $page due to: ${t.message}")
                        Toast.makeText(this, "Failed to load page: $page due to: ${t.message}",
                            Toast.LENGTH_SHORT).show()

                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                //failed to load journal
                Log.d(TAG, "onFailure: Failed to load journal: ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}