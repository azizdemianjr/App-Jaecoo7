package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.data.VehicleSyncResult
import com.example.data.VehicleType
import com.example.ui.theme.*

@Composable
fun VehicleSyncSummaryDialog(
    syncResult: VehicleSyncResult,
    onSelectVehicle: (Vehicle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VoltageSecondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = VoltageSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Sincronização Concluída",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface
                    )
                    Text(
                        text = syncResult.formattedDate,
                        fontSize = 11.sp,
                        color = VoltageOnSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overview metrics card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VoltageSurfaceContainerHighest)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricBadge(
                        count = syncResult.totalCount.toString(),
                        label = "Total Frota",
                        color = VoltageOnSurface
                    )
                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = VoltageOutline.copy(alpha = 0.3f)
                    )
                    MetricBadge(
                        count = "+${syncResult.newModelsCount}",
                        label = "Novos Modelos",
                        color = VoltageSecondary
                    )
                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = VoltageOutline.copy(alpha = 0.3f)
                    )
                    MetricBadge(
                        count = syncResult.updatedModelsCount.toString(),
                        label = "Atualizados",
                        color = VoltagePrimary
                    )
                }

                if (syncResult.newModelsCount > 0) {
                    Text(
                        text = "Novos Veículos Adicionados:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = VoltageSecondary
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(syncResult.newlyDiscoveredVehicles, key = { it.id }) { vehicle ->
                            val tagColor = when (vehicle.type) {
                                VehicleType.BEV -> VoltageSecondary
                                VehicleType.PHEV -> VoltagePrimary
                                VehicleType.HEV -> VoltageTertiary
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VoltageCardBg)
                                    .border(0.5.dp, VoltageOutline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSelectVehicle(vehicle)
                                        onDismiss()
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(tagColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (vehicle.type) {
                                            VehicleType.BEV -> Icons.Default.ElectricCar
                                            else -> Icons.Default.DirectionsCar
                                        },
                                        contentDescription = null,
                                        tint = tagColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = vehicle.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = VoltageOnSurface
                                    )
                                    Text(
                                        text = "${vehicle.batteryCapacityKwh} kWh • ${if (vehicle.electricConsumptionKwh100km > 0) "${vehicle.electricConsumptionKwh100km} kWh/100km" else ""}${if (vehicle.gasolineConsumptionKmL > 0) " • ${vehicle.gasolineConsumptionKmL} km/L" else ""}",
                                        fontSize = 10.sp,
                                        color = VoltageOnSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Selecionar",
                                    tint = VoltageSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "O catálogo está 100% atualizado com as especificações oficiais mais recentes.",
                            fontSize = 12.sp,
                            color = VoltageOnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer),
                modifier = Modifier.testTag("btn_close_sync_summary")
            ) {
                Text("OK, Entendido", color = VoltageOnPrimaryContainer)
            }
        },
        containerColor = VoltageSurfaceContainer
    )
}

@Composable
private fun MetricBadge(
    count: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = VoltageOnSurfaceVariant
        )
    }
}
