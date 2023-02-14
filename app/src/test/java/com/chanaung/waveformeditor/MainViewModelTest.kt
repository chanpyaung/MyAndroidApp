package com.chanaung.waveformeditor

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class MainViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var mainViewModel: MainViewModel
    private val waveforms = listOf(
        Pair(-0.05807495f, 0.037902832f),
        Pair(-0.15789795f, 0.14797974f),
        Pair(-0.41952515f, 0.65631104f),
        Pair(-0.5923767f, 0.78240967f),
        Pair(-0.3600464f, 0.55496216f),
        Pair(-0.3465271f, 0.53848267f),
        Pair(-0.25769043f, 0.30825806f),
    )

    
    @Before
    fun setUp() {
        mainViewModel = MainViewModel(Dispatchers.Unconfined, object : WaveFormRepository {
            override var originalFileName: String? = "file.txt"

            override suspend fun readWaveFormEnvelopeFromFile(uri: Uri): List<Pair<Float, Float>> {
                return waveforms
            }

            override suspend fun writeWaveFormToFile(
                waveforms: List<Pair<Float, Float>>,
                uri: Uri
            ): Boolean {
                return true
            }
        })
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testLoadWaveForms() = runTest {
        mainViewModel.loadWaveFormFromFile(Uri.parse(""))
        mainViewModel.waveForms.observeForever {

        }
        Assert.assertEquals("same value", mainViewModel.waveForms.value?.first(), waveforms.first())
        Assert.assertEquals("same value", mainViewModel.waveForms.value?.last(), waveforms.last())
        Assert.assertNotEquals("different value", mainViewModel.waveForms.value?.last()?.first, waveforms.last().second)
    }


}