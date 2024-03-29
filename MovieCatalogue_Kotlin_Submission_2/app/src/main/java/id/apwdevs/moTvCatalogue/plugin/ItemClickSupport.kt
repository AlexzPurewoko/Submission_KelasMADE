package id.apwdevs.moTvCatalogue.plugin

import android.support.v7.widget.RecyclerView
import android.view.View
import id.apwdevs.moTvCatalogue.R

class ItemClickSupport private constructor(private val mRecyclerView: RecyclerView) {
    var onItemClickListener: OnItemClickListener? = null
    var onItemLongClickListener: OnItemLongClickListener? = null
    private val mOnClickListener = View.OnClickListener { v ->
        val viewHolder = mRecyclerView.getChildViewHolder(v)
        onItemClickListener?.onItemClicked(mRecyclerView, viewHolder.adapterPosition, v)
    }

    private val mOnLongClickListener = View.OnLongClickListener { v ->
        if (onItemLongClickListener != null) {
            val holder = mRecyclerView.getChildViewHolder(v)
            val onLongClick = onItemLongClickListener?.onItemLongClicked(
                mRecyclerView,
                holder.adapterPosition,
                v
            )
            if (onLongClick != null)
                return@OnLongClickListener onLongClick
        }
        false
    }

    private val mAttachListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            if (onItemClickListener != null) {
                view.setOnClickListener(mOnClickListener)
            }
            if (onItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener)
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {

        }
    }

    init {
        this.mRecyclerView.setTag(R.id.item_click_support, this)
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener)
    }

    companion object {

        fun addTo(view: RecyclerView): ItemClickSupport {
            var support = view.getTag(R.id.item_click_support)
            if (support == null) {
                support = ItemClickSupport(view)
            }
            return support as ItemClickSupport
        }

        fun removeFrom(view: RecyclerView): ItemClickSupport? {
            val support = view.getTag(R.id.item_click_support) as ItemClickSupport
            support.detach(view)
            return support
        }
    }

    private fun detach(view: RecyclerView) {
        view.removeOnChildAttachStateChangeListener(mAttachListener)
        view.setTag(R.id.item_click_support, null)
    }

    interface OnItemClickListener {
        fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View)
    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(recyclerView: RecyclerView, position: Int, v: View): Boolean
    }
}