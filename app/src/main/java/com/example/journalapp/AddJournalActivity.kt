package com.example.journalapp

import android.app.AlertDialog
import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.journalapp.databinding.ActivityAddJournalBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AddJournalActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityAddJournalBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri of uploaded journal
    private var journalUri: Uri? = null

    //TAG
    private val TAG = "ADD_JOURNAL_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadJournalCategories()

        //initialize progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back button click, navigate to dashboard admin activity
        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle handle click, pick category
        binding.etJournalCategory.setOnClickListener {
            //pick category
            categoryPickDialog()
        }

        //handle click, upload journal
        binding.btnAddFile.setOnClickListener {
            //upload journal
            journalUploadIntent()
        }

        //handle click, start upload journal
        binding.btnUpload.setOnClickListener {
            //validate data
            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: Validating data...")

        //get data
        title = binding.etJournalTitle.text.toString().trim()
        description = binding.etJournalDescription.text.toString().trim()
        category = binding.etJournalCategory.text.toString().trim()

        //validate data
        if (title.isEmpty()) {
            //title is empty
            binding.etJournalTitle.error = "Please enter title"
        } else if (description.isEmpty()) {
            //description is empty
            binding.etJournalDescription.error = "Please enter description"
        } else if (category.isEmpty()) {
            //category is empty
            Toast.makeText(this, "Please pick category", Toast.LENGTH_SHORT).show()
        } else if (journalUri == null) {
            //journal uri is null
            Toast.makeText(this, "Please upload journal file", Toast.LENGTH_SHORT).show()
        } else {
            //data is valid, upload journal
            uploadJournalToStorage()
        }
    }

    private fun uploadJournalToStorage() {
        Log.d(TAG, "uploadJournalToStorage: Uploading journal to storage...")

        //show progress
        progressDialog.setMessage("Uploading journal...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //path of journal in firebase storage
        val filePathAndName = "Journals/$timestamp"

        //upload journal
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(journalUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadJournalToStorage: Journal uploaded...")
                //journal uploaded, get url of uploaded journal
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadJournalUrl = "${uriTask.result}"
                uploadJournalToDb(uploadJournalUrl, timestamp)
            }
            .addOnFailureListener { e ->
                //failed uploading journal
                Log.d(TAG, "uploadJournalToStorage: Failed uploading journal due to: ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadJournalToDb(uploadJournalUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadJournalToDb: Uploading journal to db...")

        //show progress
        progressDialog.setMessage("Saving journal...")
        progressDialog.show()

        //uid of current user
        val uid = firebaseAuth.uid

        //setup data to upload
        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["categoryId"] = selectedCategoryId
        hashMap["journalUrl"] = uploadJournalUrl
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        //path to store journal data
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //journal data uploaded
                Log.d(TAG, "uploadJournalToDb: Journal uploaded to db...")
                progressDialog.dismiss()
                Toast.makeText(this, "Journal uploaded", Toast.LENGTH_SHORT).show()
                //clear data
                binding.etJournalTitle.setText("")
                binding.etJournalDescription.setText("")
                binding.etJournalCategory.setText("")
                journalUri = null
            }
            .addOnFailureListener { e ->
                //failed uploading journal data
                Log.d(TAG, "uploadJournalToDb: Failed uploading journal data due to: ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload journal data due to: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadJournalCategories() {
        Log.d(TAG, "loadJournalCategories: Loading journal categories...")

        //init arraylist
        categoryArrayList = ArrayList()

        //get all categories from firebase
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing category pick dialog...")

        //get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        //dialog
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                //handle item clicks
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                //set category to edittext
                binding.etJournalCategory.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected category Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun journalUploadIntent() {
        Log.d(TAG, "journalUploadIntent: Starting journal upload intent...")

        //upload journal intent
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        journalActivityResultLauncher.launch(intent)
    }

    val journalActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "journalActivityResultLauncher: Journal uploaded")
            journalUri = result.data!!.data
        } else {
            Log.d(TAG, "journalActivityResultLauncher: Journal upload cancelled")
            Toast.makeText(this, "Journal upload cancelled", Toast.LENGTH_SHORT).show()
        }
    })
}