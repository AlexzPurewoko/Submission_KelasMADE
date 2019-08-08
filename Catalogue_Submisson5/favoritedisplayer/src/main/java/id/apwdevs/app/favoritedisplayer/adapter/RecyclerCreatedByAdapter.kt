package id.apwdevs.app.catalogue.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.model.onDetail.ModelTvCreatedBy
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles

class RecyclerCreatedByAdapter(
    private val peopleList: List<ModelTvCreatedBy>
) : RecyclerView.Adapter<RecyclerCreatedByAdapter.RecyclerCreatedByVH>() {
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerCreatedByVH =
        RecyclerCreatedByVH(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_createdby, parent, false)
        )

    override fun getItemCount(): Int = peopleList.size

    override fun onBindViewHolder(holder: RecyclerCreatedByVH, position: Int) {
        holder.bind(peopleList[position])
    }


    class RecyclerCreatedByVH(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.item_createdby_text)
        private val img: ImageView = view.findViewById(R.id.item_createdby_image)

        fun bind(createdBy: ModelTvCreatedBy) {
            name.text = createdBy.name
            createdBy.profilePath.let {
                AndroidNetworking.get(GetImageFiles.getImg(95, it))
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsBitmap(object : BitmapRequestListener {
                        override fun onResponse(response: Bitmap?) {
                            img.setImageBitmap(response)
                        }

                        override fun onError(anError: ANError?) {
                            Log.e("CreatedBy Adapter", "Error resolving images", anError?.cause)
                        }

                    })
            }
        }
    }
}