package org.sitebay.android.ui.sitecreation.services

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
data class SiteCreationServiceData(
    val segmentId: Long?,
    val siteDesign: String?,
    val domain: String,
    val wpValues: Map<String, String>
) : Parcelable
