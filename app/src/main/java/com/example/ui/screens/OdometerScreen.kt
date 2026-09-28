package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChargingLocation
import com.example.data.ExamplePhevTestData
import com.example.data.OdometerEntry
import com.example.data.VehicleType
import com.example.ui.AppUiState
import com.example.ui.MainViewModel
import com.example.ui.formatCurrency
import com.example.ui.formatNumber
import com.example.ui.components.CsvDataDialog
import com.example.ui.components.RechargeTariffDialog
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

/**
 * Controle interativo e digitável de porcentagem da bateria (%)
 * Permite ao usuário digitar diretamente o valor pelo teclado numérico,
 * além de botões de ajuste fino (-5% e +5%) e atalho para 100%.
 */
@Composable
fun EditableBatteryPercentControl(
    value: Double,
    onValueChange: (Double) -> Unit,
    onStep: (Double) -> Unit,
    onSet100: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "battery_percent",
    compact: Boolean = false,
    showShortcut100: Boolean = false
) {
    val isWarning = value <= 25.0
    var inputText by remember(value) {
        mutableStateOf(if (value % 1.0 == 0.0) value.toInt().toString() else value.toString())
    }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 4.dp)
    ) {
        // Botão Diminuir [-] (-5%)
        Surface(
            onClick = {
                focusManager.clearFocus()
                onStep(-5.0)
            },
            shape = RoundedCornerShape(if (compact) 4.dp else 6.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .size(if (compact) 26.dp else 28.dp)
                .testTag("${testTagPrefix}_minus")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Diminuir 5%",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 12.dp else 14.dp)
                )
            }
        }

        // Campo Digitável para a Porcentagem (%)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color(0xFF0F172A), RoundedCornerShape(if (compact) 4.dp else 6.dp))
                .border(
                    BorderStroke(1.dp, if (isWarning) Color(0xFF7F1D1D) else Color(0xFF334155)),
                    RoundedCornerShape(if (compact) 4.dp else 6.dp)
                )
                .padding(horizontal = if (compact) 5.dp else 7.dp, vertical = if (compact) 2.dp else 3.dp)
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { newStr ->
                    val clean = newStr.filter { it.isDigit() }.take(3)
                    inputText = clean
                    val parsed = clean.toDoubleOrNull()
                    if (parsed != null) {
                        val clamped = parsed.coerceIn(0.0, 100.0)
                        if (parsed > 100.0) {
                            inputText = "100"
                        }
                        onValueChange(clamped)
                    }
                },
                textStyle = MaterialTheme.typography.labelMedium.copy(
                    color = if (isWarning) Color(0xFFF87171) else Color(0xFFC084FC),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (compact) 11.5.sp else 13.sp,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(if (isWarning) Color(0xFFF87171) else Color(0xFFC084FC)),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (inputText.isBlank()) {
                            inputText = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .width(if (compact) 32.dp else 38.dp)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && inputText.isBlank()) {
                            inputText = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
                        }
                    }
                    .testTag("${testTagPrefix}_input")
            )
            Text(
                text = "%",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isWarning) Color(0xFFF87171) else Color(0xFFC084FC),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 10.sp else 11.5.sp
                )
            )
        }

        // Botão Aumentar [+] (+5%)
        Surface(
            onClick = {
                focusManager.clearFocus()
                onStep(5.0)
            },
            shape = RoundedCornerShape(if (compact) 4.dp else 6.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .size(if (compact) 26.dp else 28.dp)
                .testTag("${testTagPrefix}_plus")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumentar 5%",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 12.dp else 14.dp)
                )
            }
        }
    }
}

/**
 * Controle interativo e digitável de tarifa da recarga (R$/kWh)
 * Permite ao usuário digitar diretamente pelo teclado numérico decimal ou usar os botões [-] e [+]
 */
@Composable
fun EditablePriceControl(
    value: Double,
    onValueChange: (Double) -> Unit,
    onStep: (Double) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "recharge_price",
    compact: Boolean = true,
    stepAmount: Double = 0.10
) {
    var inputText by remember(value) {
        mutableStateOf(String.format(java.util.Locale.US, "%.2f", value).replace('.', ','))
    }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 4.dp)
    ) {
        // Botão Diminuir [-]
        Surface(
            onClick = {
                focusManager.clearFocus()
                onStep(-stepAmount)
            },
            shape = RoundedCornerShape(if (compact) 4.dp else 6.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .size(if (compact) 26.dp else 28.dp)
                .testTag("${testTagPrefix}_minus")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Diminuir tarifa",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 12.dp else 14.dp)
                )
            }
        }

        // Campo Digitável para o Preço (ex: 2,20)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(if (compact) 54.dp else 64.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(if (compact) 4.dp else 6.dp))
                .border(
                    BorderStroke(1.dp, Color(0xFF334155)),
                    RoundedCornerShape(if (compact) 4.dp else 6.dp)
                )
                .padding(horizontal = 4.dp, vertical = if (compact) 2.dp else 3.dp)
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { newStr ->
                    val filtered = newStr.filter { it.isDigit() || it == '.' || it == ',' }.take(6)
                    inputText = filtered
                    val normalized = filtered.replace(',', '.')
                    val parsed = normalized.toDoubleOrNull()
                    if (parsed != null && parsed >= 0.0) {
                        onValueChange(parsed)
                    }
                },
                textStyle = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (compact) 11.sp else 12.5.sp,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Color(0xFF38BDF8)),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (inputText.isBlank()) {
                            inputText = String.format(java.util.Locale.US, "%.2f", value).replace('.', ',')
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && inputText.isBlank()) {
                            inputText = String.format(java.util.Locale.US, "%.2f", value).replace('.', ',')
                        }
                    }
                    .testTag("${testTagPrefix}_input")
            )
        }

        // Botão Aumentar [+]
        Surface(
            onClick = {
                focusManager.clearFocus()
                onStep(stepAmount)
            },
            shape = RoundedCornerShape(if (compact) 4.dp else 6.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .size(if (compact) 26.dp else 28.dp)
                .testTag("${testTagPrefix}_plus")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumentar tarifa",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 12.dp else 14.dp)
                )
            }
        }
    }
}

