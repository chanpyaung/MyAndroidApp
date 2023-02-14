package com.chanaung.waveformeditor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.chanaung.waveformeditor.databinding.ActivityMainBinding
import java.io.File

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

    private val saveFile = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let {
            trimmedWaveForms?.let { waveforms ->
                viewModel.saveToFile(it, waveforms)
            }
        }
    }

    private var trimmedWaveForms: List<Pair<Float, Float>>? = null
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
                binding.waveFormView.invalidate()
                binding.waveFormView.addWaveForms(emptyList())
                binding.waveFormView.addWaveForms(waveForms)
            }
        }

        viewModel.fileSaveResult.observe(this) {
            it.getContentIfNotEmitted()?.let { result ->
                if (result)
                    Toast.makeText(this, "File save successfully.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.openFileBtn.setOnClickListener {
            binding.waveFormView.clear()
            getContent.launch("text/plain")
        }

        binding.saveFileBtn.setOnClickListener {
            saveFile.launch(viewModel.getFileName())
        }

        binding.cropBtn.setOnClickListener {
            val trimmed = binding.waveFormView.getTrimmedWaveForm()
            trimmedWaveForms = trimmed
            binding.waveFormView.clear()
            trimmedWaveForms?.run {
                if (isNotEmpty()) {
                    binding.waveFormView.addWaveForms(this)
                }
            }
        }
    }
}