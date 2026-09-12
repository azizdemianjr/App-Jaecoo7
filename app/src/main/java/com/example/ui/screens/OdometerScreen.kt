package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChargingLocation
import com.example.data.OdometerEntry
import com.example.data.VehicleType
import com.example.ui.AppUiState
import com.example.ui.MainViewModel
import com.example.ui.formatCurrency
import com.example.ui.formatNumber
import com.example.ui.localization.AppDictionary
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

// Styling palette inspired by Jaecoo 7 Consumo screen
private val BgDarkCard = Color(0xFF0C1322)
private val BgCardBorder = Color(0xFF1E293B)
private val CyanText = Color(0xFF38BDF8)
private val AmberText = Color(0xFFF59E0B)
private val AmberIconBg = Color(0xFF451A03)
private val PurpleEvText = Color(0xFFC084FC)
private val PurpleBadgeBg = Color(0xFF3B0764)
private val PurpleBadgeText = Color(0xFFE9D5FF)
private val PurpleEvBar = Color(0xFFA855F7)
private val OrangeHevBar = Color(0xFFF97316)
private val GreenResultBorder = Color(0xFF059669)
private val GreenResultBadgeBg = Color(0xFF064E3B)
private val GreenResultBadgeText = Color(0xFF34D399)
private val AmberResultCardBg = Color(0xFF1A1208)
private val AmberResultCardBorder = Color(0xFFB45309)
private val GreenGlobalText = Color(0xFF10B981)
private val SaveButtonBg = Color(0xFF22C55E)

