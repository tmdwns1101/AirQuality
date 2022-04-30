package com.example.airquality.adpater

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.airquality.databinding.ItemRecentLocationBinding
import com.example.airquality.repository.room.entity.RecentLocation

class RecentLocationAdapter: RecyclerView.Adapter<RecentLocationAdapter.Holder>() {

    private var recentLocations: List<RecentLocation> = ArrayList()

    class Holder(val binding: ItemRecentLocationBinding): RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener {
        fun onItemClick(v: View, item: RecentLocation)
        fun onItemLongClick(v: View, item: RecentLocation)
    }

    private var listener: OnItemClickListener? = null

    //어떤 xml 으로 뷰 홀더를 생성할지 지정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecentLocationBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.tvTitle.text = recentLocations[position].title
        holder.binding.tvSubTitle.text = recentLocations[position].subTitle

        holder.binding.root.setOnClickListener {
            listener?.onItemClick(holder.itemView, recentLocations[position])
        }

        holder.binding.root.setOnLongClickListener {
            listener?.onItemLongClick(holder.itemView, recentLocations[position])
            return@setOnLongClickListener false
        }
    }

    override fun getItemCount(): Int {
        return recentLocations.size;
    }

    fun setData(newList: List<RecentLocation>) {
        recentLocations = newList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener : OnItemClickListener) { this.listener = listener }


}