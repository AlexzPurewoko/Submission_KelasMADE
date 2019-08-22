package id.apwdevs.app.catalogue.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.model.onDetail.ReviewResultModel
import kotlin.math.min

class RecyclerReviewAdapter(
    private val reviewResultModels: List<ReviewResultModel>,
    private val maxContentLimits: Int

) : RecyclerView.Adapter<RecyclerReviewAdapter.RecyclerReviewVH>() {
    init {
        setHasStableIds(true)
    }

    var onLaunchItemClicked: OnLaunchItemClickListener? = null

    interface OnLaunchItemClickListener {
        fun onLaunch(link: String, view: View)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerReviewVH =
        RecyclerReviewVH(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_review, parent, false)
        )

    override fun getItemCount(): Int =
        reviewResultModels.size.let {
            if (maxContentLimits == NO_LIMITS)
                it
            else {
                min(it, maxContentLimits)
            }
        }

    override fun onBindViewHolder(holder: RecyclerReviewVH, position: Int) {
        holder.bind(reviewResultModels[position], onLaunchItemClicked)
    }


    class RecyclerReviewVH(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.item_review_reviewer)
        private val content: TextView = view.findViewById(R.id.item_review_content)
        private val launch: ImageView = view.findViewById(R.id.item_review_btn_launch)

        fun bind(
            reviewResultModel: ReviewResultModel,
            onLaunchItemClickListener: OnLaunchItemClickListener?
        ) {
            name.text = reviewResultModel.author
            content.text = reviewResultModel.content
            launch.setOnClickListener {
                Log.d("ReviewAdapter", "Launch on ${reviewResultModel.url}")
                onLaunchItemClickListener?.onLaunch(reviewResultModel.url, itemView)
            }
        }
    }

    companion object {
        const val NO_LIMITS = -1
        const val DEFAULT_LIMITS = 5
    }
}