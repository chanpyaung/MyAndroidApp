package com.chanaung.waveformeditor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.chanaung.waveformeditor.utils.Event
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainViewModel(context: Context): ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()
                return MainViewModel(application.applicationContext) as T
            }
        }
    }

    private val waveFormRepository = WaveFormRepositoryImpl(context)
    val waveForms = MutableLiveData<List<Pair<Float, Float>>>()
    val fileSaveResult = MutableLiveData<Event<Boolean>>()

    fun loadWaveFormFromFile(uri: Uri) {
        val mWaveForms = mutableListOf<Pair<Float, Float>>()
        viewModelScope.launch {
            val loadWaveForm = async { waveFormRepository.readWaveFormEnvelopeFromFile(uri) }
            mWaveForms.addAll(loadWaveForm.await())
            waveForms.postValue(mWaveForms)
        }
    }

    fun saveToFile(uri: Uri, trimmedWaveForms: List<Pair<Float, Float>>) {
        viewModelScope.launch {
            val saveFile = async { waveFormRepository.writeWaveFormToFile(trimmedWaveForms, uri) }
            val result = saveFile.await()
            fileSaveResult.postValue(Event(result))
        }
    }

    fun getFileName(): String = waveFormRepository.originalFileName?.let {
        "copyOf$it"
    } ?: "file.txt"

}