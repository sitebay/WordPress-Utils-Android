package org.sitebay.android.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

inline fun <T : ViewBinding> ViewGroup.viewBinding(inflateBinding: (LayoutInflater, ViewGroup, Boolean) -> T) =
        inflateBinding(LayoutInflater.from(context), this, false)
