package org.sitebay.android.ui.uploads

import org.sitebay.android.fluxc.model.MediaModel

interface VideoOptimizerProvider {
    fun start()
}

interface VideoOptimizationListener {
    fun onVideoOptimizationCompleted(media: MediaModel)
    fun onVideoOptimizationProgress(media: MediaModel, progress: Float)
}

data class ProgressEvent(@JvmField val media: MediaModel, @JvmField val progress: Float)
