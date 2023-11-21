package com.example.journalapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.journalapp.databinding.ActivityEditJournalBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditJournalActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityEditJournalBinding

    private companion object {
        private const val TAG = "JOURNAL_EDIT_TAG"
    }

    //journal id
    private var journalId = ""

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arrayList to hold category
    private lateinit var categoryArrayList: ArrayList<String>

    //arrayList to hold category ids
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //get journal id from intent
        journalId = intent.getStringExtra("journalId")!!

        //initialize progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadJournalDetails()

        //handle click, go back
        binding.ibBack.setOnClickListener {
            onBackPressed()
        }

        //handle click, pick category
        binding.etJournalCategory.setOnClickListener {
            //pick category
            categoryPickDialog()
        }

        //handle click, update journal
        binding.btnSave.setOnClickListener {
            //validate data
            validateData()
        }
    }

    private fun loadJournalDetails() {
        Log.d(TAG, "loadJournalDetails: Loading journal details...")
        //show progress
        progressDialog.setMessage("Loading journal details")
        progressDialog.show()

        //load journal details
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.child(journalId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    selectedCategoryId = "${snapshot.child("categoryId").value}"
                    val title = "${snapshot.child("title").value}"
                    val description = "${snapshot.child("description").value}"

                    //set data
                    binding.etJournalTitle.setText(title)
                    binding.etJournalDescription.setText(description)

                    //load journal details using category id
                    val refJournalCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refJournalCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = "${snapshot.child("category").value}"
                                //set category
                                binding.etJournalCategory.text = category
                            }

                            override fun onCancelled(error: DatabaseError) {
                                //failed getting category
                                Log.d(TAG, "onCancelled: ${error.message}")
                            }
                        })

                    //dismiss progress
                    progressDialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    //failed loading journal details
                    Log.d(TAG, "onCancelled: ${error.message}")
                    //dismiss progress
                    progressDialog.dismiss()
                    Toast.makeText(this@EditJournalActivity, "Failed to load journal details",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private var title = ""
    private var description = ""

    private fun validateData() {
        Log.d(TAG, "validateData: Validating data...")

        //get data
        title = binding.etJournalTitle.text.toString().trim()
        description = binding.etJournalDescription.text.toString().trim()

        //validate data
        if (title.isEmpty()) {
            Log.d(TAG, "validateData: Title is empty")
            binding.etJournalTitle.error = "Please enter title"
        }
        else if (description.isEmpty()) {
            Log.d(TAG, "validateData: Description is empty")
            binding.etJournalDescription.error = "Please enter description"
        }
        else if (selectedCategoryId.isEmpty()) {
            Log.d(TAG, "validateData: Category is empty")
            Toast.makeText(this, "Please pick category", Toast.LENGTH_SHORT).show()
        }
        else {
            Log.d(TAG, "validateData: Data is valid")
            updateJournal()
        }
    }

    private fun updateJournal() {
        Log.d(TAG, "updateJournal: Updating journal...")

        //show progress
        progressDialog.setMessage("Updating journal")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //setup data to update
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["categoryId"] = selectedCategoryId
        hashMap["timestamp"] = timestamp

        //update to db
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.child(journalId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateJournal: Journal updated")
                //updated, dismiss progress
                progressDialog.dismiss()
                Toast.makeText(this, "Journal updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "updateJournal: Failed to update due to: ${e.message}")
                //failed, dismiss progress, get and show error message
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategory = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing category pick dialog...")

        //get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i]
        }

        //dialog
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                //handle item clicks
                selectedCategoryId = categoryIdArrayList[which]
                selectedCategory = categoryArrayList[which]
                //set category to edittext
                binding.etJournalCategory.text = selectedCategory

                Log.d(TAG, "categoryPickDialog: Selected category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected category Title: $selectedCategory")
            }
            .show()
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories...")

        //init arraylist
        categoryArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        //get all categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear arraylist
                categoryArrayList.clear()
                categoryIdArrayList.clear()

                for (ds in snapshot.children) {
                    //get data
                    val categoryId = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    //add to arraylist
                    categoryArrayList.add(category)
                    categoryIdArrayList.add(categoryId)

                    Log.d(TAG, "onDataChange: category: $categoryId")
                    Log.d(TAG, "onDataChange: category: $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
            }
        })
    }
}