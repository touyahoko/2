package com.mhxx.gansimu.ui.simulator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mhxx.gansimu.databinding.ActivityResultDetailBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        finish() // delegates to ResultDetailActivity
    }
}
