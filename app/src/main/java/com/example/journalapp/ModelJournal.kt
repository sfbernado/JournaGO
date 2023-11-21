package com.example.journalapp

class ModelJournal {
    //Firebase journal variables
    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var categoryId: String = ""
    var journalUrl: String = ""
    var timestamp: Long = 0
    var viewCount: Long = 0
    var downloadsCount: Long = 0

    //empty constructor (required for Firebase)
    constructor()

    //parameterized constructor
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        journalUrl: String,
        timestamp: Long,
        viewCount: Long,
        downloadsCount: Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.journalUrl = journalUrl
        this.timestamp = timestamp
        this.viewCount = viewCount
        this.downloadsCount = downloadsCount
    }
}