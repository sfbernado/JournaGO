package com.example.journalapp

import android.annotation.SuppressLint
import android.widget.Filter

class FilterCategory: Filter {
    //arraylist in which we want to search
    private val filterList: ArrayList<ModelCategory>

    //adapter in which filter need to be implemented
    private var adapterCategory: AdapterCategory

    //constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory): super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        //this method will run in background thread
        //filter list will contain filtered results
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            //search filed not empty, searching something, perform search
            //change to upper case, to avoid case insensitive
            constraint = constraint.toString().uppercase()

            //store our filtered list
            val filteredModels = ArrayList<ModelCategory>()

            for (i in filterList.indices) {
                //check, search by title and category
                if (filterList[i].category.uppercase().contains(constraint)) {
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
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        //refresh list
        adapterCategory.notifyDataSetChanged()
    }
}