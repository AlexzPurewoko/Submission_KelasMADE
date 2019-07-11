package id.apwdevs.app.catalogue.adapter

import android.content.Context
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
import id.apwdevs.app.catalogue.model.onDetail.CastModel
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles

class RecyclerCastsAdapter(
    private val context: Context,
    private val mListCasts: List<CastModel>
) : RecyclerView.Adapter<RecyclerCastsAdapter.RecyclerCastsViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerCastsViewHolder =
        RecyclerCastsViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_item_list_casts, parent, false))

    override fun getItemCount(): Int = mListCasts.size

    override fun onBindViewHolder(holder: RecyclerCastsViewHolder, position: Int) {
        holder.bind(mListCasts[position])
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    class RecyclerCastsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.item_cast_image)
        private val name: TextView = view.findViewById(R.id.item_cast_name)
        private val asCharacter: TextView = view.findViewById(R.id.item_cast_ascharacter)

        fun bind(castModel: CastModel) {
            name.text = castModel.name
            asCharacter.text = castModel.asCharacter
            castModel.profilePath?.let {
                AndroidNetworking.get(GetImageFiles.getImg(95, it))
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsBitmap(object : BitmapRequestListener {
                        override fun onResponse(response: Bitmap?) {
                            image.setImageBitmap(response)
                        }

                        override fun onError(anError: ANError?) {
                            Log.e("CastAdapter", "Error resolving images", anError?.cause)
                        }

                    })
            }
        }
    }

}