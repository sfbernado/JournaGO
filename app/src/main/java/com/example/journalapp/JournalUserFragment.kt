package com.example.journalapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.journalapp.databinding.FragmentJournalUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JournalUserFragment//constructor
    () : Fragment() {
    //view binding
    private lateinit var binding: FragmentJournalUserBinding

    public companion object {
        private const val TAG = "JOURNAL_USER_TAG"

        //receive data from activity to load journal
        fun newInstance(categoryId: String, category: String, uid: String): JournalUserFragment {
            val fragment = JournalUserFragment()
            //put data to bundle
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    //arraylist of journal model
    private lateinit var journalLArrayList: ArrayList<ModelJournal>

    //adapter
    private lateinit var adapterJournalUser: AdapterJournalUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get data from bundle
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentJournalUserBinding.inflate(LayoutInflater.from(context), container, false)

        //load journal according to category
        Log.d(TAG, "onCreateView: Category: $category")
        if (category == "All") {
            //load all
            loadAllJournals()
        } else if (category == "Most Viewed") {
            //load most viewed
            loadMostViewedDownloadedJournals("viewsCount")
        } else if (category == "Most Downloaded") {
            //load most downloaded
            loadMostViewedDownloadedJournals("downloadsCount")
        } else {
            //load selected category
            loadCategorizedJournals()
        }

        //handle search
        binding.etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //before search
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //while searching
                try {
                    adapterJournalUser.filter.filter(s)
                } catch (e: Exception) {
                    Log.d("TAG", "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //after search
            }
        })

        return binding.root
    }

    private fun loadAllJournals() {
        //init arraylist
        journalLArrayList = ArrayList()

        //get all journals
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data into it
                journalLArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val modelJournal = ds.getValue(ModelJournal::class.java)
                    if (modelJournal != null) {
                        journalLArrayList.add(modelJournal)
                    }
                }

                //setup adapter
                adapterJournalUser = AdapterJournalUser(context!!, journalLArrayList)
                //set adapter to recyclerview
                binding.rvJournals.adapter = adapterJournalUser
            }

            override fun onCancelled(error: DatabaseError) {
                //in case of error
                Log.d(TAG, "onCancelled: ${error.message}")
            }
        })
    }

    private fun loadMostViewedDownloadedJournals(orderBy: String) {
        //init arraylist
        journalLArrayList = ArrayList()

        //get all journals
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.orderByChild(orderBy).limitToLast(10).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data into it
                journalLArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val modelJournal = ds.getValue(ModelJournal::class.java)
                    if (modelJournal != null) {
                        journalLArrayList.add(modelJournal)
                    }
                }

                //setup adapter
                adapterJournalUser = AdapterJournalUser(context!!, journalLArrayList)
                //set adapter to recyclerview
                binding.rvJournals.adapter = adapterJournalUser
            }

            override fun onCancelled(error: DatabaseError) {
                //in case of error
                Log.d(TAG, "onCancelled: ${error.message}")
            }
        })
    }

    private fun loadCategorizedJournals() {
        //init arraylist
        journalLArrayList = ArrayList()

        //get all journals
        val ref = FirebaseDatabase.getInstance().getReference("Journals")
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data into it
                journalLArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val modelJournal = ds.getValue(ModelJournal::class.java)
                    if (modelJournal != null) {
                        journalLArrayList.add(modelJournal)
                    }
                }

                //setup adapter
                adapterJournalUser = AdapterJournalUser(context!!, journalLArrayList)
                //set adapter to recyclerview
                binding.rvJournals.adapter = adapterJournalUser
            }

            override fun onCancelled(error: DatabaseError) {
                //in case of error
                Log.d(TAG, "onCancelled: ${error.message}")
            }
        })
    }

}