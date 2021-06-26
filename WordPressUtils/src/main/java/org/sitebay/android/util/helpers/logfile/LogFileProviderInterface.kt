package org.sitebay.android.util.helpers.logfile

import java.io.File

/**
 * An interface to retrieve log files
 */
interface LogFileProviderInterface {
    fun getLogFiles(): List<File>

    fun getLogFileDirectory(): File
}
