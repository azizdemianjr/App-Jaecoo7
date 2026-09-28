package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.data.VehicleCatalog
import com.example.ui.theme.*

@Composable
fun EditParametersDialog(
    vehicle: Vehicle,
    onSaveParameters: (
        name: String,
        batteryCapacity: Double,
        electricConsumption: Double,
        gasolineConsumption: Double,
        maxAcCharge: Double,
        imageUrl: String
    ) -> Unit,
    onResetToFactory: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var name by remember(vehicle) { mutableStateOf(vehicle.name) }
    var batteryCapacity by remember(vehicle) { mutableStateOf(vehicle.batteryCapacityKwh.toString()) }
    var electricConsumption by remember(vehicle) { mutableStateOf(vehicle.electricConsumptionKwh100km.toString()) }
    var gasolineConsumption by remember(vehicle) { mutableStateOf(vehicle.gasolineConsumptionKmL.toString()) }
    var maxAcCharge by remember(vehicle) { mutableStateOf(vehicle.maxAcChargeKw.toString()) }

    val isDefaultModel = remember(vehicle.id) {
        VehicleCatalog.defaultVehicles.any { it.id == vehicle.id }
    }

    AlertDialog(
        onDismissRequest = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = VoltagePrimary
                )
                Text(
                    text = "Ajustar Parâmetros",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Veículo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = batteryCapacity,
                    onValueChange = { batteryCapacity = it.replace(',', '.') },
                    label = { Text("Capacidade da Bateria Útil (kWh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_battery_kwh")
                )

                OutlinedTextField(
                    value = electricConsumption,
                    onValueChange = { electricConsumption = it.replace(',', '.') },
                    label = { Text("Consumo Elétrico (kWh/100km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_electric_cons")
                )

                OutlinedTextField(
                    value = gasolineConsumption,
                    onValueChange = { gasolineConsumption = it.replace(',', '.') },
                    label = { Text("Consumo Gasolina (km/L) [0 se 100% EV]") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_gas_cons")
                )

                OutlinedTextField(
                    value = maxAcCharge,
                    onValueChange = { maxAcCharge = it.replace(',', '.') },
                    label = { Text("Potência Máx AC Wallbox (kW)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDefaultModel && onResetToFactory != null) {
                    TextButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onResetToFactory()
                        },
                        modifier = Modifier.testTag("btn_reset_vehicle_factory")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = VoltageSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fábrica", color = VoltageSecondary, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        val parsedBattery = batteryCapacity.trim().replace(',', '.').toDoubleOrNull() ?: vehicle.batteryCapacityKwh
                        val parsedElec = electricConsumption.trim().replace(',', '.').toDoubleOrNull() ?: vehicle.electricConsumptionKwh100km
                        val parsedGas = gasolineConsumption.trim().replace(',', '.').toDoubleOrNull() ?: vehicle.gasolineConsumptionKmL
                        val parsedAc = maxAcCharge.trim().replace(',', '.').toDoubleOrNull() ?: vehicle.maxAcChargeKw
                        onSaveParameters(
                            name.trim(),
                            parsedBattery,
                            parsedElec,
                            parsedGas,
                            parsedAc,
                            vehicle.imageUrl
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer),
                    modifier = Modifier.testTag("btn_save_parameters")
                ) {
                    Text("Salvar", color = VoltageOnPrimaryContainer)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                onDismiss()
            }) {
                Text("Cancelar", color = VoltageOnSurfaceVariant)
            }
        },
        containerColor = VoltageSurfaceContainer
    )
}
