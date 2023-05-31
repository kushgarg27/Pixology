package com.pixel.pixology.ui.scanandsearch

class DataModal(// getter and setter methods.
    // title for our search result.
    var title: String, // link of our search result.
    var link: String, displayed_link: String, snippet: String
) {

    // display link for our search result.
    var displayed_link: String

    // snippet for our search result.
    var snippet: String

    // constructor class.
    init {
        link = link
        this.displayed_link = displayed_link
        this.snippet = snippet
    }

}

