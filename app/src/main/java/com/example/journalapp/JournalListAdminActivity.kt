package com.example.journalapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.journalapp.databinding.ActivityJournalListAdminBinding
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JournalListAdminActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityJournalListAdminBinding

    //category id, title
    private var categoryId = ""
    private var category = ""

    //arrayList to hold list of data of type ModelJournal
    private lateinit var journalArrayList: ArrayList<ModelJournal>

    //adapter
    private lateinit var adapterJournalAdmin: AdapterJournalAdmin

    private companion object {
        const val TAG = "JOURNAL_LIST_ADMIN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJournalListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //get category id and category title from intent
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //set category title
        binding.tvTitle.text = category

        //load journals
        loadJournalList()

        //handle search
        binding.etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //before search
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //while searching
                try {
                    adapterJournalAdmin.filter!!.filter(s)
                } catch (e: Exception) {
                    Log.d(TAG , "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //after search
            }
        })

        //handle back button click
        binding.ibBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadJournalList() {
        //init arraylist
        journalArrayList = ArrayList()

        //get all journals
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before adding data into it
                    journalArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(ModelJournal::class.java)

                        //add to list
                        if (model != null) {
                            journalArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }

                    //setup adapter
                    adapterJournalAdmin = AdapterJournalAdmin(
                        this@JournalListAdminActivity,
                        journalArrayList
                    )

                    //set adapter to recyclerview
                    binding.rvJournals.adapter = adapterJournalAdmin
                }

                override fun onCancelled(error: DatabaseError) {
                    //in case of error
                    Toast.makeText(this@JournalListAdminActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}