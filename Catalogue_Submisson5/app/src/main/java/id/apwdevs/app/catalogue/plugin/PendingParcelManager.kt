package id.apwdevs.app.catalogue.plugin

import android.os.Parcel
import android.os.Parcelable

object PendingParcelManager {
    fun <T : Parcelable> fromParcelToByteArray(objParcel: T): ByteArray {
        val parcel = Parcel.obtain()
        objParcel.writeToParcel(parcel, 0)
        val result = parcel.marshall()
        parcel.recycle()
        return result
    }

    fun <T : Parcelable> fromByteArrToParcelObj(byteArray: ByteArray, creator: Parcelable.Creator<T>): T {
        val result: T
        val parcel = Parcel.obtain()
        parcel.apply {
            unmarshall(byteArray, 0, byteArray.size)
            setDataPosition(0)
            result = creator.createFromParcel(this)
        }
        parcel.recycle()
        return result
    }
}