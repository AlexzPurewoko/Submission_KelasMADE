package id.apwdevs.app.catalogue.adapter

import android.content.Context
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.adapter.RecyclerReviewAdapter.OnLaunchItemClickListener
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onDetail.*
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.gone
import id.apwdevs.app.catalogue.plugin.visible
import id.apwdevs.app.catalogue.repository.onDetail.DetailActivityRepository
import id.apwdevs.app.catalogue.viewModel.DetailViewModel
import kotlinx.android.parcel.Parcelize
import kotlin.math.abs

class DetailLayoutRecyclerAdapter(
    private val context: Context,
    contentType: DetailActivityRepository.TypeContentContract,
    private val viewModel: DetailViewModel,
    private val maxReviewLimits: Int,
    private val maxCreditsLimit: Int

) : RecyclerView.Adapter<DetailLayoutRecyclerAdapter.DetailLayoutVH>() {

    private var data1Model: ResettableItem? = null
    private var data2OtherModel: ResettableItem? = null
    private var listCrewModel: List<CrewModel>? = null
    private var listCastModel: List<CastModel>? = null
    private var reviewModel: ReviewResponse? = null
    private var createdBy: List<ModelTvCreatedBy>? = null // for Tv usages only

    var onItemAction: OnItemActionListener? = null

    interface OnItemActionListener {
        fun onAction(viewType: ViewType, vararg action: Any)
    }

    private val contentTypes: MutableList<ViewType>

    init {
        setHasStableIds(true)
        contentTypes = mutableListOf(
            ViewType.CONTENT_OVERVIEW,
            ViewType.CONTENT_ABOUT,
            ViewType.CONTENT_LIST_CAST,
            ViewType.CONTENT_LIST_CREW,
            ViewType.CONTENT_REVIEWS
        )

        if (contentType == DetailActivityRepository.TypeContentContract.TV_SHOWS)
            contentTypes.add(4, ViewType.CONTENT_CREATED_BY)
        computeRequirements(contentTypes, viewModel).forEach {
            when (it) {
                MODEL_DATA_1 -> data1Model = viewModel.data1Obj.value
                MODEL_DATA_2 -> data2OtherModel = viewModel.data2Obj.value
                MODEL_DATA_CASTS -> listCastModel = viewModel.credits.value?.allCasts
                MODEL_DATA_CREWS -> listCrewModel = viewModel.credits.value?.allCrew
                MODEL_DATA_REVIEWS -> reviewModel = viewModel.reviews.value
            }


        }
        if (contentType == DetailActivityRepository.TypeContentContract.TV_SHOWS)
            createdBy = (data2OtherModel as OtherTVAboutModel?)?.createdBy
        notifyDataSetChanged()
    }

    private fun computeRequirements(contentTypes: MutableList<ViewType>, viewModel: DetailViewModel): List<String> {
        val listRequirementsKey = mutableListOf<String>()
        var sz = contentTypes.size
        var pos = 0
        while (pos < sz) {
            val type = contentTypes[pos]
            val count = type.mustContains.size
            var notMuchRequirements = false

            // check if this key is available in data or not
            // and set notMuchRequirements into false if any data is missing
            for (i in 0 until count) {
                if (
                    when (type.mustContains[i]) {
                        MODEL_DATA_1 -> viewModel.data1Obj.value == null
                        MODEL_DATA_2 -> viewModel.data2Obj.value == null
                        MODEL_DATA_CASTS -> viewModel.credits.value?.allCasts == null
                        MODEL_DATA_CREWS -> viewModel.credits.value?.allCrew == null
                        MODEL_DATA_REVIEWS -> viewModel.reviews.value == null

                        else -> false
                    }
                ) {
                    notMuchRequirements = true
                    break
                }
            }
            // remove the unnecessary content
            if (notMuchRequirements) {
                contentTypes.removeAt(pos)
                sz = contentTypes.size
                pos++
                continue
            }
            // add the key if not contains in list before
            type.mustContains.iterator().forEach {
                if (!listRequirementsKey.contains(it)) {
                    listRequirementsKey.add(it)
                }
            }
            pos++

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

                }
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
        private var textError: TextView? = null
        private var recyclerView: RecyclerView? = null
        private var actionBtn: ImageView? = null

        init {
            val vType = ViewType.findItem(itemType)
            when (vType) {
                ViewType.CONTENT_ABOUT, ViewType.CONTENT_LIST_CAST, ViewType.CONTENT_LIST_CREW, ViewType.CONTENT_REVIEWS, ViewType.CONTENT_CREATED_BY -> {
                    textTitle = view.findViewById(R.id.actdetail_text_item_title)
                    recyclerView = view.findViewById(R.id.actdetail_recycler_content)
                    actionBtn = view.findViewById(R.id.actdetail_drop_content)

                    actionBtn?.tag = TAG_BTN_ACTION_DROP_UP
                    textTitle?.tag = TAG_BTN_ACTION_DROP_UP
                    textTitle?.setOnClickListener(this)
                    actionBtn?.setOnClickListener(this)
                }
                ViewType.CONTENT_OVERVIEW -> {
                    textTitle = view.findViewById(R.id.item_overview_title)
                    textContent = view.findViewById(R.id.item_overview_content)
                    textContent?.text = ""
                }
                ViewType.CONTENT_NULL -> {
                } // do nothing
            }
            textError = view.findViewById(R.id.actdetail_text_error)
            textTitle?.setText(vType.titleRes)
        }


        /**
         * Sets the item content, the @param item can be vary as match as viewType
         * Note :
         *  Please to specify the values as defined in ViewType's enum :
         *      CONTENT_OVERVIEW -> value can be of (String)
         *      CONTENT_ABOUT -> value can be of (model1, otherModel2)
         *      CONTENT_REVIEWS -> value can be of (ReviewResponse, limits)
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
                    if (content is ReviewResponse && content.results.isNotEmpty()) {
                        Log.d("RECVIEW", "DATA CONTENT IS $content")
                        hideErrorText()
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
                    if (textContent?.text?.isNotBlank() == true) {
                        textError?.gone()
                        textContent?.visible()
                    }
                }
                ViewType.CONTENT_CREATED_BY, ViewType.CONTENT_LIST_CREW, ViewType.CONTENT_LIST_CAST -> {
                    val content = item[0] ?: return
                    if ((content is List<*>) && content.size > 0) {
                        hideErrorText()
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
                    Log.d("HHH", "true $content")
                }
                ViewType.CONTENT_ABOUT -> {
                    val aboutModel = item[0]
                    val otherAboutModel = item[1]
                    val ctx = itemView.context
                    hideErrorText()
                    recyclerView?.layoutManager = LinearLayoutManager(ctx, RecyclerView.VERTICAL, false)
                    if (aboutModel is TvAboutModel && otherAboutModel is OtherTVAboutModel) {
                        recyclerView?.adapter = RecyclerAboutAdapter(
                            ctx,
                            aboutModel,
                            otherAboutModel,
                            PublicContract.ContentDisplayType.TV_SHOWS
                        )
                    } else if (aboutModel is MovieAboutModel && otherAboutModel is OtherMovieAboutModel) {
                        recyclerView?.adapter = RecyclerAboutAdapter(
                            ctx,
                            aboutModel,
                            otherAboutModel,
                            PublicContract.ContentDisplayType.MOVIE
                        )
                    }
                }
                ViewType.CONTENT_NULL -> {
                }
            }

        }

        private fun hideErrorText() {
            recyclerView?.visible()
            textError?.gone()
            textError?.tag = NO_ERROR_AVAILABLE
        }

        override fun onClick(v: View?) {
            when (v?.tag) {
                TAG_BTN_ACTION_DROP_DOWN -> {
                    actionBtn?.tag = TAG_BTN_ACTION_DROP_UP
                    textTitle?.tag = TAG_BTN_ACTION_DROP_UP
                    actionBtn?.setImageResource(R.drawable.ic_drop_up_24dp)
                    recyclerView?.visible()
                    if (textError?.isVisible == false && textError?.tag?.equals(NO_ERROR_AVAILABLE) != true)
                        textError?.visible()
                }
                TAG_BTN_ACTION_DROP_UP -> {
                    actionBtn?.tag = TAG_BTN_ACTION_DROP_DOWN
                    textTitle?.tag = TAG_BTN_ACTION_DROP_DOWN
                    actionBtn?.setImageResource(R.drawable.ic_drop_down_24dp)
                    recyclerView?.gone()
                    if (textError?.isVisible == true && textError?.tag?.equals(NO_ERROR_AVAILABLE) != true)
                        textError?.gone()
                }
            }
        }

        companion object {
            //const val TAG_BTN_LAUNCH = "ACTION_LAUNCH"
            const val TAG_BTN_ACTION_DROP_UP = "ACTION_DROP_UP"
            const val TAG_BTN_ACTION_DROP_DOWN = "ACTION_DROP_DOWN"
        }

    }

    @Parcelize
    enum class ViewType(val type: Int, val layoutResId: Int, val titleRes: Int, vararg val mustContains: String) :
        Parcelable {
        CONTENT_LIST_CREW(0x5d, R.layout.actdetail_layout_list, R.string.crews, MODEL_DATA_CREWS),
        CONTENT_LIST_CAST(0x4f, R.layout.actdetail_layout_list, R.string.casts, MODEL_DATA_CASTS),
        CONTENT_OVERVIEW(0x3f, R.layout.actdetail_overview, R.string.overview, MODEL_DATA_1),
        CONTENT_CREATED_BY(0x2a, R.layout.actdetail_layout_list, R.string.created_by, MODEL_DATA_2),
        CONTENT_ABOUT(0xffa, R.layout.actdetail_layout_list, R.string.abouts, MODEL_DATA_1, MODEL_DATA_2),
        CONTENT_REVIEWS(0x22, R.layout.actdetail_layout_list, R.string.reviews, MODEL_DATA_REVIEWS),
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
        const val MODEL_DATA_1 = "MODEL_DATA_1"
        const val MODEL_DATA_2 = "EXTRA_DATA_2"
        const val MODEL_DATA_CREWS = "EXTRA_DATA_CREWS"
        const val MODEL_DATA_CASTS = "EXTRA_DATA_CASTS"
        const val MODEL_DATA_REVIEWS = "EXTRA_DATA_REVIEWS"
        private const val NO_ERROR_AVAILABLE = "NO_ERROR_AVAILABLE"
    }
}