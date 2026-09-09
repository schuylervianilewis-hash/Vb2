package com.example.ime.quicknotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.R

class QuickNotesCardsAdapter(
    private val storage: QuickNotesStorage,
    private val onItemClick: (String) -> Unit,
    private val onItemLongClick: (View, String) -> Unit,
    private val onPinToggle: (String) -> Unit,
    private val onEdit: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<QuickNotesCardsAdapter.ViewHolder>() {

    private val items = mutableListOf<String>()

    fun updateData(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quick_note_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text = items[position]
        holder.tvNoteText.text = text

        val isPinned = storage.isPinned(text)
        if (isPinned) {
            holder.ivNotePin.setColorFilter(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_dark))
        } else {
            holder.ivNotePin.setColorFilter(0xFF94A3B8.toInt())
        }
        holder.ivNoteEdit.setColorFilter(0xFF94A3B8.toInt())
        holder.ivNoteDelete.setColorFilter(0xFF94A3B8.toInt())

        holder.itemView.setOnClickListener { onItemClick(text) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(holder.itemView, text)
            true
        }

        holder.ivNotePin.setOnClickListener { onPinToggle(text) }
        holder.ivNoteEdit.setOnClickListener { onEdit(text) }
        holder.ivNoteDelete.setOnClickListener { onDelete(text) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoteText: TextView = itemView.findViewById(R.id.tvNoteText)
        val ivNotePin: ImageView = itemView.findViewById(R.id.ivNotePin)
        val ivNoteEdit: ImageView = itemView.findViewById(R.id.ivNoteEdit)
        val ivNoteDelete: ImageView = itemView.findViewById(R.id.ivNoteDelete)
    }
}
