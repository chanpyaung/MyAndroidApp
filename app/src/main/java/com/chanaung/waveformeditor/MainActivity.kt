package com.chanaung.waveformeditor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.chanaung.waveformeditor.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

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
            val file = File(it.path)

            try {
                val outputStream = contentResolver.openOutputStream(it)
                trimmedWaveForms?.let { waveforms ->
                    if (waveforms.isNotEmpty()) {
                        for (pair in waveforms) {
                            outputStream?.write("${pair.first} ${pair.second}\n".toByteArray())
                        }
                        outputStream?.close()
//                        binding.waveFormView.clear()
//                        viewModel.loadWaveFormFromFile(uri)
                    }











































                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        Log.d(MainActivity::class.java.canonicalName, uri?.toString().orEmpty())
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

        binding.openFileBtn.setOnClickListener {
            binding.waveFormView.clear()
            getContent.launch("text/plain")
        }

        binding.saveFileBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, "trimmedfile.txt")
            }
            saveFile.launch("file.txt")
        }

        binding.cropBtn.setOnClickListener {
            val trimmed = binding.waveFormView.getTrimmedWaveForm()
            trimmedWaveForms = trimmed
            val stringBuilder = java.lang.StringBuilder()
            for (i in trimmed) {
                stringBuilder.append("${i.first} ${i.second}\n")
            }
            binding.waveFormView.clear()
            if (trimmedWaveForms.isNullOrEmpty().not()) {
                binding.waveFormView.addWaveForms(trimmed)
            }
            Log.d("TRIMMED==> ", stringBuilder.toString())
        }
    }
}