package com.example.criminal_intent

import android.icu.text.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat.jumpDrawablesToCurrentState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminal_intent.database.Crime

class CrimeAdapter(
    private val callbacks: CrimeListFragment.Callbacks?
): ListAdapter<Crime, CrimeAdapter.CrimeViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }

    }

    class CrimeViewHolder(view: View, private val callbacks: CrimeListFragment.Callbacks?): RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var crime: Crime
        private val dateFormat = DateFormat.getInstanceForSkeleton(DateFormat.YEAR_ABBR_MONTH_WEEKDAY_DAY)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_crime_date)
        private val imgSolved: ImageView = itemView.findViewById(R.id.img_crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = dateFormat.format(this.crime.date)
            imgSolved.visibility = if (this.crime.isSolved) View.VISIBLE else View.INVISIBLE
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_crime, parent, false)
        return CrimeViewHolder(view, callbacks)
    }


    override fun onBindViewHolder(holder: CrimeViewHolder, position: Int) {
        val crime = getItem(position)
        holder.bind(crime)
    }

}