package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.data.VehicleCatalog
import com.example.data.VehicleSearchEngine
import com.example.data.VehicleType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelectorDialog(
    currentVehicle: Vehicle,
    vehiclesList: List<Vehicle>,
    isSyncing: Boolean = false,
    lastSyncTimestamp: Long = 0L,
    onSelectVehicle: (Vehicle) -> Unit,
    onOpenAddVehicle: () -> Unit,
    onEditVehicle: (Vehicle) -> Unit = {},
    onSyncVehicles: () -> Unit = {},
    onDeleteVehicle: (String) -> Unit,
    onDeleteCustomVehicle: (String) -> Unit = onDeleteVehicle,
    onRestoreDefaults: () -> Unit = {},
    onOpenCustomizer: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<VehicleType?>(null) }
    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }

    // Popular brands in Brazil for fast 1-tap filtering
    val popularBrands = listOf(
        "BYD", "GWM", "GAC", "Aion", "Chery", "Toyota", "Volvo", "Zeekr", 
        "Jaecoo", "Omoda", "Honda", "BMW", "Renault", "Kia", "Hyundai"
    )

    // Confirmation dialog before deleting any vehicle
    vehicleToDelete?.let { targetVehicle ->
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            title = {
                Text(
                    text = "Remover Veículo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VoltageOnSurface
                )
            },
            text = {
                Text(
                    text = "Deseja remover \"${targetVehicle.name}\" da sua lista de veículos?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoltageOnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVehicle(targetVehicle.id)
                        vehicleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("btn_confirm_delete_vehicle")
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vehicleToDelete = null },
                    modifier = Modifier.testTag("btn_cancel_delete_vehicle")
                ) {
                    Text("Cancelar", color = VoltageOnSurfaceVariant)
                }
            },
            containerColor = VoltageSurfaceContainerHigh
        )
    }

    // Intelligent Search using VehicleSearchEngine with fuzzy matching and synonyms
    val filteredVehicles = remember(vehiclesList, searchQuery, selectedCategoryFilter) {
        VehicleSearchEngine.search(vehiclesList, searchQuery, selectedCategoryFilter)
    }

    // Global fallback catalog search if local list produces no match
    val globalMatches = remember(searchQuery, selectedCategoryFilter, filteredVehicles) {
        if (searchQuery.isNotBlank() && filteredVehicles.isEmpty()) {
            VehicleSearchEngine.search(VehicleCatalog.defaultVehicles, searchQuery, selectedCategoryFilter)
        } else {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selecionar Veículo",
                    style = MaterialTheme.typography.titleLarge,
                    color = VoltageOnSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSyncVehicles,
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("btn_sync_vehicles_header")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = VoltageSecondary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Atualizar catálogo online",
                                tint = VoltageSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onOpenAddVehicle,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = VoltagePrimaryContainer.copy(alpha = 0.8f),
                            contentColor = VoltageOnPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_open_add_vehicle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Novo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onOpenCustomizer,
                        modifier = Modifier.testTag("btn_custom_vehicle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar parâmetros",
                            tint = VoltagePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Intelligent Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Busca inteligente: BYD, H6 PHEV, Song, Omoda, EX30...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = VoltagePrimary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar", tint = VoltageOnSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_vehicle_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = VoltageOnSurface,
                        unfocusedTextColor = VoltageOnSurface,
                        focusedBorderColor = VoltagePrimary,
                        unfocusedBorderColor = VoltageOutline.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Brand Filters Carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularBrands) { brand ->
                        val isBrandSelected = searchQuery.equals(brand, ignoreCase = true)
                        SuggestionChip(
                            onClick = {
                                searchQuery = if (isBrandSelected) "" else brand
                            },
                            label = {
                                Text(
                                    text = brand,
                                    fontSize = 11.sp,
                                    fontWeight = if (isBrandSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isBrandSelected) VoltagePrimaryContainer else VoltageSurfaceContainerHigh,
                                labelColor = if (isBrandSelected) VoltageOnPrimaryContainer else VoltageOnSurfaceVariant
                            ),
                            border = BorderStroke(
                                0.5.dp, 
                                if (isBrandSelected) VoltagePrimary else VoltageOutline.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Category Chips & Counter
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("Todos (${if (searchQuery.isBlank()) vehiclesList.size else filteredVehicles.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VoltagePrimary.copy(alpha = 0.2f),
                                selectedLabelColor = VoltagePrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == VehicleType.PHEV,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == VehicleType.PHEV) null else VehicleType.PHEV },
                            label = { Text("Plug-in (PHEV)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VoltagePrimary.copy(alpha = 0.25f),
                                selectedLabelColor = VoltagePrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == VehicleType.BEV,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == VehicleType.BEV) null else VehicleType.BEV },
                            label = { Text("100% Elétrico", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VoltageSecondary.copy(alpha = 0.25f),
                                selectedLabelColor = VoltageSecondary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == VehicleType.HEV,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == VehicleType.HEV) null else VehicleType.HEV },
                            label = { Text("Híbrido (HEV)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VoltageTertiary.copy(alpha = 0.25f),
                                selectedLabelColor = VoltageTertiary
                            )
                        )
                    }
                }

                if (filteredVehicles.isEmpty()) {
                    if (globalMatches.isNotEmpty()) {
                        // Smart Auto-Discovery from Global Catalog
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VoltageSecondary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, VoltageSecondary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CloudDownload, null, tint = VoltageSecondary, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Modelos encontrados no banco de dados geral:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VoltageSecondary
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(globalMatches, key = { it.id }) { vehicle ->
                                    VehicleCardItem(
                                        vehicle = vehicle,
                                        isSelected = vehicle.id == currentVehicle.id,
                                        onSelect = {
                                            onSelectVehicle(vehicle)
                                        },
                                        onDelete = null
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = VoltageOnSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Nenhum veículo encontrado para \"$searchQuery\".",
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 13.sp
                                )
                                Button(
                                    onClick = onOpenAddVehicle,
                                    colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer)
                                ) {
                                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cadastrar este Modelo", fontSize = 12.sp, color = VoltageOnPrimaryContainer)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredVehicles, key = { it.id }) { vehicle ->
                            VehicleCardItem(
                                vehicle = vehicle,
                                isSelected = vehicle.id == currentVehicle.id,
                                onSelect = { onSelectVehicle(vehicle) },
                                onEdit = { onEditVehicle(vehicle) },
                                onDelete = { vehicleToDelete = vehicle }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRestoreDefaults,
                    modifier = Modifier.testTag("btn_restore_default_vehicles")
                ) {
                    Text("Restaurar Padrões", color = VoltageSecondary, fontSize = 12.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer)
                ) {
                    Text("Fechar", color = VoltageOnPrimaryContainer)
                }
            }
        },
        containerColor = VoltageSurfaceContainer
    )
}

@Composable
private fun VehicleCardItem(
    vehicle: Vehicle,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val tagColor = when (vehicle.type) {
        VehicleType.BEV -> VoltageSecondary
        VehicleType.PHEV -> VoltagePrimary
        VehicleType.HEV -> VoltageTertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("vehicle_item_${vehicle.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VoltageSurfaceContainerHighest else VoltageCardBg
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) tagColor else VoltageOutline.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Top row: Icon + Name + Tag Badge at Top Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tagColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (vehicle.type) {
                                VehicleType.BEV -> Icons.Default.ElectricCar
                                VehicleType.PHEV -> Icons.Default.DirectionsCar
                                VehicleType.HEV -> Icons.Default.DirectionsCar
                            },
                            contentDescription = null,
                            tint = tagColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = vehicle.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = VoltageOnSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge no canto superior direito
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = tagColor.copy(alpha = 0.18f),
                    border = BorderStroke(0.5.dp, tagColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = vehicle.tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom row: Specifications on left + Actions / Selection indicator on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp)
                ) {
                    Text(
                        text = "Bateria: ${vehicle.batteryCapacityKwh} kWh${if (vehicle.electricConsumptionKwh100km > 0) " • ${vehicle.electricConsumptionKwh100km} kWh/100km" else ""}",
                        fontSize = 11.sp,
                        color = VoltageOnSurfaceVariant
                    )
                    if (vehicle.gasolineConsumptionKmL > 0) {
                        Text(
                            text = "Gasolina: ${vehicle.gasolineConsumptionKmL} km/L${if (vehicle.maxAcChargeKw > 0) " • AC: ${vehicle.maxAcChargeKw} kW" else ""}",
                            fontSize = 11.sp,
                            color = VoltageOnSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (onEdit != null) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_edit_vehicle_${vehicle.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar especificações deste veículo",
                                tint = VoltageSecondary.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_delete_vehicle_${vehicle.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover veículo da lista",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(tagColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selecionado",
                                tint = tagColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
