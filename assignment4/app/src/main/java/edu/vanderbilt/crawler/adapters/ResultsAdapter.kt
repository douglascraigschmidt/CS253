package edu.vanderbilt.crawler.adapters

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.vanderbilt.crawler.R

/**
 * Created by monte on 2017-09-04.
 */

class Holder(val textView: TextView) : RecyclerView.ViewHolder(textView)

class Adapter(val list: MutableList<String> = mutableListOf()) : RecyclerView.Adapter<Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(TextView(parent.context).apply {
            textSize = 20f
            background = context.obtainStyledAttributes(
                    arrayOf(R.attr.selectableItemBackground).toIntArray()).getDrawable(0)
            isClickable = true
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        })
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.textView.text = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun push(text: String) {
        list.add(0, text)
        notifyItemInserted(0)
    }

    fun pop() {
        list.removeAt(list.count())
        notifyItemRemoved(list.count())
    }

    fun add(text: String) {
        list += text
        notifyItemInserted(list.count())
    }
}