@Composable
fun OdometerScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val strings = state.strings
    val language = state.language
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Garante que os odômetros anteriores sejam preenchidos (especialmente para o Exemplo PHEV ou veículos com histórico)
    LaunchedEffect(state.selectedVehicle.id, state.odometerEntries.size) {
        viewModel.ensureVehicleBaselineLoaded()
    }

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

    var rechargeCountText by remember(state.odometerRechargeCount) {
        mutableStateOf(state.odometerRechargeCount.toString())
    }

    var historyPageIndex by remember { mutableIntStateOf(0) }
    var editingEntry by remember { mutableStateOf<OdometerEntry?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var editingRechargePriceIndex by remember { mutableStateOf<Int?>(null) }

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
                            if (state.currentVehicleOdometerEntries.isNotEmpty()) {
                                Badge(
                                    containerColor = AmberText,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = state.currentVehicleOdometerEntries.size.toString(),
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

                // Planilha CSV (Exportar / Importar)
                IconButton(
                    onClick = { showCsvDialog = true },
                    modifier = Modifier.testTag("odometer_btn_csv_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = strings.exportSpreadsheetBtn,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(22.dp)
                    )
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
                        val introSubtitle = when (state.selectedVehicle.type) {
                            VehicleType.BEV -> strings.odometerIntroSubtitleBev
                            VehicleType.HEV -> strings.odometerIntroSubtitleHev
                            VehicleType.PHEV -> strings.odometerIntroSubtitle
                        }.format(state.selectedVehicle.name)
                        Text(
                            text = introSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = VoltageOnSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                val introDescription = when (state.selectedVehicle.type) {
                    VehicleType.BEV -> strings.odometerIntroDescriptionBev
                    VehicleType.HEV -> strings.odometerIntroDescriptionHev
                    VehicleType.PHEV -> strings.odometerIntroDescription
                }

                Text(
                    text = introDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = VoltageOnSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )
            }
        }

        // 1. CARD: Nome da Viagem (Opcional)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("odometer_note_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.odometerStepTripNameTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyanText,
                        fontSize = 14.sp
                    )
                )

                // Input Nome da Viagem (Opcional)
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { str ->
                        noteText = str
                        viewModel.updateOdometerTripNote(str)
                    },
                    label = { Text(strings.odometerTripNoteLabel, fontSize = 12.sp) },
                    placeholder = { Text(strings.odometerTripNotePlaceholder, fontSize = 12.sp, color = Color(0xFF64748B)) },
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

        // 2. CARD: Odômetros do Veículo
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

                // Banner informativo e botões rápidos para o veículo de demonstração "Exemplo PHEV"
                if (state.selectedVehicle.id == ExamplePhevTestData.VEHICLE_ID || state.selectedVehicle.name.contains("Exemplo", ignoreCase = true)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0369A1).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth().testTag("example_odometer_helper_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "💡", fontSize = 16.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (language == AppLanguage.EN_US) "Exemplo PHEV • Demo Odometers" else "Exemplo PHEV • Odômetros Anteriores",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = if (language == AppLanguage.EN_US)
                                            "Baseline (1 yr history): Total ${formatNumber(ExamplePhevTestData.LAST_TOTAL_END_KM, 0, language)} km | HEV ${formatNumber(ExamplePhevTestData.LAST_HEV_END_KM, 0, language)} km"
                                        else
                                            "Anterior (1 ano histórico): Total ${formatNumber(ExamplePhevTestData.LAST_TOTAL_END_KM, 0, language)} km | HEV ${formatNumber(ExamplePhevTestData.LAST_HEV_END_KM, 0, language)} km",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.restoreExampleBaselineOdometers() },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7DD3FC))
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.EN_US) "Apply 16,400 / 6,595 km" else "Aplicar 16.400 / 6.595 km",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Button(
                                    onClick = { viewModel.loadExampleDemoTrip() },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.EN_US) "Pre-fill Demo (+270 km)" else "Preencher Exemplo (+270 km)",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Subseção Total (mostra rótulo específico apenas se for PHEV para distinguir de HEV)
                if (state.selectedVehicle.type == VehicleType.PHEV) {
                    Text(
                        text = strings.odometerTotalHeader,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    )
                }

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

                // Para BEV: Dica de autonomia máxima da carga e alerta se exceder
                if (state.selectedVehicle.type == VehicleType.BEV) {
                    val maxKm = state.odometerMaxRechargeEvKm
                    val delta = deltaTotal
                    if (delta > maxKm && maxKm > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF3B1506),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Excede a autonomia máxima da carga (~${formatNumber(maxKm, 1)} km para ${state.odometerRechargeCount}x recarga)!",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.weight(1f),
                                        softWrap = true
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.limitTripToMaxRechargeRange() },
                                        modifier = Modifier.weight(1f).height(30.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = "Limitar a ${formatNumber(maxKm, 0)} km",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFFECACA),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    val pFinal = state.odometerEffectiveRechargeBatteryPercents.firstOrNull() ?: 100.0
                                    val reserveP = if (state.selectedVehicle.type == VehicleType.PHEV) state.odometerEvReservePercent else 0.0
                                    val usableP = (pFinal - reserveP).coerceIn(5.0, 100.0)
                                    val kwhPerRecharge = state.selectedVehicle.batteryCapacityKwh * (usableP / 100.0)
                                    val rangePerRecharge = if (kwhPerRecharge > 0 && state.odometerEffectiveElectricConsumption > 0) {
                                        (kwhPerRecharge / state.odometerEffectiveElectricConsumption) * 100.0
                                    } else 200.0
                                    val reqCount = kotlin.math.ceil(state.odometerEffectiveTotalKm / rangePerRecharge).toInt().coerceAtLeast(1)

                                    Button(
                                        onClick = { viewModel.autoAdjustRechargeCountForTrip() },
                                        modifier = Modifier.weight(1f).height(30.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = "Ajustar p/ ${reqCount}x recargas",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFBAE6FD),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    } else if (maxKm > 0) {
                        Text(
                            text = "⚡ Autonomia máx. com ${state.odometerRechargeCount}x recarga(s): ~${formatNumber(maxKm, 1)} km",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF38BDF8),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, top = 2.dp),
                            softWrap = true
                        )
                    }
                }

                // Subseção HEV exclusiva para veículos PHEV (onde há separador total vs combustão)
                if (state.selectedVehicle.type == VehicleType.PHEV) {
                    Spacer(modifier = Modifier.height(2.dp))

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
        }

        // 4. CARD ODÔMETROS EV (Calculados pelo App - Exclusivo para PHEV)
        if (state.selectedVehicle.type == VehicleType.PHEV) {
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

                    // Última linha com a diferença entre EV Anterior e EV Atual
                    val evDiffText = if (totalStart > 0 && totalEnd > 0) {
                        "${formatNumber(deltaEv, 1)} km"
                    } else if (totalEnd > 0) {
                        "${formatNumber(evEnd, 1)} km"
                    } else {
                        "—"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF140D26))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = strings.odometerEvDifferenceLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PurpleBadgeText,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = evDiffText,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color(0xFFC084FC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // 4.1 CARD ODÔMETROS HEV (Combustão / Híbrido)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("odometer_hev_calculated_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191207)),
                border = BorderStroke(1.dp, Color(0xFF4D2C0D))
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
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = AmberText,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = strings.odometerHevCardTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AmberText,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AmberIconBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = strings.odometerHevFormulaBadge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFDE68A),
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
                        // HEV Anterior
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0B07)),
                            border = BorderStroke(1.dp, Color(0xFF33200E))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = strings.odometerHevStart,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = VoltageOnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = if (hevStart > 0) "${formatNumber(hevStart, 1)} km" else if (totalStart > 0) "0,0 km" else "—",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VoltageOnSurface,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (hevStart > 0) "Registrado" else if (totalStart > 0) "Zero ou não informado" else "—",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // HEV Atual
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0B07)),
                            border = BorderStroke(1.dp, Color(0xFF33200E))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = strings.odometerHevEnd,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = VoltageOnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = if (hevEnd > 0) "${formatNumber(hevEnd, 1)} km" else if (totalEnd > 0) "${formatNumber(hevStart, 1)} km" else "—",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VoltageOnSurface,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if (hevEnd > 0) "Registrado" else if (totalEnd > 0) "Sem aumento HEV" else "—",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    // Última linha com a diferença entre HEV Anterior e HEV Atual
                    val hevDiffText = if (totalStart > 0 && totalEnd > 0) {
                        "${formatNumber(deltaHev, 1)} km"
                    } else if (hevEnd > 0) {
                        "${formatNumber(deltaHev, 1)} km"
                    } else {
                        "—"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF231505))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
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
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = AmberText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = strings.odometerHevDifferenceLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFFDE68A),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = hevDiffText,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = AmberText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 5. CARD 2: Combustível Utilizado no Período (Oculto em BEV 100% elétrico)
        if (state.selectedVehicle.type != VehicleType.BEV) {
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
                }
            }
        }

        // 5.1 CARD 3: Recargas Realizadas no Período (Oculto em HEV tradicional não-plug-in)
        if (state.selectedVehicle.type != VehicleType.HEV) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("odometer_recharges_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val rechargesTitle = if (state.selectedVehicle.type == VehicleType.BEV) {
                    strings.odometerStep3RechargesTitleBev
                } else {
                    strings.odometerStep3RechargesTitle
                }
                Text(
                    text = rechargesTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PurpleEvText,
                        fontSize = 14.sp
                    )
                )

                // Caixa de digitação com botões de ajuste fino (-) e (+)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botão Decrementar (-)
                    IconButton(
                        onClick = {
                            if (state.odometerRechargeCount > 1) {
                                viewModel.stepOdometerRechargeCount(-1)
                            }
                        },
                        enabled = state.odometerRechargeCount > 1,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (state.odometerRechargeCount > 1) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.4f))
                            .border(1.dp, BgCardBorder, RoundedCornerShape(10.dp))
                            .testTag("btn_decrement_recharge_count")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuir recargas",
                            tint = if (state.odometerRechargeCount > 1) PurpleEvText else VoltageOnSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }

                    // Input Número de Recargas
                    OutlinedTextField(
                        value = rechargeCountText,
                        onValueChange = { str ->
                            val cleanDigits = str.filter { it.isDigit() }
                            rechargeCountText = cleanDigits
                            val num = cleanDigits.toIntOrNull()
                            if (num != null && num >= 1) {
                                viewModel.updateOdometerRechargeCount(num)
                            }
                        },
                        label = { Text(strings.odometerRechargeCountLabel, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EvStation,
                                contentDescription = null,
                                tint = PurpleEvText
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_recharge_count"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleEvText,
                            unfocusedBorderColor = BgCardBorder,
                            focusedLabelColor = PurpleEvText
                        )
                    )

                    // Botão Incrementar (+)
                    IconButton(
                        onClick = { viewModel.stepOdometerRechargeCount(1) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, BgCardBorder, RoundedCornerShape(10.dp))
                            .testTag("btn_increment_recharge_count")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar recargas",
                            tint = PurpleEvText
                        )
                    }
                }

                Text(
                    text = strings.odometerRechargeCountHint,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = VoltageOnSurfaceVariant,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                )

                // SELETOR DO LOCAL DAS RECARGAS (CASA vs POSTO)
                val effectiveLocations = state.odometerEffectiveRechargeLocations
                val effectiveBatteryPercents = state.odometerEffectiveRechargeBatteryPercents
                val effectiveInitialBatteryPercents = state.odometerEffectiveRechargeInitialBatteryPercents
                val rechargeCount = state.odometerRechargeCount.coerceAtLeast(1)

                HorizontalDivider(
                    color = BgCardBorder.copy(alpha = 0.6f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                if (rechargeCount == 1) {
                    // 1 Recarga: Seletor direto Casa vs Posto
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Opção: Em Casa
                            val isHome = effectiveLocations.firstOrNull() == ChargingLocation.HOME
                            Surface(
                                onClick = { viewModel.setOdometerChargingLocation(ChargingLocation.HOME) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_charge_home"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isHome) Color(0xFF1E1B4B) else Color(0xFF070A14),
                                border = BorderStroke(1.2.dp, if (isHome) Color(0xFF818CF8) else BgCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = if (state.homeEnergyPrice <= 0.0) "R$ 0,00" else "R$ ${formatNumber(state.homeEnergyPrice, 2)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isHome) Color(0xFFC7D2FE) else Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            // Opção: No Posto de Recarga
                            val isStation = effectiveLocations.firstOrNull() == ChargingLocation.STATION
                            Surface(
                                onClick = { viewModel.setOdometerChargingLocation(ChargingLocation.STATION) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_charge_station"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isStation) Color(0xFF082F49) else Color(0xFF070A14),
                                border = BorderStroke(1.2.dp, if (isStation) Color(0xFF38BDF8) else BgCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = "R$ ${formatNumber(state.publicEnergyPrice, 2)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isStation) Color(0xFFBAE6FD) else Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            // Opção: Sem Recarga (Bateria no Limite / Motor & Regen)
                            val isNone = effectiveLocations.firstOrNull() == ChargingLocation.NONE
                            Surface(
                                onClick = { viewModel.setOdometerChargingLocation(ChargingLocation.NONE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_charge_none"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isNone) Color(0xFF372007) else Color(0xFF070A14),
                                border = BorderStroke(1.2.dp, if (isNone) Color(0xFFF59E0B) else BgCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = null,
                                            tint = if (isNone) Color(0xFFFBBF24) else Color(0xFF64748B),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = if (state.language == AppLanguage.EN_US) "No Recharge" else "Sem Recarga",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isNone) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isNone) Color.White else Color(0xFF94A3B8),
                                                fontSize = 11.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = "R$ 0,00",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isNone) Color(0xFFFDE68A) else Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        // Linha de baixo dos botões em casa/no posto: Nível inicial e Nível final da bateria (%)
                        val singleInitialPercent = effectiveInitialBatteryPercents.firstOrNull() ?: 25.0
                        val singleFinalPercent = effectiveBatteryPercents.firstOrNull() ?: 100.0
                        val singleLocation = effectiveLocations.firstOrNull() ?: ChargingLocation.HOME
                        val singleDefaultEnergyPrice = when (singleLocation) {
                            ChargingLocation.HOME -> state.homeEnergyPrice
                            ChargingLocation.STATION -> state.publicEnergyPrice
                            ChargingLocation.NONE -> 0.0
                        }
                        val singleEnergyPrice = state.odometerEffectiveRechargePrices.firstOrNull() ?: singleDefaultEnergyPrice
                        val singleIsCustomPrice = Math.abs(singleEnergyPrice - singleDefaultEnergyPrice) > 0.001
                        val singleBatteryCap = if (state.selectedVehicle.batteryCapacityKwh > 0) state.selectedVehicle.batteryCapacityKwh else 18.3
                        val singleDeltaP = (singleFinalPercent - singleInitialPercent).coerceAtLeast(0.0)
                        val singleRechargeKwh = singleBatteryCap * (singleDeltaP / 100.0)
                        val singleRechargeCost = singleRechargeKwh * singleEnergyPrice

                        if (singleLocation == ChargingLocation.NONE) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recharge_none_info_card"),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E170A),
                                border = BorderStroke(1.dp, Color(0xFF78350F))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (state.language == AppLanguage.EN_US)
                                            "No plug recharge linked to this trip. All EV mileage (${formatNumber(state.odometerEffectiveEvKm, 1)} km) was generated by the engine & regen, counted together with ${formatNumber(state.odometerEffectiveHevKm, 1)} km HEV for fuel consumption."
                                        else
                                            "Sem recarga na tomada vinculada a este trajeto. Toda a quilometragem EV (${formatNumber(state.odometerEffectiveEvKm, 1)} km) foi gerada pelo motor a combustão e pela regeneração, sendo somada aos ${formatNumber(state.odometerEffectiveHevKm, 1)} km HEV para o consumo médio.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFDE68A),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    )
                                }
                            }
                        } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recharge_single_battery_row"),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF070B14),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1ª Entrada: Nível inicial (padrão 25%)
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
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = strings.odometerRechargeInitialLevelLabel,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    EditableBatteryPercentControl(
                                        value = singleInitialPercent,
                                        onValueChange = { viewModel.updateOdometerRechargeInitialBatteryPercent(0, it) },
                                        onStep = { viewModel.stepOdometerRechargeInitialBatteryPercent(0, it) },
                                        testTagPrefix = "recharge_single_initial_battery"
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xFF1E293B),
                                    thickness = 0.8.dp
                                )

                                // 2ª Entrada (linha de baixo): Nível final (padrão 100%)
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
                                            tint = if (singleFinalPercent <= 25.0) Color(0xFFF87171) else Color(0xFFA78BFA),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = strings.odometerRechargeFinalLevelLabel,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    EditableBatteryPercentControl(
                                        value = singleFinalPercent,
                                        onValueChange = { viewModel.updateOdometerRechargeBatteryPercent(0, it) },
                                        onStep = { viewModel.stepOdometerRechargeBatteryPercent(0, it) },
                                        testTagPrefix = "recharge_single_final_battery"
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xFF1E293B),
                                    thickness = 0.8.dp
                                )

                                // Entrada 3: Tarifa (R$/kWh)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = null,
                                            tint = if (singleLocation == ChargingLocation.HOME) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column(
                                            modifier = Modifier.clickable { editingRechargePriceIndex = 0 }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = strings.odometerRechargeTariffLabel,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = strings.odometerRechargeTariffDialogTitle,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                            if (singleIsCustomPrice) {
                                                Text(
                                                    text = "${strings.odometerRechargeTariffCustomBadge} (pad: R$ ${formatNumber(singleDefaultEnergyPrice, 2)})",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Normal
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        EditablePriceControl(
                                            value = singleEnergyPrice,
                                            onValueChange = { viewModel.updateOdometerRechargePrice(0, it) },
                                            onStep = { viewModel.stepOdometerRechargePrice(0, it) },
                                            compact = true,
                                            testTagPrefix = "recharge_single_price"
                                        )
                                        if (singleIsCustomPrice) {
                                            IconButton(
                                                onClick = { viewModel.resetOdometerRechargePrice(0) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.RestartAlt,
                                                    contentDescription = strings.odometerRechargeTariffDefaultBtn,
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    color = Color(0xFF1E293B),
                                    thickness = 0.8.dp
                                )

                                // Texto centralizado com o valor total da recarga considerando a porcentagem recarregada
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .testTag("recharge_single_total_value"),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${strings.odometerRechargeTotalValueLabel} ",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "R$ ${formatNumber(singleRechargeCost, 2)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (singleLocation == ChargingLocation.HOME) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xFF1E293B),
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                // Dois botões centralizados abaixo do valor total: Adicionar e Remover
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        onClick = { viewModel.addOdometerRecharge() },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                                        modifier = Modifier.testTag("btn_recharge_single_add")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = strings.odometerAddRechargeBtn,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF38BDF8),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = { },
                                        enabled = false,
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A).copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, Color(0xFF64748B).copy(alpha = 0.25f)),
                                        modifier = Modifier.testTag("btn_recharge_single_remove")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = strings.odometerRemoveRechargeBtn,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF64748B),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                } else {
                    // Múltiplas Recargas (> 1): Escolher onde foi feita cada uma + Atalhos rápidos
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Título da seção em sua própria linha
                        Text(
                            text = strings.odometerRechargeLocationsHeader,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = VoltageOnSurface,
                                fontSize = 12.sp
                            )
                        )

                        // Botões de atalho rápido centralizados em outra linha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Todas em Casa
                            Surface(
                                onClick = { viewModel.setAllOdometerRechargeLocations(ChargingLocation.HOME) },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E1B4B),
                                border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.7f)),
                                modifier = Modifier.testTag("btn_all_recharges_home")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = Color(0xFFA5B4FC),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = strings.odometerAllAtHome,
                                        color = Color(0xFFE0E7FF),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Todas no Posto
                            Surface(
                                onClick = { viewModel.setAllOdometerRechargeLocations(ChargingLocation.STATION) },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF082F49),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                                modifier = Modifier.testTag("btn_all_recharges_station")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EvStation,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = strings.odometerAllAtStation,
                                        color = Color(0xFFE0F2FE),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Lista de Recargas Individuais
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (index in 0 until rechargeCount) {
                                val loc = effectiveLocations.getOrElse(index) { ChargingLocation.HOME }
                                val isHomeLoc = loc == ChargingLocation.HOME
                                val isStationLoc = loc == ChargingLocation.STATION

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("recharge_location_row_$index"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0A0F1D),
                                    border = BorderStroke(1.dp, if (isHomeLoc) Color(0xFF312E81) else Color(0xFF0C4A6E))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Linha 1: "Recarga X" e indicador do local selecionado
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isHomeLoc) Color(0xFF312E81) else Color(0xFF0C4A6E)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        color = if (isHomeLoc) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = String.format(strings.odometerRechargeNumberPrefix, index + 1),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = VoltageOnSurface,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp
                                                    )
                                                )
                                            }

                                            // Valor em reais da tarifa da recarga no canto superior direito (interativo com botão de edição rápida)
                                            val defaultLocPrice = if (isHomeLoc) state.homeEnergyPrice else state.publicEnergyPrice
                                            val curPrice = state.odometerEffectiveRechargePrices.getOrElse(index) { defaultLocPrice }
                                            val isCustomPrice = Math.abs(curPrice - defaultLocPrice) > 0.001
                                            Surface(
                                                onClick = { editingRechargePriceIndex = index },
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isCustomPrice) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0F172A),
                                                border = BorderStroke(1.dp, if (isCustomPrice) Color(0xFF38BDF8) else Color(0xFF1E293B)),
                                                modifier = Modifier.testTag("recharge_${index}_price_badge")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = strings.odometerRechargeTariffDialogTitle,
                                                        tint = if (isCustomPrice) Color(0xFF38BDF8) else (if (isHomeLoc) Color(0xFFA5B4FC) else Color(0xFF38BDF8)),
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        text = if (curPrice <= 0.0) "R$ 0,00/kWh (${if (isHomeLoc) "Solar" else "Grátis"})" else "R$ ${formatNumber(curPrice, 2)}/kWh",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (isCustomPrice) Color(0xFF7DD3FC) else (if (isHomeLoc) Color(0xFFA5B4FC) else Color(0xFF38BDF8)),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.5.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Linha 2: Onde foi recarregado (Em Casa vs No Posto)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Botão Casa
                                            Surface(
                                                onClick = { viewModel.updateOdometerRechargeLocation(index, ChargingLocation.HOME) },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isHomeLoc) Color(0xFF1E1B4B) else Color(0xFF050811),
                                                border = BorderStroke(1.2.dp, if (isHomeLoc) Color(0xFF818CF8) else Color(0xFF1E293B)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("recharge_${index}_home")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Home,
                                                        contentDescription = null,
                                                        tint = if (isHomeLoc) Color(0xFFA5B4FC) else Color(0xFF64748B),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = strings.odometerAtHomeOption,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (isHomeLoc) Color.White else Color(0xFF94A3B8),
                                                            fontSize = 11.5.sp,
                                                            fontWeight = if (isHomeLoc) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    )
                                                }
                                            }

                                            // Botão Posto
                                            Surface(
                                                onClick = { viewModel.updateOdometerRechargeLocation(index, ChargingLocation.STATION) },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isStationLoc) Color(0xFF082F49) else Color(0xFF050811),
                                                border = BorderStroke(1.2.dp, if (isStationLoc) Color(0xFF38BDF8) else Color(0xFF1E293B)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("recharge_${index}_station")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.EvStation,
                                                        contentDescription = null,
                                                        tint = if (isStationLoc) Color(0xFF38BDF8) else Color(0xFF64748B),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = strings.odometerAtStationOption,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (isStationLoc) Color.White else Color(0xFF94A3B8),
                                                            fontSize = 11.5.sp,
                                                            fontWeight = if (isStationLoc) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Linha 3: Nível inicial e Nível final nesta recarga (%)
                                        val curInitialPercent = effectiveInitialBatteryPercents.getOrElse(index) { 25.0 }
                                        val curFinalPercent = effectiveBatteryPercents.getOrElse(index) { 100.0 }
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("recharge_${index}_battery_row"),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF070B14),
                                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Entrada 1: Nível inicial (padrão 25%)
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
                                                            tint = Color(0xFF38BDF8),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Text(
                                                            text = strings.odometerRechargeInitialLevelLabel,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = Color(0xFF94A3B8),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        )
                                                    }

                                                    EditableBatteryPercentControl(
                                                        value = curInitialPercent,
                                                        onValueChange = { viewModel.updateOdometerRechargeInitialBatteryPercent(index, it) },
                                                        onStep = { viewModel.stepOdometerRechargeInitialBatteryPercent(index, it) },
                                                        testTagPrefix = "recharge_${index}_initial_battery",
                                                        compact = true
                                                    )
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
                                                    thickness = 0.8.dp
                                                )

                                                // Entrada 2: Nível final (padrão 100%)
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
                                                            tint = if (curFinalPercent <= 25.0) Color(0xFFF87171) else Color(0xFFA78BFA),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Text(
                                                            text = strings.odometerRechargeFinalLevelLabel,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = Color(0xFF94A3B8),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        )
                                                    }

                                                    EditableBatteryPercentControl(
                                                        value = curFinalPercent,
                                                        onValueChange = { viewModel.updateOdometerRechargeBatteryPercent(index, it) },
                                                        onStep = { viewModel.stepOdometerRechargeBatteryPercent(index, it) },
                                                        testTagPrefix = "recharge_${index}_final_battery",
                                                        compact = true
                                                    )
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
                                                    thickness = 0.8.dp
                                                )

                                                // Entrada 3: Tarifa desta recarga (R$/kWh)
                                                val defaultLocPrice = if (isHomeLoc) state.homeEnergyPrice else state.publicEnergyPrice
                                                val curPrice = state.odometerEffectiveRechargePrices.getOrElse(index) { defaultLocPrice }
                                                val isCustomPrice = Math.abs(curPrice - defaultLocPrice) > 0.001

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ElectricBolt,
                                                            contentDescription = null,
                                                            tint = if (isHomeLoc) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Column {
                                                            Text(
                                                                text = strings.odometerRechargeTariffLabel,
                                                                style = MaterialTheme.typography.bodySmall.copy(
                                                                    color = Color(0xFF94A3B8),
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Medium
                                                                ),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (isCustomPrice) {
                                                                Text(
                                                                    text = "${strings.odometerRechargeTariffCustomBadge} (pad: R$ ${formatNumber(defaultLocPrice, 2)})",
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        color = Color(0xFF38BDF8),
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Normal
                                                                    ),
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        EditablePriceControl(
                                                            value = curPrice,
                                                            onValueChange = { viewModel.updateOdometerRechargePrice(index, it) },
                                                            onStep = { viewModel.stepOdometerRechargePrice(index, it) },
                                                            compact = true,
                                                            testTagPrefix = "recharge_${index}_price"
                                                        )
                                                        if (isCustomPrice) {
                                                            IconButton(
                                                                onClick = { viewModel.resetOdometerRechargePrice(index) },
                                                                modifier = Modifier.size(24.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.RestartAlt,
                                                                    contentDescription = strings.odometerRechargeTariffDefaultBtn,
                                                                    tint = Color(0xFF94A3B8),
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
                                                    thickness = 0.8.dp
                                                )

                                                val curBatteryCap = if (state.selectedVehicle.batteryCapacityKwh > 0) state.selectedVehicle.batteryCapacityKwh else 18.3
                                                val curDeltaP = (curFinalPercent - curInitialPercent).coerceAtLeast(0.0)
                                                val curKwh = curBatteryCap * (curDeltaP / 100.0)
                                                val curCost = curKwh * curPrice

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp)
                                                        .testTag("recharge_${index}_total_value"),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${strings.odometerRechargeTotalValueLabel} ",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = Color(0xFF94A3B8),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    )
                                                    Text(
                                                        text = "R$ ${formatNumber(curCost, 2)}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = if (isHomeLoc) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
                                                    thickness = 0.8.dp,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )

                                                // Dois botões centralizados abaixo do valor total: Adicionar e Remover
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 2.dp, bottom = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        onClick = { viewModel.addOdometerRecharge() },
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFF0F172A),
                                                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                                                        modifier = Modifier.testTag("btn_recharge_${index}_add")
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Add,
                                                                contentDescription = null,
                                                                tint = Color(0xFF38BDF8),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Text(
                                                                text = strings.odometerAddRechargeBtn,
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = Color(0xFF38BDF8),
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 11.5.sp
                                                                )
                                                            )
                                                        }
                                                    }

                                                    val canRemove = rechargeCount > 1
                                                    Surface(
                                                        onClick = { viewModel.removeOdometerRecharge(index) },
                                                        enabled = canRemove,
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (canRemove) Color(0xFF2A1215) else Color(0xFF0F172A).copy(alpha = 0.5f),
                                                        border = BorderStroke(
                                                            1.dp,
                                                            if (canRemove) Color(0xFFF87171).copy(alpha = 0.7f) else Color(0xFF64748B).copy(alpha = 0.25f)
                                                        ),
                                                        modifier = Modifier.testTag("btn_recharge_${index}_remove")
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Remove,
                                                                contentDescription = null,
                                                                tint = if (canRemove) Color(0xFFF87171) else Color(0xFF64748B),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Text(
                                                                text = strings.odometerRemoveRechargeBtn,
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = if (canRemove) Color(0xFFF87171) else Color(0xFF64748B),
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 11.5.sp
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Resumo ponderado das recargas
                        val homeCount = effectiveLocations.count { it == ChargingLocation.HOME }
                        val stationCount = effectiveLocations.count { it == ChargingLocation.STATION }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡 ${homeCount}x Casa • ${stationCount}x Posto",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tarifa média: R$ ${formatNumber(state.odometerEffectiveEnergyPrice, 2)}/kWh",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        // CARD RESULTADO DA MEDIÇÃO
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
                            .background(if (deltaTotal > 0) GreenResultBadgeBg else Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (deltaTotal > 0) "${strings.odometerDeltaTotalPrefix} = ${formatNumber(deltaTotal, 1)} km" else "${strings.odometerDeltaTotalPrefix} = —",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (deltaTotal > 0) GreenResultBadgeText else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                if (deltaTotal <= 0.0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("odometer_awaiting_inputs_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = CyanText,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = strings.odometerWaitingCurrentKmTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageOnSurface,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            val guidanceText = if (totalEndText.isNotEmpty() && totalEnd <= totalStart) {
                                strings.odometerCurrentMustBeGreater
                            } else {
                                strings.odometerWaitingCurrentKmDesc
                            }
                            Text(
                                text = guidanceText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val isPureEv = deltaTotal > 0 && deltaHev <= 0.0
                    val globalL100km = if (deltaTotal > 0 && fuelLiters > 0) (fuelLiters / deltaTotal) * 100.0 else 0.0

                    // Card Central de Destaque: MODO 100% ELÉTRICO (se BEV ou PHEV 100% EV) ou CONSUMO MÉDIO HEV (se HEV ou PHEV modo híbrido)
                    if (state.selectedVehicle.type == VehicleType.BEV || (state.selectedVehicle.type == VehicleType.PHEV && isPureEv)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1035)),
                            border = BorderStroke(1.2.dp, Color(0xFF7C3AED).copy(alpha = 0.8f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3B0764)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = null,
                                            tint = Color(0xFFC084FC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = strings.odometerPureEvBannerTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC084FC),
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                Text(
                                    text = if (state.selectedVehicle.type == VehicleType.BEV) "Trajeto 100% elétrico • Zero emissão" else strings.odometerPureEvBannerSubtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                )

                                if (state.selectedVehicle.type == VehicleType.BEV && state.odometerExcessEvKm > 0) {
                                    Column {
                                        Text(
                                            text = "${formatNumber(state.odometerRechargeEvKm, 1)} km EV",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFC084FC),
                                                fontSize = 28.sp
                                            )
                                        )
                                        Text(
                                            text = "(supridos pela recarga • ${formatNumber(deltaEv, 1)} km totais)",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "${formatNumber(deltaEv, 1)} km EV",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFC084FC),
                                            fontSize = 32.sp
                                        )
                                    )
                                }

                                if (state.odometerExcessEvKm > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF3B1506),
                                        border = BorderStroke(1.dp, if (state.selectedVehicle.type == VehicleType.BEV) Color(0xFFDC2626) else Color(0xFFF97316)),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.WarningAmber,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFDBA74),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = if (state.selectedVehicle.type == VehicleType.BEV) {
                                                        "A carga da bateria suporta no máximo ${formatNumber(state.odometerMaxRechargeEvKm, 1)} km. Excedente impossível com esta recarga: +${formatNumber(state.odometerExcessEvKm, 1)} km."
                                                    } else {
                                                        "Excede a capacidade das recargas em ${formatNumber(state.odometerExcessEvKm, 1)} km. Reveja os km ou recargas."
                                                    },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color(0xFFFED7AA),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                    softWrap = true
                                                )
                                            }

                                            // Ações de correção imediata
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Button(
                                                    onClick = { viewModel.limitTripToMaxRechargeRange() },
                                                    modifier = Modifier.weight(1f).height(30.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                                ) {
                                                    Text(
                                                        text = "Limitar a ${formatNumber(state.odometerMaxRechargeEvKm, 0)} km",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color(0xFFFECACA),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        maxLines = 1
                                                    )
                                                }
                                                val pFinal = state.odometerEffectiveRechargeBatteryPercents.firstOrNull() ?: 100.0
                                                val reserveP = if (state.selectedVehicle.type == VehicleType.PHEV) state.odometerEvReservePercent else 0.0
                                                val usableP = (pFinal - reserveP).coerceIn(5.0, 100.0)
                                                val kwhPerRecharge = state.selectedVehicle.batteryCapacityKwh * (usableP / 100.0)
                                                val rangePerRecharge = if (kwhPerRecharge > 0 && state.odometerEffectiveElectricConsumption > 0) {
                                                    (kwhPerRecharge / state.odometerEffectiveElectricConsumption) * 100.0
                                                } else 200.0
                                                val reqCount = kotlin.math.ceil(state.odometerEffectiveTotalKm / rangePerRecharge).toInt().coerceAtLeast(1)

                                                Button(
                                                    onClick = { viewModel.autoAdjustRechargeCountForTrip() },
                                                    modifier = Modifier.weight(1f).height(30.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                                ) {
                                                    Text(
                                                        text = "Ajustar p/ ${reqCount}x recargas",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color(0xFFBAE6FD),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (state.selectedVehicle.type == VehicleType.HEV || (state.selectedVehicle.type == VehicleType.PHEV && deltaHev > 0.0)) {
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

                            // Linha 2: Valor do consumo
                            Text(
                                text = if (averageHevKmL > 0) "${formatNumber(averageHevKmL, 2)} km/L" else "-- km/L",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFBBF24),
                                    fontSize = 34.sp
                                )
                            )

                            // Na sequência: Outros textos
                            if (state.selectedVehicle.type == VehicleType.HEV) {
                                Text(
                                    text = if (fuelLiters > 0) "${formatNumber(deltaTotal, 1)} km ÷ ${formatNumber(fuelLiters, 2)} L abastecidos" else "Informe os litros no campo Combustível",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                )
                            } else if (state.odometerExcessEvKm > 0) {
                                Text(
                                    text = "${formatNumber(state.odometerEffectiveFuelKm, 1)} km a combustível ÷ Litros",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.5.sp
                                    )
                                )
                                Text(
                                    text = "${formatNumber(state.odometerDeltaHevKm, 1)} km HEV + ${formatNumber(state.odometerExcessEvKm, 1)} km EV reg.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.5.sp
                                    )
                                )
                            } else {
                                Text(
                                    text = strings.odometerCombustionModeFormula,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Card de Destaque: CONSUMO MÉDIO GLOBAL (Abaixo do Consumo Médio HEV)
                    if (state.selectedVehicle.type == VehicleType.PHEV) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_odometer_global_consumption"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF042018)),
                            border = BorderStroke(1.2.dp, Color(0xFF10B981))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Linha 1: Título e badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (language == AppLanguage.EN_US) "GLOBAL AVERAGE CONSUMPTION" else "CONSUMO MÉDIO GLOBAL",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF34D399),
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (globalL100km > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF064E3B))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${formatNumber(globalL100km, 1)} L/100km",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFFA7F3D0),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                ),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }

                                // Linha 2: Valor do consumo
                                Text(
                                    text = if (globalKmL > 0) "${formatNumber(globalKmL, 2)} km/L" else "-- km/L",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF34D399),
                                        fontSize = 34.sp
                                    )
                                )

                                // Na sequência: Outros textos
                                if (fuelLiters > 0 && deltaTotal > 0) {
                                    Text(
                                        text = "${formatNumber(deltaTotal, 1)} km totais (${formatNumber(deltaEv, 1)} km EV + ${formatNumber(deltaHev, 1)} km HEV) ÷ ${formatNumber(fuelLiters, 2)} L abastecidos",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 11.sp
                                        )
                                    )
                                } else {
                                    Text(
                                        text = strings.odometerDiffTotalOverLiters,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Grid de 2 cards adaptado ao tipo de veículo
                if (state.selectedVehicle.type == VehicleType.BEV) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                    text = "Custo Médio / km",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = VoltageOnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = if (state.odometerCostPerKm > 0) "R$ ${formatNumber(state.odometerCostPerKm, 2)}" else "--",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399),
                                        fontSize = 19.sp
                                    )
                                )
                                Text(
                                    text = "Energia elétrica",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                } else if (state.selectedVehicle.type == VehicleType.HEV) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                    text = "Custo Médio / km",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = VoltageOnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = if (state.odometerCostPerKm > 0) "R$ ${formatNumber(state.odometerCostPerKm, 2)}" else "--",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399),
                                        fontSize = 19.sp
                                    )
                                )
                                Text(
                                    text = "Combustível",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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

                        // Custo Médio / km
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
                                    text = "Custo Médio / km",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = VoltageOnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = if (state.odometerCostPerKm > 0) "R$ ${formatNumber(state.odometerCostPerKm, 2)}" else "--",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399),
                                        fontSize = 19.sp
                                    )
                                )
                                Text(
                                    text = "EV + Gasolina",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Detalhamento de Propulsão (Modo EV vs HEV - Apenas para PHEV)
                if (state.selectedVehicle.type == VehicleType.PHEV) {
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

                    if (deltaTotal > 0 && state.odometerExcessEvKm > 0) {
                        if (deltaHev <= 0.0) {
                            // Alerta quando o carro rodou no 100% elétrico e excedeu a capacidade da bateria/recargas
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("odometer_excess_ev_alert_card"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF261208)),
                                border = BorderStroke(1.2.dp, Color(0xFFF97316))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF7C2D12)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WarningAmber,
                                                contentDescription = null,
                                                tint = Color(0xFFFDBA74),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = strings.odometerExcessEvWarningTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFDBA74),
                                                fontSize = 13.sp
                                            ),
                                            modifier = Modifier.weight(1f),
                                            softWrap = true
                                        )
                                    }

                                    Text(
                                        text = "O veículo rodou no modo 100% elétrico (${formatNumber(deltaEv, 1)} km), porém a capacidade máxima das recargas informadas é de apenas ${formatNumber(state.odometerRechargeEvKm, 1)} km (excedente de ${formatNumber(state.odometerExcessEvKm, 1)} km).\n\nA quilometragem informada pode estar errada ou a quantidade de recargas está incorreta. Por favor, reveja os valores informados (ajuste a quilometragem ou adicione mais recargas).",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFED7AA),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF431407),
                                        border = BorderStroke(0.8.dp, Color(0xFF9A3412))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = Color(0xFFFB923C),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = strings.odometerExcessEvNoFuelNotice,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFFFDBA74),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                modifier = Modifier.weight(1f),
                                                softWrap = true
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "⚡ ${formatNumber(state.odometerRechargeEvKm, 1)} km da tomada • ⛽ ${formatNumber(state.odometerExcessEvKm, 1)} km elétricos gerados pelo motor/regen",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Os ${formatNumber(state.odometerExcessEvKm, 1)} km elétricos excedentes são somados aos ${formatNumber(deltaHev, 1)} km HEV para o cálculo da média de combustível (${formatNumber(state.odometerEffectiveFuelKm, 1)} km efetivos ÷ ${if (fuelLiters > 0) "${formatNumber(fuelLiters, 1)} L" else "litros"}).",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp
                                        )
                                    )
                                }
                            }
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

                        // Detalhamento: Combustível e Elétrico adaptado ao tipo de veículo
                        val isAtEvLimitTrip = if (state.odometerRechargeCount > 1) state.odometerTotalUsableBatteryPercent <= 0.0 else state.odometerBatteryStartPercent <= 25.0
                        if (state.selectedVehicle.type == VehicleType.BEV) {
                            // Bloco Elétrico Full Width para BEV
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF130E26)),
                                border = BorderStroke(1.dp, Color(0xFF4C1D95))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFFA78BFA),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = strings.odometerElectricLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFDDD6FE),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
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
                                            fontSize = 17.sp
                                        )
                                    )
                                    Text(
                                        text = if (state.odometerRechargeCount > 1) "${formatNumber(state.odometerTotalEnergyKwh, 1)} kWh (${state.odometerRechargeCount}x recargas)" else "${formatNumber(state.odometerTotalEnergyKwh, 1)} kWh",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (state.odometerEffectiveEnergyPrice > 0) {
                                        Text(
                                            text = "R$ ${formatNumber(state.odometerEffectiveEnergyPrice, 2)}/kWh • ${formatNumber(state.odometerEffectiveElectricConsumption, 1)} kWh/100km",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else if (state.selectedVehicle.type == VehicleType.HEV) {
                            // Bloco Combustível Full Width para HEV
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1606)),
                                border = BorderStroke(1.dp, Color(0xFF78350F))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalGasStation,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = strings.odometerFuelLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFFDE68A),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "R$ ${formatNumber(state.odometerGasolineCost, 2)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFBBF24),
                                            fontSize = 17.sp
                                        )
                                    )
                                    Text(
                                        text = "${formatNumber(state.odometerTotalGasolineLiters, 2)} L",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (state.gasolinePrice > 0) {
                                        val displayKmL = if (state.odometerAverageHevKmL > 0) {
                                            state.odometerAverageHevKmL
                                        } else {
                                            state.odometerEffectiveGasolineConsumption
                                        }
                                        Text(
                                            text = "R$ ${formatNumber(state.gasolinePrice, 2)}/L • ${formatNumber(displayKmL, 1)} km/L",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                            // PHEV: Row com Bloco Combustível e Bloco Elétrico lado a lado
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
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
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
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
                                            text = "${formatNumber(state.odometerTotalGasolineLiters, 2)} L",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (deltaTotal > 0 && deltaHev <= 0.0 && fuelLiters <= 0.0) {
                                            Text(
                                                text = strings.odometerPureEvBannerSubtitle,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF34D399),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else if (state.gasolinePrice > 0) {
                                            val displayKmL = if (state.odometerAverageHevKmL > 0) {
                                                state.odometerAverageHevKmL
                                            } else {
                                                state.odometerEffectiveGasolineConsumption
                                            }
                                            Text(
                                                text = "R$ ${formatNumber(state.gasolinePrice, 2)}/L • ${formatNumber(displayKmL, 1)} km/L",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 9.sp
                                                ),
                                                maxLines = 2,
                                                softWrap = true,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (isAtEvLimitTrip) {
                                            Text(
                                                text = "Trajeto a combustão",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFFFBBF24),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Bloco Elétrico
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isAtEvLimitTrip) Color(0xFF1B1015) else Color(0xFF130E26)),
                                    border = BorderStroke(1.dp, if (isAtEvLimitTrip) Color(0xFF7F1D1D) else Color(0xFF4C1D95))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isAtEvLimitTrip) Icons.Default.PowerOff else Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = if (isAtEvLimitTrip) Color(0xFFF87171) else Color(0xFFA78BFA),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = strings.odometerElectricLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isAtEvLimitTrip) Color(0xFFFECACA) else Color(0xFFDDD6FE),
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
                                                color = if (isAtEvLimitTrip) Color(0xFFFCA5A5) else Color(0xFFC084FC),
                                                fontSize = 15.sp
                                            )
                                        )
                                        Text(
                                            text = if (isAtEvLimitTrip) "0,0 kWh (rede)" else if (state.odometerRechargeCount > 1) "${formatNumber(state.odometerTotalEnergyKwh, 1)} kWh (${state.odometerRechargeCount}x recargas)" else "${formatNumber(state.odometerTotalEnergyKwh, 1)} kWh",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!isAtEvLimitTrip && state.odometerEffectiveEnergyPrice > 0) {
                                            Text(
                                                text = "R$ ${formatNumber(state.odometerEffectiveEnergyPrice, 2)}/kWh • ${formatNumber(state.odometerEffectiveElectricConsumption, 1)} kWh/100km",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 9.sp
                                                ),
                                                maxLines = 2,
                                                softWrap = true,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (isAtEvLimitTrip) {
                                            Text(
                                                text = strings.odometerBatteryStartEvLimitBtn,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFFF87171),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else {
                                            val rawCalculatedKwh = (state.odometerDeltaEvKm * state.odometerEffectiveElectricConsumption / 100.0)
                                            val isLimitApplied = rawCalculatedKwh > state.odometerTotalEnergyKwh && state.odometerTotalEnergyKwh > 0
                                            if (isLimitApplied) {
                                                val appliedPercentText = if (state.odometerRechargeCount > 1) "${state.odometerTotalUsableBatteryPercent.toInt()}% aplic." else "${state.odometerBatteryStartPercent.toInt()}% aplic."
                                                Text(
                                                    text = "Bat. $appliedPercentText",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color(0xFFA78BFA),
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Se BEV com excesso de autonomia, exibir aviso e ações de ajuste antes de salvar
                if (state.selectedVehicle.type == VehicleType.BEV && state.odometerExcessEvKm > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF2D0607),
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Não é possível salvar: viagem excede o limite da bateria (${formatNumber(state.odometerMaxRechargeEvKm, 1)} km)!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.weight(1f),
                                    softWrap = true
                                )
                            }
                            Text(
                                text = "É fisicamente impossível rodar ${formatNumber(deltaTotal, 1)} km com apenas ${state.odometerRechargeCount} recarga(s). Escolha uma correção:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFECACA),
                                    fontSize = 10.5.sp
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.limitTripToMaxRechargeRange() },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = "Limitar a ${formatNumber(state.odometerMaxRechargeEvKm, 0)} km",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFECACA),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                val pFinal = state.odometerEffectiveRechargeBatteryPercents.firstOrNull() ?: 100.0
                                val reserveP = if (state.selectedVehicle.type == VehicleType.PHEV) state.odometerEvReservePercent else 0.0
                                val usableP = (pFinal - reserveP).coerceIn(5.0, 100.0)
                                val kwhPerRecharge = state.selectedVehicle.batteryCapacityKwh * (usableP / 100.0)
                                val rangePerRecharge = if (kwhPerRecharge > 0 && state.odometerEffectiveElectricConsumption > 0) {
                                    (kwhPerRecharge / state.odometerEffectiveElectricConsumption) * 100.0
                                } else 200.0
                                val reqCount = kotlin.math.ceil(state.odometerEffectiveTotalKm / rangePerRecharge).toInt().coerceAtLeast(1)

                                Button(
                                    onClick = { viewModel.autoAdjustRechargeCountForTrip() },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = "Ajustar p/ ${reqCount}x recargas",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFBAE6FD),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                val isBevSaveDisabled = state.selectedVehicle.type == VehicleType.BEV && state.odometerExcessEvKm > 0

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
                        containerColor = if (isBevSaveDisabled) Color(0xFF334155) else SaveButtonBg,
                        contentColor = if (isBevSaveDisabled) Color(0xFF94A3B8) else Color(0xFF022C22)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isBevSaveDisabled) Color(0xFF94A3B8) else Color(0xFF022C22)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.odometerSaveToHistoryBtn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isBevSaveDisabled) Color(0xFF94A3B8) else Color(0xFF022C22)
                    )
                }
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
                // Header da seção: Ícone velocímetro + Título + Botão Limpar + Contador centralizado na linha de baixo
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                            Text(
                                text = strings.odometerHistorySectionTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VoltageOnSurface,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.3.sp
                                )
                            )
                            if (state.currentVehicleOdometerEntries.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${state.currentVehicleOdometerEntries.size}",
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF38BDF8),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        if (state.currentVehicleOdometerEntries.isNotEmpty()) {
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
                }

                if (state.currentVehicleOdometerEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val emptyMsg = if (state.odometerFilterVehicleOnly) {
                            if (language == AppLanguage.EN_US) {
                                "No trips recorded yet for ${state.selectedVehicle.name}.\nClick 'Save to History' above to log trips for this vehicle."
                            } else {
                                "Nenhuma medição salva ainda para ${state.selectedVehicle.name}.\nClique em 'Salvar no Histórico' acima para registrar viagens deste veículo."
                            }
                        } else {
                            strings.odometerNoEntriesText
                        }
                        Text(
                            text = emptyMsg,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = VoltageOnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        )
                    }
                } else {
                    // CÁLCULO DAS MÉDIAS DE CONSUMO DOS HISTÓRICOS FILTRADOS
                    val entries = state.currentVehicleOdometerEntries
                    val entriesCount = entries.size
                    val n = entriesCount.toDouble()

                    val totalDistanceKm = entries.sumOf { it.totalKm }
                    val totalEvKm = entries.sumOf { it.evKm }
                    val totalHevKm = entries.sumOf { it.hevKm }

                    val totalCost = entries.sumOf { it.totalCost }
                    val avgTripCost = if (n > 0) totalCost / n else 0.0
                    val avgCostPerKm = if (totalDistanceKm > 0) totalCost / totalDistanceKm else 0.0
                    val avgCostPer100Km = avgCostPerKm * 100.0

                    val totalGasCost = entries.sumOf { it.gasolineCost }
                    val avgGasCost = if (n > 0) totalGasCost / n else 0.0

                    val totalFuelLiters = entries.sumOf { it.totalGasolineLiters }
                    val avgGasLiters = if (n > 0) totalFuelLiters / n else 0.0

                    val avgHevKmL = if (totalHevKm > 0 && totalFuelLiters > 0) {
                        totalHevKm / totalFuelLiters
                    } else {
                        val validKmL = entries.filter { it.realHevKmL > 0 }
                        if (validKmL.isNotEmpty()) validKmL.map { it.realHevKmL }.average() else 0.0
                    }

                    val totalElecCost = entries.sumOf { it.electricCost }
                    val avgElecCost = if (n > 0) totalElecCost / n else 0.0

                    val totalEnergyKwh = entries.sumOf { it.totalEnergyKwh }
                    val avgEnergyKwh = if (n > 0) totalEnergyKwh / n else 0.0

                    val avgEvKwh100km = if (totalEvKm > 0 && totalEnergyKwh > 0) {
                        (totalEnergyKwh / totalEvKm) * 100.0
                    } else {
                        val validKwh = entries.filter { it.electricConsumptionKwh100km > 0 }
                        if (validKwh.isNotEmpty()) validKwh.map { it.electricConsumptionKwh100km }.average() else 0.0
                    }

                    val avgGasPrice = if (n > 0) entries.map { it.gasPriceLiter }.average() else state.gasolinePrice
                    val avgEquivKmL = if (totalCost > 0 && avgGasPrice > 0 && totalDistanceKm > 0) {
                        totalDistanceKm / (totalCost / avgGasPrice)
                    } else 0.0

                    // CARD DE MÉDIAS DE CONSUMO DE TODOS OS HISTÓRICOS (Idêntico ao Card de Custo da Viagem)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("odometer_history_averages_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
                        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Header: Linha 1 = Ícone + Título
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.odometerHistoryAveragesTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VoltageOnSurface,
                                        fontSize = 13.5.sp
                                    )
                                )
                            }

                            // Linha de baixo (badge com quantidade de viagens e km acumulados)
                            val tripsText = if (entriesCount == 1) {
                                "1 ${strings.odometerHistoryTripSingular}"
                            } else {
                                "$entriesCount ${strings.odometerHistoryTripsCount}"
                            }
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0xFF0C243B),
                                border = BorderStroke(1.dp, Color(0xFF0284C7))
                            ) {
                                Text(
                                    text = "$tripsText • ${formatNumber(totalDistanceKm, 0)} km",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.5.dp)
                                )
                            }

                            // Custo Médio em destaque (Verde Menta, idêntico ao card de custo da viagem)
                            Text(
                                text = "R$ ${formatNumber(avgTripCost, 2)}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF34D399),
                                    fontSize = 26.sp
                                )
                            )

                            // Subtítulo descritivo com médias por km e km/L equivalente
                            val subtitleText = buildString {
                                append(strings.odometerHistoryAvgPerTrip)
                                append(" • R$ ")
                                append(formatNumber(avgCostPerKm, 2))
                                append("/km")
                                if (avgEquivKmL > 0) {
                                    append(" • ")
                                    append(formatNumber(avgEquivKmL, 1))
                                    append(" km/L equiv.")
                                }
                            }
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 2
                            )

                            // Detalhamento: Combustível e Elétrico (idêntico ao card de custo da viagem)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Bloco Combustível (Âmbar)
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
                                            text = "R$ ${formatNumber(avgGasCost, 2)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFBBF24),
                                                fontSize = 15.sp
                                            )
                                        )
                                    Text(
                                        text = "${formatNumber(avgGasLiters, 2)} L méd.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (avgGasPrice > 0) {
                                        Text(
                                            text = "R$ ${formatNumber(avgGasPrice, 2)}/L",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    }
                                }

                                // Bloco Elétrico (Roxo)
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
                                            text = "R$ ${formatNumber(avgElecCost, 2)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC084FC),
                                                fontSize = 15.sp
                                            )
                                        )
                                        val avgEnergyPrice = if (n > 0) entries.map { it.energyPriceKwh }.average() else state.odometerEffectiveEnergyPrice
                                        Text(
                                            text = "${formatNumber(avgEnergyKwh, 1)} kWh méd.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (avgEnergyPrice > 0) {
                                            Text(
                                                text = "R$ ${formatNumber(avgEnergyPrice, 2)}/kWh",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 9.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val pageSize = 3
                    val totalPages = ((state.currentVehicleOdometerEntries.size + pageSize - 1) / pageSize).coerceAtLeast(1)
                    val safePage = historyPageIndex.coerceIn(0, totalPages - 1)
                    val pagedEntries = state.currentVehicleOdometerEntries.drop(safePage * pageSize).take(pageSize)

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

        Spacer(modifier = Modifier.height(20.dp))
    }

    // DIÁLOGO DE CONFIRMAÇÃO PARA LIMPAR O HISTÓRICO
    if (showClearConfirmDialog) {
        val isVehicleOnly = state.odometerFilterVehicleOnly
        val dialogMsg = if (isVehicleOnly) {
            if (language == AppLanguage.EN_US) {
                "Do you really want to delete the trip history for ${state.selectedVehicle.name}? Trips from other vehicles will be kept."
            } else {
                "Deseja realmente apagar o histórico de viagens de ${state.selectedVehicle.name}? As viagens dos outros veículos serão preservadas."
            }
        } else {
            strings.odometerClearHistoryDialogMsg
        }
        val confirmBtnText = if (isVehicleOnly) {
            if (language == AppLanguage.EN_US) "Clear ${state.selectedVehicle.name}" else "Apagar ${state.selectedVehicle.name}"
        } else {
            strings.odometerClearAllConfirmBtn
        }

        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(strings.odometerClearHistoryDialogTitle, fontWeight = FontWeight.Bold, color = VoltageOnSurface)
            },
            text = {
                Text(
                    dialogMsg,
                    color = VoltageOnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isVehicleOnly) {
                            viewModel.clearCurrentVehicleOdometerEntries()
                        } else {
                            viewModel.clearAllOdometerEntries()
                        }
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(confirmBtnText, color = Color.White, fontWeight = FontWeight.Bold)
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
            onSave = { updatedTitle, totStart, totEnd, hStart, hEnd, liters, location, batStartPct, batMaxPct, rechCount, rechLocs, rechPcts, rechInitPcts, rechPrices ->
                viewModel.updateOdometerEntry(
                    id = entryToEdit.id,
                    title = updatedTitle,
                    totalStartKm = totStart,
                    totalEndKm = totEnd,
                    hevStartKm = hStart,
                    hevEndKm = hEnd,
                    fuelLiters = liters,
                    chargingLocation = location,
                    batteryStartPercent = batStartPct,
                    batteryMaxPercent = batMaxPct,
                    rechargeCount = rechCount,
                    rechargeLocations = rechLocs,
                    rechargeBatteryPercents = rechPcts,
                    rechargeInitialBatteryPercents = rechInitPcts,
                    rechargePrices = rechPrices
                )
                editingEntry = null
            }
        )
    }

    // DIALOG DE AJUSTE RÁPIDO DE TARIFA DA RECARGA
    editingRechargePriceIndex?.let { priceIdx ->
        val isSingle = state.odometerRechargeCount <= 1
        val loc = if (isSingle) {
            if (state.odometerUseHomeTariff) ChargingLocation.HOME else ChargingLocation.STATION
        } else {
            state.odometerEffectiveRechargeLocations.getOrElse(priceIdx) { ChargingLocation.HOME }
        }
        val isHome = loc == ChargingLocation.HOME
        val defaultPrice = if (isHome) state.homeEnergyPrice else state.publicEnergyPrice
        val currentPrice = state.odometerEffectiveRechargePrices.getOrElse(priceIdx) { defaultPrice }

        RechargeTariffDialog(
            rechargeIndex = priceIdx,
            isSingle = isSingle,
            currentPrice = currentPrice,
            defaultPrice = defaultPrice,
            isHome = isHome,
            homeEnergyPrice = state.homeEnergyPrice,
            publicEnergyPrice = state.publicEnergyPrice,
            strings = strings,
            onDismiss = { editingRechargePriceIndex = null },
            onPriceChange = { newPrice ->
                viewModel.updateOdometerRechargePrice(priceIdx, newPrice)
            },
            onReset = {
                viewModel.resetOdometerRechargePrice(priceIdx)
                editingRechargePriceIndex = null
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
                if (state.currentVehicleOdometerEntries.isNotEmpty()) {
                    TextButton(onClick = {
                        showClearConfirmDialog = true
                    }) {
                        Text(strings.odometerClearHistoryBtn, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = CyanText)
                    Text(strings.odometerHistorySectionTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (state.currentVehicleOdometerEntries.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${state.currentVehicleOdometerEntries.size}",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            viewModel.openOdometerHistoryDialog(false)
                            showCsvDialog = true
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_history_csv")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = strings.exportSpreadsheetBtn,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                if (state.currentVehicleOdometerEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val emptyMsg = if (state.odometerFilterVehicleOnly) {
                            if (language == AppLanguage.EN_US) {
                                "No trips recorded yet for ${state.selectedVehicle.name}."
                            } else {
                                "Nenhuma medição salva ainda para ${state.selectedVehicle.name}."
                            }
                        } else {
                            strings.odometerNoEntriesText
                        }
                        Text(
                            text = emptyMsg,
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
                        state.currentVehicleOdometerEntries.forEach { entry ->
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
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // DIALOG DE PLANILHA CSV (EXPORTAÇÃO E IMPORTAÇÃO)
    if (showCsvDialog) {
        CsvDataDialog(
            entries = state.odometerEntries,
            currentVehicle = state.selectedVehicle,
            allVehicles = state.vehiclesList,
            language = language,
            strings = strings,
            onDismiss = { showCsvDialog = false },
            onImportTrips = { importedTrips, replaceAll ->
                viewModel.importOdometerEntries(importedTrips, replaceAll)
            }
        )
    }

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

            // Box Destaque: Condicional ao tipo de veículo
            when (entry.vehicleType) {
                VehicleType.BEV -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1035)),
                        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B0764)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.odometerPureEvBannerTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC084FC),
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = strings.odometerPureEvBannerSubtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 10.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                VehicleType.HEV -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261505)),
                        border = BorderStroke(1.dp, Color(0xFFD97706))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Linha 1: Texto consumo médio HEV
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

                            // Linha 2: Valor do consumo
                            Text(
                                text = if (entry.realGlobalKmL > 0) "${formatNumber(entry.realGlobalKmL, 2)} km/L"
                                else if (entry.realHevKmL > 0) "${formatNumber(entry.realHevKmL, 2)} km/L" else "--",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24),
                                    fontSize = 18.sp
                                ),
                                maxLines = 1,
                                softWrap = false
                            )

                            // Na sequência: Outros textos
                            Text(
                                text = "${formatNumber(entry.fuelLiters, 2)} L ${strings.odometerCombustionModeDetail}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFDE68A),
                                    fontSize = 10.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                VehicleType.PHEV -> {
                    if (entry.hevKm <= 0.0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1035)),
                            border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.8f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B0764)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.odometerPureEvBannerTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC084FC),
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = strings.odometerPureEvBannerSubtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFE9D5FF),
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (entry.isPureEvWithExcess) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF261208),
                                border = BorderStroke(1.dp, Color(0xFFF97316)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFFDBA74),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Km EV (${formatNumber(entry.evKm, 1)}) excede recargas (${formatNumber(entry.maxRechargeEvKm, 1)} km) • Sem custo de combustível lançado",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFED7AA),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.weight(1f),
                                        softWrap = true
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF261505)),
                            border = BorderStroke(1.dp, Color(0xFFD97706))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Linha 1: Texto consumo médio HEV
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

                                // Linha 2: Valor do consumo
                                Text(
                                    text = if (entry.realHevKmL > 0) "${formatNumber(entry.realHevKmL, 2)} km/L" else "--",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFBBF24),
                                        fontSize = 18.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )

                                // Na sequência: Outros textos
                                if (entry.excessEvKm > 0) {
                                    Text(
                                        text = "${formatNumber(entry.effectiveFuelKm, 1)} km a combustível ÷ Litros",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFDE68A),
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatNumber(entry.hevKm, 1)} km HEV + ${formatNumber(entry.excessEvKm, 1)} km EV reg.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 9.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
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
                            }
                        }

                        // Caixa de Destaque: CONSUMO MÉDIO GLOBAL (Abaixo do Consumo Médio HEV)
                        if (entry.realGlobalKmL > 0.0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("history_item_global_consumption_${entry.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF042018)),
                                border = BorderStroke(1.dp, Color(0xFF059669))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    // Linha 1: Texto consumo médio GLOBAL
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (language == AppLanguage.EN_US) "GLOBAL AVERAGE CONSUMPTION" else "CONSUMO MÉDIO GLOBAL",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF34D399),
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Linha 2: Valor do consumo
                                    Text(
                                        text = "${formatNumber(entry.realGlobalKmL, 2)} km/L",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399),
                                            fontSize = 18.sp
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )

                                    // Na sequência: Outros textos
                                    val displayFuel = if (entry.fuelLiters > 0.0) entry.fuelLiters else entry.totalGasolineLiters
                                    Text(
                                        text = "${formatNumber(entry.totalKm, 1)} km (EV + HEV) ÷ ${formatNumber(displayFuel, 2)} L",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFA7F3D0),
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Odômetros e Consumo Alinhados
            val evStartKm = (entry.totalStartKm - entry.hevStartKm).coerceAtLeast(0.0)
            val evEndKm = (entry.totalEndKm - entry.hevEndKm).coerceAtLeast(0.0)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Total (sempre presente)
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

                // HEV: Exibe apenas para PHEV para evitar redundância em HEV (onde total já é HEV) e BEV
                if (entry.vehicleType == VehicleType.PHEV) {
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
                        val hevText = if (entry.hevEndKm > 0.0 || entry.hevStartKm > 0.0) {
                            "${entry.hevStartKm.toInt()} ➔ ${entry.hevEndKm.toInt()} km (+${formatNumber(entry.hevKm, 1)} km)"
                        } else {
                            "${formatNumber(entry.hevKm, 1)} km"
                        }
                        Text(
                            text = hevText,
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
                }

                // Combustível (Litros): Exibido abaixo do HEV (apenas para PHEV e HEV)
                if (entry.vehicleType != VehicleType.BEV) {
                    val displayLiters = if (entry.fuelLiters > 0.0) entry.fuelLiters else entry.totalGasolineLiters
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
                            text = "${formatNumber(displayLiters, 2)} L",
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

                // HEV + EV Reg. (Combustão Efetiva): Exibe para PHEV quando há regeneração sustentada pelo motor
                if (entry.vehicleType == VehicleType.PHEV && entry.excessEvKm > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HEV + EV Reg.:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${formatNumber(entry.effectiveFuelKm, 1)} km (${formatNumber(entry.hevKm, 1)} HEV + ${formatNumber(entry.excessEvKm, 1)} reg.)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp
                            ),
                            maxLines = 3,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                // EV (Calc): Exibe apenas para PHEV para evitar redundância em BEV (onde total já é EV) e HEV
                if (entry.vehicleType == VehicleType.PHEV) {
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
                        val evText = if (evEndKm > 0.0 || evStartKm > 0.0) {
                            "${evStartKm.toInt()} ➔ ${evEndKm.toInt()} km (+${formatNumber(entry.evKm, 1)} km)"
                        } else {
                            "${formatNumber(entry.evKm, 1)} km"
                        }
                        Text(
                            text = evText,
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

                    // EV Puro (Tomada/Recarga): Exibe quando parte do EV foi regenerado
                    if (entry.excessEvKm > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN_US) "EV Pure (Plug):" else "EV Puro (Tomada):",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFA78BFA).copy(alpha = 0.85f),
                                    fontSize = 11.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${formatNumber(entry.rechargeEvKm, 1)} km",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFDDD6FE),
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
                }

                // Energia (kWh): Exibido abaixo do EV (para PHEV e BEV)
                if (entry.vehicleType != VehicleType.HEV) {
                    val displayEnergyKwh = if (entry.totalEnergyKwh > 0.0) {
                        entry.totalEnergyKwh
                    } else if (entry.evKm > 0.0 && entry.electricConsumptionKwh100km > 0.0) {
                        entry.evKm * entry.electricConsumptionKwh100km / 100.0
                    } else {
                        0.0
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.odometerHistoryEnergy,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${formatNumber(displayEnergyKwh, 2)} kWh",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF38BDF8),
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

                    // Linha Combustível (Apenas para veículos com combustão: HEV e PHEV)
                    if (entry.vehicleType != VehicleType.BEV) {
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
                    }

                    // Linha Elétrico (Apenas para veículos elétricos/híbridos plug-in: BEV e PHEV)
                    if (entry.vehicleType != VehicleType.HEV) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isNoRecharge = entry.chargingLocation == ChargingLocation.NONE || entry.usableBatteryPercent <= 0.0 || entry.batteryStartPercent <= 25.0
                            Text(
                                text = strings.odometerElectricLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isNoRecharge) Color(0xFFFECACA) else Color(0xFFDDD6FE),
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "R$ ${formatNumber(entry.electricCost, 2)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isNoRecharge) Color(0xFFFCA5A5) else Color(0xFFC084FC),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Barra Bicolor EV e HEV
            when (entry.vehicleType) {
                VehicleType.BEV -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFA855F7))
                    )
                }
                VehicleType.HEV -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFF97316))
                    )
                }
                VehicleType.PHEV -> {
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
                }
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
        chargingLocation: ChargingLocation,
        batteryStartPercent: Double,
        batteryMaxPercent: Double,
        rechargeCount: Int,
        rechargeLocations: List<ChargingLocation>,
        rechargeBatteryPercents: List<Double>,
        rechargeInitialBatteryPercents: List<Double>,
        rechargePrices: List<Double>
    ) -> Unit
) {
    val focusManager = LocalFocusManager.current
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
    var rechargeCountText by remember {
        mutableStateOf(entry.rechargeCount.coerceAtLeast(1).toString())
    }
    var selectedLocation by remember { mutableStateOf(if (entry.chargingLocation == ChargingLocation.NONE) ChargingLocation.HOME else entry.chargingLocation) }
    var rechargeLocations by remember {
        mutableStateOf(entry.effectiveRechargeLocations)
    }
    var batteryStartPercent by remember {
        mutableStateOf(if (entry.batteryStartPercent > 0) entry.batteryStartPercent else 100.0)
    }
    var rechargeBatteryPercents by remember {
        mutableStateOf(entry.effectiveRechargeBatteryPercents)
    }
    var rechargeInitialBatteryPercents by remember {
        mutableStateOf(entry.effectiveRechargeInitialBatteryPercents)
    }
    var rechargePrices by remember {
        mutableStateOf(entry.effectiveRechargePrices)
    }

    val curTotalStart = totalStartText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curTotalEnd = totalEndText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curHevStart = hevStartText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curHevEnd = hevEndText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curFuelLiters = fuelLitersText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val curRechargeCount = rechargeCountText.toIntOrNull()?.coerceAtLeast(1) ?: 1

    val effectiveLocations = remember(rechargeLocations, curRechargeCount, selectedLocation) {
        val list = rechargeLocations.toMutableList()
        while (list.size < curRechargeCount) {
            list.add(list.lastOrNull() ?: selectedLocation)
        }
        list.take(curRechargeCount)
    }

    val effectiveBatteryPercents = remember(rechargeBatteryPercents, curRechargeCount) {
        val list = rechargeBatteryPercents.toMutableList()
        while (list.size < curRechargeCount) {
            list.add(100.0)
        }
        list.take(curRechargeCount)
    }

    val effectiveInitialBatteryPercents = remember(rechargeInitialBatteryPercents, curRechargeCount) {
        val list = rechargeInitialBatteryPercents.toMutableList()
        while (list.size < curRechargeCount) {
            list.add(list.lastOrNull() ?: 25.0)
        }
        list.take(curRechargeCount)
    }

    val effectivePrices = remember(rechargePrices, curRechargeCount, effectiveLocations, homeEnergyPrice, publicEnergyPrice, selectedLocation) {
        val list = rechargePrices.toMutableList()
        while (list.size < curRechargeCount) {
            val loc = effectiveLocations.getOrElse(list.size) { selectedLocation }
            val defPrice = if (loc == ChargingLocation.HOME) homeEnergyPrice else publicEnergyPrice
            list.add(defPrice)
        }
        list.take(curRechargeCount)
    }

    val calcDeltaTotal = (curTotalEnd - curTotalStart).coerceAtLeast(0.0)
    val calcDeltaHev = (curHevEnd - curHevStart).coerceAtLeast(0.0)
    val calcDeltaEv = (calcDeltaTotal - calcDeltaHev).coerceAtLeast(0.0)

    val batteryCap = if (entry.batteryCapacityKwh > 0) entry.batteryCapacityKwh else 18.3
    val elecCons = if (entry.electricConsumptionKwh100km > 0) entry.electricConsumptionKwh100km else 15.0

    val reserveP = if (entry.vehicleType == VehicleType.PHEV) 25.0 else 0.0
    val activeIndices = (0 until curRechargeCount).filter { effectiveLocations.getOrElse(it) { selectedLocation } != ChargingLocation.NONE }
    var totalUsableKwh = 0.0
    for (idx in activeIndices.indices) {
        val i = activeIndices[idx]
        val pFinal = effectiveBatteryPercents.getOrElse(i) { 100.0 }
        val usableP = if (idx == activeIndices.size - 1) {
            (pFinal - reserveP).coerceIn(0.0, 100.0)
        } else {
            val nextI = activeIndices[idx + 1]
            val nextInit = effectiveInitialBatteryPercents.getOrElse(nextI) { 0.0 }
            val deltaToNext = (pFinal - nextInit).coerceAtLeast(0.0)
            val leg0 = if (idx == 0) {
                val pInit = effectiveInitialBatteryPercents.getOrElse(i) { 25.0 }
                if (pFinal < 100.0) {
                    (100.0 - pFinal).coerceAtLeast(0.0)
                } else if (pInit > reserveP) {
                    (100.0 - pInit).coerceAtLeast(0.0)
                } else {
                    0.0
                }
            } else {
                0.0
            }
            leg0 + deltaToNext
        }
        totalUsableKwh += (batteryCap * (usableP / 100.0))
    }

    val maxRechargeEvKm = if (totalUsableKwh > 0 && elecCons > 0) {
        (totalUsableKwh / elecCons) * 100.0
    } else 0.0
    val calcRechargeEvKm = calcDeltaEv.coerceAtMost(maxRechargeEvKm)
    val calcExcessEvKm = (calcDeltaEv - calcRechargeEvKm).coerceAtLeast(0.0)
    val calcEffectiveFuelKm = if (calcDeltaHev <= 0.0) 0.0 else calcDeltaHev + calcExcessEvKm

    val calcAvgHevKmL = if (curFuelLiters > 0 && calcEffectiveFuelKm > 0) calcEffectiveFuelKm / curFuelLiters else 0.0
    val calcAvgGlobalKmL = if (curFuelLiters > 0 && calcDeltaTotal > 0) calcDeltaTotal / curFuelLiters else 0.0

    val effectiveTariff = if (effectivePrices.isNotEmpty()) {
        effectivePrices.average()
    } else homeEnergyPrice

    val calcElectricKwh = if (totalUsableKwh <= 0.0 || activeIndices.isEmpty()) 0.0 else (calcRechargeEvKm * elecCons / 100.0).coerceAtMost(totalUsableKwh)
    val calcElectricCost = if (totalUsableKwh <= 0.0 || activeIndices.isEmpty()) {
        0.0
    } else {
        var remainingKwh = calcElectricKwh
        var cost = 0.0
        for (idx in activeIndices.indices) {
            if (remainingKwh <= 0.0) break
            val i = activeIndices[idx]
            val loc = effectiveLocations[i]
            val pFinal = effectiveBatteryPercents.getOrElse(i) { 100.0 }
            val usableP = if (idx == activeIndices.size - 1) {
                (pFinal - reserveP).coerceIn(0.0, 100.0)
            } else {
                val nextI = activeIndices[idx + 1]
                val nextInit = effectiveInitialBatteryPercents.getOrElse(nextI) { 0.0 }
                val deltaToNext = (pFinal - nextInit).coerceAtLeast(0.0)
                val leg0 = if (idx == 0) {
                    val pInit = effectiveInitialBatteryPercents.getOrElse(i) { 25.0 }
                    if (pFinal < 100.0) {
                        (100.0 - pFinal).coerceAtLeast(0.0)
                    } else if (pInit > reserveP) {
                        (100.0 - pInit).coerceAtLeast(0.0)
                    } else {
                        0.0
                    }
                } else {
                    0.0
                }
                leg0 + deltaToNext
            }
            val kwhCapThis = if (batteryCap > 0) batteryCap * (usableP / 100.0) else 0.0
            val kwh = if (kwhCapThis > 0) remainingKwh.coerceAtMost(kwhCapThis) else remainingKwh
            val tariff = if (loc == ChargingLocation.NONE) 0.0 else effectivePrices.getOrElse(i) {
                if (loc == ChargingLocation.HOME) homeEnergyPrice else publicEnergyPrice
            }
            cost += kwh * tariff
            remainingKwh -= kwh
        }
        if (remainingKwh > 0.0) {
            cost += remainingKwh * homeEnergyPrice
        }
        cost
    }
    val effGasCons = if (entry.gasolineConsumptionKmL > 0) entry.gasolineConsumptionKmL else 16.0
    val calcGasLiters = if (curFuelLiters > 0) curFuelLiters else if (calcDeltaHev <= 0.0) 0.0 else if (effGasCons > 0 && calcEffectiveFuelKm > 0) calcEffectiveFuelKm / effGasCons else 0.0
    val calcGasCost = if (entry.vehicleType == VehicleType.BEV) 0.0 else if (calcDeltaHev <= 0.0 && curFuelLiters <= 0.0) 0.0 else calcGasLiters * entry.gasPriceLiter
    val calcTotalCost = when (entry.vehicleType) {
        VehicleType.BEV -> calcElectricCost
        VehicleType.HEV -> calcGasCost
        VehicleType.PHEV -> calcElectricCost + calcGasCost
    }

    val totalUsableBatteryPercent = if (batteryCap > 0.0) (totalUsableKwh / batteryCap) * 100.0 else 0.0
    val usableBatteryPercent = if (curRechargeCount > 1) totalUsableBatteryPercent else ((effectiveBatteryPercents.firstOrNull() ?: 100.0) - reserveP).coerceIn(0.0, 100.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val finalHevStart = when (entry.vehicleType) {
                        VehicleType.BEV -> 0.0
                        VehicleType.HEV -> curTotalStart
                        VehicleType.PHEV -> curHevStart
                    }
                    val finalHevEnd = when (entry.vehicleType) {
                        VehicleType.BEV -> 0.0
                        VehicleType.HEV -> curTotalEnd
                        VehicleType.PHEV -> curHevEnd
                    }
                    val finalFuelLiters = if (entry.vehicleType == VehicleType.BEV) 0.0 else curFuelLiters
                    val finalRechargeCount = if (entry.vehicleType == VehicleType.HEV) 0 else curRechargeCount
                    val finalLocation = if (entry.vehicleType == VehicleType.HEV) ChargingLocation.NONE else selectedLocation
                    onSave(
                        titleText.trim(),
                        curTotalStart,
                        curTotalEnd,
                        finalHevStart,
                        finalHevEnd,
                        finalFuelLiters,
                        finalLocation,
                        effectiveBatteryPercents.firstOrNull() ?: 100.0,
                        usableBatteryPercent,
                        finalRechargeCount,
                        effectiveLocations,
                        effectiveBatteryPercents,
                        effectiveInitialBatteryPercents,
                        effectivePrices
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

                // Odômetro HEV (Apenas PHEV)
                if (entry.vehicleType == VehicleType.PHEV) {
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
                }

                // Combustível (Apenas HEV e PHEV)
                if (entry.vehicleType != VehicleType.BEV) {
                    OutlinedTextField(
                        value = fuelLitersText,
                        onValueChange = { fuelLitersText = it },
                        label = { Text(strings.odometerFuelUsedLabel, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Número de Recargas (Apenas BEV e PHEV)
                if (entry.vehicleType != VehicleType.HEV) {
                    OutlinedTextField(
                    value = rechargeCountText,
                    onValueChange = { str ->
                        val cleanDigits = str.filter { it.isDigit() }
                        rechargeCountText = cleanDigits
                    },
                    label = { Text(strings.odometerRechargeCountLabel, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // SELETOR DO LOCAL DAS RECARGAS (CASA vs POSTO)
                if (curRechargeCount == 1) {
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isHome = effectiveLocations.firstOrNull() == ChargingLocation.HOME
                            Surface(
                                onClick = {
                                    selectedLocation = ChargingLocation.HOME
                                    rechargeLocations = listOf(ChargingLocation.HOME)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isHome) Color(0xFF1E1B4B) else Color(0xFF070A14),
                                border = BorderStroke(1.2.dp, if (isHome) Color(0xFF818CF8) else BgCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = if (homeEnergyPrice <= 0.0) "R$ 0,00" else "R$ ${formatNumber(homeEnergyPrice, 2)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isHome) Color(0xFFC7D2FE) else Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            val isStation = effectiveLocations.firstOrNull() == ChargingLocation.STATION
                            Surface(
                                onClick = {
                                    selectedLocation = ChargingLocation.STATION
                                    rechargeLocations = listOf(ChargingLocation.STATION)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isStation) Color(0xFF082F49) else Color(0xFF070A14),
                                border = BorderStroke(1.2.dp, if (isStation) Color(0xFF38BDF8) else BgCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = "R$ ${formatNumber(publicEnergyPrice, 2)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isStation) Color(0xFFBAE6FD) else Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            val isNoneLoc = effectiveLocations.firstOrNull() == ChargingLocation.NONE
                            Surface(
                                onClick = {
                                    selectedLocation = ChargingLocation.NONE
                                    rechargeLocations = listOf(ChargingLocation.NONE)
                                    rechargeInitialBatteryPercents = listOf(25.0)
                                    rechargeBatteryPercents = listOf(25.0)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isNoneLoc) Color(0xFF372007) else Color(0xFF070A14),
                                border = BorderStroke(1.2.dp, if (isNoneLoc) Color(0xFFF59E0B) else BgCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = null,
                                            tint = if (isNoneLoc) Color(0xFFFBBF24) else Color(0xFF64748B),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = strings.odometerNoRechargeOption.substringBefore(" ("),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isNoneLoc) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isNoneLoc) Color.White else Color(0xFF94A3B8),
                                                fontSize = 11.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = "R$ 0,00",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isNoneLoc) Color(0xFFFDE68A) else Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Linha de baixo dos botões em casa/no posto: Nível inicial e Nível final da bateria (%)
                        val singleInitialPercent = effectiveInitialBatteryPercents.firstOrNull() ?: 25.0
                        val singleFinalPercent = effectiveBatteryPercents.firstOrNull() ?: 100.0

                        if (effectiveLocations.firstOrNull() == ChargingLocation.NONE) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E170A),
                                border = BorderStroke(1.dp, Color(0xFF78350F))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = strings.odometerNoRechargeExplanation,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFDE68A),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    )
                                }
                            }
                        } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF070B14),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Entrada 1: Nível inicial (padrão 25%)
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
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = strings.odometerRechargeInitialLevelLabel,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    EditableBatteryPercentControl(
                                        value = singleInitialPercent,
                                        onValueChange = { newVal ->
                                            val current = effectiveInitialBatteryPercents.toMutableList()
                                            if (current.isEmpty()) current.add(newVal) else current[0] = newVal
                                            rechargeInitialBatteryPercents = current
                                        },
                                        onStep = { delta ->
                                            val current = effectiveInitialBatteryPercents.toMutableList()
                                            val newVal = ((current.firstOrNull() ?: 25.0) + delta).coerceIn(0.0, 100.0)
                                            if (current.isEmpty()) current.add(newVal) else current[0] = newVal
                                            rechargeInitialBatteryPercents = current
                                        },
                                        compact = true,
                                        testTagPrefix = "dialog_single_initial_battery"
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xFF1E293B),
                                    thickness = 0.8.dp
                                )

                                // Entrada 2: Nível final (padrão 100%)
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
                                            tint = if (singleFinalPercent <= 25.0) Color(0xFFF87171) else Color(0xFFA78BFA),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = strings.odometerRechargeFinalLevelLabel,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    EditableBatteryPercentControl(
                                        value = singleFinalPercent,
                                        onValueChange = { newVal ->
                                            val current = effectiveBatteryPercents.toMutableList()
                                            if (current.isEmpty()) current.add(newVal) else current[0] = newVal
                                            rechargeBatteryPercents = current
                                            batteryStartPercent = newVal
                                        },
                                        onStep = { delta ->
                                            val current = effectiveBatteryPercents.toMutableList()
                                            val newVal = ((current.firstOrNull() ?: 100.0) + delta).coerceIn(0.0, 100.0)
                                            if (current.isEmpty()) current.add(newVal) else current[0] = newVal
                                            rechargeBatteryPercents = current
                                            batteryStartPercent = newVal
                                        },
                                        compact = true,
                                        testTagPrefix = "dialog_single_final_battery"
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xFF1E293B),
                                    thickness = 0.8.dp
                                )

                                val singleLoc = effectiveLocations.firstOrNull() ?: ChargingLocation.HOME
                                val singleDefaultPrice = if (singleLoc == ChargingLocation.HOME) homeEnergyPrice else publicEnergyPrice
                                val singlePrice = effectivePrices.firstOrNull() ?: singleDefaultPrice
                                val singleIsCustomPrice = Math.abs(singlePrice - singleDefaultPrice) > 0.001

                                // Entrada 3: Tarifa da recarga
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = null,
                                            tint = if (singleLoc == ChargingLocation.HOME) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Column {
                                            Text(
                                                text = strings.odometerRechargeTariffLabel,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (singleIsCustomPrice) {
                                                Text(
                                                    text = "${strings.odometerRechargeTariffCustomBadge} (pad: R$ ${formatNumber(singleDefaultPrice, 2)})",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Normal
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    EditablePriceControl(
                                        value = singlePrice,
                                        onValueChange = { newVal ->
                                            val current = effectivePrices.toMutableList()
                                            if (current.isEmpty()) current.add(newVal) else current[0] = newVal
                                            rechargePrices = current
                                        },
                                        onStep = { delta ->
                                            val current = effectivePrices.toMutableList()
                                            val cur = current.firstOrNull() ?: singleDefaultPrice
                                            val newVal = (cur + delta).coerceAtLeast(0.0)
                                            if (current.isEmpty()) current.add(newVal) else current[0] = newVal
                                            rechargePrices = current
                                        },
                                        compact = true,
                                        testTagPrefix = "dialog_single_price"
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xFF1E293B),
                                    thickness = 0.8.dp
                                )

                                val singleDeltaP = (singleFinalPercent - singleInitialPercent).coerceAtLeast(0.0)
                                val singleKwh = batteryCap * (singleDeltaP / 100.0)
                                val singleCost = singleKwh * singlePrice

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .testTag("dialog_single_total_value"),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${strings.odometerRechargeTotalValueLabel} ",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "R$ ${formatNumber(singleCost, 2)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (singleLoc == ChargingLocation.HOME) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        }
                    }
                } else {
                    // Múltiplas Recargas no Dialog de Edição
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Título da seção em sua própria linha
                        Text(
                            text = strings.odometerRechargeLocationsHeader,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = VoltageOnSurface,
                                fontSize = 11.5.sp
                            )
                        )

                        // Botões de atalho rápido centralizados em outra linha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Todas em Casa
                            Surface(
                                onClick = {
                                    selectedLocation = ChargingLocation.HOME
                                    rechargeLocations = List(curRechargeCount) { ChargingLocation.HOME }
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E1B4B),
                                border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = Color(0xFFA5B4FC),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = strings.odometerAllAtHome,
                                        color = Color(0xFFE0E7FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Todas no Posto
                            Surface(
                                onClick = {
                                    selectedLocation = ChargingLocation.STATION
                                    rechargeLocations = List(curRechargeCount) { ChargingLocation.STATION }
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF082F49),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EvStation,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = strings.odometerAllAtStation,
                                        color = Color(0xFFE0F2FE),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Lista para cada recarga
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (index in 0 until curRechargeCount) {
                                val loc = effectiveLocations.getOrElse(index) { ChargingLocation.HOME }
                                val isHomeLoc = loc == ChargingLocation.HOME
                                val isStationLoc = loc == ChargingLocation.STATION

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0A0F1D),
                                    border = BorderStroke(1.dp, if (isHomeLoc) Color(0xFF312E81) else Color(0xFF0C4A6E))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = String.format(strings.odometerRechargeNumberPrefix, index + 1),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = VoltageOnSurface,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                            // Valor em reais no canto superior direito
                                            val defaultLocPrice = if (isHomeLoc) homeEnergyPrice else publicEnergyPrice
                                            val curLocPrice = effectivePrices.getOrElse(index) { defaultLocPrice }
                                            val isCustomPrice = Math.abs(curLocPrice - defaultLocPrice) > 0.001
                                            Text(
                                                text = if (curLocPrice <= 0.0) "R$ 0,00/kWh (${if (isHomeLoc) "Solar" else "Grátis"})" else "R$ ${formatNumber(curLocPrice, 2)}/kWh",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isCustomPrice) Color(0xFF38BDF8) else (if (isHomeLoc) Color(0xFFA5B4FC) else Color(0xFF38BDF8)),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Casa
                                            Surface(
                                                onClick = {
                                                    val updated = effectiveLocations.toMutableList()
                                                    updated[index] = ChargingLocation.HOME
                                                    rechargeLocations = updated
                                                    if (index == 0) selectedLocation = ChargingLocation.HOME
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isHomeLoc) Color(0xFF1E1B4B) else Color(0xFF050811),
                                                border = BorderStroke(1.dp, if (isHomeLoc) Color(0xFF818CF8) else Color(0xFF1E293B))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Home,
                                                        contentDescription = null,
                                                        tint = if (isHomeLoc) Color(0xFFA5B4FC) else Color(0xFF64748B),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = strings.odometerAtHomeOption,
                                                        color = if (isHomeLoc) Color.White else Color(0xFF94A3B8),
                                                        fontSize = 10.5.sp,
                                                        fontWeight = if (isHomeLoc) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }

                                            // Posto
                                            Surface(
                                                onClick = {
                                                    val updated = effectiveLocations.toMutableList()
                                                    updated[index] = ChargingLocation.STATION
                                                    rechargeLocations = updated
                                                    if (index == 0) selectedLocation = ChargingLocation.STATION
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isStationLoc) Color(0xFF082F49) else Color(0xFF050811),
                                                border = BorderStroke(1.dp, if (isStationLoc) Color(0xFF38BDF8) else Color(0xFF1E293B))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.EvStation,
                                                        contentDescription = null,
                                                        tint = if (isStationLoc) Color(0xFF38BDF8) else Color(0xFF64748B),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = strings.odometerAtStationOption,
                                                        color = if (isStationLoc) Color.White else Color(0xFF94A3B8),
                                                        fontSize = 10.5.sp,
                                                        fontWeight = if (isStationLoc) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }

                                        // Linha 3: Nível inicial e Nível final nesta recarga
                                        val curInitialPercent = effectiveInitialBatteryPercents.getOrElse(index) { 25.0 }
                                        val curFinalPercent = effectiveBatteryPercents.getOrElse(index) { 100.0 }
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF070B14),
                                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                // Entrada 1: Nível inicial
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
                                                            imageVector = Icons.Default.Bolt,
                                                            contentDescription = null,
                                                            tint = Color(0xFF38BDF8),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Text(
                                                            text = strings.odometerRechargeInitialLevelLabel,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = Color(0xFF94A3B8),
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        )
                                                    }

                                                    EditableBatteryPercentControl(
                                                        value = curInitialPercent,
                                                        onValueChange = { newVal ->
                                                            val current = effectiveInitialBatteryPercents.toMutableList()
                                                            current[index] = newVal
                                                            rechargeInitialBatteryPercents = current
                                                        },
                                                        onStep = { delta ->
                                                            val current = effectiveInitialBatteryPercents.toMutableList()
                                                            val newVal = (curInitialPercent + delta).coerceIn(0.0, 100.0)
                                                            current[index] = newVal
                                                            rechargeInitialBatteryPercents = current
                                                        },
                                                        compact = true,
                                                        testTagPrefix = "dialog_recharge_${index}_initial_battery"
                                                    )
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
                                                    thickness = 0.8.dp
                                                )

                                                // Entrada 2: Nível final
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
                                                            imageVector = Icons.Default.Bolt,
                                                            contentDescription = null,
                                                            tint = if (curFinalPercent <= 25.0) Color(0xFFF87171) else Color(0xFFA78BFA),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Text(
                                                            text = strings.odometerRechargeFinalLevelLabel,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = Color(0xFF94A3B8),
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        )
                                                    }

                                                    EditableBatteryPercentControl(
                                                        value = curFinalPercent,
                                                        onValueChange = { newVal ->
                                                            val current = effectiveBatteryPercents.toMutableList()
                                                            current[index] = newVal
                                                            rechargeBatteryPercents = current
                                                            if (index == 0) batteryStartPercent = newVal
                                                        },
                                                        onStep = { delta ->
                                                            val current = effectiveBatteryPercents.toMutableList()
                                                            val newVal = (curFinalPercent + delta).coerceIn(0.0, 100.0)
                                                            current[index] = newVal
                                                            rechargeBatteryPercents = current
                                                            if (index == 0) batteryStartPercent = newVal
                                                        },
                                                        compact = true,
                                                        testTagPrefix = "dialog_recharge_${index}_final_battery"
                                                    )
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
                                                    thickness = 0.8.dp
                                                )

                                                val curLoc = effectiveLocations.getOrElse(index) { ChargingLocation.HOME }
                                                // Entrada 3: Tarifa desta recarga (R$/kWh)
                                                val defaultPriceThis = if (curLoc == ChargingLocation.HOME) homeEnergyPrice else publicEnergyPrice
                                                val curPriceThis = effectivePrices.getOrElse(index) { defaultPriceThis }
                                                val isCustomPriceThis = Math.abs(curPriceThis - defaultPriceThis) > 0.001

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ElectricBolt,
                                                            contentDescription = null,
                                                            tint = if (curLoc == ChargingLocation.HOME) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Column {
                                                            Text(
                                                                text = strings.odometerRechargeTariffLabel,
                                                                style = MaterialTheme.typography.bodySmall.copy(
                                                                    color = Color(0xFF94A3B8),
                                                                    fontSize = 10.5.sp,
                                                                    fontWeight = FontWeight.Medium
                                                                ),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (isCustomPriceThis) {
                                                                Text(
                                                                    text = "${strings.odometerRechargeTariffCustomBadge} (pad: R$ ${formatNumber(defaultPriceThis, 2)})",
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        color = Color(0xFF38BDF8),
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Normal
                                                                    ),
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }

                                                    EditablePriceControl(
                                                        value = curPriceThis,
                                                        onValueChange = { newVal ->
                                                            val current = effectivePrices.toMutableList()
                                                            current[index] = newVal
                                                            rechargePrices = current
                                                        },
                                                        onStep = { delta ->
                                                            val current = effectivePrices.toMutableList()
                                                            val newVal = (current[index] + delta).coerceAtLeast(0.0)
                                                            current[index] = newVal
                                                            rechargePrices = current
                                                        },
                                                        compact = true,
                                                        testTagPrefix = "dialog_recharge_${index}_price"
                                                    )
                                                }

                                                HorizontalDivider(
                                                    color = Color(0xFF1E293B),
                                                    thickness = 0.8.dp
                                                )

                                                val curDeltaP = (curFinalPercent - curInitialPercent).coerceAtLeast(0.0)
                                                val curKwh = batteryCap * (curDeltaP / 100.0)
                                                val curCost = curKwh * curPriceThis

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp)
                                                        .testTag("dialog_recharge_${index}_total_value"),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${strings.odometerRechargeTotalValueLabel} ",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = Color(0xFF94A3B8),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    )
                                                    Text(
                                                        text = "R$ ${formatNumber(curCost, 2)}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = if (curLoc == ChargingLocation.HOME) Color(0xFFA5B4FC) else Color(0xFF38BDF8),
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Resumo ponderado no Dialog
                        val homeCount = effectiveLocations.count { it == ChargingLocation.HOME }
                        val stationCount = effectiveLocations.count { it == ChargingLocation.STATION }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡 ${homeCount}x Casa • ${stationCount}x Posto",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "Tarifa média: R$ ${formatNumber(effectiveTariff, 2)}/kWh",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
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

                        // 2. Delta HEV (Apenas PHEV)
                        if (entry.vehicleType == VehicleType.PHEV) {
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
                        }

                        // 3. Delta EV (Apenas PHEV)
                        if (entry.vehicleType == VehicleType.PHEV) {
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
                        }

                        if (entry.vehicleType == VehicleType.PHEV) {
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
                        }

                        // 5. Consumo Global / Médio (Apenas HEV e PHEV)
                        if (entry.vehicleType != VehicleType.BEV) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (entry.vehicleType == VehicleType.HEV) strings.odometerHevConsumptionLabel else strings.odometerGlobalConsumptionLabel,
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

                        // 7. Combustível (Apenas HEV e PHEV)
                        if (entry.vehicleType != VehicleType.BEV) {
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
                        }

                        // 8. Elétrico (Apenas BEV e PHEV)
                        if (entry.vehicleType != VehicleType.HEV) {
                            val isAtEvLimit = totalUsableKwh <= 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val elecPreviewLabel = if (isAtEvLimit) {
                                    "${strings.odometerElectricLabel} (${strings.odometerBatteryStartEvLimitBtn}):"
                                } else {
                                    "${strings.odometerElectricLabel} (${formatNumber(calcElectricKwh, 1)} kWh):"
                                }
                                Text(
                                    text = elecPreviewLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isAtEvLimit) Color(0xFFFECACA) else VoltageOnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = "R$ ${formatNumber(calcElectricCost, 2)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isAtEvLimit) Color(0xFFFCA5A5) else Color(0xFFC084FC),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        if (entry.vehicleType == VehicleType.PHEV && calcDeltaTotal > 0 && calcDeltaHev <= 0.0 && calcExcessEvKm > 0.0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF261208),
                                border = BorderStroke(1.dp, Color(0xFFF97316)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFFDBA74),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Km EV (${formatNumber(calcDeltaEv, 1)}) excede recargas (${formatNumber(maxRechargeEvKm, 1)} km). Sem custo de combustível lançado.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFED7AA),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
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
    val rechargeInfoEn = if (state.odometerRechargeCount > 1) " • ${state.odometerRechargeCount} recharges" else ""
    val rechargeInfoPt = if (state.odometerRechargeCount > 1) " • ${state.odometerRechargeCount} recargas" else ""

    val isEn = state.language == AppLanguage.EN_US

    val consumptionSection = if (state.odometerDeltaHevKm <= 0.0) {
        if (isEn) {
            """
            ⚡ *Propulsion:*
            • Mode: *100% Electric (EV)* (Zero Fuel Used)
            """.trimIndent()
        } else {
            """
            ⚡ *Propulsão:*
            • Modo: *100% Elétrico (EV)* (Zero Combustível)
            """.trimIndent()
        }
    } else {
        if (isEn) {
            """
            ⛽ *Fuel Consumption:*
            • *AVERAGE HEV CONSUMPTION: ${if (state.odometerAverageHevKmL > 0) "$hevKmL km/L" else "--"}* ${if (state.odometerAverageHevL100km > 0) "($hevL100 L/100km)" else ""}
            • Fuel Used: *$liters Liters*
            • Global Consumption: ${if (state.odometerGlobalKmL > 0) "$globalKmL km/L" else "--"}
            """.trimIndent()
        } else {
            """
            ⛽ *Consumo de Combustível:*
            • *CONSUMO MÉDIO HEV: ${if (state.odometerAverageHevKmL > 0) "$hevKmL km/L" else "--"}* ${if (state.odometerAverageHevL100km > 0) "($hevL100 L/100km)" else ""}
            • Combustível Usado: *$liters Litros*
            • Consumo Global: ${if (state.odometerGlobalKmL > 0) "$globalKmL km/L" else "--"}
            """.trimIndent()
        }
    }

    return if (isEn) {
        """
        📊 *PHEV CONSUMPTION REPORT*
        🚗 Vehicle: *$vName*
        
        🛣️ *Logged Odometers:*
        • Total Odometer: ${state.odometerTotalStartKm.toInt()} km ➔ ${state.odometerTotalEndKm.toInt()} km
        • Total Driven: *$total km*
        • HEV Odometer: ${state.odometerHevStartKm.toInt()} km ➔ ${state.odometerHevEndKm.toInt()} km
        • HEV Driven: *$hev km* ($hevPct%)
        • EV Calculated: *${state.odometerEvStartKm.toInt()} km ➔ ${state.odometerEvEndKm.toInt()} km*
        • EV Driven: *$ev km* ($evPct%)
        
        $consumptionSection
        
        💰 *Trip Costs:*
        • Total Cost: R$ ${formatNumber(state.odometerTotalTripCost, 2)}
        • Fuel: R$ ${formatNumber(state.odometerGasolineCost, 2)}
        • Electric: R$ ${formatNumber(state.odometerElectricCost, 2)} ($chargePlace$rechargeInfoEn)
        $notePart
        
        ⚡ Generated with Voltage (PHEV & EV Calculator)
        """.trimIndent()
    } else {
        """
        📊 *RELATÓRIO DE CONSUMO PHEV*
        🚗 Veículo: *$vName*
        
        🛣️ *Odômetros Registrados:*
        • Odômetro Total: ${state.odometerTotalStartKm.toInt()} km ➔ ${state.odometerTotalEndKm.toInt()} km
        • Total Rodado: *$total km*
        • Odômetro HEV: ${state.odometerHevStartKm.toInt()} km ➔ ${state.odometerHevEndKm.toInt()} km
        • HEV Percorrido: *$hev km* ($hevPct%)
        • EV Calculado: *${state.odometerEvStartKm.toInt()} km ➔ ${state.odometerEvEndKm.toInt()} km*
        • EV Percorrido: *$ev km* ($evPct%)
        
        $consumptionSection
        
        💰 *Custos da Viagem:*
        • Custo Total: R$ ${formatNumber(state.odometerTotalTripCost, 2)}
        • Combustível: R$ ${formatNumber(state.odometerGasolineCost, 2)}
        • Elétrico: R$ ${formatNumber(state.odometerElectricCost, 2)} ($chargePlace$rechargeInfoPt)
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

    val evStartKm = (entry.totalStartKm - entry.hevStartKm).coerceAtLeast(0.0)
    val evEndKm = (entry.totalEndKm - entry.hevEndKm).coerceAtLeast(0.0)
    val hevDistanceStr = if (entry.hevEndKm > 0.0 || entry.hevStartKm > 0.0) {
        "${entry.hevStartKm.toInt()} ➔ ${entry.hevEndKm.toInt()} km (+$hev km)"
    } else {
        "$hev km"
    }
    val evDistanceStr = if (evEndKm > 0.0 || evStartKm > 0.0) {
        "${evStartKm.toInt()} ➔ ${evEndKm.toInt()} km (+$ev km)"
    } else {
        "$ev km"
    }

    val titlePart = if (entry.title.isNotBlank()) "🚗 *${entry.title}* (${entry.vehicleName})" else "🚗 *${entry.vehicleName}*"

    val isEn = language == AppLanguage.EN_US

    val costSection = if (entry.totalCost > 0 || entry.batteryStartPercent <= 25.0) {
        val isEvLimitShare = entry.batteryStartPercent <= 25.0
        val rechargePartEn = if (entry.rechargeCount > 1) " • ${entry.rechargeCount} recharges" else ""
        val rechargePartPt = if (entry.rechargeCount > 1) " • ${entry.rechargeCount} recargas" else ""
        val initBat = entry.effectiveRechargeInitialBatteryPercents.firstOrNull() ?: 25.0
        val finalBat = entry.effectiveRechargeBatteryPercents.firstOrNull() ?: entry.batteryStartPercent
        val batDetailEn = "charge: ${initBat.toInt()}% ➔ ${finalBat.toInt()}%"
        val batDetailPt = "recarga: ${initBat.toInt()}% ➔ ${finalBat.toInt()}%"
        if (isEn) {
            val elecTextEn = if (isEvLimitShare) {
                "• Electric: R$ 0,00 (Start Bat: ${initBat.toInt()}% - EV Limit, 0 kWh wall charge)"
            } else {
                "• Electric: R$ ${formatNumber(entry.electricCost, 2)} (${entry.chargingLocation.label(language)} - ${formatNumber(entry.totalEnergyKwh, 1)} kWh, $batDetailEn$rechargePartEn)"
            }
            """
            
            💰 *Trip Costs:*
            • Total Cost: R$ ${formatNumber(entry.totalCost, 2)}
            • Fuel: R$ ${formatNumber(entry.gasolineCost, 2)}
            $elecTextEn
            """.trimIndent()
        } else {
            val elecTextPt = if (isEvLimitShare) {
                "• Elétrico: R$ 0,00 (Bat. Início: ${initBat.toInt()}% - Limite EV, 0 kWh da tomada)"
            } else {
                "• Elétrico: R$ ${formatNumber(entry.electricCost, 2)} (${entry.chargingLocation.label(language)} - ${formatNumber(entry.totalEnergyKwh, 1)} kWh, $batDetailPt$rechargePartPt)"
            }
            """
            
            💰 *Custos da Viagem:*
            • Custo Total: R$ ${formatNumber(entry.totalCost, 2)}
            • Combustível: R$ ${formatNumber(entry.gasolineCost, 2)}
            $elecTextPt
            """.trimIndent()
        }
    } else ""

    val consumptionSection = if (entry.hevKm <= 0.0) {
        if (isEn) {
            """
            ⚡ *Propulsion:*
            • Mode: *100% Electric (EV)* (Zero Fuel Used)
            """.trimIndent()
        } else {
            """
            ⚡ *Propulsão:*
            • Modo: *100% Elétrico (EV)* (Zero Combustível)
            """.trimIndent()
        }
    } else {
        if (isEn) {
            """
            ⛽ *Consumption:*
            • *AVERAGE HEV CONSUMPTION: ${if (entry.realHevKmL > 0) "$hevKmL km/L" else "--"}*
            • Fuel: $liters L
            • Global Consumption: ${if (entry.realGlobalKmL > 0) "$globalKmL km/L" else "--"}
            """.trimIndent()
        } else {
            """
            ⛽ *Consumo:*
            • *CONSUMO MÉDIO HEV: ${if (entry.realHevKmL > 0) "$hevKmL km/L" else "--"}*
            • Combustível: $liters L
            • Consumo Global: ${if (entry.realGlobalKmL > 0) "$globalKmL km/L" else "--"}
            """.trimIndent()
        }
    }

    val excessEvPartEn = if (entry.excessEvKm > 0.0) {
        "\n        • HEV + EV Reg.: ${formatNumber(entry.effectiveFuelKm, 1, language)} km (${formatNumber(entry.hevKm, 1, language)} HEV + ${formatNumber(entry.excessEvKm, 1, language)} reg.)\n        • EV Pure (Plug): ${formatNumber(entry.rechargeEvKm, 1, language)} km"
    } else ""

    val excessEvPartPt = if (entry.excessEvKm > 0.0) {
        "\n        • HEV + EV Reg.: ${formatNumber(entry.effectiveFuelKm, 1, language)} km (${formatNumber(entry.hevKm, 1, language)} HEV + ${formatNumber(entry.excessEvKm, 1, language)} reg.)\n        • EV Puro (Tomada): ${formatNumber(entry.rechargeEvKm, 1, language)} km"
    } else ""

    return if (isEn) {
        """
        📊 *PHEV CONSUMPTION HISTORY*
        $titlePart
        📅 ${entry.formattedDate(language)}
        
        🛣️ *Distances:*
        • Total: ${entry.totalStartKm.toInt()} ➔ ${entry.totalEndKm.toInt()} km (+$total km)
        • HEV: $hevDistanceStr ($hevPct%)$excessEvPartEn
        • EV (Calc): $evDistanceStr ($evPct%)
        
        $consumptionSection$costSection
        
        ⚡ Generated with Voltage (PHEV & EV Calculator)
        """.trimIndent()
    } else {
        """
        📊 *HISTÓRICO DE CONSUMO PHEV*
        $titlePart
        📅 ${entry.formattedDate(language)}
        
        🛣️ *Distâncias:*
        • Total: ${entry.totalStartKm.toInt()} ➔ ${entry.totalEndKm.toInt()} km (+$total km)
        • HEV: $hevDistanceStr ($hevPct%)$excessEvPartPt
        • EV (Calc): $evDistanceStr ($evPct%)
        
        $consumptionSection$costSection
        
        ⚡ Gerado com o App Jaecoo & PHEV Calculator
        """.trimIndent()
    }
}
