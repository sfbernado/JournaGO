package com.example.journalapp

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Layout
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.journalapp.databinding.ActivityDetailJournalBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

class DetailJournalActivity : AppCompatActivity() {
    private var PERMISSION_STORAGE_CODE = 1000

    //view binding
    private lateinit var binding: ActivityDetailJournalBinding

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //journal id, get from intent
    private var journalId = ""

    //journal data, get from database
    private var title = ""
    private var journalUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //initialize progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //get journal id from intent
        journalId = intent.getStringExtra("journalId")!!

        //increment views count
        MyApplication.incrementViewsCount(journalId)

        loadJournalDetails()

        //handle back button click, go back
        binding.ibBack.setOnClickListener {
            onBackPressed()
        }

        //handle read button click, open journal
        binding.btnRead.setOnClickListener {
            val intent = Intent(this, ViewJournalActivity::class.java)
            intent.putExtra("journalId", journalId)
            startActivity(intent)
        }

        //handle download button click, download journal
        binding.btnDownload.setOnClickListener {
            downloadJournal()
        }
    }

    private fun downloadJournal() {
        Log.d("DOWNLOAD_JOURNAL_TAG", "downloadBook: Downloading journal...")
        //progress dialog
        progressDialog.setMessage("Downloading...")
        progressDialog.show()

        //download journal
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(journalUrl)
        storageRef.getBytes(Constants.MAX_BYTES_JOURNAL)
            .addOnSuccessListener { bytes ->
                //download success, save to device
                progressDialog.dismiss()
                Log.d("DOWNLOAD_JOURNAL_TAG", "onSuccess: Journal downloaded")
                saveToStorage(bytes)
            }
            .addOnFailureListener { e ->
                //download failed
                progressDialog.dismiss()
                Log.d("DOWNLOAD_JOURNAL_TAG", "onFailure: Failed to download journal due to: ${e.message}")
                Toast.makeText(this, "Failed to download journal due to: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToStorage(bytes: ByteArray?) {
        Log.d("DOWNLOAD_JOURNAL_TAG", "saveToStorage: Saving journal to device...")
        //progress dialog
        progressDialog.setMessage("Saving...")
        progressDialog.show()

        //save to device
        val fileNameWithExtension = "$title ${System.currentTimeMillis()}.pdf"

        try {
            //download folder
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadFolder.exists()) {
                downloadFolder.mkdirs()
            }
            val filePath = downloadFolder.absolutePath + "/" + fileNameWithExtension
            //save to storage
            val fileOutputStream = FileOutputStream(filePath)
            fileOutputStream.write(bytes)
            fileOutputStream.close()

            progressDialog.dismiss()
            Log.d("DOWNLOAD_JOURNAL_TAG", "saveToStorage: Journal saved to device")
            Toast.makeText(this, "Journal saved to device", Toast.LENGTH_SHORT).show()
            MyApplication.incrementDownloadsCount(journalId)
        } catch (e: Exception) {
            //failed to save
            progressDialog.dismiss()
            Log.d("DOWNLOAD_JOURNAL_TAG", "saveToStorage: Failed to save journal due to: ${e.message}")
            Toast.makeText(this, "Failed to save journal due to: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadJournalDetails() {
        //get journal details
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.child(journalId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    title = "${snapshot.child("title").value}"
                    journalUrl = "${snapshot.child("journalUrl").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //convert timestamp to dd/MM/yyyy
                    val formattedDate = MyApplication.formatTimestamp(timestamp.toLong())

                    //load category
                    MyApplication.loadCategory(categoryId, binding.tvJournalCategory)

                    //load journal thumbnail, page count
                    MyApplication.loadJournalThumbnail(journalUrl, title, binding.pdfView,
                        binding.progressBar, binding.tvJournalPages)

                    //load size
                    MyApplication.loadJournalSize(journalUrl, title, binding.tvJournalSize)

                    //set data
                    binding.tvJournalTitle.text = title
                    binding.tvJournalDescription.text = description
                    binding.tvJournalViews.text = viewsCount
                    binding.tvJournalDate.text = formattedDate
                    binding.tvJournalDownloads.text = downloadsCount
                    binding.tvJournalPages.text = "0"
                }

                override fun onCancelled(error: DatabaseError) {
                    //failed to get data, show error message
                    Toast.makeText(this@DetailJournalActivity, "Error to load due to: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }
}