package id.apwdevs.app.catalogue.plugin

import android.widget.Filter
import android.widget.Filterable
import id.apwdevs.app.catalogue.model.ResettableItem

abstract class SearchComponent<T : ResettableItem> : Filterable {

    /**
     * Call this method if you have to filter your data model
     *
     * @return Filter Object Filters
     * @author Alexzander Purwoko Widiantoro <purwoko908@gmail.com>
     * @since 1.0
     */
    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val objSearch = objectToBeSearch()
            if (objSearch.isNullOrEmpty()) return FilterResults()
            val charString = constraint.toString()
            val newLists: MutableList<T> = if (charString.isEmpty()) objSearch else {
                val filteredList = mutableListOf<T>()
                for (modelData in objSearch) {
                    modelData.onReset()
                    if (compareObject(charString, modelData)) {
                        onEqualAndSearch(charString, modelData)
                        filteredList.add(modelData)
                    }
                }
                filteredList
            }
            val filterResults = FilterResults()
            filterResults.values = newLists
            return filterResults
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            onSearchFinished(results?.values as MutableList<T>?)
        }

    }

    /**
     * Used to get the Position of the any part of the character string that equal to
     * @param comparatorString
     *
     * Parameters :
     * @param comparatorString The comparator string
     * @param source The source string to be searched
     * @param ignoreCase Should to be ignored case when comparing?
     *
     * @return void
     * @author Alexzander Purwoko Widiantoro <purwoko908@gmail.com>
     * @since 1.0
     */
    protected fun getItemMatchedPosition(
        comparatorString: CharSequence,
        source: CharSequence?,
        @Suppress("SameParameterValue") ignoreCase: Boolean
    ): MutableList<ItemPosition> {
        val listPositionItem = mutableListOf<ItemPosition>()
        if (source == null) return listPositionItem
        var index = 0
        while (true) {
            var startPos = -1
            var endPos: Int
            var countEqual = 0
            for (compChar in comparatorString) {
                if (index == source.length) break
                val baseChar = source[index++]
                if (compChar.equals(baseChar, ignoreCase)) {
                    if (startPos == -1) {
                        startPos = index - 1
                    }
                    countEqual++
                } else break
            }
            // if match with length, add the spannable strings
            if (countEqual == comparatorString.length) {
                endPos = index
                listPositionItem.add(ItemPosition(startPos, endPos))
            }
            if (index == source.length) break
        }
        return listPositionItem
    }

    /**
     * This method will be called after conditional checking on {@see compareObject(constraint, obj)}
     * is returning true.
     * This is empty methods, so you have to implement it if you have any change within searching
     *
     * Parameters :
     * @param constraint the String to be searched
     * @param obj The Object that have to be searched
     *
     * @author Alexzander Purwoko Widiantoro <purwoko908@gmail.com>
     * @return void
     * @since 1.0
     */
    protected open fun onEqualAndSearch(constraint: String, obj: T) {}

    /**
     * This method will be called when the search is completed
     * You must implement this method
     *
     * Parameters :
     * @param aList The returned list that have been filtered
     *
     * @return void
     * @author Alexzander Purwoko Widiantoro
     * @since 1.0
     */
    protected abstract fun onSearchFinished(aList: MutableList<T>?)

    /**
     * This method will be called before start searching
     * We have to get the object to perform the searching
     * You must implement this method
     *
     * Parameters :
     * @noparam
     *
     * @return MutableList<T>? The list to be filtered, null can make the filter methods isn't performing
     * @author Alexzander Purwoko Widiantoro
     * @since 1.0
     */
    protected abstract fun objectToBeSearch(): MutableList<T>?

    /**
     * This method will be called when looping,
     * we doesn't have the access to your data model, so
     * You have to implement this method to check your
     * own data model. If your data model does not contain
     * any character of this {@param constraint} you can return false or if have
     * you can return true, because that is measuring of filtering content
     *
     * Parameters :
     * @param constraint The string to be filtered
     * @param obj object to be filtered
     *
     * @return Boolean The conditional stage
     * @author Alexzander Purwoko Widiantoro
     * @since 1.0
     */
    protected abstract fun compareObject(constraint: String, obj: T): Boolean

    data class ItemPosition(
        var startPosition: Int,
        var endPosition: Int
    )
}
