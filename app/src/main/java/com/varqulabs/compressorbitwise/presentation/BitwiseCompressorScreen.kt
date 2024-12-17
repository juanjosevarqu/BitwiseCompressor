package com.varqulabs.compressorbitwise.presentation

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitwiseCompressorScreen(viewModel: BitwiseCompressorViewModel) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val createPlainTextDocumentLauncher = rememberCreateTXTLauncher(context, state.inputText)

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            uri?.let {
                val compressedData = viewModel.compressInput()
                if (compressedData != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(compressedData)
                    }
                } else {
                    Toast.makeText(context, "Error al comprimir el archivo", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val fileName = getFileName(context, uri)
                if (!isSupportedFileType(fileName)) {
                    Toast.makeText(context, "Tipo de archivo no soportado", Toast.LENGTH_SHORT).show()
                    return@let
                }
                val mimeType = context.contentResolver.getType(uri)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    when (mimeType) {
                        "application/octet-stream" -> {
                            val data = inputStream.readBytes()
                            val decompressedText = viewModel.decompressData(data)
                            if (decompressedText == null) {
                                Toast.makeText(context, "Error al descomprimir. Archivo inválido o no soportado.", Toast.LENGTH_LONG).show()
                            }
                        }
                        "text/plain" -> {
                            val txt = inputStream.bufferedReader().use { it.readText() }
                            viewModel.updateSelectedTextFromDocument(txt)
                        }
                    }
                }
            }
        }
    )

    val isValidText = state.inputText.isNotEmpty() && state.errorMessage == null

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { focusManager.clearFocus() },
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            InputField(
                modifier = Modifier.fillMaxWidth(),
                currentText = state.inputText,
                isError = state.errorMessage != null,
                onTextChanged = { viewModel.updateAndCalculateSizes(it) },
            )

            AnimatedVisibility(state.errorMessage != null) {
                state.errorMessage?.let { Text(it, color = Color.Red) }
            }

            AnimatedVisibility(state.isLoading) {
                CircularProgressIndicator(Modifier.size(48.dp))
            }

            AnimatedVisibility(isValidText) {
                SizeInfo(
                    modifier = Modifier.fillMaxWidth(),
                    compressedSizeBytes = state.compressedSizeBytes,
                    originalSizeBytes = state.originalSizeBytes
                )
            }

            TextActions(
                modifier = Modifier.fillMaxWidth(),
                enabledSaveTXT = isValidText,
                enabledSaveCompressed = isValidText && state.compressedSizeBytes <= state.originalSizeBytes,
                onSaveTXT = { createPlainTextDocumentLauncher.launch("normal.txt") },
                onSaveCompressed = { createDocumentLauncher.launch("comp.bwise") },
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    openDocumentLauncher.launch(arrayOf("application/octet-stream", "text/plain"))
                }
            ) {
                Text("Abrir archivos")
            }

            state.selectedTextFromDocument?.let {
                Text("Texto seleccionado:", style = MaterialTheme.typography.titleMedium)

                SelectionContainer { Text(it) }
            }
        }
    }
}

@Composable
private fun rememberCreateTXTLauncher(
    context: Context,
    inputText: String,
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("text/plain"),
    onResult = { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(inputText.toByteArray())
            }
        }
    }
)

@Composable
private fun InputField(
    modifier: Modifier = Modifier,
    currentText: String,
    isError: Boolean,
    onTextChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = currentText,
        onValueChange = { newTextValue -> onTextChanged(newTextValue) },
        label = { Text("Ingrese texto (A-Z, a-z, 0-9, espacios)") },
        isError = isError,
        trailingIcon = {
            if (currentText.isNotEmpty()) {
                IconButton(
                    onClick = { onTextChanged("") },
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                    )
                }
            }
        },
        modifier = modifier.height(180.dp),
    )
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
        AnimatedVisibility(compressedSizeBytes <= originalSizeBytes) {
            Text("Tamaño comprimido aprox (bytes): $compressedSizeBytes")
        }

        Text("Tamaño original (bytes): $originalSizeBytes")
    }
}

@Composable
private fun TextActions(
    modifier: Modifier = Modifier,
    enabledSaveTXT: Boolean,
    enabledSaveCompressed: Boolean,
    onSaveTXT: () -> Unit,
    onSaveCompressed: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSaveTXT,
            enabled = enabledSaveTXT,
        ) {
            Text("Guardar .TXT")
        }

        Button(
            onClick = onSaveCompressed,
            enabled = enabledSaveCompressed,
            modifier = Modifier.weight(1f),
        ) {
            Text("Guardar Comprimido", maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var fileName: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(
            /* uri = */ uri,
            /* projection = */ arrayOf(OpenableColumns.DISPLAY_NAME),
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    return fileName.orEmpty()
}

private fun isSupportedFileType(fileName: String): Boolean {
    return fileName.endsWith(".bwise") || fileName.endsWith(".txt")
}
