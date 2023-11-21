package com.example.journalapp

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        //function to format timestamp to dd/MM/yyyy
        fun formatTimestamp(timestamp: Long): String {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = timestamp
            //format dd/MM/yyyy
            return DateFormat.format("dd/MM/yyyy", calendar).toString()
        }

        fun loadJournalSize(journalUrl: String, title: String, tvSize: TextView) {
            val TAG = "JOURNAL_SIZE_TAG"

            //get metadata of journal using url from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(journalUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    //got file metadata
                    Log.d(TAG, "loadJournalSize: Got file metadata")
                    //get file size in MB
                    val bytes = storageMetadata.sizeBytes.toDouble()
                    Log.d(TAG, "loadJournalSize: Size in bytes: $bytes")
                    //convert bytes to KB, MB, GB
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    val gb = mb / 1024
                    if (gb >= 1) {
                        //size in GB
                        tvSize.text = "${String.format("%.2f", gb)} GB"
                    } else if (mb >= 1) {
                        //size in MB
                        tvSize.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        //size in KB
                        tvSize.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        //size in bytes
                        tvSize.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener { e ->
                    //failed to get metadata
                    Log.d(TAG, "loadJournalSize: Failed to get metadata due to: ${e.message}")
                }
        }

        fun loadJournalThumbnail(
            journalUrl: String,
            title: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            tvJournalPages: TextView?
        ) {
            val TAG = "JOURNAL_THUMBNAIL_TAG"

            //get reference of journal using url from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(journalUrl)
            ref.getBytes(Constants.MAX_BYTES_JOURNAL)
                .addOnSuccessListener { bytes ->
                    //get file size in MB
                    Log.d(TAG, "loadJournalSize: Size in bytes: $bytes")
                    pdfView.fromBytes(bytes)
                        .spacing(0)
                        .enableSwipe(false)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .enableDoubletap(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadJournalThumbnail: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadJournalThumbnail: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG, "loadJournalThumbnail: Pages: $nbPages")
                            progressBar.visibility = View.INVISIBLE
                            if (tvJournalPages != null) {
                                tvJournalPages.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener { e ->
                    //failed to get metadata
                    Log.d(TAG, "loadJournalSize: Failed to get metadata due to: ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, tvCategory: TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category = "${snapshot.child("category").value}"
                        //set category
                        tvCategory.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //failed getting category
                        Log.d("CATEGORY_TAG", "onCancelled: ${error.message}")
                    }
                })
        }

        fun deleteJournal(context: Context, journalId: String, journalUrl: String, title: String) {
            val TAG = "DELETE_JOURNAL_TAG"

            Log.d(TAG, "deleteJournal: Deleting journal: $title")
            //show progress
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting journal")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteJournal: Deleting journal using url: $journalUrl")
            //delete journal using url
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(journalUrl)
            storageRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteJournal: Journal deleted from firebase storage")
                    //deleted from firebase storage, now delete from firebase database
                    val ref = FirebaseDatabase.getInstance().getReference("Journals")
                    ref.child(journalId)
                        .removeValue()
                        .addOnSuccessListener {
                            Log.d(TAG, "deleteJournal: Journal deleted from firebase database")
                            //deleted from firebase database
                            progressDialog.dismiss()
                            Toast.makeText(context, "Journal deleted",
                                Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "deleteJournal: Failed to delete from database due to: ${e.message}")
                            //failed deleting from firebase database
                            progressDialog.dismiss()
                            Toast.makeText(context, "Failed to delete due to: ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "deleteJournal: Failed to delete from storage due to: ${e.message}")
                    //failed deleting from firebase storage
                    progressDialog.dismiss()
                    Toast.makeText(context, "Failed to delete due to: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }

        }

        fun incrementViewsCount(journalId: String) {
            //get current journal views count
            val ref = FirebaseDatabase.getInstance().getReference("Journals")
            ref.child(journalId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}".toInt()
                        //check if views count is 0
                        if (viewsCount == 0) {
                            //views count is 0, set to 1
                            viewsCount = 0
                        }
                        //increment views count
                        viewsCount++
                        //update views count
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = viewsCount
                        val dbRef = FirebaseDatabase.getInstance().getReference("Journals")
                        dbRef.child(journalId)
                            .updateChildren(hashMap)
                            .addOnSuccessListener {
                                //views count updated
                                Log.d("VIEWS_COUNT_TAG", "onDataChange: Views count updated")
                            }
                            .addOnFailureListener { e ->
                                //failed updating views count
                                Log.d("VIEWS_COUNT_TAG", "onDataChange: Failed to update views count due to: ${e.message}")
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //failed getting views count
                        Log.d("VIEWS_COUNT_TAG", "onCancelled: ${error.message}")
                    }
                })
        }

        fun incrementDownloadsCount(journalId: String) {
            Log.d("DOWNLOAD_COUNT_TAG", "incrementDownloadsCount: Incrementing downloads count...")
            //get current downloads count
            val ref = FirebaseDatabase.getInstance().getReference("Journals")
            ref.child(journalId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get current downloads count
                        var downloadsCount = "${snapshot.child("downloadsCount").value}".toInt()
                        Log.d("DOWNLOAD_COUNT_TAG", "onDataChange: Current downloads count: $downloadsCount")

                        if (downloadsCount == 0) {
                            downloadsCount = 0
                        }
                        //increment downloads count
                        downloadsCount++
                        //update downloads count
                        val hashMap = HashMap<String, Any>()
                        hashMap["downloadsCount"] = downloadsCount
                        val dbRef = FirebaseDatabase.getInstance().getReference("Journals")
                        dbRef.child(journalId)
                            .updateChildren(hashMap)
                            .addOnSuccessListener {
                                //downloads count updated
                                Log.d("DOWNLOAD_COUNT_TAG", "onDataChange: Downloads count updated")
                            }
                            .addOnFailureListener { e ->
                                //failed updating downloads count
                                Log.d("DOWNLOAD_COUNT_TAG", "onDataChange: Failed to update downloads count due to: ${e.message}")
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //failed to get data
                        Log.d("DOWNLOAD_COUNT_TAG", "onCancelled: Failed to get downloads count due to: ${error.message}")
                    }
                })

        }
    }
}