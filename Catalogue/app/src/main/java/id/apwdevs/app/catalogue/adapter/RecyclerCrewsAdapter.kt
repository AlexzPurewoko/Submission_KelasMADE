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
import id.apwdevs.app.catalogue.model.onDetail.CrewModel
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles
import kotlin.math.min

class RecyclerCrewsAdapter(
    private val crewModels: List<CrewModel>,
    internal val maxContentLimits: Int
) : RecyclerView.Adapter<RecyclerCrewsAdapter.RecyclerCrewsVH>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerCrewsVH =
        RecyclerCrewsVH(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_list_crews, parent, false)
        )

    override fun getItemCount(): Int = crewModels.size.let {
        if (maxContentLimits == NO_LIMITS)
            it
        else {
            min(it, maxContentLimits)
        }
    }

    override fun onBindViewHolder(holder: RecyclerCrewsVH, position: Int) {
        holder.bind(crewModels[position])
    }


    class RecyclerCrewsVH(view: View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.item_crew_image)
        private val name: TextView = view.findViewById(R.id.item_crew_name)
        private val department: TextView = view.findViewById(R.id.item_crew_department)
        private val jobs: TextView = view.findViewById(R.id.item_crew_jobs)

        fun bind(crewModel: CrewModel) {
            name.text = crewModel.name
            department.text = crewModel.department
            jobs.text = crewModel.job
            crewModel.profilePath?.let {
                AndroidNetworking.get(GetImageFiles.getImg(95, it))
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsBitmap(object : BitmapRequestListener {
                        override fun onResponse(response: Bitmap?) {
                            image.setImageBitmap(response)
                        }

                        override fun onError(anError: ANError?) {
                            Log.e("CrewAdapter", "Error resolving images", anError?.cause)
                        }

                    })
            }
        }
    }

    companion object {
        const val NO_LIMITS = -1
    }
}
