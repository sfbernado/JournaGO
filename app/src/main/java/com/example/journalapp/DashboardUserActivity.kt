package com.example.journalapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.journalapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityDashboardUserBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist of category
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //view pager adapter
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    //arraylist of journal model
    private lateinit var journalLArrayList: ArrayList<ModelJournal>

    //adapter
    private lateinit var adapterJournalUser: AdapterJournalUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //setup viewpager
        setupWithViewPagerAdapter(binding.vpDashboard)
        binding.tlDashboard.setupWithViewPager(binding.vpDashboard)

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

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this)

        //initiate arraylist
        categoryArrayList = ArrayList()

        //get all categories from firebase
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data into it
                categoryArrayList.clear()

                //get static categories
                //add data to model
                val modelAll = ModelCategory("01", "All", 1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", 1, "")
                val modelMostDownloaded = ModelCategory("01", "Most Downloaded", 1, "")

                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)

                //add to viewPagerAdapter
                viewPagerAdapter.addFragment(
                    JournalUserFragment.newInstance(
                        modelAll.id,
                        modelAll.category,
                        modelAll.uid
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    JournalUserFragment.newInstance(
                        modelMostViewed.id,
                        modelMostViewed.category,
                        modelMostViewed.uid
                    ), modelMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    JournalUserFragment.newInstance(
                        modelMostDownloaded.id,
                        modelMostDownloaded.category,
                        modelMostDownloaded.uid
                    ), modelMostDownloaded.category
                )

                //refresh adapter
                viewPagerAdapter.notifyDataSetChanged()

                //load from firebase
                for (ds in snapshot.children) {
                    //get data
                    val modelCategory = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(modelCategory!!)
                    //add to viewPagerAdapter
                    viewPagerAdapter.addFragment(
                        JournalUserFragment.newInstance(
                            modelCategory.id,
                            modelCategory.category,
                            modelCategory.uid
                        ), modelCategory.category
                    )
                    //refresh adapter
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //in case of error
                Log.d("TAG", "onCancelled: ${error.message}")
            }
        })

        //set adapter to viewpager
        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fm, behavior) {
        //hold list of fragments
        private val fragmentList: ArrayList<JournalUserFragment> = ArrayList()
        //list of titles of categories, for tabs
        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: JournalUserFragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
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
            //user logged in, change tvName text to user name index 0 or trim
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user name
                        val name = "${snapshot.child("name").value}"
                        //set to tvName
                        binding.tvName.text = name
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //in case of error
                        Log.d("TAG", "onCancelled: ${error.message}")
                    }
                })
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