package com.varqulabs.compressorbitwise.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitwiseCompressorScreen(viewModel: BitwiseCompressorViewModel) {

    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            uri?.let {
                val compressedData = viewModel.compressInput()
                compressedData?.let { data ->
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(data)
                    }
                }
            }
        }
    )

    val createPlainTextDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(state.inputText.toByteArray())
                }
            }
        }
    )

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val mimeType = context.contentResolver.getType(uri)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = when (mimeType) {
                        "application/octet-stream" -> viewModel.decompressData(inputStream.readBytes())
                        "text/plain" -> inputStream.bufferedReader().use { it.readText() }
                        else -> "Tipo de archivo no soportado."
                    }
                    viewModel.updateSelectedTextFromDocument(content)
                }
            }
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Compressor Bitwise") },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            OutlinedTextField(
                value = state.inputText,
                onValueChange = { newTextValue -> viewModel.validateAndCalculateSizes(newTextValue) },
                label = { Text("Ingrese texto (A-Z, a-z, 0-9, espacios") },
                isError = state.errorMessage != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )

            AnimatedVisibility(state.errorMessage != null) {
                state.errorMessage?.let { Text(it, color = Color.Red) }
            }

            AnimatedVisibility(state.inputText.isNotEmpty() && state.errorMessage == null) {
                SizeInfo(
                    compressedSizeBytes = state.compressedSizeBytes,
                    originalSizeBytes = state.originalSizeBytes
                )
            }

            Button(
                onClick = {
                    createDocumentLauncher.launch("compressed.bin")
                },
                enabled = state.errorMessage == null && state.inputText.isNotEmpty()
            ) {
                Text("Guardar TXT Comprimido")
            }

            Button(
                onClick = {
                    openDocumentLauncher.launch(arrayOf("application/octet-stream", "text/plain"))
                }
            ) {
                Text("Seleccionar Archivos")
            }

            Button(
                onClick = {
                    createPlainTextDocumentLauncher.launch("sin_comp.txt")
                },
                enabled = state.errorMessage == null && state.inputText.isNotEmpty()
            ) {
                Text("Guardar .TXT Convencional")
            }

            Spacer(modifier = Modifier.height(16.dp))

            state.selectedTextFromDocument?.let {
                Text("Texto Seleccionado:", style = MaterialTheme.typography.bodyLarge)
                SelectionContainer {
                    Text(it)
                }
            }
        }
    }
}

@Composable
private fun SizeInfo(
    modifier: Modifier = Modifier,
    compressedSizeBytes: Int,
    originalSizeBytes: Int
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Text("Tamaño comprimido esperado (bytes): $compressedSizeBytes")

        Text("Tamaño original (bytes): $originalSizeBytes")
    }
}

