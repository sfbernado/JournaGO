package com.example.journalapp

class ModelCategory {
    // Firebase category variables
    var id: String = ""
    var category: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    // Empty constructor (required for Firebase)
    constructor()

    //parameterized constructor
    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }

}