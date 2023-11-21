package com.example.journalapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.journalapp.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory: RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {
    //view binding
    private lateinit var binding: RowCategoryBinding

    //context
    private val context: Context

    //arrayList to hold list of data of type ModelCategory
    var categoryArrayList: ArrayList<ModelCategory>

    //arrayList to hold filtered list of data of type ModelCategory
    private var filterList: ArrayList<ModelCategory>

    //filter
    private var filterCategory: FilterCategory? = null

    //constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //inflate layout
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        //get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val timestamp = model.timestamp
        val uid = model.uid

        //set data
        holder.tvCategory.text = category

        //handle delete button click
        holder.ibDelete.setOnClickListener {
            //show delete confirm dialog
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Delete")
                .setMessage("Are you sure you want to delete category $category?")
                .setPositiveButton("Delete") { dialogInterface, which ->
                    //delete from db
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
            }
                .setNegativeButton("Cancel") { dialogInterface, which ->
                //cancel, dismiss dialog
                dialogInterface.dismiss()
            }
            //show dialog
            .show()
        }

        //handle click, start journal list admin activity
        holder.itemView.setOnClickListener {
            //pass category id and category to next activity to show journals of this category
            val intent = Intent(context, JournalListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        //get id of category to be deleted
        val id = model.id

        //db reference
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                //category deleted
                Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                //failed deleting category
                Toast.makeText(context, "Unable to delete due to: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getFilter(): Filter {
        if (filterCategory == null) {
            filterCategory = FilterCategory(filterList, this)
        }
        return filterCategory as FilterCategory
    }

    //viewHolder class for row_category.xml
    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //ui views of row_category.xml
        var tvCategory: TextView = binding.tvCategory
        var ibDelete: ImageButton = binding.ibDelete
    }
}