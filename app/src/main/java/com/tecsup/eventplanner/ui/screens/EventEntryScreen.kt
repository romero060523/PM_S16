package com.tecsup.eventplanner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.eventplanner.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEntryScreen(
    eventId: String? = null,
    eventTitle: String? = null,
    eventDate: String? = null,
    eventDescription: String? = null,
    onNavigateBack: () -> Unit,
    eventViewModel: EventViewModel = viewModel()
) {
    val context = LocalContext.current
    val isEditing = eventId != null
    
    var title by remember { mutableStateOf(eventTitle ?: "") }
    var date by remember { mutableStateOf(eventDate ?: "") }
    var description by remember { mutableStateOf(eventDescription ?: "") }
    
    val eventState by eventViewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Evento" else "Nuevo Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = title.isBlank() && title.isNotEmpty()
            )
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha (ej: 25/12/2024)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("DD/MM/YYYY") }
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (isEditing && eventId != null) {
                        eventViewModel.updateEvent(
                            eventId = eventId,
                            title = title,
                            date = date,
                            description = description,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Evento actualizado ✅",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onNavigateBack()
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    "Error: $error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    } else {
                        eventViewModel.createEvent(
                            title = title,
                            date = date,
                            description = description,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Evento creado ✅",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onNavigateBack()
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    "Error: $error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !eventState.isLoading && title.isNotBlank()
            ) {
                if (eventState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditing) "Actualizar" else "Guardar")
                }
            }
        }
    }
}

