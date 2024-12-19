package com.varqulabs.compressorbitwise.presentacion

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.varqulabs.compressorbitwise.negocio.CompresorBitwise
import com.varqulabs.compressorbitwise.negocio.VectorBitsG
import java.io.DataInputStream
import java.io.DataOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCompresor(compresor: CompresorBitwise) {

    val context = LocalContext.current
    val administradorDeTeclado = LocalFocusManager.current

    var textoDeEntrada by remember { mutableStateOf("") }
    val pesoOriginalBytes by remember(textoDeEntrada) { derivedStateOf { textoDeEntrada.length } }
    val pesoComprimidoBytes by remember(textoDeEntrada) {
        derivedStateOf {
            val totalBits = textoDeEntrada.length * compresor.validador.bitsPorCaracter + 6 + 12
            val totalInts = (totalBits + 31) / 32
            totalInts * 4
        }
    }

    val mensajeDeError by remember(textoDeEntrada) {
        derivedStateOf {
            if (textoDeEntrada.isEmpty()) {
                ""
            } else {
                var mensaje = ""
                for (caracter in textoDeEntrada) {
                    if (!compresor.validador.esValido(caracter)) {
                        mensaje = "Carácter '$caracter' no permitido."
                        break
                    }
                }
                mensaje
            }
        }
    }

    val creadorDeTXT = recordarCreadorDeTXT(context, texto = textoDeEntrada)

    val creadorDeArchivosBinarios = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            uri?.let {
                /*val textoComprimido = compresor.comprimir(textoDeEntrada)
                if (compressedData != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(compressedData)
                    }
                } else {
                    Toast.makeText(context, "Error al comprimir el archivo", Toast.LENGTH_LONG).show()
                }*/
                guardarVectorComprimido(context, compresor, textoDeEntrada, uri)
            }
        }
    )

    val seleccionadorDeDocumentos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                /*val fileName = obtenerNombreDeArchivo(context, uri)
                if (!esUnArchivoSoportado(fileName)) {
                    Toast.makeText(context, "Tipo de archivo no soportado", Toast.LENGTH_SHORT).show()
                    return@let
                }
                val tipoDeArchivo = context.contentResolver.getType(uri)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    when (tipoDeArchivo) {
                        "application/octet-stream" -> {
                            val data = inputStream.readBytes()
                            val decompressedText = compresor.descomprimir() // Archivo que contiene el VectorDeBitsG
                            if (decompressedText == null) {
                                Toast.makeText(context, "Error al descomprimir. Archivo inválido o no soportado.", Toast.LENGTH_LONG).show()
                            }
                        }
                        "text/plain" -> {
                            val txt = inputStream.bufferedReader().use { it.readText() }
                            textoDeEntrada = txt
                        }
                    }
                }*/
                procesarArchivoSeleccionado(context, uri, compresor, onTextoCargado = { texto ->
                    textoDeEntrada = texto
                })
            }
        }
    )

    val esUnTextoValido = textoDeEntrada.isNotEmpty() && mensajeDeError.isEmpty()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { administradorDeTeclado.clearFocus() },
        topBar = {
            TopAppBar(
                title = { Text("Compresor/Descompresor de TXT") },
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

            CampoDeEntradaDeTexto(
                modifier = Modifier.fillMaxWidth(),
                textoDeEntrada = textoDeEntrada,
                hayError = mensajeDeError.isNotEmpty(),
                cambioElTexto = { nuevoTexto ->
                    textoDeEntrada = nuevoTexto
                }
            )

            ComponenteConAnimacion(visible = mensajeDeError.isNotEmpty()) {
                Text(mensajeDeError, color = Color.Red)
            }

            ComponenteConAnimacion(visible = esUnTextoValido) {
                InformacionDePesosDeArchivos(
                    modifier = Modifier.fillMaxWidth(),
                    pesoComprimido = pesoComprimidoBytes,
                    pesoOriginal = pesoOriginalBytes,
                )
            }

            BotonesDeCrear(
                modifier = Modifier.fillMaxWidth(),
                crearTXTHabilitado = esUnTextoValido,
                comprimirHabilitado = esUnTextoValido && pesoComprimidoBytes <= pesoOriginalBytes,
                clickEnCrearTXT = { creadorDeTXT.launch("normal.txt") },
                clickEnComprimir = { creadorDeArchivosBinarios.launch("comp.bwise") },
            )

            Button(
                onClick = {
                    administradorDeTeclado.clearFocus()
                    seleccionadorDeDocumentos.launch(arrayOf("application/octet-stream", "text/plain"))
                }
            ) {
                Text("Abrir archivo")
            }
        }
    }
}

