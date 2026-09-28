package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CsvManager
import com.example.data.OdometerEntry
import com.example.data.Vehicle
import com.example.ui.localization.AppDictionary
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.VoltageOnSurface
import com.example.ui.theme.VoltageOnSurfaceVariant
import com.example.ui.theme.VoltagePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BgDarkCard = Color(0xFF0C1322)
private val BgCardBorder = Color(0xFF1E293B)
private val CyanText = Color(0xFF38BDF8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvDataDialog(
    entries: List<OdometerEntry>,
    currentVehicle: Vehicle,
    allVehicles: List<Vehicle>,
    language: AppLanguage,
    strings: AppDictionary,
    onDismiss: () -> Unit,
    onImportTrips: (List<OdometerEntry>, Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Exportar, 1 = Importar

    // Filtro por veículo para exportação: null = todos os veículos, ou um veículo específico
    var selectedVehicleForExport by remember { mutableStateOf<Vehicle?>(currentVehicle) }
    var isVehicleMenuExpanded by remember { mutableStateOf(false) }

    val availableVehiclesForExport = remember(entries, allVehicles) {
        val list = mutableListOf<Vehicle>()
        list.addAll(allVehicles)
        val existingNames = allVehicles.map { it.name.lowercase(Locale.ROOT) }.toSet()
        entries.forEach { entry ->
            if (entry.vehicleName.isNotBlank() && !existingNames.contains(entry.vehicleName.lowercase(Locale.ROOT))) {
                list.add(
                    Vehicle(
                        id = entry.vehicleId.ifBlank { entry.vehicleName },
                        name = entry.vehicleName,
                        type = entry.vehicleType,
                        batteryCapacityKwh = 18.0,
                        electricConsumptionKwh100km = 16.0,
                        gasolineConsumptionKmL = 15.0
                    )
                )
            }
        }
        list
    }

    val filteredEntries = remember(entries, selectedVehicleForExport) {
        val target = selectedVehicleForExport
        if (target == null) {
            entries
        } else {
            entries.filter { it.matchesVehicle(target) }
        }
    }

    // Import states
    var importedTrips by remember { mutableStateOf<List<OdometerEntry>?>(null) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var replaceAll by remember { mutableStateOf(false) }
    var rawCsvPastedText by remember { mutableStateOf("") }
    var showPasteSection by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: byteArrayOf()
                val parsed = CsvManager.parseSpreadsheetBytes(
                    bytes = bytes,
                    defaultVehicle = currentVehicle,
                    allVehicles = allVehicles
                )
                if (parsed.isNotEmpty()) {
                    importedTrips = parsed
                    importErrorMessage = null
                } else {
                    importedTrips = null
                    importErrorMessage = strings.csvImportErrorInvalid
                }
            } catch (e: Exception) {
                importedTrips = null
                importErrorMessage = "Erro ao ler arquivo: ${e.message}"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    tint = CyanText,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (language == AppLanguage.EN_US) "Spreadsheet Manager (CSV)" else "Gerenciador de Planilha (CSV)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface
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
                // Tab Selector
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E293B),
                    contentColor = VoltagePrimary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (language == AppLanguage.EN_US) "Export" else "Exportar",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (language == AppLanguage.EN_US) "Import" else "Importar",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }

                // TAB 0: EXPORTAR PLANILHA
                if (selectedTab == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                        border = BorderStroke(1.dp, BgCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN_US)
                                    "Export recorded trips to an Excel and Sheets compatible CSV file. You can choose to export by a specific vehicle or all vehicles."
                                else
                                    "Exporte as viagens para um arquivo CSV compatível com Excel e Google Sheets. Você pode exportar por veículo específico ou todos os veículos.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                            )

                            // SELETOR DE ESCOPO POR VEÍCULO
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (language == AppLanguage.EN_US) "Export by Vehicle:" else "Exportar por Veículo:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = VoltageOnSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )

                                // Dropdown seletor de veículo
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Surface(
                                        onClick = { isVehicleMenuExpanded = true },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, Color(0xFF334155)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("export_vehicle_selector")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = CyanText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = if (selectedVehicleForExport == null) {
                                                        if (language == AppLanguage.EN_US) "All Vehicles (${entries.size} trips)" else "Todos os Veículos (${entries.size} viagens)"
                                                    } else {
                                                        "${selectedVehicleForExport?.name} (${filteredEntries.size} viagens)"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = VoltageOnSurface,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = VoltageOnSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isVehicleMenuExpanded,
                                        onDismissRequest = { isVehicleMenuExpanded = false },
                                        modifier = Modifier.background(Color(0xFF1E293B))
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(Icons.Default.FilterList, contentDescription = null, tint = CyanText, modifier = Modifier.size(18.dp))
                                                    Text(
                                                        text = if (language == AppLanguage.EN_US) "All Vehicles (${entries.size} trips)" else "Todos os Veículos (${entries.size} viagens)",
                                                        color = if (selectedVehicleForExport == null) CyanText else VoltageOnSurface,
                                                        fontWeight = if (selectedVehicleForExport == null) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedVehicleForExport = null
                                                isVehicleMenuExpanded = false
                                            }
                                        )

                                        availableVehiclesForExport.forEach { vehicle ->
                                            val count = entries.count { it.matchesVehicle(vehicle) }
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = VoltagePrimary, modifier = Modifier.size(18.dp))
                                                        Text(
                                                            text = "${vehicle.name} ($count viagens)",
                                                            color = if (selectedVehicleForExport?.id == vehicle.id || selectedVehicleForExport?.name == vehicle.name) CyanText else VoltageOnSurface,
                                                            fontWeight = if (selectedVehicleForExport?.id == vehicle.id || selectedVehicleForExport?.name == vehicle.name) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedVehicleForExport = vehicle
                                                    isVehicleMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Quick filter chips for immediate 1-tap switching
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedVehicleForExport == null,
                                        onClick = { selectedVehicleForExport = null },
                                        label = {
                                            Text(
                                                text = if (language == AppLanguage.EN_US) "All" else "Todos",
                                                fontSize = 11.5.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF0284C7),
                                            selectedLabelColor = Color.White
                                        )
                                    )

                                    FilterChip(
                                        selected = selectedVehicleForExport?.id == currentVehicle.id,
                                        onClick = { selectedVehicleForExport = currentVehicle },
                                        label = {
                                            Text(
                                                text = currentVehicle.name,
                                                fontSize = 11.5.sp,
                                                maxLines = 1
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF0284C7),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // BADGE RESUMO
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    val badgeDesc = if (selectedVehicleForExport != null) {
                                        if (language == AppLanguage.EN_US)
                                            "${filteredEntries.size} trip(s) of ${selectedVehicleForExport?.name} ready"
                                        else
                                            "${filteredEntries.size} viagem(ns) de ${selectedVehicleForExport?.name} prontas"
                                    } else {
                                        if (language == AppLanguage.EN_US)
                                            "${filteredEntries.size} trip(s) of all vehicles ready"
                                        else
                                            "${filteredEntries.size} viagem(ns) de todos os veículos prontas"
                                    }
                                    Text(
                                        text = badgeDesc,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFE2E8F0),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            // Botão Compartilhar / Abrir no Excel
                            Button(
                                onClick = {
                                    CsvManager.shareCsv(
                                        context = context,
                                        entries = filteredEntries,
                                        language = language,
                                        vehicleName = selectedVehicleForExport?.name
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_share_csv"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.EN_US) "Share / Open in Excel & Sheets" else "Compartilhar / Abrir no Excel & Sheets",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Botão Copiar para Área de Transferência
                            OutlinedButton(
                                onClick = {
                                    val csv = CsvManager.exportToCsv(filteredEntries, language)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val label = selectedVehicleForExport?.name ?: "Viagens"
                                    val clip = ClipData.newPlainText("Viagens $label CSV", csv)
                                    clipboard.setPrimaryClip(clip)
                                    val toastMsg = if (selectedVehicleForExport != null) {
                                        if (language == AppLanguage.EN_US)
                                            "CSV spreadsheet for ${selectedVehicleForExport?.name} copied!"
                                        else
                                            "Planilha CSV (${selectedVehicleForExport?.name}) copiada!"
                                    } else {
                                        if (language == AppLanguage.EN_US)
                                            "CSV spreadsheet for all vehicles copied!"
                                        else
                                            "Planilha CSV (todos os veículos) copiada!"
                                    }
                                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_copy_csv"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.EN_US) "Copy CSV Text" else "Copiar Texto da Planilha (CSV)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }

                // TAB 1: IMPORTAR PLANILHA
                if (selectedTab == 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                        border = BorderStroke(1.dp, BgCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN_US)
                                    "Import a CSV spreadsheet edited on your PC or phone. It will automatically update odometers and calculate your metrics."
                                else
                                    "Importe um arquivo CSV editado no computador ou celular. As quilometragens e cálculos serão atualizados automaticamente.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = VoltageOnSurfaceVariant,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                            )

                            // Botão Selecionar Arquivo
                            Button(
                                onClick = {
                                    filePickerLauncher.launch("*/*")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_pick_csv_file"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.csvImportPickFileBtn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Botão para Colar Texto Manualmente
                            TextButton(
                                onClick = { showPasteSection = !showPasteSection },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (showPasteSection)
                                        (if (language == AppLanguage.EN_US) "Hide manual paste" else "Ocultar colar texto")
                                    else
                                        (if (language == AppLanguage.EN_US) "Or paste CSV text directly" else "Ou colar texto CSV diretamente"),
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp
                                )
                            }

                            if (showPasteSection) {
                                OutlinedTextField(
                                    value = rawCsvPastedText,
                                    onValueChange = { rawCsvPastedText = it },
                                    placeholder = {
                                        Text(
                                            text = "Cole o conteúdo da planilha aqui (ex: ID;Data;Total_Inicio_Km...)",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    },
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = VoltageOnSurface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Button(
                                    onClick = {
                                        val parsed = CsvManager.parseCsv(
                                            csvText = rawCsvPastedText,
                                            defaultVehicle = currentVehicle,
                                            allVehicles = allVehicles
                                        )
                                        if (parsed.isNotEmpty()) {
                                            importedTrips = parsed
                                            importErrorMessage = null
                                        } else {
                                            importedTrips = null
                                            importErrorMessage = strings.csvImportErrorInvalid
                                        }
                                    },
                                    enabled = rawCsvPastedText.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.EN_US) "Parse Pasted Text" else "Processar Texto Colado",
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Mensagem de Erro
                            if (importErrorMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = importErrorMessage ?: "",
                                        color = Color(0xFFFCA5A5),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            // Pré-visualização dos dados importados
                            val trips = importedTrips
                            if (trips != null && trips.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF064E3B).copy(alpha = 0.3f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF34D399),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = String.format(Locale.getDefault(), strings.csvImportTripsFound, trips.size),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF34D399),
                                                fontSize = 13.sp
                                            )
                                        }

                                        val vehicles = trips.map { it.vehicleName }.distinct().joinToString(", ")
                                        Text(
                                            text = if (language == AppLanguage.EN_US) "Vehicles: $vehicles" else "Veículos: $vehicles",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFD1FAE5),
                                                fontSize = 11.5.sp
                                            )
                                        )

                                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        val firstDate = dateFormat.format(Date(trips.minOf { it.timestamp }))
                                        val lastDate = dateFormat.format(Date(trips.maxOf { it.timestamp }))
                                        Text(
                                            text = if (language == AppLanguage.EN_US) "Date range: $firstDate to $lastDate" else "Período: $firstDate até $lastDate",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFA7F3D0),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                // Modo de Importação: Substituir vs Mesclar
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (language == AppLanguage.EN_US) "Import Mode:" else "Modo de Importação:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = VoltageOnSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        RadioButton(
                                            selected = !replaceAll,
                                            onClick = { replaceAll = false }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = strings.csvImportMergeRadio,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = VoltageOnSurface,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        RadioButton(
                                            selected = replaceAll,
                                            onClick = { replaceAll = true }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = strings.csvImportReplaceAllRadio,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFF87171),
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                // Botão Confirmar Importação
                                Button(
                                    onClick = {
                                        onImportTrips(trips, replaceAll)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_confirm_import_csv"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.csvImportConfirmBtn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = strings.odometerCloseDialogBtn,
                    color = VoltagePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
