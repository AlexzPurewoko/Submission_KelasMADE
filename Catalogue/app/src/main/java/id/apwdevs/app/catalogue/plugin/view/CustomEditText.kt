package id.apwdevs.app.catalogue.plugin.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import id.apwdevs.app.catalogue.R

class CustomEditText : AppCompatEditText {
    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet) :super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttrs: Int): super(context, attrs, defStyleAttrs) {
        init()
    }
    var mClearButtonImage: Drawable? = null
    var onBtnClearClicked : OnClrBtnClicked? = null
    private fun init(){
        mClearButtonImage = ResourcesCompat.getDrawable(resources, R.drawable.ic_close_black_24dp, null)
        setOnTouchListener(object : OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(event == null) return false
                compoundDrawablesRelative[2]?.apply {
                    val clearBtnStart : Int
                    val clearBtnEnd : Int
                    var isClrBtnClicked = false
                    if(Build.VERSION.SDK_INT >= 23 && layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                        clearBtnEnd = (mClearButtonImage?.intrinsicWidth ?: 0) + paddingStart
                        if (event.x < clearBtnEnd)
                            isClrBtnClicked = true
                    } else {
                        clearBtnStart = width - paddingEnd - (mClearButtonImage?.intrinsicWidth ?: 0)
                        if(event.x > clearBtnStart)
                            isClrBtnClicked = true
                    }

                    if(isClrBtnClicked){
                        if(event.action == MotionEvent.ACTION_DOWN){
                            mClearButtonImage = ResourcesCompat.getDrawable(resources, R.drawable.ic_close_black_24dp, null)
                            showClrBtn()
                            return true
                        } else if(event.action == MotionEvent.ACTION_UP){
                            mClearButtonImage = ResourcesCompat.getDrawable(resources, R.drawable.ic_close_black_24dp, null)
                            onBtnClearClicked?.onClear(text?.toString())
                            text?.clear()
                            hideClrBtn()
                            return true
                        } else {
                            return false
                        }
                    } else {
                        return false
                    }
                }
                return false
            }

        })
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!s.toString().isEmpty())
                    showClrBtn()
            }

        })
    }

    private fun showClrBtn() {
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, mClearButtonImage, null)
    }
    fun hideClrBtn() {
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
    }

    interface OnClrBtnClicked {
        fun onClear(historyText: String?)
    }
}