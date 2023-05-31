package com.pixel.pixology.ui.scanandsearch

//noinspection SuspiciousImport

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pixel.pixology.R


class SearchResultsRVAdapter    // constructor for our variables.
    (// arraylist for storing our data and context
    private val dataModalArrayList: ArrayList<DataModal>, private val context: Context
) :
    RecyclerView.Adapter<SearchResultsRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inside on create view holder method we are inflating our layout file which we created.
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_result_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // inside on bind view holder method we are setting
        // data to each item of recycler view.
        val modal = dataModalArrayList[position]
        holder.titleTV.text = modal.title
        holder.snippetTV.text = modal.displayed_link
        holder.descTV.text = modal.snippet
        holder.itemView.setOnClickListener { // opening a link in your browser.
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(modal.link)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        // returning the size of array list.
        return dataModalArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // creating variables for our text view.
        val titleTV: TextView
        val descTV: TextView
        val snippetTV: TextView

        init {
            // initializing our views with their ids.
            titleTV = itemView.findViewById<TextView>(R.id.idTVTitle)
            descTV = itemView.findViewById<TextView>(R.id.idTVDescription)
            snippetTV = itemView.findViewById<TextView>(R.id.idTVSnippet)
        }
    }
}
