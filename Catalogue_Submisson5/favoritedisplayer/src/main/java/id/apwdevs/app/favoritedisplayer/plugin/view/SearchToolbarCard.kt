package id.apwdevs.app.catalogue.plugin.view

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.zawadz88.materialpopupmenu.MaterialPopupMenu
import com.github.zawadz88.materialpopupmenu.popupMenu
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.SettingsActivity
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.viewModel.ToolbarCardViewModel

class SearchToolbarCard(
    private val activity: AppCompatActivity,
    cardView: View,
    private val searchCb: OnSearchCallback
) : CustomEditText.OnClrBtnClicked {

    private var imageSearch: ImageView = cardView.findViewById(R.id.button_search)
    private var edtSearch: CustomEditText = cardView.findViewById(R.id.text_search)
    private var contentMore: ImageView = cardView.findViewById(R.id.img_more)
    private var listMode: ImageView = cardView.findViewById(R.id.item_list_modes)
    private lateinit var popupMenu: MaterialPopupMenu
    private var dataVModel: ToolbarCardViewModel = ViewModelProviders.of(activity).get(ToolbarCardViewModel::class.java)
    private var hasToSubmitted: Boolean = false
    private var hasFirstUserSearch: Boolean = false
    internal val currentListMode: Int?
        get() = dataVModel.currentListMode.value


    init {
        dataVModel.currentListMode.observe(activity, Observer {
            setIconListMode(it)
        })
        dataVModel.isInSearchMode.observe(activity, Observer {
            imageSearch.setImageResource(
                when (it) {
                    true -> {
                        R.drawable.ic_arrow_back_black_24dp
                    }
                    false -> {
                        R.drawable.ic_search_black_24dp
                    }
                }
            )
        })
        dataVModel.queryTextSearch.observe(activity, Observer {
            if (edtSearch.text?.toString() != it.s?.toString()) {
                edtSearch.setText(it.s)
            } else {
                val (s, start, before, count) = it
                searchCb.querySearch(edtSearch, s, start, before, count)
            }
        })
        initEditSearch()
        initImgLeft()
        initImgListMode()
        initImgMore()
    }

    private fun initImgMore() {
        popupMenu = popupMenu {
            section {
                item {
                    icon = R.drawable.ic_settings_black_24dp
                    label = activity.getString(R.string.setting)
                    dismissOnSelect = true
                    callback = {
                        startActivity(activity, Intent(activity, SettingsActivity::class.java), null)
                    }
                }
                item {
                    label = activity.getString(R.string.close)
                    icon = R.drawable.ic_close_black_24dp
                    dismissOnSelect = true
                }
            }
        }
        contentMore.setOnClickListener {
            popupMenu.show(activity, it)
            Toast.makeText(activity, R.string.message_popup, Toast.LENGTH_LONG).show()
        }
    }

    override fun onClear(historyText: String?) {
        searchCb.onTextCleared(historyText)
    }

    private fun setIconListMode(it: Int?) {
        when (it) {
            PublicContract.RecyclerMode.MODE_LIST -> {
                listMode.setImageResource(R.drawable.ic_view_list_black_24dp)
            }
            PublicContract.RecyclerMode.MODE_GRID -> {
                listMode.setImageResource(R.drawable.ic_view_grid_black_24dp)
            }
            PublicContract.RecyclerMode.MODE_STAGERRED_LIST -> {
                listMode.setImageResource(R.drawable.ic_stagerred_black_24dp)
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
            PublicContract.RecyclerMode.MODE_LIST -> {
                searchCb.onListModeChange(PublicContract.RecyclerMode.MODE_GRID)
                dataVModel.currentListMode.value = PublicContract.RecyclerMode.MODE_GRID
            }
            PublicContract.RecyclerMode.MODE_GRID -> {
                searchCb.onListModeChange(PublicContract.RecyclerMode.MODE_STAGERRED_LIST)
                dataVModel.currentListMode.value = PublicContract.RecyclerMode.MODE_STAGERRED_LIST

            }
            PublicContract.RecyclerMode.MODE_STAGERRED_LIST -> {
                searchCb.onListModeChange(PublicContract.RecyclerMode.MODE_LIST)
                dataVModel.currentListMode.value = PublicContract.RecyclerMode.MODE_LIST

            }
        }
    }

    private fun initImgLeft() {
        imageSearch.setOnClickListener {
            when (dataVModel.isInSearchMode.value) {
                true -> {
                    setFocusable(false)
                    dataVModel.isInSearchMode.value = false
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
                searchCb.onSubmit(edtSearch.text.toString())
                hasToSubmitted = true
                setFocusable(false)
            }
            true
        }
        edtSearch.setOnClickListener {
            if (!edtSearch.isFocusable) {
                setFocusable(true)
                edtSearch.requestFocusFromTouch()
            }
        }
        edtSearch.setOnFocusChangeListener { _, hasFocus ->
            dataVModel.isInSearchMode.value = hasFocus
            setFocusable(hasFocus)
        }
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dataVModel.queryTextSearch.value = ToolbarCardViewModel.QueryStrData(s, start, before, count)
            }

        })
    }

    private fun setFocusable(condition: Boolean) {
        edtSearch.isFocusable = condition
        edtSearch.isFocusableInTouchMode = condition
        if (condition) {
            edtSearch.requestFocusFromTouch()
            getSystemService(activity, InputMethodManager::class.java)?.apply {
                showSoftInput(edtSearch, InputMethodManager.RESULT_SHOWN)
            }
        } else {
            getSystemService(activity, InputMethodManager::class.java)?.apply {
                hideSoftInputFromWindow(edtSearch.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            }
        }
    }

    fun forceSearchCancel() {
        dataVModel.isInSearchMode.value = false
        if (hasFirstUserSearch) {
            edtSearch.text?.clear()
            searchCb.onSearchCancelled()
        } else
            hasFirstUserSearch = true
        setFocusable(false)
        edtSearch.hideClrBtn()
    }

    fun close() {
        setFocusable(false)
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