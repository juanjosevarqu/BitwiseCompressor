package com.varqulabs.compressorbitwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.varqulabs.compressorbitwise.presentation.BitwiseCompressorScreen
import com.varqulabs.compressorbitwise.presentation.BitwiseCompressorViewModel
import com.varqulabs.compressorbitwise.ui.theme.CompressorBitwiseTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<BitwiseCompressorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompressorBitwiseTheme {
                BitwiseCompressorScreen(viewModel)
            }
        }
    }
}
