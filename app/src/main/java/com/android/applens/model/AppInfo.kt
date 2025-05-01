package com.android.applens.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppInfo(
    val appName: String,
    val packageName: String,
    val lastUsedTime: Long,
    val versionName: String
) : Parcelable {
    @IgnoredOnParcel
    var iconBitmap: Bitmap? = null
}
