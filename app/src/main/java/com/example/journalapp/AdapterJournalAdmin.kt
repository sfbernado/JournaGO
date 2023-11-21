package com.example.journalapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.journalapp.databinding.RowJournalAdminBinding

class AdapterJournalAdmin: RecyclerView.Adapter<AdapterJournalAdmin.HolderJournalAdmin>, Filterable {
    //view binding
    private lateinit var binding: RowJournalAdminBinding

    //context
    private var context: Context

    //arrayList to hold list of data of type ModelJournal
    var journalArrayList: ArrayList<ModelJournal>

    //arrayList to hold filtered list of data of type ModelJournal
    private val filterList: ArrayList<ModelJournal>

    //filter
    private var filterJournalAdmin: FilterJournalAdmin? = null

    //constructor
    constructor(context: Context, journalArrayList: ArrayList<ModelJournal>) {
        this.context = context
        this.journalArrayList = journalArrayList
        this.filterList = journalArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderJournalAdmin {
        //inflate layout
        binding = RowJournalAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderJournalAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return journalArrayList.size
    }

    override fun onBindViewHolder(holder: HolderJournalAdmin, position: Int) {
        //get data
        val model = journalArrayList[position]
        val journalId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val journalUrl = model.journalUrl
        val timestamp = model.timestamp
        //convert timestamp to dd/MM/yyyy
        val formattedDate = MyApplication.formatTimestamp(timestamp)

        //set data
        holder.tvTitle.text = title
        holder.tvDescription.text = description
        holder.tvDate.text = formattedDate

        //load journal details (size, page, category) from url
        //load size
        MyApplication.loadJournalSize(journalUrl, title, holder.tvSize)
        //load page
        MyApplication.loadJournalThumbnail(
            journalUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )
        //load category
        MyApplication.loadCategory(categoryId, holder.tvCategory)

        //handle more button click listener (show options like edit, delete etc)
        holder.ibMore.setOnClickListener {
            //show options menu
            moreOptionsDialog(model, holder)
        }

        //handle item click, open detail journal activity
        holder.itemView.setOnClickListener {
            //open detail journal activity
            val intent = Intent(context, DetailJournalActivity::class.java)
            intent.putExtra("journalId", journalId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(
        model: ModelJournal,
        holder: HolderJournalAdmin
    ) {
        //get id, url, title of journal
        val journalId = model.id
        val journalUrl = model.journalUrl
        val title = model.title

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("Choose Option")
            .setItems(options) { dialog, which ->
                //handle item clicks
                if (which == 0) {
                    //edit clicked
                    //start AddJournalActivity to edit journal
                    val intent = Intent(context, EditJournalActivity::class.java)
                    intent.putExtra("journalId", journalId)
                    context.startActivity(intent)
                } else if (which == 1) {
                    //show delete confirm dialog
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this journal?")
                        .setPositiveButton("DELETE") { dialog, which ->
                            //delete clicked
                            MyApplication.deleteJournal(context, journalId, journalUrl, title)
                        }
                        .setNegativeButton("CANCEL") { dialog, which ->
                            //cancel clicked
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            .show()
    }

    override fun getFilter(): Filter {
        if (filterJournalAdmin == null) {
            filterJournalAdmin = FilterJournalAdmin(filterList, this)
        }
        return filterJournalAdmin as FilterJournalAdmin
    }

    //view holder class for row_journal.xml
    inner class HolderJournalAdmin(itemView: View): RecyclerView.ViewHolder(itemView) {
        //views from row_journal.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val tvSize = binding.tvSize
        val tvDate = binding.tvDate
        val tvCategory = binding.tvCategory
        val ibMore = binding.ibMore

    }
}