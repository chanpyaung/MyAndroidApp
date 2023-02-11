package com.chanaung.waveformeditor

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader

interface WaveFormRepository {

    suspend fun readWaveFormEnvelopeFromFile(uri: Uri): List<Pair<Float, Float>>

    suspend fun writeWaveFormToFile(waveforms: List<Pair<Float, Float>>): File

}

class WaveFormRepositoryImpl(private val context: Context): WaveFormRepository {

    override suspend fun readWaveFormEnvelopeFromFile(uri: Uri): List<Pair<Float, Float>> = withContext(Dispatchers.IO) {
        readTextFromUri(context, uri)
    }

    override suspend fun writeWaveFormToFile(waveforms: List<Pair<Float, Float>>): File {
        TODO("Not yet implemented")
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
}