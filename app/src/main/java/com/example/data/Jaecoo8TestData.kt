package com.example.data

import java.util.*

/**
 * Dados de teste fictícios para o Jaecoo 8 PHEV abrangendo 1 ano de consumo.
 * Utilizado para testes visuais completos e validação dos gráficos analíticos.
 * Pode ser facilmente removido ou desativado a qualquer momento.
 */
object Jaecoo8TestData {

    const val VEHICLE_ID = "jaecoo_8"
    const val VEHICLE_NAME = "Jaecoo 8 PHEV"
    const val BATTERY_CAPACITY_KWH = 34.4
    const val ELEC_CONS_KWH_100KM = 22.50
    const val GAS_CONS_KM_L = 14.50

    /**
     * Gera 44 viagens fictícias realistas distribuídas ao longo de 12 meses (1 ano),
     * cobrindo viagens urbanas, rodoviárias e uso misto com progressão contínua do odômetro.
     */
    fun generateOneYearTrips(baseTimestamp: Long = System.currentTimeMillis()): List<OdometerEntry> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTimestamp
        }

        // Definir especificações de cada uma das ~44 viagens ao longo dos 12 meses
        // (aproximadamente 3 a 4 viagens por mês recuando 360 dias)
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
            TripSpec(241, "Uso Leve Pós-Férias", 220.0, 180.0, 2.7, 6.12, 1.05, 2),
            TripSpec(232, "Semana Trabalho", 285.0, 215.0, 4.8, 6.12, 1.05, 2),
            TripSpec(223, "Fim de Semana Família", 340.0, 190.0, 10.1, 6.12, 1.05, 2),

            // Mês 5 (Fevereiro 2026)
            TripSpec(212, "Uso Urbano", 275.0, 210.0, 4.4, 6.08, 1.05, 2),
            TripSpec(203, "Viagem Carnaval", 490.0, 160.0, 21.8, 6.18, 1.05, 2),
            TripSpec(194, "Retorno Carnaval", 460.0, 150.0, 20.4, 6.18, 1.05, 1),
            TripSpec(185, "Rotina Cidade", 260.0, 205.0, 3.7, 6.08, 1.05, 2),

            // Mês 6 (Março 2026)
            TripSpec(174, "Semana Chuvosa Cidade", 290.0, 215.0, 5.2, 6.05, 1.05, 2),
            TripSpec(165, "Passeio Serra Rodovia", 390.0, 150.0, 15.8, 6.09, 1.05, 2),
            TripSpec(156, "Uso Diário Urbano", 280.0, 220.0, 4.1, 6.05, 1.05, 2),
            TripSpec(147, "Trabalho e Reuniões", 310.0, 235.0, 5.0, 6.05, 1.05, 2),

            // Mês 7 (Abril 2026)
            TripSpec(137, "Semana Feriado Páscoa", 360.0, 180.0, 11.9, 6.10, 1.08, 2),
            TripSpec(128, "Rotina Escritório", 270.0, 210.0, 4.0, 6.10, 1.08, 2),
            TripSpec(119, "Bate e Volta Interior", 410.0, 160.0, 16.5, 6.12, 1.08, 2),
            TripSpec(110, "Uso Cidade", 295.0, 225.0, 4.7, 6.12, 1.08, 2),

            // Mês 8 (Maio 2026)
            TripSpec(100, "Semana Dia das Mães", 330.0, 200.0, 8.7, 6.15, 1.08, 2),
            TripSpec(91, "Deslocamento Urbano", 285.0, 220.0, 4.3, 6.15, 1.08, 2),
            TripSpec(82, "Visita Clientes Rodovia", 440.0, 160.0, 18.5, 6.19, 1.08, 2),
            TripSpec(73, "Uso Urbano Misto", 290.0, 220.0, 4.7, 6.15, 1.08, 2),

            // Mês 9 (Junho 2026)
            TripSpec(64, "Passeio Fim de Semana Frio", 350.0, 180.0, 11.4, 6.19, 1.08, 2),
            TripSpec(55, "Semana Cidade", 275.0, 215.0, 4.0, 6.19, 1.08, 2),
            TripSpec(46, "Viagem Campos do Jordão", 420.0, 150.0, 18.1, 6.22, 1.08, 2),
            TripSpec(37, "Rotina Cidade", 280.0, 215.0, 4.4, 6.19, 1.08, 2),

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

        // Odômetro inicial fictício do Jaecoo 8 (por exemplo, 1.500 km)
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
                id = "jaecoo8_mock_trip_${index + 1}",
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
