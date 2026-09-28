package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.data.VehicleCatalog
import com.example.data.VehicleSearchEngine
import com.example.data.VehicleType
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleDialog(
    onAddVehicle: (Vehicle) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VehicleType.PHEV) }
    
    var batteryCapacity by remember { mutableStateOf("18.3") }
    var electricConsumption by remember { mutableStateOf("19.0") }
    var gasolineConsumption by remember { mutableStateOf("16.5") }
    var maxAcCharge by remember { mutableStateOf("7.0") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Smart suggestions as the user types
    val smartSuggestions = remember(name, brand) {
        val query = "$brand $name".trim()
        if (query.length >= 2) {
            VehicleSearchEngine.search(VehicleCatalog.defaultVehicles, query).take(4)
        } else {
            emptyList()
        }
    }

    fun applySuggestion(s: Vehicle) {
        name = s.name
        brand = s.brand
        selectedType = s.type
        batteryCapacity = s.batteryCapacityKwh.toString()
        electricConsumption = s.electricConsumptionKwh100km.toString()
        gasolineConsumption = s.gasolineConsumptionKmL.toString()
        maxAcCharge = s.maxAcChargeKw.toString()
        errorMessage = null
    }

    // Update defaults when type changes
    fun onTypeChanged(type: VehicleType) {
        selectedType = type
        when (type) {
            VehicleType.BEV -> {
                if (batteryCapacity == "18.3" || batteryCapacity == "1.8") batteryCapacity = "50.0"
                if (electricConsumption == "19.0" || electricConsumption == "0.0") electricConsumption = "15.0"
                gasolineConsumption = "0.0"
                if (maxAcCharge == "0.0" || maxAcCharge == "7.0") maxAcCharge = "11.0"
            }
            VehicleType.PHEV -> {
                if (batteryCapacity == "50.0" || batteryCapacity == "1.8") batteryCapacity = "19.0"
                if (electricConsumption == "0.0" || electricConsumption == "15.0") electricConsumption = "19.5"
                if (gasolineConsumption == "0.0") gasolineConsumption = "16.0"
                if (maxAcCharge == "0.0" || maxAcCharge == "11.0") maxAcCharge = "7.0"
            }
            VehicleType.HEV -> {
                batteryCapacity = "1.8"
                electricConsumption = "0.0"
                if (gasolineConsumption == "0.0") gasolineConsumption = "17.5"
                maxAcCharge = "0.0"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = VoltagePrimary
                )
                Text(
                    text = "Adicionar Novo Carro",
                    style = MaterialTheme.typography.titleLarge,
                    color = VoltageOnSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cadastre qualquer veículo elétrico ou híbrido vendido no Brasil:",
                    style = MaterialTheme.typography.bodySmall,
                    color = VoltageOnSurfaceVariant
                )

                // Type Selection Chips
                Text(
                    text = "Tipo de Motorização:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = VoltageOnSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedType == VehicleType.PHEV,
                        onClick = { onTypeChanged(VehicleType.PHEV) },
                        label = { Text("Plug-in (PHEV)", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.ElectricBolt, null, Modifier.size(14.dp), tint = VoltagePrimary)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VoltagePrimary.copy(alpha = 0.2f),
                            selectedLabelColor = VoltagePrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedType == VehicleType.BEV,
                        onClick = { onTypeChanged(VehicleType.BEV) },
                        label = { Text("100% EV", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.ElectricBolt, null, Modifier.size(14.dp), tint = VoltageSecondary)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VoltageSecondary.copy(alpha = 0.2f),
                            selectedLabelColor = VoltageSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedType == VehicleType.HEV,
                        onClick = { onTypeChanged(VehicleType.HEV) },
                        label = { Text("Híbrido (HEV)", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.LocalGasStation, null, Modifier.size(14.dp), tint = VoltageTertiary)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VoltageTertiary.copy(alpha = 0.2f),
                            selectedLabelColor = VoltageTertiary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Brand & Model Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Marca (ex: BYD)") },
                        placeholder = { Text("GWM, Omoda...") },
                        singleLine = true,
                        modifier = Modifier.weight(0.45f)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Modelo do Veículo") },
                        placeholder = { Text("Ex: Shark PHEV") },
                        singleLine = true,
                        modifier = Modifier.weight(0.55f).testTag("input_new_vehicle_name")
                    )
                }

                // Predictive Smart Autocomplete Chips
                if (smartSuggestions.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = VoltagePrimary, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Preencher dados com sugestão oficial:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VoltagePrimary
                            )
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(smartSuggestions) { suggestion ->
                                SuggestionChip(
                                    onClick = { applySuggestion(suggestion) },
                                    label = {
                                        Text(
                                            text = "${suggestion.name} (${suggestion.batteryCapacityKwh}kWh)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = VoltagePrimaryContainer.copy(alpha = 0.6f),
                                        labelColor = VoltageOnPrimaryContainer
                                    ),
                                    border = BorderStroke(0.5.dp, VoltagePrimary.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }

                // Specs: Battery & Electric Consumption
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = batteryCapacity,
                        onValueChange = { batteryCapacity = it.replace(',', '.') },
                        label = { Text("Bateria (kWh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_new_battery")
                    )

                    if (selectedType != VehicleType.HEV) {
                        OutlinedTextField(
                            value = electricConsumption,
                            onValueChange = { electricConsumption = it.replace(',', '.') },
                            label = { Text("Consumo (kWh/100km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_new_electric_cons")
                        )
                    }
                }

                // Specs: Gas & Max AC
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedType != VehicleType.BEV) {
                        OutlinedTextField(
                            value = gasolineConsumption,
                            onValueChange = { gasolineConsumption = it.replace(',', '.') },
                            label = { Text("Gasolina (km/L)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_new_gas_cons")
                        )
                    }

                    if (selectedType != VehicleType.HEV) {
                        OutlinedTextField(
                            value = maxAcCharge,
                            onValueChange = { maxAcCharge = it.replace(',', '.') },
                            label = { Text("Máx AC (kW)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.trim()
                    if (finalName.isBlank()) {
                        errorMessage = "Por favor, digite o nome do veículo."
                        return@Button
                    }
                    val bat = batteryCapacity.toDoubleOrNull() ?: 0.0
                    if (bat <= 0.0) {
                        errorMessage = "Capacidade de bateria inválida."
                        return@Button
                    }
                    val elCons = if (selectedType == VehicleType.HEV) 0.0 else (electricConsumption.toDoubleOrNull() ?: 18.0)
                    val gasCons = if (selectedType == VehicleType.BEV) 0.0 else (gasolineConsumption.toDoubleOrNull() ?: 15.0)
                    val acCharge = if (selectedType == VehicleType.HEV) 0.0 else (maxAcCharge.toDoubleOrNull() ?: 7.0)

                    val fullName = if (brand.isNotBlank() && !finalName.startsWith(brand.trim(), ignoreCase = true)) {
                        "${brand.trim()} $finalName"
                    } else {
                        finalName
                    }

                    val newCar = Vehicle(
                        id = "custom_${UUID.randomUUID().toString().take(8)}",
                        name = fullName,
                        brand = brand.trim().ifBlank { "Personalizado" },
                        type = selectedType,
                        tag = selectedType.label,
                        batteryCapacityKwh = bat,
                        electricConsumptionKwh100km = elCons,
                        gasolineConsumptionKmL = gasCons,
                        maxAcChargeKw = acCharge,
                        imageUrl = "",
                        isCustom = true
                    )
                    onAddVehicle(newCar)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer),
                modifier = Modifier.testTag("btn_confirm_add_vehicle")
            ) {
                Text("Cadastrar e Selecionar", color = VoltageOnPrimaryContainer)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = VoltageOnSurfaceVariant)
            }
        },
        containerColor = VoltageSurfaceContainer
    )
}
