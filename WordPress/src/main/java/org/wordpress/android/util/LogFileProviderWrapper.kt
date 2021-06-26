package org.sitebay.android.util

import android.content.Context
import org.sitebay.android.util.helpers.logfile.LogFileProvider
import javax.inject.Inject

class LogFileProviderWrapper @Inject constructor(context: Context) {
    private val logFileProvider = LogFileProvider.fromContext(context)

    fun getLogFilesDirectory() = logFileProvider.getLogFileDirectory()

    fun getLogFiles() = logFileProvider.getLogFiles()
}
