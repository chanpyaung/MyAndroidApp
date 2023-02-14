package com.chanaung.waveformeditor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

interface WaveFormRepository {

    suspend fun readWaveFormEnvelopeFromFile(uri: Uri): List<Pair<Float, Float>>

    suspend fun writeWaveFormToFile(waveforms: List<Pair<Float, Float>>, uri: Uri): Boolean

}

class WaveFormRepositoryImpl(private val context: Context): WaveFormRepository {

    var originalFileName: String? = null
    override suspend fun readWaveFormEnvelopeFromFile(uri: Uri): List<Pair<Float, Float>> = withContext(Dispatchers.IO) {
        originalFileName = getFileName(uri)
        readTextFromUri(context, uri)
    }

    override suspend fun writeWaveFormToFile(trimmedWaveForms: List<Pair<Float, Float>>, uri: Uri): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                trimmedWaveForms?.let { waveforms ->
                    if (waveforms.isNotEmpty()) {
                        for (pair in waveforms) {
                            outputStream?.write("${pair.first} ${pair.second}\n".toByteArray())
                        }
                        outputStream?.close()
                        result = true
                    }
                }
            } catch (e: IOException) {
                result = false
                e.printStackTrace()
            }
        }
        return result
    }

    @Throws(FileNotFoundException::class)
    private fun readTextFromUri(context: Context, uri: Uri): List<Pair<Float, Float>> {
        val waveFormsInText = mutableListOf<Pair<Float, Float>>()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    val values = line.split(" ")
                    if (values.size == 2) {
                        waveFormsInText.add(Pair(values[0].toFloat(), values[1].toFloat()))
                    }
                    line = reader.readLine()
                }
            }
        }
        return waveFormsInText
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        fileName = it.getString(index)
                    }
                }
            }
        }
        if (fileName == null) {
            val path = uri.path
            val cut = path?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                fileName = path?.substring(cut+1)
            }
        }
        return fileName
    }
}