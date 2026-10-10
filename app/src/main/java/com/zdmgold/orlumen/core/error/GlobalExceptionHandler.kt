package com.zdmgold.orlumen.core.error

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

class GlobalExceptionHandler(private val crashFile: File, private val delegate: Thread.UncaughtExceptionHandler?) :
    Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        writeReport(thread, throwable)
        delegate?.uncaughtException(thread, throwable)
    }

    private fun writeReport(thread: Thread, throwable: Throwable) {
        val report = buildString {
            appendLine("time=${System.currentTimeMillis()}")
            appendLine("thread=${thread.name}")
            appendLine(Log.getStackTraceString(throwable))
        }
        try {
            crashFile.writeText(report)
        } catch (ioException: IOException) {
            Log.e(TAG, "Unable to persist crash report", ioException)
        }
    }

    companion object {
        private const val TAG = "OrlumenCrash"
        const val CRASH_FILE_NAME = "last_crash.txt"

        fun install(context: Context) {
            val crashFile = File(context.filesDir, CRASH_FILE_NAME)
            Thread.setDefaultUncaughtExceptionHandler(
                GlobalExceptionHandler(crashFile, Thread.getDefaultUncaughtExceptionHandler())
            )
        }
    }
}
