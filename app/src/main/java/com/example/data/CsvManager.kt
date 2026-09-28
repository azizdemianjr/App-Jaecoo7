package com.example.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.ui.localization.AppLanguage
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.Charset
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

object CsvManager {

    private const val DELIMITER = ";"

    private val DATE_FORMAT_EXPORT = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)

    private val DATE_PATTERNS = listOf(
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy HH:mm",
        "dd/MM/yyyy",
        "dd/MM/yy HH:mm:ss",
        "dd/MM/yy HH:mm",
        "dd/MM/yy",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd HH:mm",
        "yyyy/MM/dd",
        "dd-MM-yyyy HH:mm:ss",
        "dd-MM-yyyy HH:mm",
        "dd-MM-yyyy",
        "d/M/yyyy HH:mm",
        "d/M/yyyy",
        "d/M/yy"
    )

    /**
     * Generates a CSV string formatted for spreadsheets (Excel, Google Sheets, LibreOffice).
     * Includes UTF-8 BOM (\uFEFF) so Excel on Windows/Mac properly identifies UTF-8 characters.
     */
    fun exportToCsv(entries: List<OdometerEntry>, language: AppLanguage): String {
        val sb = StringBuilder()
        // UTF-8 BOM
        sb.append('\uFEFF')

        // Header row
        val headers = listOf(
            "ID",
            "Data",
            "Titulo",
            "Veiculo",
            "Tipo",
            "Total_Inicio_Km",
            "Total_Fim_Km",
            "Total_Km",
            "HEV_Inicio_Km",
            "HEV_Fim_Km",
            "HEV_Km",
            "EV_Km",
            "Combustivel_Litros",
            "Consumo_HEV_KmL",
            "Consumo_Global_KmL",
            "Preco_Gasolina",
            "Preco_Energia",
            "Local_Recarga",
            "Bateria_Inicio_Pct",
            "Bateria_Fim_Pct",
            "Recargas_Qtd",
            "Custo_Eletrico",
            "Custo_Gasolina",
            "Custo_Total",
            "Custo_Km",
            "Economia_Gasolina"
        )
        sb.append(headers.joinToString(DELIMITER)).append("\r\n")

        for (entry in entries) {
            val dateStr = synchronized(DATE_FORMAT_EXPORT) {
                DATE_FORMAT_EXPORT.format(Date(entry.timestamp))
            }
            // Nível inicial da bateria na recarga (ex: 25%) e nível final (ex: 100%)
            val initialBatteryPct = entry.effectiveRechargeInitialBatteryPercents.firstOrNull() ?: 25.0
            val finalBatteryPct = entry.effectiveRechargeBatteryPercents.firstOrNull()
                ?: if (entry.batteryStartPercent > 0.0) entry.batteryStartPercent else 100.0

            val row = listOf(
                escapeCsv(entry.id),
                escapeCsv(dateStr),
                escapeCsv(entry.title),
                escapeCsv(entry.vehicleName),
                escapeCsv(entry.vehicleType.name),
                formatDouble(entry.totalStartKm),
                formatDouble(entry.totalEndKm),
                formatDouble(entry.totalKm),
                formatDouble(entry.hevStartKm),
                formatDouble(entry.hevEndKm),
                formatDouble(entry.hevKm),
                formatDouble(entry.evKm),
                formatDouble(entry.fuelLiters),
                formatDouble(entry.realHevKmL),
                formatDouble(entry.realGlobalKmL),
                formatDouble(entry.gasPriceLiter),
                formatDouble(entry.energyPriceKwh),
                escapeCsv(entry.chargingLocation.name),
                formatDouble(initialBatteryPct),
                formatDouble(finalBatteryPct),
                entry.rechargeCount.toString(),
                formatDouble(entry.electricCost),
                formatDouble(entry.gasolineCost),
                formatDouble(entry.totalCost),
                formatDouble(entry.costPerKm),
                formatDouble(entry.savingsVsGas)
            )
            sb.append(row.joinToString(DELIMITER)).append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Parses spreadsheet bytes from file picker (supports .csv, .txt and Excel .xlsx).
     * Handles UTF-8 (with/without BOM), UTF-16LE, UTF-16BE, Windows-1252, ANSI and Excel ZIP (.xlsx).
     */
    fun parseSpreadsheetBytes(
        bytes: ByteArray,
        defaultVehicle: Vehicle? = null,
        allVehicles: List<Vehicle> = emptyList()
    ): List<OdometerEntry> {
        if (bytes.isEmpty()) return emptyList()

        // 1. Check if user selected an Excel workbook (.xlsx)
        if (isZipOrXlsx(bytes)) {
            val xlsxCsv = extractXlsxToCsv(bytes)
            if (!xlsxCsv.isNullOrBlank()) {
                val parsed = parseCsv(xlsxCsv, defaultVehicle, allVehicles)
                if (parsed.isNotEmpty()) return parsed
            }
        }

        // 2. Decode raw bytes as text with comprehensive charset detection
        val decodedText = decodeCsvBytes(bytes)
        return parseCsv(decodedText, defaultVehicle, allVehicles)
    }

    /**
     * Decodes byte array into a String, accurately identifying UTF-16LE, UTF-16BE,
     * UTF-8 (with or without BOM), Windows-1252, or ISO-8859-1.
     */
    fun decodeCsvBytes(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        // UTF-16 LE BOM (0xFF, 0xFE)
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        // UTF-16 BE BOM (0xFE, 0xFF)
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }
        // UTF-8 BOM (0xEF, 0xBB, 0xBF)
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }

        // UTF-16 LE without BOM (Windows Excel unicode text often has alternating 0x00 bytes)
        if (bytes.size >= 4 && bytes[1] == 0.toByte() && bytes[3] == 0.toByte()) {
            return String(bytes, Charsets.UTF_16LE)
        }
        // UTF-16 BE without BOM
        if (bytes.size >= 4 && bytes[0] == 0.toByte() && bytes[2] == 0.toByte()) {
            return String(bytes, Charsets.UTF_16BE)
        }

        // Check UTF-8 validity
        val utf8 = String(bytes, Charsets.UTF_8)
        if (!utf8.contains("\uFFFD")) {
            return utf8
        }

        // Fallback to Windows-1252 (Standard Brazilian Excel ANSI on Windows)
        return try {
            String(bytes, Charset.forName("windows-1252"))
        } catch (_: Exception) {
            String(bytes, Charsets.ISO_8859_1)
        }
    }

    /**
     * Cleans imported CSV text by removing BOMs, null bytes from UTF-16, and normalizing line endings.
     */
    fun cleanCsvText(text: String): String {
        return text
            .replace("\uFEFF", "")
            .replace("\u0000", "")
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    /**
     * Detects spreadsheet delimiter (semicolon, comma, tab, pipe) by inspecting unquoted characters.
     */
    fun detectDelimiter(text: String): Char {
        val sampleLines = text.split('\n')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(15)

        if (sampleLines.isEmpty()) return ';'

        var semicolons = 0
        var commas = 0
        var tabs = 0
        var pipes = 0

        for (line in sampleLines) {
            var inQuotes = false
            for (ch in line) {
                if (ch == '"') inQuotes = !inQuotes
                if (!inQuotes) {
                    when (ch) {
                        ';' -> semicolons++
                        ',' -> commas++
                        '\t' -> tabs++
                        '|' -> pipes++
                    }
                }
            }
        }

        return when {
            tabs > semicolons && tabs > commas -> '\t'
            pipes > semicolons && pipes > commas -> '|'
            semicolons >= commas -> ';'
            else -> ','
        }
    }

    /**
     * Parses CSV text exported or edited in spreadsheets (Excel, Google Sheets, LibreOffice).
     * Handles semicolons, commas, tabs, varied Brazilian/US number formats (e.g. 1.500,50, 15.000, 1234,5),
     * Excel date serials, accented headers, and discards empty Excel rows.
     */
    fun parseCsv(
        csvText: String,
        defaultVehicle: Vehicle? = null,
        allVehicles: List<Vehicle> = emptyList()
    ): List<OdometerEntry> {
        val cleaned = cleanCsvText(csvText)
        if (cleaned.isBlank()) return emptyList()

        val rawLines = splitCsvRows(cleaned)
        if (rawLines.isEmpty()) return emptyList()

        val delimiter = detectDelimiter(cleaned)

        // Find header row: scan first 10 lines for known column names
        var headerLineIndex = -1
        for (i in 0 until minOf(rawLines.size, 10)) {
            val line = rawLines[i]
            val norm = normalizeHeader(line)
            if (norm.contains("km") || norm.contains("data") || norm.contains("date") ||
                norm.contains("veiculo") || norm.contains("carro") || norm.contains("total") ||
                norm.contains("combustivel") || norm.contains("id") || norm.contains("distancia") ||
                norm.contains("inicio") || norm.contains("fim") || norm.contains("odometro")
            ) {
                headerLineIndex = i
                break
            }
        }

        val colIndex = mutableMapOf<String, Int>()
        val startRowIndex: Int

        if (headerLineIndex != -1) {
            val headerTokens = parseCsvLine(rawLines[headerLineIndex], delimiter).map { normalizeHeader(it) }
            headerTokens.forEachIndexed { index, token ->
                if (token.isNotBlank()) {
                    colIndex[token] = index
                }
            }
            startRowIndex = headerLineIndex + 1
        } else {
            // Positional fallback if no header line is present
            startRowIndex = 0
        }

        // Helper to find column index with accent-insensitive and synonym matching
        fun getIdx(vararg keys: String): Int {
            val normalizedKeys = keys.map { normalizeHeader(it) }
            // 1. Exact match
            for (k in normalizedKeys) {
                val exact = colIndex.entries.find { it.key == k }
                if (exact != null) return exact.value
            }
            // 2. Substring match (avoiding "ev" inside "hev", and requiring length >= 4)
            for (k in normalizedKeys) {
                if (k.length < 4) continue
                val found = colIndex.entries.find { entry ->
                    val colKey = entry.key
                    if (colKey == k) true
                    else if (colKey.contains(k)) {
                        // Crucial: do NOT match EV when the column is HEV or PHEV
                        if (k.contains("ev") && !k.contains("hev") && colKey.contains("hev")) false
                        else true
                    } else false
                }
                if (found != null) return found.value
            }
            // 3. Reverse match: colKey is contained inside key (colKey length >= 4, not matching ev in hev)
            for (k in normalizedKeys) {
                val found = colIndex.entries.find { entry ->
                    val colKey = entry.key
                    if (colKey.length >= 4 && k.contains(colKey)) {
                        if (colKey.contains("ev") && !colKey.contains("hev") && k.contains("hev")) false
                        else true
                    } else false
                }
                if (found != null) return found.value
            }
            return -1
        }

        val idxId = if (headerLineIndex != -1) getIdx("id", "uuid", "codigo", "cod", "identificador", "viagem_id") else 0
        val idxDate = if (headerLineIndex != -1) getIdx("data", "date", "timestamp", "dia", "data_hora", "datetime", "horario", "periodo", "hora") else 1
        val idxTitle = if (headerLineIndex != -1) getIdx("titulo", "title", "nome", "name", "nota", "note", "desc", "descricao", "observacao", "obs", "rotulo", "trajeto", "destino") else 2
        val idxVehicle = if (headerLineIndex != -1) getIdx("veiculo", "vehicle", "carro", "car", "modelo", "model", "automovel", "auto") else 3
        val idxType = if (headerLineIndex != -1) getIdx("tipo", "type", "tipo_veiculo", "categoria", "modo") else 4
        val idxTotalStart = if (headerLineIndex != -1) getIdx("total_inicio_km", "total_inicio", "total_start", "inicio_total", "odometro_total_inicio", "km_inicial", "km_inicio", "odometro_inicial", "odometro_inicio", "km_total_inicio", "inicio_km", "partida_km") else 5
        val idxTotalEnd = if (headerLineIndex != -1) getIdx("total_fim_km", "total_fim", "total_end", "fim_total", "odometro_total_fim", "km_final", "km_fim", "odometro_final", "odometro_fim", "km_total_fim", "fim_km", "chegada_km") else 6
        val idxTotalKm = if (headerLineIndex != -1) getIdx("total_km", "distancia_total", "total_dist", "distancia", "km_total", "total_rodado", "km_rodados", "distancia_km", "km_percorridos", "percurso", "dist_total") else 7
        val idxHevStart = if (headerLineIndex != -1) getIdx("hev_inicio_km", "hev_inicio", "hev_start", "inicio_hev", "odometro_hev_inicio", "km_hev_inicio", "hev_inicial", "odometro_hev_inicial") else 8
        val idxHevEnd = if (headerLineIndex != -1) getIdx("hev_fim_km", "hev_fim", "hev_end", "fim_hev", "odometro_hev_fim", "km_hev_fim", "hev_final", "odometro_hev_final") else 9
        val idxHevKm = if (headerLineIndex != -1) getIdx("hev_km", "distancia_hev", "km_hev", "hev_dist", "hibrido_km", "km_hibrido", "combustao_km") else 10
        val idxEvKm = if (headerLineIndex != -1) getIdx("ev_km", "distancia_ev", "km_ev", "ev_dist", "eletrico_km", "km_eletrico", "distancia_eletrico") else 11
        val idxFuel = if (headerLineIndex != -1) getIdx("combustivel_litros", "combustivel", "fuel", "litros", "liters", "gasolina_litros", "litros_gasolina", "abastecimento", "combustivel_l", "consumo_litros") else 12
        val idxHevKmL = if (headerLineIndex != -1) getIdx("consumo_hev_kml", "consumo_hev", "hev_km_l", "hev_kml", "kml_hev", "media_hev") else 13
        val idxGlobalKmL = if (headerLineIndex != -1) getIdx("consumo_global_kml", "consumo_global", "global_km_l", "global_kml", "kml_global", "media_global", "consumo_medio", "kml") else 14
        val idxGasPrice = if (headerLineIndex != -1) getIdx("preco_gasolina", "gas_price", "gasolina_preco", "valor_gasolina", "preco_litro", "gasolina") else 15
        val idxEnergyPrice = if (headerLineIndex != -1) getIdx("preco_energia", "energy_price", "energia_preco", "valor_energia", "preco_kwh", "energia_kwh", "tarifa_energia", "energia") else 16
        val idxChargeLoc = if (headerLineIndex != -1) getIdx("local_recarga", "charge_loc", "local", "charging_location", "onde_carregou", "ponto_recarga") else 17
        val idxBatStart = if (headerLineIndex != -1) getIdx("bateria_inicio_pct", "bateria_inicio", "bat_inicio", "battery_start", "bateria_inicial", "soc_inicio", "soc_inicial") else 18
        val idxBatEnd = if (headerLineIndex != -1) getIdx("bateria_fim_pct", "bateria_fim", "bat_fim", "battery_end", "bateria_final", "soc_fim", "soc_final") else 19
        val idxRechargeCount = if (headerLineIndex != -1) getIdx("recargas_qtd", "recargas", "recharge", "qtd_recargas", "num_recargas", "cargas") else 20
        val idxBatCapacity = if (headerLineIndex != -1) getIdx("capacidade_bateria_kwh", "capacidade_bateria", "bateria_kwh", "bateria_capacidade") else -1
        val idxElecCons = if (headerLineIndex != -1) getIdx("consumo_eletrico_kwh100km", "consumo_eletrico", "kwh_100km", "kwh100km") else -1
        val idxGasCons = if (headerLineIndex != -1) getIdx("consumo_gasolina_kml", "consumo_gasolina", "gasolina_kml") else -1
        val idxHomeEnergyPrice = if (headerLineIndex != -1) getIdx("preco_energia_casa", "energia_casa", "tarifa_casa") else -1
        val idxPublicEnergyPrice = if (headerLineIndex != -1) getIdx("preco_energia_posto", "energia_posto", "tarifa_posto") else -1

        val result = mutableListOf<OdometerEntry>()

        for (i in startRowIndex until rawLines.size) {
            val line = rawLines[i]
            if (line.isBlank()) continue
            val tokens = parseCsvLine(line, delimiter)
            if (tokens.isEmpty() || tokens.all { it.trim().trim('"').isBlank() }) continue

            fun cell(idx: Int): String = if (idx in tokens.indices) tokens[idx].trim().trim('"') else ""
            fun cellDouble(idx: Int, default: Double = 0.0): Double {
                val str = cell(idx)
                return parseSpreadsheetDouble(str, default)
            }
            fun cellInt(idx: Int, default: Int = 1): Int {
                val str = cell(idx)
                if (str.isBlank()) return default
                val digitsOnly = str.filter { it.isDigit() }
                return digitsOnly.toIntOrNull() ?: default
            }

            var totalStart = cellDouble(idxTotalStart)
            var totalEnd = cellDouble(idxTotalEnd)
            var totalKm = cellDouble(idxTotalKm)

            if (totalKm <= 0.0 && totalEnd > totalStart && totalStart > 0.0) {
                totalKm = totalEnd - totalStart
            }
            if (totalEnd <= 0.0 && totalStart > 0.0 && totalKm > 0.0) {
                totalEnd = totalStart + totalKm
            }
            if (totalStart <= 0.0 && totalEnd > totalKm && totalKm > 0.0) {
                totalStart = totalEnd - totalKm
            }

            var hevStart = cellDouble(idxHevStart)
            var hevEnd = cellDouble(idxHevEnd)
            var hevKm = cellDouble(idxHevKm)

            if (hevKm <= 0.0 && hevEnd > hevStart && hevStart > 0.0) {
                hevKm = hevEnd - hevStart
            }
            if (hevEnd <= 0.0 && hevStart > 0.0 && hevKm > 0.0) {
                hevEnd = hevStart + hevKm
            }

            var evKm = cellDouble(idxEvKm)
            if (evKm <= 0.0 && totalKm > 0.0) {
                evKm = (totalKm - hevKm).coerceAtLeast(0.0)
            }

            val fuelLiters = cellDouble(idxFuel)

            val dateStr = cell(idxDate)
            val titleStr = cell(idxTitle)
            val rawVehicleName = cell(idxVehicle).trim()

            // Discard empty Excel trailing rows (rows with no date, title, car, or numbers)
            val hasIdentifier = (dateStr.isNotBlank() && dateStr != "-") ||
                titleStr.isNotBlank() ||
                (rawVehicleName.isNotBlank() && !rawVehicleName.equals("Veículo", ignoreCase = true) && !rawVehicleName.equals("Veiculo", ignoreCase = true))

            val hasMetrics = totalKm > 0.0 || totalStart > 0.0 || totalEnd > 0.0 ||
                hevKm > 0.0 || hevStart > 0.0 || hevEnd > 0.0 ||
                evKm > 0.0 || fuelLiters > 0.0

            if (!hasIdentifier && !hasMetrics) {
                continue
            }

            val idStr = cell(idxId).ifBlank { UUID.randomUUID().toString() }
            val timestamp = parseDateToMillis(dateStr)

            // Resolve vehicle accurately
            val matchedVehicle = if (rawVehicleName.isNotBlank()) {
                allVehicles.find {
                    it.name.equals(rawVehicleName, ignoreCase = true) ||
                    rawVehicleName.contains(it.name, ignoreCase = true) ||
                    it.name.contains(rawVehicleName, ignoreCase = true)
                }
            } else null

            val resolvedVehicleName = when {
                matchedVehicle != null -> matchedVehicle.name
                rawVehicleName.isNotBlank() && !rawVehicleName.equals("Veículo", ignoreCase = true) && !rawVehicleName.equals("Veiculo", ignoreCase = true) -> rawVehicleName
                defaultVehicle != null -> defaultVehicle.name
                else -> "Veículo"
            }

            val resolvedVehicleId = when {
                matchedVehicle != null -> matchedVehicle.id
                defaultVehicle != null && resolvedVehicleName.equals(defaultVehicle.name, ignoreCase = true) -> defaultVehicle.id
                else -> ""
            }

            val typeStr = cell(idxType).uppercase(Locale.ROOT)
            val vehicleType = when {
                matchedVehicle != null -> matchedVehicle.type
                defaultVehicle != null && resolvedVehicleName.equals(defaultVehicle.name, ignoreCase = true) -> defaultVehicle.type
                typeStr.contains("BEV") || typeStr.contains("ELETRICO") || typeStr.contains("ELECTRIC") -> VehicleType.BEV
                typeStr.contains("HEV") && !typeStr.contains("PHEV") -> VehicleType.HEV
                else -> VehicleType.PHEV
            }

            val rawGasPrice = cellDouble(idxGasPrice)
            val gasPrice = if (rawGasPrice > 0.0) rawGasPrice else 6.50

            val rawEnergyPrice = cellDouble(idxEnergyPrice)
            val energyPrice = if (rawEnergyPrice > 0.0) rawEnergyPrice else 1.30

            val rawHomeEnergyPrice = if (idxHomeEnergyPrice != -1) cellDouble(idxHomeEnergyPrice) else 0.0
            val homeEnergyPrice = if (rawHomeEnergyPrice > 0.0) rawHomeEnergyPrice else energyPrice

            val rawPublicEnergyPrice = if (idxPublicEnergyPrice != -1) cellDouble(idxPublicEnergyPrice) else 0.0
            val publicEnergyPrice = if (rawPublicEnergyPrice > 0.0) rawPublicEnergyPrice else (energyPrice * 1.6).coerceAtLeast(2.10)

            val vBatCap = matchedVehicle?.batteryCapacityKwh ?: defaultVehicle?.batteryCapacityKwh ?: 18.3
            val vElecCons = matchedVehicle?.electricConsumptionKwh100km ?: defaultVehicle?.electricConsumptionKwh100km ?: 18.0
            val vGasCons = matchedVehicle?.gasolineConsumptionKmL ?: defaultVehicle?.gasolineConsumptionKmL ?: 15.0

            val batCap = if (idxBatCapacity != -1) cellDouble(idxBatCapacity, vBatCap).let { if (it > 0.0) it else vBatCap } else vBatCap
            val elecCons = if (idxElecCons != -1) cellDouble(idxElecCons, vElecCons).let { if (it > 0.0) it else vElecCons } else vElecCons
            val gasCons = if (idxGasCons != -1) cellDouble(idxGasCons, vGasCons).let { if (it > 0.0) it else vGasCons } else vGasCons

            val locStr = cell(idxChargeLoc).uppercase(Locale.ROOT)
            val chargingLocation = when {
                locStr.contains("POSTO") || locStr.contains("STATION") || locStr.contains("PUBLIC") -> ChargingLocation.STATION
                locStr.contains("SEM") || locStr.contains("NONE") -> ChargingLocation.NONE
                else -> ChargingLocation.HOME
            }

            val rawBatStart = cellDouble(idxBatStart, 25.0)
            val rawBatEnd = cellDouble(idxBatEnd, 100.0)

            // Compatibilidade retroativa para planilhas geradas em versões antigas:
            // Onde Bateria_Inicio_Pct continha 100% e Bateria_Fim_Pct continha 75% (delta utilizável)
            val (batStart, batEnd) = if (rawBatStart > rawBatEnd && rawBatEnd <= 75.0 && rawBatStart >= 90.0) {
                val recoveredInit = (rawBatStart - rawBatEnd).coerceIn(0.0, 100.0)
                val recoveredFinal = rawBatStart
                Pair(recoveredInit, recoveredFinal)
            } else {
                Pair(rawBatStart, rawBatEnd)
            }

            val recharges = cellInt(idxRechargeCount, 1)

            val avgHevKmL = cellDouble(idxHevKmL)
            val avgGlobalKmL = cellDouble(idxGlobalKmL)

            // Se combustível não foi informado em litros mas houve percurso HEV, calcula litros estimados
            val effectiveFuelLiters = if (fuelLiters > 0.0) {
                fuelLiters
            } else if (hevKm > 0.0) {
                val effKmL = if (avgHevKmL > 0.0) avgHevKmL else gasCons
                if (effKmL > 0.0) hevKm / effKmL else 0.0
            } else 0.0

            val count = recharges.coerceAtLeast(1)
            val recLocations = List(count) { chargingLocation }
            val recFinalPercents = List(count) { batEnd }
            val recInitPercents = List(count) { batStart }

            val entry = OdometerEntry(
                id = idStr,
                timestamp = timestamp,
                title = titleStr,
                vehicleId = resolvedVehicleId,
                vehicleName = resolvedVehicleName,
                vehicleType = vehicleType,
                totalKm = totalKm,
                evKm = evKm,
                hevKm = hevKm,
                electricConsumptionKwh100km = elecCons,
                gasolineConsumptionKmL = gasCons,
                gasPriceLiter = gasPrice,
                energyPriceKwh = homeEnergyPrice,
                fuelLiters = effectiveFuelLiters,
                averageHevKmL = avgHevKmL,
                averageGlobalKmL = avgGlobalKmL,
                totalStartKm = totalStart,
                totalEndKm = totalEnd,
                hevStartKm = hevStart,
                hevEndKm = hevEnd,
                chargingLocation = chargingLocation,
                batteryCapacityKwh = batCap,
                batteryStartPercent = batEnd,
                batteryMaxPercent = (batEnd - batStart).coerceAtLeast(0.0),
                rechargeCount = count,
                rechargeLocations = recLocations,
                rechargeBatteryPercents = recFinalPercents,
                rechargeInitialBatteryPercents = recInitPercents,
                homeEnergyPrice = homeEnergyPrice,
                publicEnergyPrice = publicEnergyPrice
            )

            result.add(entry)
        }

        return result
    }

    /**
     * Parses spreadsheet numbers handling European/Brazilian formats (1.500,50 / 15.000 / 1234,5),
     * US formats (1,500.50), spaces, quotes, and currency/unit labels.
     */
    fun parseSpreadsheetDouble(raw: String, default: Double = 0.0): Double {
        if (raw.isBlank()) return default

        var s = raw.trim()
            .replace("\"", "")
            .replace("'", "")
            .replace("\u00A0", "") // non-breaking space used by Excel
            .replace(" ", "")
            .replace("R$", "", ignoreCase = true)
            .replace("$", "")
            .replace("km/l", "", ignoreCase = true)
            .replace("kml", "", ignoreCase = true)
            .replace("km", "", ignoreCase = true)
            .replace("kwh", "", ignoreCase = true)
            .replace("l", "", ignoreCase = true)
            .replace("%", "")
            .trim()

        if (s.isEmpty()) return default

        // Direct parse if standard float
        s.toDoubleOrNull()?.let { std ->
            // If it ends with .000 and has digits before (e.g. "15.000"), in Brazilian Excel with thousands formatting, it's 15000!
            if (s.matches(Regex("^\\d{1,4}\\.000$"))) {
                return (s.substringBefore(".").toLong() * 1000).toDouble()
            }
            return std
        }

        // Case 1: Both '.' and ',' present
        if (s.contains(".") && s.contains(",")) {
            val lastDot = s.lastIndexOf('.')
            val lastComma = s.lastIndexOf(',')
            s = if (lastComma > lastDot) {
                // European / Brazilian format: 1.234,56 -> remove dots, replace comma with dot
                s.replace(".", "").replace(",", ".")
            } else {
                // US format: 1,234.56 -> remove commas
                s.replace(",", "")
            }
            return s.toDoubleOrNull() ?: default
        }

        // Case 2: Only ',' present (e.g. 1234,56 or 12,5)
        if (s.contains(",")) {
            val countComma = s.count { it == ',' }
            s = if (countComma > 1) {
                // Multiple commas like 1,234,567
                s.replace(",", "")
            } else {
                s.replace(",", ".")
            }
            return s.toDoubleOrNull() ?: default
        }

        // Case 3: Only '.' present
        if (s.contains(".")) {
            val countDot = s.count { it == '.' }
            if (countDot > 1) {
                // Multiple dots like 1.234.567 (thousands separators)
                s = s.replace(".", "")
                return s.toDoubleOrNull() ?: default
            }
            // If it matches thousands format like 15.000
            if (s.matches(Regex("^\\d{1,4}\\.000$"))) {
                return (s.substringBefore(".").toLong() * 1000).toDouble()
            }
        }

        return s.toDoubleOrNull() ?: default
    }

    /**
     * Parses date strings into epoch milliseconds, handling Brazilian and ISO formats,
     * as well as Excel date serial numbers (e.g. 45558.5).
     */
    fun parseDateToMillis(dateStr: String): Long {
        val clean = dateStr.trim().trim('"').trim()
        if (clean.isBlank()) return System.currentTimeMillis()

        // 1. Unix timestamp in millis or seconds
        val num = clean.toLongOrNull()
        if (num != null && num > 1000000000000L) return num
        if (num != null && num > 1000000000L) return num * 1000L

        // 2. Excel serial date (e.g. 45558 or 45558.60416)
        val doubleVal = clean.replace(",", ".").toDoubleOrNull()
        if (doubleVal != null && doubleVal in 30000.0..65000.0) {
            val millis = ((doubleVal - 25569.0) * 86400000.0).toLong()
            if (millis > 0) return millis
        }

        // 3. Date pattern matching
        for (pattern in DATE_PATTERNS) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.US)
                val parsed = parser.parse(clean)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }
        return System.currentTimeMillis()
    }

    /**
     * Splits CSV into rows, respecting newlines within quoted fields.
     */
    fun splitCsvRows(text: String): List<String> {
        val rows = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '"') {
                inQuotes = !inQuotes
                sb.append(c)
            } else if ((c == '\n' || c == '\r') && !inQuotes) {
                if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') {
                    i++
                }
                val row = sb.toString()
                if (row.isNotBlank()) {
                    rows.add(row)
                }
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        val last = sb.toString()
        if (last.isNotBlank()) {
            rows.add(last)
        }
        return rows
    }

    /**
     * Normalizes header strings by removing accents, quotes, punctuation, and non-alphanumeric chars.
     * E.g. "Veículo" -> "veiculo", "Km Total (Início)" -> "km_total_inicio".
     */
    fun normalizeHeader(str: String): String {
        val unquoted = str.trim().trim('"').trim().replace("\u0000", "").replace("\uFEFF", "")
        val noAccents = Normalizer.normalize(unquoted, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
        return noAccents
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
    }

    /**
     * Creates a temporary CSV file in application cache and launches Android share sheet.
     */
    fun shareCsv(context: Context, entries: List<OdometerEntry>, language: AppLanguage, vehicleName: String? = null) {
        val csvContent = exportToCsv(entries, language)
        val cacheDir = context.cacheDir
        val safeVehicleSuffix = vehicleName
            ?.replace(Regex("[^a-zA-Z0-9_]"), "_")
            ?.lowercase(Locale.ROOT)
            ?.trim('_')
        val fileName = if (!safeVehicleSuffix.isNullOrBlank()) {
            "historico_viagens_${safeVehicleSuffix}.csv"
        } else {
            "historico_viagens_completo.csv"
        }
        val file = File(cacheDir, fileName)
        file.writeText(csvContent, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val subjectTitle = if (vehicleName != null) {
            if (language == AppLanguage.EN_US) "Trip History - $vehicleName (CSV)" else "Histórico de Viagens - $vehicleName (CSV)"
        } else {
            if (language == AppLanguage.EN_US) "Trip History - All Vehicles (CSV)" else "Histórico de Viagens - Todos os Veículos (CSV)"
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subjectTitle)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, if (language == AppLanguage.EN_US) "Export CSV Spreadsheet" else "Exportar Planilha CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun escapeCsv(value: String): String {
        var str = value
        if (str.contains("\"") || str.contains(";") || str.contains(",") || str.contains("\n") || str.contains("\r")) {
            str = str.replace("\"", "\"\"")
            return "\"$str\""
        }
        return str
    }

    private fun formatDouble(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString())
        return tokens
    }

    // --- Excel XLSX Workbook Support ---

    private fun isZipOrXlsx(bytes: ByteArray): Boolean {
        return bytes.size >= 4 &&
            bytes[0] == 0x50.toByte() &&
            bytes[1] == 0x4B.toByte() &&
            bytes[2] == 0x03.toByte() &&
            bytes[3] == 0x04.toByte()
    }

    private fun extractXlsxToCsv(bytes: ByteArray): String? {
        return try {
            var sharedStrings = mutableListOf<String>()
            var sheetBytes: ByteArray? = null

            val zis = ZipInputStream(ByteArrayInputStream(bytes))
            var entry = zis.nextEntry
            while (entry != null) {
                when {
                    entry.name.equals("xl/sharedStrings.xml", ignoreCase = true) -> {
                        sharedStrings = parseSharedStrings(zis.readBytes())
                    }
                    entry.name.equals("xl/worksheets/sheet1.xml", ignoreCase = true) ||
                    entry.name.matches(Regex("xl/worksheets/sheet\\d+\\.xml", RegexOption.IGNORE_CASE)) -> {
                        if (sheetBytes == null) {
                            sheetBytes = zis.readBytes()
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()

            if (sheetBytes != null) {
                parseSheetXmlToCsv(sheetBytes, sharedStrings)
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun parseSharedStrings(xmlBytes: ByteArray): MutableList<String> {
        val list = mutableListOf<String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(xmlBytes), "UTF-8")
            var eventType = parser.eventType
            var inText = false
            val currentStr = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inText = true
                        } else if (parser.name.equals("si", ignoreCase = true)) {
                            currentStr.clear()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inText) {
                            currentStr.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inText = false
                        } else if (parser.name.equals("si", ignoreCase = true)) {
                            list.add(currentStr.toString())
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Throwable) {}
        return list
    }

    private fun parseSheetXmlToCsv(sheetBytes: ByteArray, sharedStrings: List<String>): String {
        val sb = StringBuilder()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(sheetBytes), "UTF-8")
            var eventType = parser.eventType

            var currentRow = mutableMapOf<Int, String>()
            var currentCellCol = 0
            var currentCellType = ""
            var inValue = false
            val cellVal = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val name = parser.name
                        if (name.equals("row", ignoreCase = true)) {
                            currentRow = mutableMapOf()
                        } else if (name.equals("c", ignoreCase = true)) {
                            val ref = parser.getAttributeValue(null, "r") ?: "A1"
                            currentCellCol = colLettersToIndex(ref)
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            cellVal.clear()
                        } else if (name.equals("v", ignoreCase = true)) {
                            inValue = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inValue) {
                            cellVal.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val name = parser.name
                        if (name.equals("v", ignoreCase = true)) {
                            inValue = false
                            val raw = cellVal.toString()
                            val text = if (currentCellType == "s") {
                                val idx = raw.toIntOrNull() ?: -1
                                if (idx in sharedStrings.indices) sharedStrings[idx] else raw
                            } else {
                                raw
                            }
                            currentRow[currentCellCol] = text
                        } else if (name.equals("row", ignoreCase = true)) {
                            if (currentRow.isNotEmpty()) {
                                val maxCol = currentRow.keys.maxOrNull() ?: 0
                                val rowValues = (0..maxCol).map { col ->
                                    val v = currentRow[col] ?: ""
                                    escapeCsv(v)
                                }
                                sb.append(rowValues.joinToString(";")).append("\n")
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Throwable) {}
        return sb.toString()
    }

    private fun colLettersToIndex(cellRef: String): Int {
        var col = 0
        for (ch in cellRef) {
            if (ch in 'A'..'Z') {
                col = col * 26 + (ch - 'A' + 1)
            } else if (ch in 'a'..'z') {
                col = col * 26 + (ch - 'a' + 1)
            } else {
                break
            }
        }
        return (col - 1).coerceAtLeast(0)
    }
}
