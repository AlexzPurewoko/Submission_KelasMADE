package id.apwdevs.app.catalogue.manager

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PendingAlarmRunJob(
    val jobId: Int,
    val jobTags: String?,
    val workJobClassPos: Int,
    val hasRecurringAfterCalled: Boolean,
    val retryWhenJobFail: Boolean = false,
    val maxRetryCount: Int = 2
) : Parcelable