@Composable
fun OdometerScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val strings = state.strings
    val language = state.language
    val context = LocalContext.current

    // Local inputs for text fields to allow smooth typing
    var totalStartText by remember(state.odometerTotalStartKm) {
        mutableStateOf(
            if (state.odometerTotalStartKm > 0) {
                if (state.odometerTotalStartKm % 1.0 == 0.0) state.odometerTotalStartKm.toInt().toString()
                else state.odometerTotalStartKm.toString()
            } else ""
        )
    }

    var totalEndText by remember(state.odometerTotalEndKm) {
        mutableStateOf(
            if (state.odometerTotalEndKm > 0) {
                if (state.odometerTotalEndKm % 1.0 == 0.0) state.odometerTotalEndKm.toInt().toString()
                else state.odometerTotalEndKm.toString()
            } else ""
        )
    }

    var hevStartText by remember(state.odometerHevStartKm) {
        mutableStateOf(
            if (state.odometerHevStartKm > 0) {
                if (state.odometerHevStartKm % 1.0 == 0.0) state.odometerHevStartKm.toInt().toString()
                else state.odometerHevStartKm.toString()
            } else ""
        )
    }

    var hevEndText by remember(state.odometerHevEndKm) {
        mutableStateOf(
            if (state.odometerHevEndKm > 0) {
                if (state.odometerHevEndKm % 1.0 == 0.0) state.odometerHevEndKm.toInt().toString()
                else state.odometerHevEndKm.toString()
            } else ""
        )
    }

    var fuelLitersText by remember(state.odometerFuelLiters) {
        mutableStateOf(
            if (state.odometerFuelLiters > 0) {
                if (state.odometerFuelLiters % 1.0 == 0.0) state.odometerFuelLiters.toInt().toString()
                else state.odometerFuelLiters.toString()
            } else ""
        )
    }

    var noteText by remember(state.odometerTripNote) {
        mutableStateOf(state.odometerTripNote)
    }

    var historyPageIndex by remember { mutableIntStateOf(0) }
    var editingEntry by remember { mutableStateOf<OdometerEntry?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showRemoveInitialConfirmDialog by remember { mutableStateOf(false) }

    // Apresentar automaticamente a janela para cadastro da quilometragem inicial quando não existir histórico
    val hasNoHistory = state.odometerEntries.isEmpty()
    val hasInitialBaseline = state.odometerTotalStartKm > 0.0 || state.odometerHevStartKm > 0.0
    var hasAutoOpenedInitialDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(hasNoHistory, hasInitialBaseline) {
        if (hasNoHistory && !hasInitialBaseline && !hasAutoOpenedInitialDialog) {
            hasAutoOpenedInitialDialog = true
            viewModel.openInitialOdometerDialog(true)
        }
    }

    fun shareOdometerEntry(entry: OdometerEntry) {
        val report = buildOdometerEntryShareReport(entry, language, strings)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, report)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, strings.odometerShareChooser)
        context.startActivity(shareIntent)
    }

    // Calculations based on current state
    val totalStart = state.odometerTotalStartKm
    val totalEnd = state.odometerTotalEndKm
    val hevStart = state.odometerHevStartKm
    val hevEnd = state.odometerHevEndKm
    val fuelLiters = state.odometerFuelLiters

    val evStart = state.odometerEvStartKm
    val evEnd = state.odometerEvEndKm

    val deltaTotal = state.odometerDeltaTotalKm
    val deltaHev = state.odometerDeltaHevKm
    val deltaEv = state.odometerDeltaEvKm

    val evPercent = state.odometerEvPercent
    val hevPercent = state.odometerHevPercent

    val averageHevKmL = state.odometerAverageHevKmL
    val averageHevL100km = state.odometerAverageHevL100km
    val globalKmL = state.odometerGlobalKmL

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Warning / Switcher if selected vehicle is NOT PHEV
        if (state.selectedVehicle.type != VehicleType.PHEV) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("non_phev_warning_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                border = BorderStroke(1.dp, BgCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricCar,
                        contentDescription = null,
                        tint = CyanText,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = strings.odometerExclusiveWarning,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = VoltageOnSurface,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                    Button(
                        onClick = { viewModel.openChangeVehicleDialog(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltagePrimary,
                            contentColor = VoltageOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_switch_to_phev")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.odometerSelectPhevBtn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            return
        }

        // 1. TOP HEADER (Vehicle Name, Odometer & Real Consumption + History and Share Actions)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                        .clickable { viewModel.openChangeVehicleDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = strings.navOdometer,
                        tint = CyanText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = state.selectedVehicle.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = VoltageOnSurface,
                            fontSize = 17.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = strings.odometerHeaderTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = VoltageOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // History Button
                IconButton(
                    onClick = { viewModel.openOdometerHistoryDialog(true) },
                    modifier = Modifier.testTag("odometer_btn_history")
                ) {
                    BadgedBox(
                        badge = {
                            if (state.odometerEntries.isNotEmpty()) {
                                Badge(
                                    containerColor = AmberText,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = state.odometerEntries.size.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = strings.odometerTripHistoryTooltip,
                            tint = VoltageOnSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Share Button
                IconButton(
                    onClick = {
                        val shareText = buildOdometerShareReport(state, strings)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, strings.odometerShareChooser)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.testTag("odometer_btn_share_header")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = strings.odometerShareItemTooltip,
                        tint = CyanText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. CARD APRESENTAÇÃO (Consumo Médio por Odômetro)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("odometer_intro_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AmberIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = AmberText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = strings.odometerIntroTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = VoltageOnSurface,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = strings.odometerIntroSubtitle.format(state.selectedVehicle.name),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = VoltageOnSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Text(
                    text = strings.odometerIntroDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = VoltageOnSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )
            }
        }

        // 3. CARD 1: Odômetros do Veículo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("odometer_inputs_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.odometerStep1Title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyanText,
                        fontSize = 14.sp
                    )
                )

                // Subseção Total
                Text(
                    text = strings.odometerTotalHeader,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = totalStartText,
                        onValueChange = { str ->
                            val clean = str.replace(',', '.')
                            totalStartText = clean
                            viewModel.updateOdometerTotalStartKm(clean.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text(strings.odometerTotalStart, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_start_km"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanText,
                            unfocusedBorderColor = BgCardBorder,
                            focusedLabelColor = CyanText
                        )
                    )

                    OutlinedTextField(
                        value = totalEndText,
                        onValueChange = { str ->
                            val clean = str.replace(',', '.')
                            totalEndText = clean
                            viewModel.updateOdometerTotalEndKm(clean.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text(strings.odometerTotalEnd, fontSize = 11.sp) },
                        placeholder = { Text("—", fontSize = 12.sp, color = VoltageOnSurfaceVariant.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_end_km"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanText,
                            unfocusedBorderColor = BgCardBorder,
                            focusedLabelColor = CyanText
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Subseção HEV
                Text(
                    text = strings.odometerHevHeader,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AmberText,
                        fontSize = 11.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = hevStartText,
                        onValueChange = { str ->
                            val clean = str.replace(',', '.')
                            hevStartText = clean
                            viewModel.updateOdometerHevStartKm(clean.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text(strings.odometerHevStart, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_hev_start_km"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberText,
                            unfocusedBorderColor = BgCardBorder,
                            focusedLabelColor = AmberText
                        )
                    )

                    OutlinedTextField(
                        value = hevEndText,
                        onValueChange = { str ->
                            val clean = str.replace(',', '.')
                            hevEndText = clean
                            viewModel.updateOdometerHevEndKm(clean.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text(strings.odometerHevEnd, fontSize = 11.sp) },
                        placeholder = { Text("—", fontSize = 12.sp, color = VoltageOnSurfaceVariant.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_hev_end_km"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberText,
                            unfocusedBorderColor = BgCardBorder,
                            focusedLabelColor = AmberText
                        )
                    )
                }
            }
        }

        // 4. CARD ODÔMETROS EV (Calculados pelo App)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("odometer_ev_calculated_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1227)),
            border = BorderStroke(1.dp, Color(0xFF3B1C54))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = PurpleEvText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.odometerEvCardTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PurpleEvText,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PurpleBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = strings.odometerEvFormulaBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PurpleBadgeText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // EV Anterior
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF070A14)),
                        border = BorderStroke(1.dp, Color(0xFF261D3B))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = strings.odometerEvStart,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = if (totalStart > 0) "${formatNumber(evStart, 1)} km" else "—",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageOnSurface,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = if (totalStart > 0) "${totalStart.toInt()} - ${hevStart.toInt()}" else "—",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // EV Atual
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF070A14)),
                        border = BorderStroke(1.dp, Color(0xFF261D3B))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = strings.odometerEvEnd,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = if (totalEnd > 0) "${formatNumber(evEnd, 1)} km" else "—",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageOnSurface,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = if (totalEnd > 0) "${totalEnd.toInt()} - ${hevEnd.toInt()}" else "—",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 5. CARD 2: Combustível Utilizado no Período
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("odometer_fuel_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.odometerStep2Title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AmberText,
                        fontSize = 14.sp
                    )
                )

                // Input de Combustível (Litros)
                OutlinedTextField(
                    value = fuelLitersText,
                    onValueChange = { str ->
                        val clean = str.replace(',', '.')
                        fuelLitersText = clean
                        clean.toDoubleOrNull()?.let { viewModel.updateOdometerFuelLiters(it) }
                    },
                    label = { Text(strings.odometerFuelLitersLabel, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = AmberText
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = strings.odometerFuelLitersUnit,
                            color = VoltageOnSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fuel_liters"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberText,
                        unfocusedBorderColor = BgCardBorder,
                        focusedLabelColor = AmberText
                    )
                )

                // Input Observação / Rota (Opcional)
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { str ->
                        noteText = str
                        viewModel.updateOdometerTripNote(str)
                    },
                    label = { Text(strings.odometerTripNoteLabel, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_trip_note"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanText,
                        unfocusedBorderColor = BgCardBorder,
                        focusedLabelColor = CyanText
                    )
                )
            }
        }

        // 6. CARD RESULTADO DA MEDIÇÃO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("odometer_result_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.5.dp, GreenResultBorder.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cabeçalho do Resultado: Delta Total em uma linha abaixo de Resultado da Medição
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.odometerResultTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = VoltageOnSurface,
                            fontSize = 16.sp
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GreenResultBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${strings.odometerDeltaTotalPrefix} = ${formatNumber(deltaTotal, 1)} km",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GreenResultBadgeText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Card Central de Destaque Âmbar/Combustão: CONSUMO MÉDIO HEV
                val globalL100km = if (deltaTotal > 0 && fuelLiters > 0) (fuelLiters / deltaTotal) * 100.0 else 0.0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261505)),
                    border = BorderStroke(1.2.dp, Color(0xFFD97706))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.odometerHevAvgConsumptionHeader,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24),
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (averageHevL100km > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF451A03))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${formatNumber(averageHevL100km, 1)} L/100km",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFDE68A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Text(
                            text = strings.odometerCombustionModeFormula,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                        )

                        Text(
                            text = if (averageHevKmL > 0) "${formatNumber(averageHevKmL, 2)} km/L" else "-- km/L",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFBBF24),
                                fontSize = 34.sp
                            )
                        )
                    }
                }

                // Grid de 2 cards: Consumo Global & Total Rodado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Consumo Global
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF070A14)),
                        border = BorderStroke(1.dp, BgCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = strings.odometerGlobalConsumption,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = if (globalKmL > 0) "${formatNumber(globalKmL, 2)} km/L" else "-- km/L",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399),
                                    fontSize = 19.sp
                                )
                            )
                            Text(
                                text = if (globalL100km > 0) "${formatNumber(globalL100km, 1)} L/100km" else strings.odometerDiffTotalOverLiters,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Total Rodado
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF070A14)),
                        border = BorderStroke(1.dp, BgCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = strings.odometerTotalDriven,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "${formatNumber(deltaTotal, 1)} km",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageOnSurface,
                                    fontSize = 19.sp
                                )
                            )
                            Text(
                                text = strings.odometerCurrentMinusPrevious,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Detalhamento de Propulsão (Modo EV vs HEV)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFFACC15),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.odometerEvModeDetail,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Text(
                            text = "${formatNumber(deltaEv, 1)} km (${formatNumber(evPercent, 0)}%)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = VoltageOnSurface,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = OrangeHevBar,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.odometerHevModeDetail,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Text(
                            text = "${formatNumber(deltaHev, 1)} km (${formatNumber(hevPercent, 0)}%)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = VoltageOnSurface,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // Barra segmentada bicolor
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            val evWeight = (evPercent / 100.0).toFloat().coerceIn(0.01f, 0.99f)
                            val hevWeight = (hevPercent / 100.0).toFloat().coerceIn(0.01f, 0.99f)

                            Box(
                                modifier = Modifier
                                    .weight(if (deltaTotal > 0) evWeight else 0.5f)
                                    .fillMaxHeight()
                                    .background(PurpleEvBar)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(if (deltaTotal > 0) hevWeight else 0.5f)
                                    .fillMaxHeight()
                                    .background(OrangeHevBar)
                            )
                        }
                    }
                }

                // SELETOR: Onde recarregou para esta viagem (Casa vs Posto)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.odometerWhereRechargedQuestion,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = VoltageOnSurface,
                            fontSize = 12.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Opção: Em Casa
                        val isHome = state.odometerUseHomeTariff
                        Surface(
                            onClick = { viewModel.setOdometerUseHomeTariff(true) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_charge_home"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isHome) Color(0xFF1E1B4B) else Color(0xFF070A14),
                            border = BorderStroke(1.2.dp, if (isHome) Color(0xFF818CF8) else BgCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = if (isHome) Color(0xFFA5B4FC) else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = strings.odometerAtHomeOption,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isHome) Color.White else Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "R$ ${formatNumber(state.homeEnergyPrice, 2)}/kWh",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isHome) Color(0xFFC7D2FE) else Color(0xFF64748B),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        // Opção: No Posto de Recarga
                        val isStation = !state.odometerUseHomeTariff
                        Surface(
                            onClick = { viewModel.setOdometerUseHomeTariff(false) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_charge_station"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isStation) Color(0xFF082F49) else Color(0xFF070A14),
                            border = BorderStroke(1.2.dp, if (isStation) Color(0xFF38BDF8) else BgCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EvStation,
                                        contentDescription = null,
                                        tint = if (isStation) Color(0xFF38BDF8) else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = strings.odometerAtStationOption,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isStation) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isStation) Color.White else Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "R$ ${formatNumber(state.publicEnergyPrice, 2)}/kWh",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isStation) Color(0xFFBAE6FD) else Color(0xFF64748B),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                // CUSTOS DA VIAGEM (Combustível + Elétrico)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.odometerTripCostTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VoltageOnSurface,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }

                        // Custo Total em destaque
                        Text(
                            text = "R$ ${formatNumber(state.odometerTotalTripCost, 2)}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF34D399),
                                fontSize = 26.sp
                            )
                        )

                        // Detalhamento: Combustível e Elétrico
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Bloco Combustível
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1606)),
                                border = BorderStroke(1.dp, Color(0xFF78350F))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = strings.odometerFuelLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFFDE68A),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    Text(
                                        text = "R$ ${formatNumber(state.odometerGasolineCost, 2)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFBBF24),
                                            fontSize = 15.sp
                                        )
                                    )
                                    Text(
                                        text = "${formatNumber(state.odometerTotalGasolineLiters, 2)} L (${if (state.gasolinePrice > 0) "R$ ${formatNumber(state.gasolinePrice, 2)}/L" else ""})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 9.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Bloco Elétrico
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF130E26)),
                                border = BorderStroke(1.dp, Color(0xFF4C1D95))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFFA78BFA),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = strings.odometerElectricLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFDDD6FE),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "R$ ${formatNumber(state.odometerElectricCost, 2)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC084FC),
                                            fontSize = 15.sp
                                        )
                                    )
                                    Text(
                                        text = "${formatNumber(state.odometerTotalEnergyKwh, 1)} kWh (R$ ${formatNumber(state.odometerEffectiveEnergyPrice, 2)}/kWh)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 9.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Botão "Salvar no Histórico"
                Button(
                    onClick = {
                        viewModel.saveCurrentOdometerTrip(noteText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_odometer_trip"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaveButtonBg,
                        contentColor = Color(0xFF022C22)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF022C22)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.odometerSaveToHistoryBtn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF022C22)
                    )
                }
            }
        }

        // 7. SEÇÃO HISTÓRICO DE MEDIÇÕES (Layout idêntico à imagem de referência)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("odometer_recent_history_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header da seção: Ícone velocímetro + Título com contador + Botão Limpar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.odometerHistorySectionTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageOnSurface,
                                    fontSize = 12.5.sp,
                                    letterSpacing = 0.3.sp
                                )
                            )
                            Text(
                                text = "(${state.odometerEntries.size})",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    if (state.odometerEntries.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = strings.odometerClearHistoryBtn,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                if (state.odometerEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.odometerNoEntriesText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = VoltageOnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        )
                    }
                } else {
                    val pageSize = 3
                    val totalPages = ((state.odometerEntries.size + pageSize - 1) / pageSize).coerceAtLeast(1)
                    val safePage = historyPageIndex.coerceIn(0, totalPages - 1)
                    val pagedEntries = state.odometerEntries.drop(safePage * pageSize).take(pageSize)

                    // Barra de Paginação Pílula Arredondada (Apenas Recentes e Antigos)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF071120)),
                        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botão ← Recentes
                            val canGoBack = safePage > 0
                            Surface(
                                onClick = { if (canGoBack) historyPageIndex = safePage - 1 },
                                enabled = canGoBack,
                                shape = RoundedCornerShape(100.dp),
                                color = if (canGoBack) Color(0xFF0C243B) else Color(0xFF071220),
                                border = BorderStroke(1.dp, if (canGoBack) Color(0xFF0284C7) else Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = strings.odometerRecentPageBtn,
                                        tint = if (canGoBack) Color(0xFF38BDF8) else Color(0xFF475569),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.odometerRecentPageBtn,
                                        color = if (canGoBack) Color(0xFF38BDF8) else Color(0xFF475569),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (canGoBack) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            // Botão Antigos →
                            val canGoForward = safePage < totalPages - 1
                            Surface(
                                onClick = { if (canGoForward) historyPageIndex = safePage + 1 },
                                enabled = canGoForward,
                                shape = RoundedCornerShape(100.dp),
                                color = if (canGoForward) Color(0xFF0C243B) else Color(0xFF071220),
                                border = BorderStroke(1.dp, if (canGoForward) Color(0xFF0284C7) else Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.odometerOlderPageBtn,
                                        color = if (canGoForward) Color(0xFF38BDF8) else Color(0xFF475569),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (canGoForward) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = strings.odometerOlderPageBtn,
                                        tint = if (canGoForward) Color(0xFF38BDF8) else Color(0xFF475569),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Lista dos cards com até 3 registros da página atual
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pagedEntries.forEach { entry ->
                            OdometerHistoryCardItem(
                                entry = entry,
                                language = language,
                                strings = strings,
                                onEdit = { editingEntry = entry },
                                onShare = { shareOdometerEntry(entry) },
                                onDelete = { viewModel.deleteOdometerEntry(entry.id) },
                                onApplyAsPrevious = { viewModel.applyOdometerEntryAsPrevious(entry) }
                            )
                        }
                    }
                }
            }
        }

        // 8. CARD DE QUILOMETRAGEM INICIAL (Ponto de Partida) NO FINAL DA JANELA
        val hasBaseline = state.odometerTotalStartKm > 0.0 || state.odometerHevStartKm > 0.0
        val allowEditOrRemove = state.odometerEntries.isEmpty()

        if (hasBaseline) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("initial_odometer_registered_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, if (allowEditOrRemove) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (allowEditOrRemove) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (allowEditOrRemove) Icons.Default.Flag else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (allowEditOrRemove) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = strings.odometerInitialMileageTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VoltageOnSurface,
                                        fontSize = 13.5.sp
                                    )
                                )
                                Text(
                                    text = if (allowEditOrRemove) strings.odometerInitialBaselineBadge else strings.odometerInitialLockedBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (allowEditOrRemove) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 9.5.sp
                                    )
                                )
                            }
                        }
                    }

                    // Linha 1: Total Anterior Centralizado
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = strings.odometerTotalStart,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.5.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${formatNumber(state.odometerTotalStartKm, 1, language)} km",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyanText,
                                    fontSize = 18.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Linha 2: HEV Anterior e EV Anterior Centralizados
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // HEV Anterior Centralizado
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = strings.odometerHevStart,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = VoltageOnSurfaceVariant,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${formatNumber(state.odometerHevStartKm, 1, language)} km",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AmberText,
                                        fontSize = 15.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // EV Anterior Centralizado
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = strings.odometerEvStart,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = VoltageOnSurfaceVariant,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${formatNumber(state.odometerEvStartKm, 1, language)} km",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleEvText,
                                        fontSize = 15.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Botões Editar e Remover: Permitidos se não existir nenhum histórico
                    if (allowEditOrRemove) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openInitialOdometerDialog(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_edit_initial_odometer"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF38BDF8)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.odometerInitialEditBtn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { showRemoveInitialConfirmDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_remove_initial_odometer"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF4444)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.odometerInitialRemoveBtn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    } else {
                        // Mensagem de bloqueio quando já existem viagens salvas no histórico
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.odometerInitialLockedDesc,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        } else if (allowEditOrRemove) {
            // Card convidativo para cadastrar o ponto de partida (quando não há baseline e não há histórico)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("initial_odometer_prompt_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddRoad,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = strings.odometerInitialNotRegisteredBanner,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = VoltageOnSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = { viewModel.openInitialOdometerDialog(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8),
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_register_initial_odometer")
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.odometerInitialRegisterBtn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // DIÁLOGO DE CONFIRMAÇÃO PARA LIMPAR TODO O HISTÓRICO
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(strings.odometerClearHistoryDialogTitle, fontWeight = FontWeight.Bold, color = VoltageOnSurface)
            },
            text = {
                Text(
                    strings.odometerClearHistoryDialogMsg,
                    color = VoltageOnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllOdometerEntries()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.odometerClearAllConfirmBtn, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(strings.odometerCancelBtn, color = VoltageOnSurfaceVariant)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // DIALOG DE EDIÇÃO DE HISTÓRICO
    editingEntry?.let { entryToEdit ->
        EditOdometerEntryDialog(
            entry = entryToEdit,
            homeEnergyPrice = state.homeEnergyPrice,
            publicEnergyPrice = state.publicEnergyPrice,
            language = language,
            strings = strings,
            onDismiss = { editingEntry = null },
            onSave = { updatedTitle, totStart, totEnd, hStart, hEnd, liters, location ->
                viewModel.updateOdometerEntry(
                    id = entryToEdit.id,
                    title = updatedTitle,
                    totalStartKm = totStart,
                    totalEndKm = totEnd,
                    hevStartKm = hStart,
                    hevEndKm = hEnd,
                    fuelLiters = liters,
                    chargingLocation = location
                )
                editingEntry = null
            }
        )
    }

    // DIALOG DO HISTÓRICO COMPLETO DE MEDIÇÕES (aberto pelo botão no topo)
    if (state.isOdometerHistoryDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.openOdometerHistoryDialog(false) },
            confirmButton = {
                TextButton(onClick = { viewModel.openOdometerHistoryDialog(false) }) {
                    Text(strings.odometerCloseDialogBtn, color = VoltagePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (state.odometerEntries.isNotEmpty()) {
                    TextButton(onClick = {
                        viewModel.clearOdometerHistory()
                    }) {
                        Text(strings.odometerClearHistoryBtn, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = CyanText)
                    Text(strings.odometerHistorySectionTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                if (state.odometerEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.odometerNoEntriesText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = VoltageOnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.odometerEntries.forEach { entry ->
                            OdometerHistoryCardItem(
                                entry = entry,
                                language = language,
                                strings = strings,
                                onEdit = {
                                    viewModel.openOdometerHistoryDialog(false)
                                    editingEntry = entry
                                },
                                onShare = { shareOdometerEntry(entry) },
                                onDelete = { viewModel.deleteOdometerEntry(entry.id) },
                                onApplyAsPrevious = {
                                    viewModel.applyOdometerEntryAsPrevious(entry)
                                    viewModel.openOdometerHistoryDialog(false)
                                }
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color(0xFF0F172A)
        )
    }

    // DIALOG DE CADASTRO / EDIÇÃO DA QUILOMETRAGEM INICIAL
    if (state.isInitialOdometerDialogOpen) {
        InitialOdometerDialog(
            initialTotalKm = state.odometerTotalStartKm,
            initialHevKm = state.odometerHevStartKm,
            strings = strings,
            language = language,
            onDismiss = { viewModel.openInitialOdometerDialog(false) },
            onSave = { total, hev ->
                viewModel.saveInitialOdometer(total, hev)
            }
        )
    }

    // DIÁLOGO DE CONFIRMAÇÃO DE REMOÇÃO DA QUILOMETRAGEM INICIAL
    if (showRemoveInitialConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveInitialConfirmDialog = false },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = strings.odometerInitialRemoveConfirmTitle,
                    fontWeight = FontWeight.Bold,
                    color = VoltageOnSurface
                )
            },
            text = {
                Text(
                    text = strings.odometerInitialRemoveConfirmDesc,
                    color = VoltageOnSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeInitialOdometer()
                        showRemoveInitialConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_remove_initial_odometer")
                ) {
                    Text(
                        text = strings.odometerInitialRemoveBtn,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveInitialConfirmDialog = false }) {
                    Text(strings.odometerCancelBtn, color = VoltageOnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun InitialOdometerDialog(
    initialTotalKm: Double,
    initialHevKm: Double,
    strings: AppDictionary,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (total: Double, hev: Double) -> Unit
) {
    var totalText by remember {
        mutableStateOf(
            if (initialTotalKm > 0) {
                if (initialTotalKm % 1.0 == 0.0) initialTotalKm.toInt().toString()
                else initialTotalKm.toString()
            } else ""
        )
    }
    var hevText by remember {
        mutableStateOf(
            if (initialHevKm > 0) {
                if (initialHevKm % 1.0 == 0.0) initialHevKm.toInt().toString()
                else initialHevKm.toString()
            } else ""
        )
    }

    val totalVal = totalText.toDoubleOrNull() ?: 0.0
    val hevVal = hevText.toDoubleOrNull() ?: 0.0
    val isHevInvalid = hevVal > totalVal
    val evCalculated = (totalVal - hevVal).coerceAtLeast(0.0)
    val isSaveEnabled = totalVal > 0 && !isHevInvalid

    val isEditing = initialTotalKm > 0 || initialHevKm > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (isEditing) strings.odometerInitialDialogEditTitle else strings.odometerInitialDialogTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface,
                        fontSize = 16.sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.odometerInitialDialogDesc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = VoltageOnSurfaceVariant,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                )

                // Campo Total
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { totalText = it.replace(',', '.') },
                    label = { Text(strings.odometerInitialTotalLabel, fontSize = 12.sp) },
                    placeholder = { Text("ex: 15420", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_initial_total"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanText,
                        unfocusedBorderColor = BgCardBorder,
                        focusedLabelColor = CyanText
                    )
                )

                // Campo HEV
                OutlinedTextField(
                    value = hevText,
                    onValueChange = { hevText = it.replace(',', '.') },
                    label = { Text(strings.odometerInitialHevLabel, fontSize = 12.sp) },
                    placeholder = { Text("ex: 9210", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = isHevInvalid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_initial_hev"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberText,
                        unfocusedBorderColor = BgCardBorder,
                        focusedLabelColor = AmberText
                    )
                )

                if (isHevInvalid) {
                    Text(
                        text = strings.odometerInitialErrorHevGreater,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                // Card EV Calculado em tempo real
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15102A)),
                    border = BorderStroke(1.dp, Color(0xFF581C87).copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = PurpleEvText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.odometerInitialEvCalcLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleEvText,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Text(
                            text = "${formatNumber(evCalculated, 1, language)} km",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = VoltageOnSurface,
                                fontSize = 18.sp
                            )
                        )

                        if (totalVal > 0 && !isHevInvalid) {
                            val evPct = (evCalculated / totalVal * 100.0).toInt().coerceIn(0, 100)
                            val hevPct = (hevVal / totalVal * 100.0).toInt().coerceIn(0, 100)
                            Text(
                                text = if (language == AppLanguage.EN_US) "Initial share: $evPct% EV • $hevPct% HEV" else "Proporção Inicial: $evPct% EV • $hevPct% HEV",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(totalVal, hevVal) },
                enabled = isSaveEnabled,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8),
                    contentColor = Color(0xFF0F172A),
                    disabledContainerColor = Color(0xFF334155),
                    disabledContentColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("btn_save_initial_odometer_dialog")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.odometerInitialSaveBtn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_initial_odometer_dialog")
            ) {
                Text(
                    text = strings.odometerCancelBtn,
                    color = VoltageOnSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    )
}

@Composable
private fun OdometerHistoryCardItem(
    entry: OdometerEntry,
    language: AppLanguage,
    strings: AppDictionary,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onApplyAsPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF08101E)),
        border = BorderStroke(1.dp, Color(0xFF1B3252))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Data e hora no topo: ex: 11/09/2026 • 07:49
            Text(
                text = entry.formattedDate(language),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            )

            // Linha do Título e Ações (Editar, Compartilhar, Excluir)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title.ifBlank { entry.vehicleName },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Botão Editar (Verde claro)
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = strings.odometerEditEntryBtn,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Botão Compartilhar (Azul celeste)
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = strings.odometerShareEntryBtn,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Botão Excluir (Cinza azulado)
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = strings.odometerDeleteEntryBtn,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Box Destaque CONSUMO MÉDIO HEV (Fundo escuro âmbar + Borda âmbar)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261505)),
                border = BorderStroke(1.dp, Color(0xFFD97706))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = strings.odometerHevAvgConsumptionHeader,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatNumber(entry.hevKm, 1)} ${strings.odometerCombustionModeDetail}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFDE68A),
                                fontSize = 10.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (entry.realHevKmL > 0) "${formatNumber(entry.realHevKmL, 2)} km/L" else "-- km/L",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            fontSize = 18.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Odômetros e Consumo Alinhados
            val evStartKm = (entry.totalStartKm - entry.hevStartKm).coerceAtLeast(0.0)
            val evEndKm = (entry.totalEndKm - entry.hevEndKm).coerceAtLeast(0.0)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.odometerHistoryTotal,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${entry.totalStartKm.toInt()} ➔ ${entry.totalEndKm.toInt()} km (+${formatNumber(entry.totalKm, 1)} km)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // HEV: Exibe apenas os km rodados conforme solicitado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.odometerHistoryHev,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFF59E0B),
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${formatNumber(entry.hevKm, 1)} km",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // EV (Calc): Exibe apenas os km rodados conforme solicitado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.odometerHistoryEvCalc,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFA78BFA),
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${formatNumber(entry.evKm, 1)} km",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFC084FC),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Combustível
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.odometerHistoryFuel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${formatNumber(entry.fuelLiters, 2)} L  •  ${strings.odometerHistoryGlobalPrefix} ${if (entry.realGlobalKmL > 0) "${formatNumber(entry.realGlobalKmL, 2)} km/L" else "--"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            // Custo da Viagem e Local de Recarga
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF091424)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = strings.odometerHistoryTripCost,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Text(
                            text = "R$ ${formatNumber(entry.totalCost, 2)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    // Linha Combustível
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.odometerHistoryFuel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFDE68A),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "R$ ${formatNumber(entry.gasolineCost, 2)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Linha Elétrico
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.odometerElectricLabel}:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFDDD6FE),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "R$ ${formatNumber(entry.electricCost, 2)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFC084FC),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Barra Bicolor EV e HEV
            val evWeight = if (entry.totalKm > 0) (entry.evKm / entry.totalKm).toFloat().coerceIn(0.01f, 0.99f) else 0.5f
            val hevWeight = (1f - evWeight).coerceIn(0.01f, 0.99f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(evWeight)
                        .fillMaxHeight()
                        .background(Color(0xFFA855F7))
                )
                Box(
                    modifier = Modifier
                        .weight(hevWeight)
                        .fillMaxHeight()
                        .background(Color(0xFFF97316))
                )
            }

            // Botão "↑ Usar Atual deste Registro como Anterior"
            Surface(
                onClick = onApplyAsPrevious,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF07192C),
                border = BorderStroke(1.dp, Color(0xFF0369A1))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.odometerUseAsPreviousBtn,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EditOdometerEntryDialog(
    entry: OdometerEntry,
    homeEnergyPrice: Double,
    publicEnergyPrice: Double,
    language: AppLanguage = AppLanguage.PT_BR,
    strings: AppDictionary,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        totalStart: Double,
        totalEnd: Double,
        hevStart: Double,
        hevEnd: Double,
        fuelLiters: Double,
        chargingLocation: ChargingLocation
    ) -> Unit
) {
    var titleText by remember { mutableStateOf(entry.title) }
    var totalStartText by remember {
        mutableStateOf(if (entry.totalStartKm % 1.0 == 0.0) entry.totalStartKm.toInt().toString() else entry.totalStartKm.toString())
    }
    var totalEndText by remember {
        mutableStateOf(if (entry.totalEndKm % 1.0 == 0.0) entry.totalEndKm.toInt().toString() else entry.totalEndKm.toString())
    }
    var hevStartText by remember {
        mutableStateOf(if (entry.hevStartKm % 1.0 == 0.0) entry.hevStartKm.toInt().toString() else entry.hevStartKm.toString())
    }
    var hevEndText by remember {
        mutableStateOf(if (entry.hevEndKm % 1.0 == 0.0) entry.hevEndKm.toInt().toString() else entry.hevEndKm.toString())
    }
    var fuelLitersText by remember {
        mutableStateOf(if (entry.fuelLiters % 1.0 == 0.0) entry.fuelLiters.toInt().toString() else entry.fuelLiters.toString())
    }
    var selectedLocation by remember { mutableStateOf(entry.chargingLocation) }

    val curTotalStart = totalStartText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curTotalEnd = totalEndText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curHevStart = hevStartText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curHevEnd = hevEndText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curFuelLiters = fuelLitersText.replace(',', '.').toDoubleOrNull() ?: 0.0

    val calcDeltaTotal = (curTotalEnd - curTotalStart).coerceAtLeast(0.0)
    val calcDeltaHev = (curHevEnd - curHevStart).coerceAtLeast(0.0)
    val calcDeltaEv = (calcDeltaTotal - calcDeltaHev).coerceAtLeast(0.0)
    val calcAvgHevKmL = if (curFuelLiters > 0 && calcDeltaHev > 0) calcDeltaHev / curFuelLiters else 0.0
    val calcAvgGlobalKmL = if (curFuelLiters > 0 && calcDeltaTotal > 0) calcDeltaTotal / curFuelLiters else 0.0

    // Custo instantâneo recalculado com limite de 75% da capacidade da bateria
    val effectiveTariff = if (selectedLocation == ChargingLocation.HOME) homeEnergyPrice else publicEnergyPrice
    val rawElectricKwh = (calcDeltaEv * entry.electricConsumptionKwh100km / 100.0)
    val batteryCap = if (entry.batteryCapacityKwh > 0) entry.batteryCapacityKwh else 18.3
    val maxElectricKwh = batteryCap * 0.75
    val calcElectricKwh = if (maxElectricKwh > 0) rawElectricKwh.coerceAtMost(maxElectricKwh) else rawElectricKwh
    val calcElectricCost = calcElectricKwh * effectiveTariff
    val calcGasCost = curFuelLiters * entry.gasPriceLiter
    val calcTotalCost = calcElectricCost + calcGasCost

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        titleText.trim(),
                        curTotalStart,
                        curTotalEnd,
                        curHevStart,
                        curHevEnd,
                        curFuelLiters,
                        selectedLocation
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaveButtonBg,
                    contentColor = Color(0xFF022C22)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.odometerSaveEditBtn, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.odometerCancelBtn, color = VoltageOnSurfaceVariant)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = CyanText)
                Text(strings.odometerEditDialogTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.odometerEditDialogInstruction,
                    style = MaterialTheme.typography.bodySmall.copy(color = VoltageOnSurfaceVariant, fontSize = 12.sp)
                )

                // Título / Observação
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text(strings.odometerTripNoteLabel, fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Odômetro Total
                Text(
                    text = strings.odometerTotalOdometerLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyanText,
                        fontSize = 11.sp
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalStartText,
                        onValueChange = { totalStartText = it },
                        label = { Text(strings.odometerTotalStart, fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalEndText,
                        onValueChange = { totalEndText = it },
                        label = { Text(strings.odometerTotalEnd, fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Odômetro HEV
                Text(
                    text = strings.odometerHevOdometerLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AmberText,
                        fontSize = 11.sp
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hevStartText,
                        onValueChange = { hevStartText = it },
                        label = { Text(strings.odometerHevStart, fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hevEndText,
                        onValueChange = { hevEndText = it },
                        label = { Text(strings.odometerHevEnd, fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Combustível
                OutlinedTextField(
                    value = fuelLitersText,
                    onValueChange = { fuelLitersText = it },
                    label = { Text(strings.odometerFuelUsedLabel, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // SELETOR: Onde recarregou para esta viagem (Casa vs Posto)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.odometerWhereRechargedQuestion,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = VoltageOnSurface,
                            fontSize = 11.5.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isHome = selectedLocation == ChargingLocation.HOME
                        Surface(
                            onClick = { selectedLocation = ChargingLocation.HOME },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isHome) Color(0xFF1E1B4B) else Color(0xFF070A14),
                            border = BorderStroke(1.2.dp, if (isHome) Color(0xFF818CF8) else BgCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = if (isHome) Color(0xFFA5B4FC) else Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = strings.odometerAtHomeOption,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isHome) Color.White else Color(0xFF94A3B8),
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "R$ ${formatNumber(homeEnergyPrice, 2)}/kWh",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isHome) Color(0xFFC7D2FE) else Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        val isStation = selectedLocation == ChargingLocation.STATION
                        Surface(
                            onClick = { selectedLocation = ChargingLocation.STATION },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isStation) Color(0xFF082F49) else Color(0xFF070A14),
                            border = BorderStroke(1.2.dp, if (isStation) Color(0xFF38BDF8) else BgCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EvStation,
                                        contentDescription = null,
                                        tint = if (isStation) Color(0xFF38BDF8) else Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = strings.odometerAtStationOption,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isStation) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isStation) Color.White else Color(0xFF94A3B8),
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "R$ ${formatNumber(publicEnergyPrice, 2)}/kWh",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isStation) Color(0xFFBAE6FD) else Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Card de pré-visualização recalculada em tempo real
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070A14)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = strings.odometerInstantRecalcTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )

                        // 1. Delta Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.odometerDeltaTotalPrefix}:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "${formatNumber(calcDeltaTotal, 1)} km",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // 2. Delta HEV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.odometerDeltaHevPrefix}:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "${formatNumber(calcDeltaHev, 1)} km",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AmberText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // 3. Delta EV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.odometerDeltaEvPrefix}:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "${formatNumber(calcDeltaEv, 1)} km",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PurpleEvText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                        // 4. Consumo HEV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.odometerHevConsumptionLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = if (calcAvgHevKmL > 0) "${formatNumber(calcAvgHevKmL, 2)} km/L" else "--",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AmberText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        // 5. Consumo Global
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.odometerGlobalConsumptionLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = if (calcAvgGlobalKmL > 0) "${formatNumber(calcAvgGlobalKmL, 2)} km/L" else "--",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GreenGlobalText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                        // 6. Custo Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.odometerTotalCostLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "R$ ${formatNumber(calcTotalCost, 2)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        // 7. Combustível
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.odometerHistoryFuel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "R$ ${formatNumber(calcGasCost, 2)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // 8. Elétrico
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.odometerElectricLabel}:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "R$ ${formatNumber(calcElectricCost, 2)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFC084FC),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(18.dp),
        containerColor = Color(0xFF0F172A)
    )
}

private fun buildOdometerShareReport(state: AppUiState, strings: AppDictionary): String {
    val vName = state.selectedVehicle.name
    val total = formatNumber(state.odometerDeltaTotalKm, 1)
    val ev = formatNumber(state.odometerDeltaEvKm, 1)
    val hev = formatNumber(state.odometerDeltaHevKm, 1)
    val evPct = formatNumber(state.odometerEvPercent, 0)
    val hevPct = formatNumber(state.odometerHevPercent, 0)
    val hevKmL = formatNumber(state.odometerAverageHevKmL, 2)
    val hevL100 = formatNumber(state.odometerAverageHevL100km, 1)
    val globalKmL = formatNumber(state.odometerGlobalKmL, 2)
    val liters = formatNumber(state.odometerFuelLiters, 1)

    val notePart = if (state.odometerTripNote.isNotBlank()) "\n📌 ${strings.odometerTripNoteLabel}: ${state.odometerTripNote}" else ""
    val chargePlace = if (state.odometerUseHomeTariff) strings.odometerAtHomeOption else strings.odometerAtStationOption

    val isEn = state.language == AppLanguage.EN_US

    return if (isEn) {
        """
        📊 *REAL PHEV CONSUMPTION REPORT*
        🚗 Vehicle: *$vName*
        
        🛣️ *Logged Odometers:*
        • Total Odometer: ${state.odometerTotalStartKm.toInt()} km ➔ ${state.odometerTotalEndKm.toInt()} km
        • Total Driven: *$total km*
        • HEV Odometer: ${state.odometerHevStartKm.toInt()} km ➔ ${state.odometerHevEndKm.toInt()} km
        • HEV Driven: *$hev km* ($hevPct%)
        • EV Calculated: *${state.odometerEvStartKm.toInt()} km ➔ ${state.odometerEvEndKm.toInt()} km*
        • EV Driven: *$ev km* ($evPct%)
        
        ⛽ *Fuel Consumption:*
        • *AVERAGE HEV CONSUMPTION: $hevKmL km/L* ($hevL100 L/100km)
        • Fuel Used: *$liters Liters*
        • Global Consumption: $globalKmL km/L
        
        💰 *Trip Costs:*
        • Total Cost: R$ ${formatNumber(state.odometerTotalTripCost, 2)}
        • Fuel: R$ ${formatNumber(state.odometerGasolineCost, 2)}
        • Electric: R$ ${formatNumber(state.odometerElectricCost, 2)} ($chargePlace)
        $notePart
        
        ⚡ Generated with Voltage (PHEV & EV Calculator)
        """.trimIndent()
    } else {
        """
        📊 *RELATÓRIO DE CONSUMO REAL PHEV*
        🚗 Veículo: *$vName*
        
        🛣️ *Odômetros Registrados:*
        • Odômetro Total: ${state.odometerTotalStartKm.toInt()} km ➔ ${state.odometerTotalEndKm.toInt()} km
        • Total Rodado: *$total km*
        • Odômetro HEV: ${state.odometerHevStartKm.toInt()} km ➔ ${state.odometerHevEndKm.toInt()} km
        • HEV Percorrido: *$hev km* ($hevPct%)
        • EV Calculado: *${state.odometerEvStartKm.toInt()} km ➔ ${state.odometerEvEndKm.toInt()} km*
        • EV Percorrido: *$ev km* ($evPct%)
        
        ⛽ *Consumo de Combustível:*
        • *CONSUMO MÉDIO HEV: $hevKmL km/L* ($hevL100 L/100km)
        • Combustível Usado: *$liters Litros*
        • Consumo Global: $globalKmL km/L
        
        💰 *Custos da Viagem:*
        • Custo Total: R$ ${formatNumber(state.odometerTotalTripCost, 2)}
        • Combustível: R$ ${formatNumber(state.odometerGasolineCost, 2)}
        • Elétrico: R$ ${formatNumber(state.odometerElectricCost, 2)} ($chargePlace)
        $notePart
        
        ⚡ Gerado com o App Jaecoo & PHEV Calculator
        """.trimIndent()
    }
}

private fun buildOdometerEntryShareReport(entry: OdometerEntry, language: AppLanguage, strings: AppDictionary): String {
    val total = formatNumber(entry.totalKm, 1)
    val ev = formatNumber(entry.evKm, 1)
    val hev = formatNumber(entry.hevKm, 1)
    val evPct = formatNumber(entry.evPercent, 0)
    val hevPct = formatNumber(entry.hevPercent, 0)
    val hevKmL = formatNumber(entry.realHevKmL, 2)
    val globalKmL = formatNumber(entry.realGlobalKmL, 2)
    val liters = formatNumber(entry.fuelLiters, 2)

    val titlePart = if (entry.title.isNotBlank()) "🚗 *${entry.title}* (${entry.vehicleName})" else "🚗 *${entry.vehicleName}*"

    val isEn = language == AppLanguage.EN_US

    val costSection = if (entry.totalCost > 0) {
        if (isEn) {
            """
            
            💰 *Trip Costs:*
            • Total Cost: R$ ${formatNumber(entry.totalCost, 2)}
            • Fuel: R$ ${formatNumber(entry.gasolineCost, 2)}
            • Electric: R$ ${formatNumber(entry.electricCost, 2)} (${entry.chargingLocation.label(language)})
            """.trimIndent()
        } else {
            """
            
            💰 *Custos da Viagem:*
            • Custo Total: R$ ${formatNumber(entry.totalCost, 2)}
            • Combustível: R$ ${formatNumber(entry.gasolineCost, 2)}
            • Elétrico: R$ ${formatNumber(entry.electricCost, 2)} (${entry.chargingLocation.label(language)})
            """.trimIndent()
        }
    } else ""

    return if (isEn) {
        """
        📊 *PHEV CONSUMPTION HISTORY*
        $titlePart
        📅 ${entry.formattedDate(language)}
        
        🛣️ *Distances:*
        • Total: ${entry.totalStartKm.toInt()} ➔ ${entry.totalEndKm.toInt()} km (+$total km)
        • HEV: $hev km ($hevPct%)
        • EV (Calc): $ev km ($evPct%)
        
        ⛽ *Consumption:*
        • *AVERAGE HEV CONSUMPTION: ${if (entry.realHevKmL > 0) "$hevKmL km/L" else "--"}*
        • Fuel: $liters L
        • Global Consumption: ${if (entry.realGlobalKmL > 0) "$globalKmL km/L" else "--"}$costSection
        
        ⚡ Generated with Voltage (PHEV & EV Calculator)
        """.trimIndent()
    } else {
        """
        📊 *HISTÓRICO DE CONSUMO PHEV*
        $titlePart
        📅 ${entry.formattedDate(language)}
        
        🛣️ *Distâncias:*
        • Total: ${entry.totalStartKm.toInt()} ➔ ${entry.totalEndKm.toInt()} km (+$total km)
        • HEV: $hev km ($hevPct%)
        • EV (Calc): $ev km ($evPct%)
        
        ⛽ *Consumo:*
        • *CONSUMO MÉDIO HEV: ${if (entry.realHevKmL > 0) "$hevKmL km/L" else "--"}*
        • Combustível: $liters L
        • Consumo Global: ${if (entry.realGlobalKmL > 0) "$globalKmL km/L" else "--"}$costSection
        
        ⚡ Gerado com o App Jaecoo & PHEV Calculator
        """.trimIndent()
    }
}
