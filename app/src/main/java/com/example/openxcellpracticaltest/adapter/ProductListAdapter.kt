package com.example.openxcellpracticaltest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.openxcellpracticaltest.databinding.ProductListItemViewBinding
import com.example.openxcellpracticaltest.model.ProductItem

class ProductListAdapter(
    private var context: Context?,
    private var productList: List<ProductItem>
) : ListAdapter<ProductItem, ProductListAdapter.ProductViewModel>(object :
    DiffUtil.ItemCallback<ProductItem>() {
    override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
        return oldItem.title.equals(newItem.title)
    }

}) {

    companion object {
        var listener: OnViewMapClickedListener? = null
    }

    class ProductViewModel(itemView: ProductListItemViewBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private val viewBinding = itemView
        fun setData(
            productItem: ProductItem,
            context: Context?,
            listener: OnViewMapClickedListener?
        ) {
            viewBinding.product = productItem
            context?.let {
                Glide.with(it).load(productItem.product_medias[0].media_path)
                    .into(viewBinding.image)
            }

            viewBinding.tvView.setOnClickListener {
                listener?.viewOnMap(productItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewModel {
        return ProductViewModel(
            ProductListItemViewBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewModel, position: Int) {
        holder.setData(productList[position], context, listener)
    }
}