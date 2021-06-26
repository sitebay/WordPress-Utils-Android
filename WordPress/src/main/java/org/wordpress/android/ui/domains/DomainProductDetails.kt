package org.sitebay.android.ui.domains

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
data class DomainProductDetails(
    val productId: Int,
    val domainName: String
) : Parcelable
