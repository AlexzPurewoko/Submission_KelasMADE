package id.apwdevs.app.catalogue.plugin.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.github.zawadz88.materialpopupmenu.MaterialPopupMenu
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.PublicConfig

class SearchToolbarCard(
    private val activity: AppCompatActivity,
    cardView: CardView,
    private val searchCb: OnSearchCallback
) : CustomEditText.OnClrBtnClicked {

    private var imageSearch: ImageView = cardView.findViewById(R.id.button_search)
    private var edtSearch: CustomEditText = cardView.findViewById(R.id.text_search)
    private var contentMore: ImageView = cardView.findViewById(R.id.img_more)
    private var listMode: ImageView = cardView.findViewById(R.id.item_list_modes)
    private lateinit var popupMenu: MaterialPopupMenu

    private var dataVModel: ToolbarCardViewModel = ViewModelProviders.of(activity).get(ToolbarCardViewModel::class.java)


    init {
        dataVModel.currentListMode.observeForever {
            setIconListMode(it)
        }
        dataVModel.isInSearchMode.observeForever {
            imageSearch.setImageResource(
                when (it) {
                    true -> R.drawable.ic_arrow_back_black_24dp
                    false -> R.drawable.ic_search_black_24dp
                }
            )
        }
        initEditSearch()
        initImgLeft()
        initImgListMode()
    }

    override fun onClear(historyText: String?) {
        searchCb.onTextCleared(historyText)
    }

    private fun setIconListMode(it: Int?) {
        when (it) {
            PublicConfig.RecyclerMode.MODE_LIST -> {
                listMode.setImageResource(R.drawable.ic_view_grid_black_24dp)
            }
            PublicConfig.RecyclerMode.MODE_GRID -> {
                listMode.setImageResource(R.drawable.ic_stagerred_black_24dp)
            }
            PublicConfig.RecyclerMode.MODE_STAGERRED_LIST -> {
                listMode.setImageResource(R.drawable.ic_view_list_black_24dp)
            }
        }
    }

    private fun initImgListMode() {
        listMode.setOnClickListener {
            dataVModel.currentListMode.value?.let {
                setModes(it)
            }
        }
    }

    private fun setModes(mode: Int) {
        when (mode) {
            PublicConfig.RecyclerMode.MODE_LIST -> {
                searchCb.onListModeChange(PublicConfig.RecyclerMode.MODE_GRID)
                dataVModel.currentListMode.value = PublicConfig.RecyclerMode.MODE_GRID
            }
            PublicConfig.RecyclerMode.MODE_GRID -> {
                searchCb.onListModeChange(PublicConfig.RecyclerMode.MODE_STAGERRED_LIST)
                dataVModel.currentListMode.value = PublicConfig.RecyclerMode.MODE_STAGERRED_LIST

            }
            PublicConfig.RecyclerMode.MODE_STAGERRED_LIST -> {
                searchCb.onListModeChange(PublicConfig.RecyclerMode.MODE_LIST)
                dataVModel.currentListMode.value = PublicConfig.RecyclerMode.MODE_LIST

            }
        }
    }

    private fun initImgLeft() {
        imageSearch.setOnClickListener {
            when (dataVModel.isInSearchMode.value) {
                true -> {
                    setFocusable(false)
                    searchCb.onSearchCancelled()
                    edtSearch.text?.clear()
                    dataVModel.isInSearchMode.value = false
                    edtSearch.hideClrBtn()
                }
                false -> {
                    setFocusable(true)
                }
            }
        }
    }

    private fun initEditSearch() {
        edtSearch.onBtnClearClicked = this
        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                setFocusable(false)
                searchCb.onSubmit(edtSearch.text.toString())
            }
            true
        }
        edtSearch.setOnClickListener {
            if (!edtSearch.isFocusable) {
                setFocusable(true)
                edtSearch.requestFocusFromTouch()
            }
        }
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchCb.querySearch(edtSearch, s, start, before, count)
                dataVModel.isInSearchMode.value = !s.isNullOrEmpty()
            }

        })
    }

    private fun setFocusable(condition: Boolean) {
        edtSearch.isFocusable = condition
        edtSearch.isFocusableInTouchMode = condition
        if (condition) {
            edtSearch.requestFocusFromTouch()
            getSystemService(activity, InputMethodManager::class.java)?.apply {
                showSoftInput(edtSearch, InputMethodManager.SHOW_FORCED)
            }
        } else {
            getSystemService(activity, InputMethodManager::class.java)?.apply {
                hideSoftInputFromWindow(edtSearch.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            }
        }
    }

    fun forceSearchCancel() {
        dataVModel.isInSearchMode.value = false
        edtSearch.text?.clear()
        searchCb.onSearchCancelled()
        setFocusable(false)
        edtSearch.hideClrBtn()
    }

    fun dismissPopup() {
        popupMenu.dismiss()
    }

    interface OnSearchCallback {
        fun querySearch(view: View, query: CharSequence?, start: Int, before: Int, count: Int)
        fun onSubmit(query: String)
        fun onSearchCancelled()
        fun onTextCleared(searchHistory: String?)
        fun onSearchStarted()
        fun onListModeChange(listMode: Int)
    }
}

class ToolbarCardViewModel : ViewModel() {
    val currentListMode: MutableLiveData<Int> = MutableLiveData()
    val isInSearchMode: MutableLiveData<Boolean> = MutableLiveData()

    init {
        currentListMode.value = PublicConfig.RecyclerMode.MODE_LIST
        isInSearchMode.value = false
    }
}