package com.example.data

import java.util.*

/**
 * Dados de teste fictícios para o veículo "Exemplo PHEV" abrangendo 1 ano de consumo.
 * Permite ao usuário entender e visualizar imediatamente o funcionamento do app,
 * tabelas, histórico e gráficos analíticos.
 */
object ExamplePhevTestData {

    const val VEHICLE_ID = "exemplo_phev"
    const val VEHICLE_NAME = "Exemplo PHEV"
    const val BATTERY_CAPACITY_KWH = 34.4
    const val ELEC_CONS_KWH_100KM = 22.50
    const val GAS_CONS_KM_L = 14.50

    // Odômetros acumulados ao término do 1 ano de histórico simulado
    const val LAST_TOTAL_END_KM = 16400.0
    const val LAST_HEV_END_KM = 6595.0

    // Valores de sugestão para testar uma viagem no formulário
    const val DEMO_TOTAL_END_KM = 16670.0
    const val DEMO_HEV_END_KM = 6655.0
    const val DEMO_FUEL_LITERS = 4.0
    const val DEMO_TRIP_TITLE = "Viagem Demonstração (270 km)"

    val exampleVehicle = Vehicle(
        id = VEHICLE_ID,
        name = VEHICLE_NAME,
        brand = "Exemplo",
        type = VehicleType.PHEV,
        tag = "Híbrido Plug-in (Exemplo)",
        batteryCapacityKwh = BATTERY_CAPACITY_KWH,
        electricConsumptionKwh100km = ELEC_CONS_KWH_100KM,
        gasolineConsumptionKmL = GAS_CONS_KM_L,
        maxAcChargeKw = 7.0,
        imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
    )

