package com.varqulabs.compressorbitwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.varqulabs.compressorbitwise.negocio.CompresorBitwise
import com.varqulabs.compressorbitwise.negocio.ValidadorDeCaracteres
import com.varqulabs.compressorbitwise.presentacion.PantallaCompresor
import com.varqulabs.compressorbitwise.ui.theme.CompressorBitwiseTheme

class MainActivity : ComponentActivity() {

    private val compresorBitwise = CompresorBitwise(
        ValidadorDeCaracteres(
            caracteresPermitidos = buildList {
                addAll(('A'..'Z'))
                addAll(('a'..'z'))
                addAll('0'..'9')
                add('\n')
                add(' ')
            }
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompressorBitwiseTheme {
                PantallaCompresor(compresorBitwise)
            }
        }
    }
}
