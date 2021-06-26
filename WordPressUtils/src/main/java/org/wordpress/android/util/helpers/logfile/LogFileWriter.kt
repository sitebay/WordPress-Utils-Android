package org.sitebay.android.util.helpers.logfile

import android.util.Log
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.io.FileWriter
import java.util.Date
import org.sitebay.android.util.DateTimeUtils
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A class that manages writing to a log file.
 *
 * This class creates and writes to a log file, and will typically persist for the entire lifecycle
 * of its host application.
 */
class LogFileWriter @JvmOverloads constructor(
    logFileProvider: LogFileProviderInterface,
    fileId: String = DateTimeUtils.iso8601FromDate(Date())
) {
    private val file = File(logFileProvider.getLogFileDirectory(), "$fileId.log")
    private val fileWriter: FileWriter = FileWriter(file)
    private var writingFailed = false

    /**
     * A serial executor used to write to the file in a background thread
     */
    private val queue: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * A reference to the underlying {@link Java.IO.File} file.
     * Should only be used for testing.
     */
    @TestOnly
    fun getFile(): File = file

    /**
     * Writes the provided string to the log file synchronously
     */
    fun write(data: String) {
        if (!writingFailed) {
            queue.execute {
                try {
                    if (!writingFailed) {
                        fileWriter.write(data)
                        fileWriter.flush()
                    }
                } catch (ioe: IOException) {
                    writingFailed = true
                    Log.e("LogFileWriter", "Writing log failed: ${ioe.stackTrace}")
                }
            }
        }
    }
}
