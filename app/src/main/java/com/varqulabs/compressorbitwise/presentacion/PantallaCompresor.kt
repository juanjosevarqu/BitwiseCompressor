package com.varqulabs.compressorbitwise.presentacion

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.varqulabs.compressorbitwise.R
import com.varqulabs.compressorbitwise.negocio.CompresorBitwise
import com.varqulabs.compressorbitwise.negocio.VectorBitsG
import java.io.DataInputStream
import java.io.DataOutputStream
import androidx.compose.foundation.shape.RoundedCornerShape




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCompresor(compresor: CompresorBitwise) {

    val context = LocalContext.current
    val administradorDeTeclado = LocalFocusManager.current

    var textoDeEntrada by remember { mutableStateOf("") }
    val pesoOriginalBytes by remember(textoDeEntrada) { derivedStateOf { textoDeEntrada.length } }
    val pesoComprimidoBytes by remember(textoDeEntrada) {
        derivedStateOf {
            val bitsPorCaracter = compresor.validador.bitsPorCaracter
            val totalBits = textoDeEntrada.length * bitsPorCaracter + 6 + 12 // 6 bits para header, 12 para longitud
            val totalBytes = (totalBits + 7) / 8 // Dividir entre 8 y redondear hacia arriba
            totalBytes
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

    val creadorDeTXT = recordarCreadorDeTXT(
        context = context,
        texto = textoDeEntrada,
        limpiarTexto = {
            textoDeEntrada = ""
            Toast.makeText(context, "Archivo TxT creado con éxito", Toast.LENGTH_SHORT).show()
            administradorDeTeclado.clearFocus()
        }
    )

    val creadorDeArchivosBinarios = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            uri?.let {
                guardarVectorComprimido(context, compresor, textoDeEntrada, uri)
            }
        }
    )

    val seleccionadorDeDocumentos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
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
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // Distribuir elementos
                    ) {
                        // Logo en la esquina izquierda
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo), // Cambia "ic_logo" por el nombre de tu archivo
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(40.dp) // Ajusta el tamaño según necesites
                                .clip(CircleShape) // Hace que la imagen sea circular
                                .background(Color.White) // Opcional: Agrega un fondo blanco para resaltarlo
                                .padding(4.dp), // Opcional: Ajusta el padding dentro del círculo
                            tint = Color.Unspecified // Mantiene el color original de la imagen
                        )


                        // Título centrado
                        Text(
                            text = "Compresor de Archivos",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )

                        // Espacio vacío a la derecha (si no necesitas otro icono aquí)
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0x000000) // Color de fondo
                )
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
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp), // Esquinas rectas
//                modifier = Modifier.padding(6.dp)
                modifier = Modifier
                    .fillMaxWidth() // Ocupa toda la fila
                    .padding(vertical = 6.dp) // Espaciado vertical para separar del resto
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.folder_open), // Reemplaza "folder_open" con tu ícono
                    contentDescription = "Abrir Archivos",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir Archivos")
            }

        }
    }
}

@Composable
private fun recordarCreadorDeTXT(
    context: Context,
    texto: String,
    limpiarTexto: () -> Unit

) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("text/plain"),
    onResult = { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(texto.toByteArray())
                limpiarTexto()
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
//        modifier = modifier
//            .fillMaxWidth() // Asegura que el cuadro de texto use todo el espacio
//            .height(180.dp)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)// Espaciado entre los botones
    ) {
        Button(
            onClick = clickEnCrearTXT,
            enabled = crearTXTHabilitado,
            modifier = Modifier.weight(1f),// Distribución proporcional
            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp) // Esquinas rectas
        ) {
            Icon(
                painter = painterResource(id = R.drawable.file_save), // Tu ícono personalizado
                contentDescription = "Guardar archivo TXT", // Descripción accesible
                modifier = Modifier.size(20.dp) // Tamaño del ícono
            )
            Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el ícono y el texto

            Text("Guardar.txt")
        }

        Button(
            onClick = clickEnComprimir,
            enabled = comprimirHabilitado,
            modifier = Modifier.weight(1f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp) // Esquinas rectas
        ) {

            Icon(
                painter = painterResource(id = R.drawable.file_save), // Tu ícono personalizado
                contentDescription = "Comprimir archivo", // Descripción accesible
                modifier = Modifier.size(20.dp) // Tamaño del ícono
            )
            Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el ícono y el texto

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
                dos.write(vectorComprimido.obtenerArregloBytes())
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
                        val data = dis.readBytes()
                        val vectorComprimido = VectorBitsG(data.size * 8 / compresor.validador.bitsPorCaracter, compresor.validador.bitsPorCaracter.toInt())
                        vectorComprimido.actualizarDesdeByteArray(data)
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
