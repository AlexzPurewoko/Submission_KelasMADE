package id.apwdevs.app.catalogue.adapter

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.adapter.RecyclerReviewAdapter.OnLaunchItemClickListener
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.*
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.gone
import id.apwdevs.app.catalogue.plugin.visible
import kotlinx.android.parcel.Parcelize
import kotlin.math.abs

class DetailLayoutRecyclerAdapter(
    private val context: Context,
    private val contentType: DetailActivity.ContentTypes,
    relatedData: Bundle
) : RecyclerView.Adapter<DetailLayoutRecyclerAdapter.DetailLayoutVH>() {

    private var data1Model: ResettableItem? = null
    private var data2OtherModel: ResettableItem? = null
    private var listCrewModel: List<CrewModel>? = null
    private var listCastModel: List<CastModel>? = null
    private var reviewModel: ReviewModel? = null
    private var createdBy: List<ModelTvCreatedBy>? = null // for Tv usages only

    var onItemAction: OnItemActionListener? = null

    interface OnItemActionListener {
        fun onAction(viewType: ViewType, vararg action: Any)
    }

    internal var maxReviewLimits = 5
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    internal var maxCreditsLimit = 10
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val contentTypes: MutableList<ViewType>

    init {
        setHasStableIds(true)
        relatedData.apply {
            contentTypes = mutableListOf(
                ViewType.CONTENT_OVERVIEW,
                ViewType.CONTENT_ABOUT,
                ViewType.CONTENT_LIST_CAST,
                ViewType.CONTENT_LIST_CREW,
                ViewType.CONTENT_REVIEWS
            )

            when (contentType) {
                DetailActivity.ContentTypes.ITEM_MOVIE -> {
                    computeRequirements(contentTypes, this).forEach {
                        when (it) {
                            EXTRA_DATA_1 -> data1Model = getParcelable<MovieAboutModel>(it)
                            EXTRA_DATA_2 -> data2OtherModel = getParcelable<OtherMovieAboutModel>(it)
                            EXTRA_DATA_CASTS -> listCastModel = getParcelableArrayList(it)
                            EXTRA_DATA_CREWS -> listCrewModel = getParcelableArrayList(it)
                            EXTRA_DATA_REVIEWS -> reviewModel = getParcelable(it)
                        }
                    }

                    Log.d("RecyclerDetailww", "OnSet model data,... Reviews $reviewModel")
                }
                DetailActivity.ContentTypes.ITEM_TV_SHOWS -> {
                    contentTypes.add(ViewType.CONTENT_CREATED_BY)
                    computeRequirements(contentTypes, this).forEach {
                        when (it) {
                            EXTRA_DATA_1 -> data1Model = getParcelable<TvAboutModel>(it)
                            EXTRA_DATA_2 -> data2OtherModel = getParcelable<OtherTVAboutModel>(it)
                            EXTRA_DATA_CASTS -> listCastModel = getParcelableArrayList(it)
                            EXTRA_DATA_CREWS -> listCrewModel = getParcelableArrayList(it)
                            EXTRA_DATA_REVIEWS -> reviewModel = getParcelable(it)
                        }
                    }
                    createdBy = (data2OtherModel as OtherTVAboutModel?)?.createdBy
                }
            }
        }
    }

    private fun computeRequirements(contentTypes: MutableList<ViewType>, dataBundle: Bundle): List<String> {

        val listRequirementsKey = mutableListOf<String>()
        for ((idx, type) in contentTypes.withIndex()) {
            val count = type.mustContains.size
            var notMuchRequirements = false

            // check if this key is available in data or not
            // and set notMuchRequirements into false if any data is missing
            for (i in 0 until count) {
                if (!dataBundle.containsKey(type.mustContains[i])) {
                    notMuchRequirements = true
                    break
                }
            }

            // remove the unnecessary content
            if (notMuchRequirements) {
                contentTypes.removeAt(idx)
                continue
            }
            // add the key if not contains in list before
            type.mustContains.iterator().forEach {
                if (!listRequirementsKey.contains(it)) {
                    listRequirementsKey.add(it)
                }
            }

        }

        return listRequirementsKey
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailLayoutVH =
        DetailLayoutVH(
            LayoutInflater.from(context).inflate(
                ViewType.findItem(viewType).layoutResId
                , parent, false
            ),
            viewType
        )

    override fun getItemCount(): Int = contentTypes.size

    override fun onBindViewHolder(holder: DetailLayoutVH, position: Int) {
        when (contentTypes[position]) {
            ViewType.CONTENT_OVERVIEW -> {
                holder.bindItem(
                    data1Model?.let {
                        when (it) {
                            is MovieAboutModel -> (data1Model as MovieAboutModel).overview
                            is TvAboutModel -> (data1Model as TvAboutModel).overview
                            else -> ""
                        }
                    })
            }
            ViewType.CONTENT_ABOUT -> {
                holder.bindItem(data1Model, data2OtherModel)
            }
            ViewType.CONTENT_LIST_CAST -> {
                holder.bindItem(listCastModel, maxCreditsLimit)
            }
            ViewType.CONTENT_LIST_CREW -> {
                holder.bindItem(listCrewModel, maxCreditsLimit)
            }
            ViewType.CONTENT_CREATED_BY -> {
                if (data2OtherModel != null && data2OtherModel is OtherTVAboutModel) {
                    holder.bindItem(createdBy)
                }
            }
            ViewType.CONTENT_REVIEWS -> {
                reviewModel?.let {
                    holder.bindItem(it, maxReviewLimits, onItemAction)

                    Log.d("RecyclerDetailww", "Onbind Review")

                }
                Log.d("RecyclerDetailww", "OnFinish Review")
            }
            else -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return contentTypes[position].type
    }

    override fun getItemId(position: Int): Long {
        return (1 * 1000 + position * 15000 + abs(contentTypes[position].type)).toLong()
    }

    class DetailLayoutVH(
        view: View,
        private val itemType: Int
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private var textTitle: TextView? = null
        private var textContent: TextView? = null
        private var recyclerView: RecyclerView? = null
        private var actionBtn: ImageView? = null
        private var strLaunchTo: String? = null

        init {
            val vType = ViewType.findItem(itemType)
            when (vType) {
                ViewType.CONTENT_ABOUT, ViewType.CONTENT_LIST_CAST, ViewType.CONTENT_LIST_CREW, ViewType.CONTENT_REVIEWS, ViewType.CONTENT_CREATED_BY -> {
                    textTitle = view.findViewById(R.id.actdetail_text_item_title)
                    recyclerView = view.findViewById(R.id.actdetail_recycler_content)
                    actionBtn = view.findViewById(R.id.actdetail_drop_content)

                    actionBtn?.tag = TAG_BTN_ACTION_DROP_DOWN
                    actionBtn?.setOnClickListener(this)
                    recyclerView?.gone()
                }
                ViewType.CONTENT_OVERVIEW -> {
                    textTitle = view.findViewById(R.id.item_overview_title)
                    textContent = view.findViewById(R.id.item_overview_content)
                    textContent?.text = ""
                }
                ViewType.CONTENT_NULL -> {
                } // do nothing
            }
            textTitle?.setText(vType.titleRes)
        }


        /**
         * Sets the item content, the @param item can be vary as match as viewType
         * Note :
         *  Please to specify the values as defined in ViewType's enum :
         *      CONTENT_OVERVIEW -> value can be of (String)
         *      CONTENT_ABOUT -> value can be of (model1, otherModel2)
         *      CONTENT_REVIEWS -> value can be of (ReviewModel, limits)
         *      CONTENT_CREATED_BY -> value can be of (List<ModelTvCreatedBy>)
         *      CONTENT_LIST_CREW -> value can be of (List<CrewModel>, limits)
         *      CONTENT_LIST_CAST -> value can be of (List<CastModel>, limits)
         */
        @Suppress("UNCHECKED_CAST")
        fun bindItem(vararg item: Any?) {

            Log.d("RecyclerDetailww", "Onbind Review -> $itemType == ${ViewType.CONTENT_REVIEWS.type} content -> $item")
            when (ViewType.findItem(itemType)) {
                ViewType.CONTENT_REVIEWS -> {
                    val content = item[0]
                    val limits: Int = when (item[1]) {
                        is Int -> item[1] as Int
                        else -> RecyclerReviewAdapter.NO_LIMITS
                    }
                    if (content is ReviewModel) {
                        recyclerView?.apply {
                            layoutManager = LinearLayoutManager(itemView.context)
                            adapter = RecyclerReviewAdapter(content.results, limits).apply {
                                val cb = item[2]
                                if (cb is OnItemActionListener) {

                                    onLaunchItemClicked = object : OnLaunchItemClickListener {
                                        override fun onLaunch(link: String, view: View) {
                                            cb.onAction(ViewType.CONTENT_REVIEWS, link)
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
                ViewType.CONTENT_OVERVIEW -> {
                    textContent?.text = ""
                    item.iterator().forEach {
                        if (it is String)
                            textContent?.append(it)
                    }
                }
                ViewType.CONTENT_CREATED_BY, ViewType.CONTENT_LIST_CREW, ViewType.CONTENT_LIST_CAST -> {
                    val content = item[0] ?: return
                    if ((content is List<*>) && content.size > 0) {
                        recyclerView?.layoutManager =
                            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                        when (ViewType.findItem(itemType)) {
                            ViewType.CONTENT_CREATED_BY -> {
                                if (content[0] is ModelTvCreatedBy) {
                                    recyclerView?.adapter = RecyclerCreatedByAdapter(content as List<ModelTvCreatedBy>)
                                }
                            }
                            ViewType.CONTENT_LIST_CREW -> {
                                val limits: Int = when (item[1]) {
                                    is Int -> item[1] as Int
                                    else -> RecyclerReviewAdapter.NO_LIMITS
                                }
                                if (content[0] is CrewModel) {
                                    recyclerView?.adapter = RecyclerCrewsAdapter(content as List<CrewModel>, limits)
                                }

                            }
                            ViewType.CONTENT_LIST_CAST -> {
                                val limits: Int = when (item[1]) {
                                    is Int -> item[1] as Int
                                    else -> RecyclerReviewAdapter.NO_LIMITS
                                }
                                if (content[0] is CastModel) {
                                    recyclerView?.adapter =
                                        RecyclerCastsAdapter(itemView.context, content as List<CastModel>, limits)
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }
                ViewType.CONTENT_ABOUT -> {
                    val aboutModel = item[0]
                    val otherAboutModel = item[1]
                    val ctx = itemView.context
                    recyclerView?.layoutManager = LinearLayoutManager(ctx, RecyclerView.VERTICAL, false)
                    if (aboutModel is TvAboutModel && otherAboutModel is OtherTVAboutModel) {
                        recyclerView?.adapter = RecyclerAboutAdapter(
                            ctx,
                            aboutModel,
                            otherAboutModel,
                            DetailActivity.ContentTypes.ITEM_TV_SHOWS
                        )
                    } else if (aboutModel is MovieAboutModel && otherAboutModel is OtherMovieAboutModel) {
                        recyclerView?.adapter = RecyclerAboutAdapter(
                            ctx,
                            aboutModel,
                            otherAboutModel,
                            DetailActivity.ContentTypes.ITEM_MOVIE
                        )
                    }
                }
                ViewType.CONTENT_NULL -> {
                }
            }

        }

        override fun onClick(v: View?) {
            when (v?.tag) {
                TAG_BTN_LAUNCH -> {
                    Toast.makeText(v.context, "Action launch to -> $strLaunchTo", Toast.LENGTH_SHORT).show()
                }
                TAG_BTN_ACTION_DROP_DOWN -> {
                    actionBtn?.tag = TAG_BTN_ACTION_DROP_UP
                    actionBtn?.setImageResource(R.drawable.ic_drop_up_24dp)
                    recyclerView?.visible()
                }
                TAG_BTN_ACTION_DROP_UP -> {
                    actionBtn?.tag = TAG_BTN_ACTION_DROP_DOWN
                    actionBtn?.setImageResource(R.drawable.ic_drop_down_24dp)
                    recyclerView?.gone()
                }
            }
        }

        companion object {
            const val TAG_BTN_LAUNCH = "ACTION_LAUNCH"
            const val TAG_BTN_ACTION_DROP_UP = "ACTION_DROP_UP"
            const val TAG_BTN_ACTION_DROP_DOWN = "ACTION_DROP_DOWN"
        }
    }

    @Parcelize
    enum class ViewType(val type: Int, val layoutResId: Int, val titleRes: Int, vararg val mustContains: String) :
        Parcelable {
        CONTENT_LIST_CREW(0x5d, R.layout.actdetail_layout_list, R.string.crews, EXTRA_DATA_CREWS),
        CONTENT_LIST_CAST(0x4f, R.layout.actdetail_layout_list, R.string.casts, EXTRA_DATA_CASTS),
        CONTENT_OVERVIEW(0x3f, R.layout.actdetail_overview, R.string.overview, EXTRA_DATA_1),
        CONTENT_CREATED_BY(0x2a, R.layout.actdetail_layout_list, R.string.created_by, EXTRA_DATA_2),
        CONTENT_ABOUT(0xffa, R.layout.actdetail_layout_list, R.string.abouts, EXTRA_DATA_1, EXTRA_DATA_2),
        CONTENT_REVIEWS(0x22, R.layout.actdetail_layout_list, R.string.reviews, EXTRA_DATA_REVIEWS),
        CONTENT_NULL(0, 0, 0);

        companion object {
            fun findItem(type: Int): ViewType =
                when (type) {
                    CONTENT_OVERVIEW.type -> CONTENT_OVERVIEW
                    CONTENT_CREATED_BY.type -> CONTENT_CREATED_BY
                    CONTENT_REVIEWS.type -> CONTENT_REVIEWS
                    CONTENT_LIST_CREW.type -> CONTENT_LIST_CREW
                    CONTENT_LIST_CAST.type -> CONTENT_LIST_CAST
                    CONTENT_ABOUT.type -> CONTENT_ABOUT
                    else -> CONTENT_NULL
                }
        }
    }

    companion object {
        const val EXTRA_DATA_1 = "EXTRA_DATA_1"
        const val EXTRA_DATA_2 = "EXTRA_DATA_2"
        const val EXTRA_DATA_CREWS = "EXTRA_DATA_CREWS"
        const val EXTRA_DATA_CASTS = "EXTRA_DATA_CASTS"
        const val EXTRA_DATA_REVIEWS = "EXTRA_DATA_REVIEWS"
    }
}