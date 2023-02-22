package com.chanaung.waveformeditor

import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.chanaung.waveformeditor.utils.Event
import kotlinx.coroutines.*

class MainViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val waveFormRepository: WaveFormRepository): ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()
                return MainViewModel(Dispatchers.Main, WaveFormRepositoryImpl(application.applicationContext)) as T
            }
        }
    }

    private val _waveForms = MutableLiveData<List<Pair<Float, Float>>>()
    val waveForms: LiveData<List<Pair<Float, Float>>> get() = _waveForms
    val fileSaveResult = MutableLiveData<Event<Boolean>>()
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(dispatcher + viewModelJob)

    fun loadWaveFormFromFile(uri: Uri) {
        val mWaveForms = mutableListOf<Pair<Float, Float>>()
        viewModelScope.launch(dispatcher) {
            val loadWaveForm = async { waveFormRepository.readWaveFormEnvelopeFromFile(uri) }
            mWaveForms.addAll(loadWaveForm.await())
            _waveForms.postValue(mWaveForms)
        }
    }

    fun saveToFile(uri: Uri, trimmedWaveForms: List<Pair<Float, Float>>) {
        uiScope.launch(dispatcher) {
            val saveFile = async { waveFormRepository.writeWaveFormToFile(trimmedWaveForms, uri) }
            val result = saveFile.await()
            fileSaveResult.postValue(Event(result))
        }
    }

    fun getFileName(): String = waveFormRepository.originalFileName?.let {
        "copyOf$it"
    } ?: "file.txt"

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}