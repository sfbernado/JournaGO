package com.example.journalapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.journalapp.databinding.RowJournalUserBinding

class AdapterJournalUser: RecyclerView.Adapter<AdapterJournalUser.HolderJournalUser>, Filterable {
    //view binding
    private lateinit var binding: RowJournalUserBinding

    //context
    private var context: Context

    //arrayList to hold list of data of type ModelJournal
    var journalArrayList: ArrayList<ModelJournal>

    //arrayList to hold filtered list of data of type ModelJournal
    private val filterList: ArrayList<ModelJournal>

    //filter
    private var filterJournalUser: FilterJournalUser? = null

    //constructor
    constructor(context: Context, journalArrayList: ArrayList<ModelJournal>) {
        this.context = context
        this.journalArrayList = journalArrayList
        this.filterList = journalArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderJournalUser {
        //inflate layout
        binding = RowJournalUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderJournalUser(binding.root)
    }

    override fun getItemCount(): Int {
        return journalArrayList.size
    }

    override fun onBindViewHolder(holder: HolderJournalUser, position: Int) {
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

        //handle item click, open detail journal activity
        holder.itemView.setOnClickListener {
            //open detail journal activity
            val intent = Intent(context, DetailJournalActivity::class.java)
            intent.putExtra("journalId", journalId)
            context.startActivity(intent)
        }
    }

    override fun getFilter(): Filter {
        if (filterJournalUser == null) {
            //init filter
            filterJournalUser = FilterJournalUser(filterList, this)
        }
        return filterJournalUser as FilterJournalUser
    }

    inner class HolderJournalUser(itemView: View): RecyclerView.ViewHolder(itemView) {
        //views from row_journal.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val tvSize = binding.tvSize
        val tvDate = binding.tvDate
        val tvCategory = binding.tvCategory
    }
}