    /**
     * Gera 44 viagens fictícias realistas distribuídas ao longo de 12 meses (1 ano),
     * cobrindo viagens urbanas, rodoviárias e uso misto com progressão contínua do odômetro.
     */
    fun generateOneYearTrips(baseTimestamp: Long = System.currentTimeMillis()): List<OdometerEntry> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTimestamp
        }

        data class TripSpec(
            val daysAgo: Int,
            val title: String,
            val totalDistance: Double,
            val evDistance: Double,
            val fuelPumpedLiters: Double,
            val gasPrice: Double,
            val energyPrice: Double,
            val recharges: Int,
            val location: ChargingLocation = ChargingLocation.HOME
        )

        val specs = listOf(
            // Mês 1 (12 meses atrás - Outubro 2025)
            TripSpec(355, "Uso Urbano & Trabalho", 280.0, 210.0, 4.6, 5.89, 0.98, 2),
            TripSpec(346, "Fim de Semana no Interior", 430.0, 150.0, 18.2, 5.89, 0.98, 1),
            TripSpec(337, "Rotina Semanal Mista", 310.0, 220.0, 6.1, 5.92, 0.98, 2),
            TripSpec(328, "Deslocamento Cidade", 260.0, 200.0, 4.1, 5.92, 0.98, 2),

            // Mês 2 (Novembro 2025)
            TripSpec(318, "Trabalho e Serviços", 295.0, 225.0, 4.8, 5.95, 0.98, 2),
            TripSpec(308, "Bate e Volta Litoral", 380.0, 160.0, 14.3, 5.95, 1.02, 2, ChargingLocation.STATION),
            TripSpec(299, "Semana de Chuva / Trânsito", 270.0, 190.0, 5.6, 5.99, 1.02, 2),
            TripSpec(290, "Uso Urbano Diário", 305.0, 230.0, 5.1, 5.99, 1.02, 2),

            // Mês 3 (Dezembro 2025)
            TripSpec(280, "Rotina Cidade", 290.0, 220.0, 4.7, 6.05, 1.02, 2),
            TripSpec(271, "Compras de Fim de Ano", 250.0, 195.0, 3.8, 6.09, 1.02, 2),
            TripSpec(261, "Viagem Férias de Fim de Ano", 620.0, 180.0, 28.5, 6.15, 1.05, 2, ChargingLocation.STATION),

            // Mês 4 (Janeiro 2026)
            TripSpec(250, "Retorno de Férias Rodovia", 580.0, 170.0, 26.8, 6.15, 1.05, 2),
            TripSpec(241, "Uso Diário Cidade", 275.0, 215.0, 4.2, 6.15, 1.05, 2),
            TripSpec(232, "Visita a Clientes", 310.0, 225.0, 6.0, 6.15, 1.05, 2),
            TripSpec(223, "Fim de Semana Camping", 390.0, 160.0, 15.5, 6.18, 1.05, 2),

            // Mês 5 (Fevereiro 2026 - Carnaval)
            TripSpec(214, "Rotina Pré-Carnaval", 260.0, 200.0, 4.0, 6.18, 1.05, 2),
            TripSpec(204, "Viagem Carnaval Serra", 510.0, 175.0, 22.4, 6.19, 1.08, 2, ChargingLocation.STATION),
            TripSpec(195, "Retorno do Carnaval", 490.0, 170.0, 21.0, 6.19, 1.08, 2),
            TripSpec(186, "Pós-Feriado Cidade", 280.0, 220.0, 4.2, 6.19, 1.08, 2),

            // Mês 6 (Março 2026)
            TripSpec(176, "Semana Escritório", 300.0, 230.0, 4.9, 6.22, 1.08, 2),
            TripSpec(167, "Passeio Represa", 350.0, 180.0, 11.2, 6.22, 1.08, 2),
            TripSpec(158, "Rotina Mista Chuva", 285.0, 210.0, 5.2, 6.22, 1.08, 2),
            TripSpec(149, "Fechamento Mensal", 290.0, 225.0, 4.5, 6.25, 1.08, 2),

            // Mês 7 (Abril 2026)
            TripSpec(139, "Feriado Páscoa Família", 460.0, 165.0, 19.8, 6.25, 1.08, 2),
            TripSpec(130, "Retorno Páscoa Trânsito", 440.0, 170.0, 18.0, 6.25, 1.08, 2),
            TripSpec(121, "Uso Diário Cidade", 270.0, 210.0, 4.1, 6.25, 1.08, 2),
            TripSpec(112, "Reuniões Externas", 320.0, 230.0, 6.0, 6.25, 1.08, 2),

            // Mês 8 (Maio 2026)
            TripSpec(102, "Dia das Mães Interior", 410.0, 160.0, 16.5, 6.25, 1.08, 2),
            TripSpec(93, "Rotina Urbana Normal", 285.0, 220.0, 4.4, 6.25, 1.08, 2),
            TripSpec(84, "Deslocamentos Noturnos", 260.0, 205.0, 3.9, 6.25, 1.08, 2),
            TripSpec(75, "Fim de Mês Cidade", 295.0, 225.0, 4.8, 6.25, 1.08, 2),

            // Mês 9 (Junho 2026)
            TripSpec(65, "Corpus Christi Serra", 480.0, 170.0, 20.6, 6.28, 1.10, 2, ChargingLocation.STATION),
            TripSpec(56, "Retorno Feriado Frio", 460.0, 165.0, 19.5, 6.28, 1.10, 2),
            TripSpec(47, "Rotina Clima Frio", 280.0, 215.0, 4.5, 6.28, 1.10, 2),
            TripSpec(38, "Uso Misto Urbano", 300.0, 225.0, 5.0, 6.28, 1.10, 2),

            // Mês 10 / 11 (Julho e Agosto 2026)
            TripSpec(30, "Férias de Julho Interior", 520.0, 170.0, 23.1, 6.25, 1.10, 2),
            TripSpec(23, "Uso Urbano Cidade", 260.0, 205.0, 3.7, 6.22, 1.10, 2),
            TripSpec(16, "Semana Trabalho", 290.0, 225.0, 4.3, 6.22, 1.10, 2),
            TripSpec(10, "Bate e Volta Fim de Semana", 370.0, 160.0, 13.9, 6.25, 1.10, 2),

            // Mês 12 (Setembro 2026 - Dias recentes)
            TripSpec(7, "Rotina Escritório", 280.0, 220.0, 4.0, 6.25, 1.10, 2),
            TripSpec(4, "Uso Urbano & Compras", 240.0, 195.0, 3.0, 6.25, 1.10, 2),
            TripSpec(1, "Semana Atual", 270.0, 210.0, 4.0, 6.25, 1.10, 2)
        )

        var currentOdo = 1500.0
        var currentHevOdo = 450.0

        val entries = mutableListOf<OdometerEntry>()

        specs.forEachIndexed { index, spec ->
            val tripCalendar = Calendar.getInstance().apply {
                timeInMillis = baseTimestamp - (spec.daysAgo.toLong() * 24L * 60L * 60L * 1000L) + (index * 3600000L)
            }

            val hevDist = (spec.totalDistance - spec.evDistance).coerceAtLeast(0.0)
            val startTotal = currentOdo
            val endTotal = currentOdo + spec.totalDistance
            val startHev = currentHevOdo
            val endHev = currentHevOdo + hevDist

            currentOdo = endTotal
            currentHevOdo = endHev

            val entry = OdometerEntry(
                id = "exemplo_phev_mock_trip_${index + 1}",
                timestamp = tripCalendar.timeInMillis,
                title = spec.title,
                vehicleId = VEHICLE_ID,
                vehicleName = VEHICLE_NAME,
                vehicleType = VehicleType.PHEV,
                totalKm = spec.totalDistance,
                evKm = spec.evDistance,
                hevKm = hevDist,
                electricConsumptionKwh100km = ELEC_CONS_KWH_100KM,
                gasolineConsumptionKmL = GAS_CONS_KM_L,
                energyPriceKwh = spec.energyPrice,
                gasPriceLiter = spec.gasPrice,
                fuelLiters = spec.fuelPumpedLiters,
                averageHevKmL = if (spec.fuelPumpedLiters > 0) hevDist / spec.fuelPumpedLiters else GAS_CONS_KM_L,
                averageGlobalKmL = if (spec.fuelPumpedLiters > 0) spec.totalDistance / spec.fuelPumpedLiters else 0.0,
                totalStartKm = startTotal,
                totalEndKm = endTotal,
                hevStartKm = startHev,
                hevEndKm = endHev,
                chargingLocation = spec.location,
                batteryCapacityKwh = BATTERY_CAPACITY_KWH,
                batteryMaxPercent = 75.0,
                batteryStartPercent = 100.0,
                rechargeCount = spec.recharges,
                rechargeLocations = List(spec.recharges) { spec.location },
                rechargeBatteryPercents = List(spec.recharges) { 100.0 },
                rechargeInitialBatteryPercents = List(spec.recharges) { 25.0 },
                homeEnergyPrice = spec.energyPrice,
                publicEnergyPrice = 2.10
            )

            entries.add(entry)
        }

        return entries
    }
}