@Composable
private fun recordarCreadorDeTXT(
    context: Context,
    texto: String,
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("text/plain"),
    onResult = { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(texto.toByteArray())
            }
        }
    }
)

@Composable
private fun CampoDeEntradaDeTexto(
    modifier: Modifier = Modifier,
    textoDeEntrada: String,
    hayError: Boolean,
    cambioElTexto: (String) -> Unit,
) {
    OutlinedTextField(
        value = textoDeEntrada,
        onValueChange = { nuevoValorDeTexto -> cambioElTexto(nuevoValorDeTexto) },
        label = { Text("Ingrese texto (A-Z, a-z, 0-9, espacios)") },
        isError = hayError,
        trailingIcon = {
            if (textoDeEntrada.isNotEmpty()) {
                IconButton(
                    onClick = { cambioElTexto("") },
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar campo de texto",
                    )
                }
            }
        },
        modifier = modifier.height(180.dp),
    )
}

@Composable
private fun InformacionDePesosDeArchivos(
    modifier: Modifier = Modifier,
    pesoComprimido: Int,
    pesoOriginal: Int
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ComponenteConAnimacion(visible = pesoComprimido <= pesoOriginal) {
            Text("Tamaño comprimido aprox (bytes): $pesoComprimido")
        }

        Text("Tamaño original (bytes): $pesoOriginal")
    }
}

@Composable
private fun BotonesDeCrear(
    modifier: Modifier = Modifier,
    crearTXTHabilitado: Boolean,
    comprimirHabilitado: Boolean,
    clickEnCrearTXT: () -> Unit,
    clickEnComprimir: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = clickEnCrearTXT,
            enabled = crearTXTHabilitado,
            modifier = Modifier.weight(1f)
        ) {
            Text("Crear .TXT")
        }

        Button(
            onClick = clickEnComprimir,
            enabled = comprimirHabilitado,
            modifier = Modifier.weight(1f),
        ) {
            Text("Comprimir")
        }
    }
}

@Composable
private fun ComponenteConAnimacion(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(visible = visible) {
        content()
    }
}

private fun obtenerNombreDeArchivo(context: Context, direccionDeRecurso: Uri): String {
    var fileName: String? = null
    if (direccionDeRecurso.scheme == "content") {
        val cursor = context.contentResolver.query(
            /* uri = */ direccionDeRecurso,
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

private fun esUnArchivoSoportado(nombreDeArchivo: String): Boolean {
    return nombreDeArchivo.endsWith(".bwise") || nombreDeArchivo.endsWith(".txt")
}

private fun guardarVectorComprimido(
    context: Context,
    compresor: CompresorBitwise,
    texto: String,
    uri: Uri
) {
    val vectorComprimido = compresor.comprimir(texto)
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            DataOutputStream(outputStream).use { dos ->
                vectorComprimido.v.forEach { valor -> dos.writeInt(valor) }
            }
        }
        Toast.makeText(context, "Archivo comprimido guardado con éxito", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar archivo comprimido", Toast.LENGTH_LONG).show()
    }
}

private fun procesarArchivoSeleccionado(
    context: Context,
    uri: Uri,
    compresor: CompresorBitwise,
    onTextoCargado: (String) -> Unit
) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val tipoDeArchivo = context.contentResolver.getType(uri)

            val nombreDeArchivo = obtenerNombreDeArchivo(context, uri)
            if (!esUnArchivoSoportado(nombreDeArchivo)) {
                Toast.makeText(context, "Tipo de archivo no soportado", Toast.LENGTH_SHORT).show()
                return
            }

            when (tipoDeArchivo) {
                "application/octet-stream" -> {
                    DataInputStream(inputStream).use { dis ->
                        val data = mutableListOf<Int>()
                        while (dis.available() > 0) { data.add(dis.readInt()) }
                        val vectorComprimido = VectorBitsG(data.size * 32 / compresor.validador.bitsPorCaracter, compresor.validador.bitsPorCaracter.toInt())
                        vectorComprimido.v = data.toIntArray()
                        val textoDescomprimido = compresor.descomprimir(vectorComprimido)
                        onTextoCargado(textoDescomprimido)
                        Toast.makeText(context, "Archivo descomprimido con éxito", Toast.LENGTH_SHORT).show()
                    }
                }
                "text/plain" -> { // Archivo de texto normal
                    val texto = inputStream.bufferedReader().use { it.readText() }
                    onTextoCargado(texto)
                }
                else -> {
                    Toast.makeText(context, "Tipo de archivo no soportado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al procesar archivo", Toast.LENGTH_LONG).show()
    }
}
