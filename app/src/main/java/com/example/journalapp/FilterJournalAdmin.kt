package com.example.journalapp

import android.widget.Filter

class FilterJournalAdmin: Filter {
    //arraylist in which we want to search
    var filterList: ArrayList<ModelJournal>

    //adapter in which filter need to be implemented
    var adapterJournalAdmin: AdapterJournalAdmin

    //constructor
    constructor(filterList: ArrayList<ModelJournal>, adapterJournalAdmin: AdapterJournalAdmin): super() {
        this.filterList = filterList
        this.adapterJournalAdmin = adapterJournalAdmin
    }


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        //this method will run in background thread
        //filter list will contain filtered results
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            //search filed not empty, searching something, perform search
            //change to lower case, to avoid case insensitive
            constraint = constraint.toString().lowercase()

            //store our filtered list
            val filteredModels = ArrayList<ModelJournal>()

            for (i in filterList.indices) {
                //check, search by title and category
                if (filterList[i].title.lowercase().contains(constraint) ||
                    filterList[i].description.lowercase().contains(constraint)) {
                    //add filtered data to list
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            //search field empty, not searching, return original/all/complete list
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //apply filter changes
        adapterJournalAdmin.journalArrayList = results!!.values as ArrayList<ModelJournal>

        //refresh list
        adapterJournalAdmin.notifyDataSetChanged()
    }

}