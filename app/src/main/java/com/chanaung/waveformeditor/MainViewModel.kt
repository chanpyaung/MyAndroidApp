package com.chanaung.waveformeditor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
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

    fun loadWaveFormFromFile(uri: Uri) {
        val mWaveForms = mutableListOf<Pair<Float, Float>>()
        viewModelScope.launch {
            val loadWaveForm = async { waveFormRepository.readWaveFormEnvelopeFromFile(uri) }
            mWaveForms.addAll(loadWaveForm.await())
            waveForms.postValue(mWaveForms)
        }
    }

}