package com.example

import com.example.data.ChargingLocation
import com.example.data.CsvManager
import com.example.data.OdometerEntry
import com.example.data.Vehicle
import com.example.data.VehicleType
import com.example.ui.localization.AppLanguage
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun csvExportAndImport_roundtripIsAccurate() {
    val sampleEntry = OdometerEntry(
      id = "test-id-123",
      timestamp = 1726330800000L,
      title = "Viagem Trabalho",
      vehicleName = "Jaecoo 8",
      vehicleType = VehicleType.PHEV,
      totalStartKm = 15000.0,
      totalEndKm = 15300.0,
      totalKm = 300.0,
      hevStartKm = 5000.0,
      hevEndKm = 5100.0,
      hevKm = 100.0,
      evKm = 200.0,
      fuelLiters = 14.5,
      gasPriceLiter = 6.20,
      energyPriceKwh = 1.15,
      chargingLocation = ChargingLocation.HOME,
      batteryStartPercent = 100.0,
      batteryMaxPercent = 75.0,
      rechargeCount = 2
    )

    val exportedCsv = CsvManager.exportToCsv(listOf(sampleEntry), AppLanguage.PT_BR)
    assertTrue("CSV must contain headers", exportedCsv.contains("Total_Inicio_Km"))
    assertTrue("CSV must contain title", exportedCsv.contains("Viagem Trabalho"))

    val parsedEntries = CsvManager.parseCsv(exportedCsv)
    assertEquals(1, parsedEntries.size)
    val parsed = parsedEntries.first()

    assertEquals("test-id-123", parsed.id)
    assertEquals("Viagem Trabalho", parsed.title)
    assertEquals("Jaecoo 8", parsed.vehicleName)
    assertEquals(VehicleType.PHEV, parsed.vehicleType)
    assertEquals(15000.0, parsed.totalStartKm, 0.01)
    assertEquals(15300.0, parsed.totalEndKm, 0.01)
    assertEquals(300.0, parsed.totalKm, 0.01)
    assertEquals(5000.0, parsed.hevStartKm, 0.01)
    assertEquals(5100.0, parsed.hevEndKm, 0.01)
    assertEquals(100.0, parsed.hevKm, 0.01)
    assertEquals(200.0, parsed.evKm, 0.01)
    assertEquals(14.5, parsed.fuelLiters, 0.01)
    assertEquals(ChargingLocation.HOME, parsed.chargingLocation)
    assertEquals(25.0, parsed.effectiveRechargeInitialBatteryPercents.first(), 0.01)
    assertEquals(100.0, parsed.effectiveRechargeBatteryPercents.first(), 0.01)
  }

  @Test
  fun csvExport_batteryPercentages_initialIs25AndFinalIs100() {
    val trip = OdometerEntry(
      id = "bat-test-1",
      title = "Viagem Bateria",
      vehicleName = "Jaecoo 8",
      totalKm = 100.0,
      evKm = 70.0,
      hevKm = 30.0,
      chargingLocation = ChargingLocation.HOME,
      rechargeCount = 1,
      rechargeInitialBatteryPercents = listOf(25.0),
      rechargeBatteryPercents = listOf(100.0)
    )

    val csv = CsvManager.exportToCsv(listOf(trip), AppLanguage.PT_BR)
    val lines = csv.trim().lines()
    val headerCols = lines[0].split(";")
    val dataCols = lines[1].split(";")

    val idxBatInicio = headerCols.indexOf("Bateria_Inicio_Pct")
    val idxBatFim = headerCols.indexOf("Bateria_Fim_Pct")

    assertTrue("Header must contain Bateria_Inicio_Pct", idxBatInicio != -1)
    assertTrue("Header must contain Bateria_Fim_Pct", idxBatFim != -1)

    assertEquals("25", dataCols[idxBatInicio])
    assertEquals("100", dataCols[idxBatFim])

    // Test round-trip import
    val imported = CsvManager.parseCsv(csv)
    assertEquals(1, imported.size)
    assertEquals(25.0, imported[0].effectiveRechargeInitialBatteryPercents.first(), 0.01)
    assertEquals(100.0, imported[0].effectiveRechargeBatteryPercents.first(), 0.01)
  }

  @Test
  fun csvImport_handlesOldInvertedBatteryFormatGracefully() {
    val oldCsv = """
      ID;Data;Titulo;Veiculo;Tipo;Total_Inicio_Km;Total_Fim_Km;Total_Km;HEV_Inicio_Km;HEV_Fim_Km;HEV_Km;EV_Km;Combustivel_Litros;Consumo_HEV_KmL;Consumo_Global_KmL;Preco_Gasolina;Preco_Energia;Local_Recarga;Bateria_Inicio_Pct;Bateria_Fim_Pct;Recargas_Qtd;Custo_Eletrico;Custo_Gasolina;Custo_Total;Custo_Km;Economia_Gasolina
      old-1;22/09/2026;Viagem Antiga;Jaecoo 8;PHEV;1000;1100;100;200;230;30;70;2.0;15;50;6.50;1.30;HOME;100;75;1;15.0;13.0;28.0;0.28;25.0
    """.trimIndent()

    val imported = CsvManager.parseCsv(oldCsv)
    assertEquals(1, imported.size)
    // Old 100 / 75 should be recovered as 25% initial and 100% final!
    assertEquals(25.0, imported[0].effectiveRechargeInitialBatteryPercents.first(), 0.01)
    assertEquals(100.0, imported[0].effectiveRechargeBatteryPercents.first(), 0.01)
  }

  @Test
  fun csvParse_handlesSpreadsheetWithCommasAndBrazilianDecimals() {
    val spreadsheetText = """
      ID;Data;Titulo;Veiculo;Total_Inicio_Km;Total_Fim_Km;Total_Km;HEV_Inicio_Km;HEV_Fim_Km;Combustivel_Litros
      viagem-1;14/09/2026;Viagem Serra;Song Plus;12000;12250,5;250,5;4000;4050,5;12,8
    """.trimIndent()

    val parsed = CsvManager.parseCsv(spreadsheetText)
    assertEquals(1, parsed.size)
    val entry = parsed.first()

    assertEquals("viagem-1", entry.id)
    assertEquals("Viagem Serra", entry.title)
    assertEquals("Song Plus", entry.vehicleName)
    assertEquals(12000.0, entry.totalStartKm, 0.01)
    assertEquals(12250.5, entry.totalEndKm, 0.01)
    assertEquals(250.5, entry.totalKm, 0.01)
    assertEquals(4000.0, entry.hevStartKm, 0.01)
    assertEquals(4050.5, entry.hevEndKm, 0.01)
    assertEquals(50.5, entry.hevKm, 0.01)
    assertEquals(200.0, entry.evKm, 0.01) // 250.5 - 50.5 = 200.0
    assertEquals(12.8, entry.fuelLiters, 0.01)
  }

  @Test
  fun csvExportByVehicle_exportsOnlyTargetVehicleTrips() {
    val tripJaecoo = OdometerEntry(
      id = "1",
      title = "Viagem 1",
      vehicleName = "Jaecoo 8",
      totalKm = 100.0,
      hevKm = 40.0,
      evKm = 60.0
    )
    val tripByd = OdometerEntry(
      id = "2",
      title = "Viagem 2",
      vehicleName = "BYD Song Plus",
      totalKm = 150.0,
      hevKm = 50.0,
      evKm = 100.0
    )
    val entries = listOf(tripJaecoo, tripByd)
    val jaecooFiltered = entries.filter { it.vehicleName.contains("Jaecoo", ignoreCase = true) }
    assertEquals(1, jaecooFiltered.size)

    val csv = CsvManager.exportToCsv(jaecooFiltered, AppLanguage.PT_BR)
    assertTrue(csv.contains("Jaecoo 8"))
    assertFalse(csv.contains("BYD Song Plus"))
  }

  @Test
  fun csvParse_discardsTrailingEmptyExcelRowsAndMatchesAccentedHeaders() {
    val dummyVehicle = Vehicle(
      id = "jaecoo_8",
      name = "Jaecoo 8",
      type = VehicleType.PHEV,
      batteryCapacityKwh = 18.0,
      electricConsumptionKwh100km = 16.0,
      gasolineConsumptionKmL = 15.0
    )
    val csvWithExcelEmptyRows = buildString {
      appendLine("ID;Data;Título;Veículo;Tipo;Total_Início_Km;Total_Fim_Km;Total_Km;HEV_Início_Km;HEV_Fim_Km;HEV_Km;EV_Km;Combustível_Litros")
      // Real trip
      appendLine("1;15/09/2026;Viagem 1;Jaecoo 8;PHEV;1000;1050;50;200;210;10;40;1.5")
      // Another real trip without vehicle filled in (should default to currentVehicle)
      appendLine("2;16/09/2026;Viagem 2;;PHEV;1050;1100;50;210;220;10;40;1.5")
      // 50 trailing blank rows like Excel exports
      repeat(50) {
        appendLine(";;;;;;;;;;;;")
      }
    }

    val parsed = CsvManager.parseCsv(
      csvText = csvWithExcelEmptyRows,
      defaultVehicle = dummyVehicle,
      allVehicles = listOf(dummyVehicle)
    )

    // Should only have 2 real trips, completely ignoring the 50 blank rows!
    assertEquals(2, parsed.size)
    assertEquals("Jaecoo 8", parsed[0].vehicleName)
    assertEquals("jaecoo_8", parsed[0].vehicleId)
    assertEquals("Jaecoo 8", parsed[1].vehicleName)
    assertEquals("jaecoo_8", parsed[1].vehicleId)
  }

  @Test
  fun csvParse_handlesExcelEditedFormats_thousandsAndDecimals() {
    val excelContent = """
      "ID";"Data";"Título";"Veículo";"Km Inicial";"Km Final";"Km Total";"Combustível (Litros)";"Preço Gasolina";"Preço Energia"
      "1";"22/09/2026 14:30";"Viagem Cidade";"Jaecoo 8";"15.000";"15.250,50";"250,5";"12,50";"R$ 6,59";"R$ 1,25"
    """.trimIndent()

    val parsed = CsvManager.parseCsv(excelContent)
    assertEquals(1, parsed.size)
    val trip = parsed[0]
    assertEquals(15000.0, trip.totalStartKm, 0.01)
    assertEquals(15250.50, trip.totalEndKm, 0.01)
    assertEquals(250.50, trip.totalKm, 0.01)
    assertEquals(12.50, trip.fuelLiters, 0.01)
    assertEquals(6.59, trip.gasPriceLiter, 0.01)
    assertEquals(1.25, trip.energyPriceKwh, 0.01)
  }

  @Test
  fun csvParse_handlesUtf16BytesFromExcel() {
    val csvString = "ID;Data;Titulo;Veiculo;Total_Km\n1;22/09/2026;Teste UTF-16;Jaecoo 8;120\n"
    // Encode with UTF-16 LE and prepend BOM (0xFF, 0xFE) like Windows Excel
    val utf16Bytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + csvString.toByteArray(Charsets.UTF_16LE)

    val parsed = CsvManager.parseSpreadsheetBytes(utf16Bytes)
    assertEquals(1, parsed.size)
    assertEquals("Teste UTF-16", parsed[0].title)
    assertEquals(120.0, parsed[0].totalKm, 0.01)
  }

  @Test
  fun csvParse_handlesTabDelimitedExcelExport() {
    val tabDelimited = "ID\tData\tTitulo\tVeiculo\tTotal_Km\n1\t22/09/2026\tExport Tab\tJaecoo 8\t85,5\n"
    val parsed = CsvManager.parseCsv(tabDelimited)
    assertEquals(1, parsed.size)
    assertEquals("Export Tab", parsed[0].title)
    assertEquals(85.5, parsed[0].totalKm, 0.01)
  }

  @Test
  fun parseSpreadsheetDouble_handlesAllVariations() {
    assertEquals(15000.0, CsvManager.parseSpreadsheetDouble("15.000"), 0.01)
    assertEquals(15250.50, CsvManager.parseSpreadsheetDouble("15.250,50"), 0.01)
    assertEquals(15250.50, CsvManager.parseSpreadsheetDouble("15,250.50"), 0.01)
    assertEquals(12.5, CsvManager.parseSpreadsheetDouble("12,5"), 0.01)
    assertEquals(12.5, CsvManager.parseSpreadsheetDouble("12.5"), 0.01)
    assertEquals(6.59, CsvManager.parseSpreadsheetDouble("R$ 6,59"), 0.01)
    assertEquals(12.5, CsvManager.parseSpreadsheetDouble(" 12,5 L "), 0.01)
    assertEquals(15000.0, CsvManager.parseSpreadsheetDouble("15.000 km"), 0.01)
    assertEquals(0.0, CsvManager.parseSpreadsheetDouble(""), 0.01)
  }

  @Test
  fun phevHevConsumption_includesEngineGeneratedEvAndRegeneration() {
    val tripWithExcess = OdometerEntry(
      id = "excess-ev-test",
      title = "Viagem Longa",
      vehicleName = "Jaecoo 8",
      vehicleType = VehicleType.PHEV,
      totalKm = 150.0,
      hevKm = 50.0,
      evKm = 100.0,
      fuelLiters = 5.0,
      batteryCapacityKwh = 18.3,
      electricConsumptionKwh100km = 17.5,
      chargingLocation = ChargingLocation.HOME,
      rechargeInitialBatteryPercents = listOf(25.0),
      rechargeBatteryPercents = listOf(100.0)
    )

    // Usable battery = 100% - 25% = 75% -> 18.3 * 0.75 = 13.725 kWh
    // Max EV from recharge = (13.725 / 17.5) * 100 = 78.428 km
    // EV surplus (engine generated / regen) = 100 - 78.428 = 21.571 km
    assertTrue(tripWithExcess.maxRechargeEvKm > 78.0 && tripWithExcess.maxRechargeEvKm < 79.0)
    assertTrue(tripWithExcess.excessEvKm > 21.0 && tripWithExcess.excessEvKm < 22.0)
    
    // Effective fuel km = 50 HEV + 21.571 excess EV = 71.571 km
    assertEquals(tripWithExcess.hevKm + tripWithExcess.excessEvKm, tripWithExcess.effectiveFuelKm, 0.01)
    assertTrue(tripWithExcess.effectiveFuelKm > 71.0 && tripWithExcess.effectiveFuelKm < 72.0)

    // Real HEV km/L = 71.571 / 5.0 = 14.31 km/L
    assertEquals(tripWithExcess.effectiveFuelKm / 5.0, tripWithExcess.realHevKmL, 0.01)
  }

  @Test
  fun phevHevConsumption_withoutPlugRecharge_allEvIsEngineGeneratedAndRegen() {
    val tripNoPlug = OdometerEntry(
      id = "no-plug-test",
      title = "Viagem Sem Recarga",
      vehicleName = "Jaecoo 8",
      vehicleType = VehicleType.PHEV,
      totalKm = 120.0,
      hevKm = 70.0,
      evKm = 50.0,
      fuelLiters = 6.0,
      chargingLocation = ChargingLocation.NONE,
      rechargeLocations = listOf(ChargingLocation.NONE)
    )

    // With no plug-in recharge, maxRechargeEvKm is 0.0
    assertEquals(0.0, tripWithNoPlugRechargeMaxKm(tripNoPlug), 0.01)
    assertEquals(0.0, tripNoPlug.rechargeEvKm, 0.01)
    // All 50 km EV was created by ICE + Regen
    assertEquals(50.0, tripNoPlug.excessEvKm, 0.01)
    // Effective fuel km = 70 HEV + 50 EV = 120 km
    assertEquals(120.0, tripNoPlug.effectiveFuelKm, 0.01)
    // HEV average = 120 / 6 = 20 km/L
    assertEquals(20.0, tripNoPlug.realHevKmL, 0.01)
    // Electric cost from grid = R$ 0.00
    assertEquals(0.0, tripNoPlug.electricCost, 0.01)
  }

  @Test
  fun multiRecharge_usesConsecutiveBatteryLevelsAccurately() {
    // User scenario:
    // Recharge 1 charges to 100%. Battery is discharged to 45%.
    // Recharge 2 begins at 45% and charges to 100%. Discharged to reserve 25%.
    val multiTrip = OdometerEntry(
      id = "multi-recharge-test",
      title = "Viagem Multi Recarga",
      vehicleName = "Jaecoo 7",
      vehicleType = VehicleType.PHEV,
      batteryCapacityKwh = 18.3,
      electricConsumptionKwh100km = 17.5,
      totalKm = 150.0,
      evKm = 120.0,
      hevKm = 30.0,
      chargingLocation = ChargingLocation.HOME,
      rechargeCount = 2,
      rechargeLocations = listOf(ChargingLocation.HOME, ChargingLocation.STATION),
      rechargeInitialBatteryPercents = listOf(25.0, 45.0),
      rechargeBatteryPercents = listOf(100.0, 100.0),
      homeEnergyPrice = 1.0,
      publicEnergyPrice = 2.0
    )

    // Usable percent:
    // Leg 1: 100% - 45% = 55%
    // Leg 2: 100% - 25% = 75%
    // Total usable: 55% + 75% = 130%
    assertEquals(130.0, multiTrip.totalUsableBatteryPercent, 0.01)

    // Total usable kWh = 18.3 * 1.30 = 23.79 kWh
    // Max EV from recharge = (23.79 / 17.5) * 100 = 135.94 km
    assertTrue(multiTrip.maxRechargeEvKm > 135.0 && multiTrip.maxRechargeEvKm < 136.0)

    // For 120 km of EV driven:
    // Consumed kWh = 120 * 17.5 / 100 = 21.0 kWh
    assertEquals(21.0, multiTrip.totalEnergyKwh, 0.01)

    // Leg 1 (Home @ 1.0): 18.3 * 0.55 = 10.065 kWh -> Cost = 10.065 * 1.0 = 10.065
    // Leg 2 (Station @ 2.0): remaining 21.0 - 10.065 = 10.935 kWh -> Cost = 10.935 * 2.0 = 21.87
    // Total cost = 10.065 + 21.87 = 31.935
    assertEquals(31.935, multiTrip.electricCost, 0.01)
  }

  @Test
  fun addNewRecharge_finalLevelDefaultsTo100Percent() {
    val singleRechargeTrip = OdometerEntry(
      id = "test-recharge-default-100",
      timestamp = 1726330800000L,
      title = "Test Trip",
      vehicleName = "Song Plus",
      vehicleType = VehicleType.PHEV,
      totalKm = 100.0,
      evKm = 80.0,
      hevKm = 20.0,
      rechargeCount = 3,
      rechargeBatteryPercents = listOf(80.0) // initial recharge was 80%, 2 new recharges added
    )

    val finals = singleRechargeTrip.effectiveRechargeBatteryPercents
    assertEquals(3, finals.size)
    assertEquals(80.0, finals[0], 0.01)
    assertEquals(100.0, finals[1], 0.01)
    assertEquals(100.0, finals[2], 0.01)
  }

  @Test
  fun userScenario_twoRecharges_80PercentFirst_100PercentLast_withReserve25Percent() {
    val trip = OdometerEntry(
      id = "test-cuj-user-2-recharges",
      timestamp = 1726330800000L,
      title = "Viagem 2 Recargas",
      vehicleName = "Song Plus",
      vehicleType = VehicleType.PHEV,
      batteryCapacityKwh = 18.3,
      electricConsumptionKwh100km = 14.0,
      totalKm = 100.0,
      evKm = 90.0,
      hevKm = 10.0,
      fuelLiters = 0.5,
      rechargeCount = 2,
      rechargeLocations = listOf(ChargingLocation.HOME, ChargingLocation.HOME),
      rechargeInitialBatteryPercents = listOf(25.0, 80.0),
      rechargeBatteryPercents = listOf(80.0, 100.0),
      homeEnergyPrice = 1.30,
      publicEnergyPrice = 1.30
    )

    // First recharge final is 80%, so 100% - 80% = 20% used in leg 1.
    // Last recharge final is 100%, discharges down to 25% reserve: 100% - 25% = 75%.
    // Total usable: 20% + 75% = 95%
    assertEquals(95.0, trip.totalUsableBatteryPercent, 0.01)
  }

  @Test
  fun userScenario_threeRecharges_userExample_20Percent_70Percent_75Percent() {
    // User's exact prompt example:
    // Recharge 1: init 80%, final 100% -> 20% initial usage (100% - 80%) + 20% before next recharge (100% - 80%)
    // Recharge 2: init 80%, final 100% -> consumed 70% before recharge 3 (100% - 30%)
    // Recharge 3 (last): final 100% -> discharges down to 25% limit = 75%
    val trip = OdometerEntry(
      id = "test-cuj-user-3-recharges",
      timestamp = 1726330800000L,
      title = "Viagem 3 Recargas Exemplo Usuário",
      vehicleName = "Song Plus",
      vehicleType = VehicleType.PHEV,
      batteryCapacityKwh = 18.3,
      electricConsumptionKwh100km = 14.0,
      totalKm = 200.0,
      evKm = 180.0,
      hevKm = 20.0,
      rechargeCount = 3,
      rechargeLocations = listOf(ChargingLocation.HOME, ChargingLocation.HOME, ChargingLocation.HOME),
      rechargeInitialBatteryPercents = listOf(80.0, 80.0, 30.0),
      rechargeBatteryPercents = listOf(100.0, 100.0, 100.0),
      homeEnergyPrice = 1.30,
      publicEnergyPrice = 1.30
    )

    // Leg 0: 100% - 80% = 20%
    // Recharge 1 -> 2: 100% - 80% = 20%
    // Recharge 2 -> 3: 100% - 30% = 70%
    // Last recharge: 100% - 25% = 75%
    // Total: 20% + 20% + 70% + 75% = 185%
    assertEquals(185.0, trip.totalUsableBatteryPercent, 0.01)
  }

  private fun tripWithNoPlugRechargeMaxKm(entry: OdometerEntry): Double {
    return entry.maxRechargeEvKm
  }
}

