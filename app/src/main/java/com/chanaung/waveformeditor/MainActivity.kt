package com.chanaung.waveformeditor

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chanaung.waveformeditor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory
    }
    private lateinit var binding: ActivityMainBinding
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.loadWaveFormFromFile(it)
        }
    }

    private val saveFile = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
        Log.d(MainActivity::class.java.canonicalName, it?.toString().orEmpty())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.waveForms.observe(this) { waveForms ->
            if (waveForms.isNotEmpty()) {
                val stringBuilder = StringBuilder()
                waveForms.map { pair ->
                    stringBuilder.append("${pair.first} ${pair.second}\n")
                }
                binding.waveFormView.addWaveForms(waveForms)
            }
        }

        binding.openFileBtn.setOnClickListener {
            getContent.launch("text/plain")
        }

        binding.saveFileBtn.setOnClickListener {
            saveFile.launch("Hello File")
        }
    }
}