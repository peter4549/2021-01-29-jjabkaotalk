package com.grand.duke.elliot.jjabkaotalk.util.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentSimpleListDialogBinding
import kotlinx.android.synthetic.main.item_simple.view.*
import timber.log.Timber

class SimpleListDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentSimpleListDialogBinding
    private var title: String? = null
    private var simpleItems: ArrayList<SimpleItem> = arrayListOf()
    private var simpleItemAdapter = SimpleItemAdapter()

    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var onScrollReachedBottomListener: OnScrollReachedBottomListener? = null

    fun setTitle(title: String) {
        this.title = title
    }

    fun setItems(simpleItems: ArrayList<SimpleItem>) {
        this.simpleItems = simpleItems
    }

    interface OnItemSelectedListener {
        fun onItemSelected(dialogFragment: DialogFragment, simpleItem: SimpleItem)
    }

    interface OnScrollReachedBottomListener {
        fun onScrollReachedBottom(simpleItemAdapter: SimpleItemAdapter)
    }

    interface FragmentContainer {
        fun onRequestOnItemSelectedListener(): OnItemSelectedListener?
        fun onRequestOnScrollReachedBottom(): OnScrollReachedBottomListener?
    }

    fun clear() {
        title = null
        simpleItems.clear()
        onItemSelectedListener = null
        simpleItemAdapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentContainer) {
            onItemSelectedListener = context.onRequestOnItemSelectedListener()
            onScrollReachedBottomListener = context.onRequestOnScrollReachedBottom()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSimpleListDialogBinding.inflate(inflater, container, false)
        title?.let {
            binding.textTitle.visibility = View.VISIBLE
            binding.textTitle.text = it
        } ?: run {
            binding.textTitle.visibility = View.GONE
        }
        binding.recyclerViewItem.apply {
            layoutManager = LinearLayoutManager(binding.root.context)
            setHasFixedSize(true)
            adapter = simpleItemAdapter
        }

        binding.recyclerViewItem.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.d("recyclerViewVideo scrolled. dx: $dx, dy: $dy")
                val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager) ?: return
                val itemCount = layoutManager.itemCount
                val lastCompletelyVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                if (lastCompletelyVisibleItemPosition >= itemCount.dec()) {
                    onScrollReachedBottomListener?.onScrollReachedBottom(simpleItemAdapter)
                }
            }
        })

        return binding.root
    }

    inner class SimpleItemAdapter: RecyclerView.Adapter<SimpleItemAdapter.ViewHolder>() {
        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view)

        fun addItems(simpleItems: ArrayList<SimpleItem>, allowRedundancy: Boolean = false) {
            val positionStart = itemCount

            if (allowRedundancy)
                this@SimpleListDialogFragment.simpleItems.addAll(simpleItems)
            else
                for (simpleItem in simpleItems) {
                    if (this@SimpleListDialogFragment.simpleItems.notContain(simpleItem))
                        this@SimpleListDialogFragment.simpleItems.add(simpleItem)
                }

            notifyItemRangeInserted(positionStart, simpleItems.count())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_simple,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = simpleItems[position]
            holder.view.text_name.text = item.name
            holder.view.image.visibility = View.VISIBLE
            item.drawable?.let {
                holder.view.image.setImageDrawable(it)
            } ?: run {
                holder.view.image.visibility = View.GONE
            }

            holder.view.setOnClickListener {
                onItemSelectedListener?.onItemSelected(this@SimpleListDialogFragment, item)
            }
        }

        override fun getItemCount(): Int = simpleItems.count()
    }

    private fun ArrayList<SimpleItem>.notContain(element: SimpleItem) = !contains(element)
}

data class SimpleItem(
    val id: String,
    val name: String,
    val drawable: Drawable? = null
)