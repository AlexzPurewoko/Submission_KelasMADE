package id.apwdevs.app.catalogue.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.model.onDetail.ReviewResultModel

class RecyclerReviewAdapter(
    private val reviewResultModels: List<ReviewResultModel>

) : RecyclerView.Adapter<RecyclerReviewAdapter.RecyclerReviewVH>() {
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerReviewVH =
        RecyclerReviewVH(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_list_crews, parent, false)
        )

    override fun getItemCount(): Int = reviewResultModels.size

    override fun onBindViewHolder(holder: RecyclerReviewVH, position: Int) {
        holder.bind(reviewResultModels[position])
    }


    class RecyclerReviewVH(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.item_review_reviewer)
        private val content: TextView = view.findViewById(R.id.item_crew_department)
        private val launch: ImageButton = view.findViewById(R.id.item_review_btn_launch)

        fun bind(reviewResultModel: ReviewResultModel) {
            name.text = reviewResultModel.author
            content.text = reviewResultModel.content
            launch.setOnClickListener {
                Log.d("ReviewAdapter", "Launch on ${reviewResultModel.url}")
            }
        }
    }
}