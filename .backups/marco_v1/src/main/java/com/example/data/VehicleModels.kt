package com.example.data

enum class VehicleType(val label: String, val shortTag: String) {
    BEV("100% Elétrico", "Elétrico"),
    PHEV("Híbrido Plug-in", "PHEV"),
    HEV("Híbrido Convencional", "Híbrido")
}

data class Vehicle(
    val id: String,
    val name: String,
    val brand: String = "",
    val type: VehicleType = VehicleType.PHEV,
    val tag: String = type.label,
    val batteryCapacityKwh: Double,
    val electricConsumptionKwh100km: Double,
    val gasolineConsumptionKmL: Double,
    val maxAcChargeKw: Double = 7.0,
    val imageUrl: String = "",
    val isCustom: Boolean = false
)

object VehicleCatalog {
    val defaultVehicles = listOf(
        // ==================== BYD ====================
        Vehicle(
            id = "dolphin_mini",
            name = "BYD Dolphin Mini EV",
            brand = "BYD",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 38.0,
            electricConsumptionKwh100km = 13.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_dolphin_gs",
            name = "BYD Dolphin GS EV",
            brand = "BYD",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 44.9,
            electricConsumptionKwh100km = 14.20,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_dolphin_plus",
            name = "BYD Dolphin Plus EV",
            brand = "BYD",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 60.48,
            electricConsumptionKwh100km = 15.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_song_plus",
            name = "BYD Song Plus DM-i",
            brand = "BYD",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 18.3,
            electricConsumptionKwh100km = 19.20,
            gasolineConsumptionKmL = 17.5,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_song_pro_dmi",
            name = "BYD Song Pro DM-i",
            brand = "BYD",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 18.3,
            electricConsumptionKwh100km = 18.50,
            gasolineConsumptionKmL = 18.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_king",
            name = "BYD King DM-i",
            brand = "BYD",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 18.3,
            electricConsumptionKwh100km = 18.00,
            gasolineConsumptionKmL = 19.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_yuan_pro",
            name = "BYD Yuan Pro EV",
            brand = "BYD",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 45.1,
            electricConsumptionKwh100km = 14.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_yuan_plus",
            name = "BYD Yuan Plus EV",
            brand = "BYD",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 60.5,
            electricConsumptionKwh100km = 16.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_shark_phev",
            name = "BYD Shark PHEV",
            brand = "BYD",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 29.58,
            electricConsumptionKwh100km = 28.00,
            gasolineConsumptionKmL = 13.5,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_seal",
            name = "BYD Seal EV",
            brand = "BYD",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 82.5,
            electricConsumptionKwh100km = 18.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_tan",
            name = "BYD Tan EV",
            brand = "BYD",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 108.8,
            electricConsumptionKwh100km = 22.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "byd_han",
            name = "BYD Han EV",
            brand = "BYD",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 85.4,
            electricConsumptionKwh100km = 18.80,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== GWM / Haval / Ora / Tank ====================
        Vehicle(
            id = "haval_h6_hev",
            name = "GWM Haval H6 HEV",
            brand = "GWM Haval",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 1.8,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 16.5,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "haval_h6_phev16",
            name = "GWM Haval H6 PHEV19",
            brand = "GWM Haval",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 19.09,
            electricConsumptionKwh100km = 19.20,
            gasolineConsumptionKmL = 17.5,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "haval_h6_phev34",
            name = "GWM Haval H6 PHEV34",
            brand = "GWM Haval",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 34.0,
            electricConsumptionKwh100km = 21.50,
            gasolineConsumptionKmL = 15.5,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "haval_h6_gt",
            name = "GWM Haval H6 GT PHEV",
            brand = "GWM Haval",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 34.0,
            electricConsumptionKwh100km = 22.00,
            gasolineConsumptionKmL = 15.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gwm_ora_03_skin",
            name = "GWM Ora 03 Skin EV",
            brand = "GWM Ora",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 48.0,
            electricConsumptionKwh100km = 14.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gwm_ora_03_gt",
            name = "GWM Ora 03 GT EV",
            brand = "GWM Ora",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 63.0,
            electricConsumptionKwh100km = 15.20,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gwm_tank_300_hi4t",
            name = "GWM Tank 300 Hi4-T",
            brand = "GWM Tank",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 37.1,
            electricConsumptionKwh100km = 27.00,
            gasolineConsumptionKmL = 11.5,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== JAECOO & OMODA ====================
        Vehicle(
            id = "jaecoo_7",
            name = "Jaecoo 7 PHEV",
            brand = "Jaecoo",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 18.3,
            electricConsumptionKwh100km = 20.33,
            gasolineConsumptionKmL = 16.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "jaecoo_8",
            name = "Jaecoo 8 PHEV",
            brand = "Jaecoo",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 34.4,
            electricConsumptionKwh100km = 22.50,
            gasolineConsumptionKmL = 14.5,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "omoda_5_ev",
            name = "Omoda 5 EV (E5)",
            brand = "Omoda",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 61.0,
            electricConsumptionKwh100km = 15.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "omoda_5_phev",
            name = "Omoda 5 PHEV",
            brand = "Omoda",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 18.3,
            electricConsumptionKwh100km = 17.50,
            gasolineConsumptionKmL = 16.5,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== CAOA CHERY ====================
        Vehicle(
            id = "tiggo_8_pro",
            name = "Caoa Chery Tiggo 8 Pro Plug-in",
            brand = "Caoa Chery",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 19.27,
            electricConsumptionKwh100km = 22.00,
            gasolineConsumptionKmL = 14.5,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "tiggo_7_pro_phev",
            name = "Caoa Chery Tiggo 7 Pro Plug-in",
            brand = "Caoa Chery",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 19.27,
            electricConsumptionKwh100km = 20.50,
            gasolineConsumptionKmL = 15.2,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "tiggo_7_pro_hybrid",
            name = "Caoa Chery Tiggo 7 Pro Hybrid Max Drive",
            brand = "Caoa Chery",
            type = VehicleType.HEV,
            tag = "Híbrido (48V)",
            batteryCapacityKwh = 1.0,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 13.8,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "chery_icar",
            name = "Caoa Chery iCar EV",
            brand = "Caoa Chery",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 30.8,
            electricConsumptionKwh100km = 12.80,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== TOYOTA ====================
        Vehicle(
            id = "toyota_corolla_cross_hybrid",
            name = "Toyota Corolla Cross Hybrid",
            brand = "Toyota",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 1.3,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 17.8,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "toyota_corolla_sedan_hybrid",
            name = "Toyota Corolla Altis Hybrid",
            brand = "Toyota",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 1.3,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 18.2,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "toyota_rav4_phev",
            name = "Toyota RAV4 Plug-in Hybrid",
            brand = "Toyota",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 18.1,
            electricConsumptionKwh100km = 19.50,
            gasolineConsumptionKmL = 17.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== VOLVO ====================
        Vehicle(
            id = "volvo_ex30",
            name = "Volvo EX30 Ultra / Plus EV",
            brand = "Volvo",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 69.0,
            electricConsumptionKwh100km = 16.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1502877338535-766e1452684a?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "volvo_xc60",
            name = "Volvo XC60 Recharge PHEV",
            brand = "Volvo",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 18.8,
            electricConsumptionKwh100km = 23.00,
            gasolineConsumptionKmL = 13.0,
            maxAcChargeKw = 6.4,
            imageUrl = "https://images.unsplash.com/photo-1502877338535-766e1452684a?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "volvo_xc40_recharge",
            name = "Volvo XC40 / EX40 Recharge EV",
            brand = "Volvo",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 82.0,
            electricConsumptionKwh100km = 18.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1502877338535-766e1452684a?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "volvo_xc90_recharge",
            name = "Volvo XC90 Recharge PHEV",
            brand = "Volvo",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 18.8,
            electricConsumptionKwh100km = 25.50,
            gasolineConsumptionKmL = 11.8,
            maxAcChargeKw = 6.4,
            imageUrl = "https://images.unsplash.com/photo-1502877338535-766e1452684a?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== ZEEKR ====================
        Vehicle(
            id = "zeekr_x",
            name = "Zeekr X Flagship EV",
            brand = "Zeekr",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 66.0,
            electricConsumptionKwh100km = 16.80,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "zeekr_001",
            name = "Zeekr 001 Performance EV",
            brand = "Zeekr",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 100.0,
            electricConsumptionKwh100km = 18.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "zeekr_7x",
            name = "Zeekr 7X SUV EV",
            brand = "Zeekr",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 100.0,
            electricConsumptionKwh100km = 19.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== HONDA ====================
        Vehicle(
            id = "honda_civic_ehev",
            name = "Honda Civic Advanced Hybrid e:HEV",
            brand = "Honda",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 1.05,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 18.3,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "honda_crv_ehev",
            name = "Honda CR-V Advanced Hybrid e:HEV",
            brand = "Honda",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 1.5,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 14.6,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== GAC / AION / HYPTEC ====================
        Vehicle(
            id = "gac_aion_y_plus",
            name = "GAC Aion Y Plus EV",
            brand = "GAC Aion",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 63.2,
            electricConsumptionKwh100km = 15.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_aion_es",
            name = "GAC Aion ES EV",
            brand = "GAC Aion",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 55.2,
            electricConsumptionKwh100km = 14.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_aion_v_plus",
            name = "GAC Aion V Plus EV",
            brand = "GAC Aion",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 75.3,
            electricConsumptionKwh100km = 16.20,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_aion_s_max",
            name = "GAC Aion S Max EV",
            brand = "GAC Aion",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 67.9,
            electricConsumptionKwh100km = 15.20,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_hyptec_ht",
            name = "GAC Hyptec HT EV (Gull-wing)",
            brand = "GAC Hyptec",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 82.5,
            electricConsumptionKwh100km = 17.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_hyptec_ssr",
            name = "GAC Hyptec SSR Supercar EV",
            brand = "GAC Hyptec",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 74.7,
            electricConsumptionKwh100km = 19.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_gs8_hybrid",
            name = "GAC GS8 Hybrid 7L HEV",
            brand = "GAC Motor",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 2.1,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 15.0,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_gs8_phev",
            name = "GAC GS8 Plug-in Hybrid 7L PHEV",
            brand = "GAC Motor",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 25.6,
            electricConsumptionKwh100km = 21.00,
            gasolineConsumptionKmL = 14.5,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_emkoo_hybrid",
            name = "GAC Emkoo Hybrid HEV",
            brand = "GAC Motor",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 2.1,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 16.8,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_empow_hybrid",
            name = "GAC Empow Hybrid Sport HEV",
            brand = "GAC Motor",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 2.1,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 17.2,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gac_e9_phev",
            name = "GAC E9 Luxury Minivan PHEV",
            brand = "GAC Motor",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 25.6,
            electricConsumptionKwh100km = 21.50,
            gasolineConsumptionKmL = 14.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== JETOUR ====================
        Vehicle(
            id = "jetour_dashing",
            name = "Jetour Dashing i-DM PHEV",
            brand = "Jetour",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 19.27,
            electricConsumptionKwh100km = 19.50,
            gasolineConsumptionKmL = 16.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "jetour_t2_phev",
            name = "Jetour T2 PHEV",
            brand = "Jetour",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 26.7,
            electricConsumptionKwh100km = 22.00,
            gasolineConsumptionKmL = 14.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== LEAPMOTOR & NETA ====================
        Vehicle(
            id = "leapmotor_c10",
            name = "Leapmotor C10 REEV / EV",
            brand = "Leapmotor",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 28.4,
            electricConsumptionKwh100km = 21.00,
            gasolineConsumptionKmL = 16.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "neta_x",
            name = "Neta X 500 EV",
            brand = "Neta",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 61.7,
            electricConsumptionKwh100km = 15.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "neta_aya",
            name = "Neta Aya EV",
            brand = "Neta",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 40.7,
            electricConsumptionKwh100km = 13.80,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 6.6,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== RENAULT, PEUGEOT, CITROËN, JEEP ====================
        Vehicle(
            id = "renault_kwid_etech",
            name = "Renault Kwid E-Tech EV",
            brand = "Renault",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 26.8,
            electricConsumptionKwh100km = 13.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 7.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "renault_megane_etech",
            name = "Renault Megane E-Tech EV",
            brand = "Renault",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 60.0,
            electricConsumptionKwh100km = 15.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "citroen_e_c3",
            name = "Citroën ë-C3 EV",
            brand = "Citroën",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 44.0,
            electricConsumptionKwh100km = 14.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 7.4,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "peugeot_e_2008",
            name = "Peugeot e-2008 GT EV",
            brand = "Peugeot",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 54.0,
            electricConsumptionKwh100km = 15.80,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "jeep_compass_4xe",
            name = "Jeep Compass 4xe PHEV",
            brand = "Jeep",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 11.4,
            electricConsumptionKwh100km = 21.00,
            gasolineConsumptionKmL = 14.0,
            maxAcChargeKw = 7.4,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== KIA & HYUNDAI ====================
        Vehicle(
            id = "kia_ev5",
            name = "Kia EV5 Land / Earth EV",
            brand = "Kia",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 88.1,
            electricConsumptionKwh100km = 17.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "kia_niro_hybrid",
            name = "Kia Niro Hybrid HEV",
            brand = "Kia",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 1.32,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 19.8,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "hyundai_ioniq_5",
            name = "Hyundai Ioniq 5 EV",
            brand = "Hyundai",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 84.0,
            electricConsumptionKwh100km = 17.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "hyundai_kona_ev",
            name = "Hyundai Kona EV",
            brand = "Hyundai",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 64.8,
            electricConsumptionKwh100km = 15.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== BMW & MINI ====================
        Vehicle(
            id = "bmw_ix1",
            name = "BMW iX1 eDrive20 / xDrive30 EV",
            brand = "BMW",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 64.7,
            electricConsumptionKwh100km = 17.20,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "bmw_330e",
            name = "BMW 330e M Sport PHEV",
            brand = "BMW",
            type = VehicleType.PHEV,
            tag = "Híbrido Plug-in",
            batteryCapacityKwh = 12.0,
            electricConsumptionKwh100km = 21.50,
            gasolineConsumptionKmL = 14.5,
            maxAcChargeKw = 3.7,
            imageUrl = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "mini_cooper_se",
            name = "Mini Cooper SE EV",
            brand = "Mini",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 54.2,
            electricConsumptionKwh100km = 14.80,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== CHEVROLET & FORD ====================
        Vehicle(
            id = "gm_blazer_ev",
            name = "Chevrolet Blazer EV RS",
            brand = "Chevrolet",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 102.0,
            electricConsumptionKwh100km = 21.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "gm_equinox_ev",
            name = "Chevrolet Equinox EV",
            brand = "Chevrolet",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 85.0,
            electricConsumptionKwh100km = 18.20,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "ford_mustang_mach_e",
            name = "Ford Mustang Mach-E GT EV",
            brand = "Ford",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 98.7,
            electricConsumptionKwh100km = 20.50,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "ford_maverick_hybrid",
            name = "Ford Maverick Hybrid HEV",
            brand = "Ford",
            type = VehicleType.HEV,
            tag = "Híbrido Convencional",
            batteryCapacityKwh = 1.1,
            electricConsumptionKwh100km = 0.0,
            gasolineConsumptionKmL = 15.7,
            maxAcChargeKw = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80"
        ),

        // ==================== PORSCHE & AUDI ====================
        Vehicle(
            id = "porsche_taycan",
            name = "Porsche Taycan 4S EV",
            brand = "Porsche",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 105.0,
            electricConsumptionKwh100km = 19.80,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "porsche_macan_ev",
            name = "Porsche Macan Electric EV",
            brand = "Porsche",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 100.0,
            electricConsumptionKwh100km = 20.00,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        ),
        Vehicle(
            id = "audi_q6_etron",
            name = "Audi Q6 e-tron Performance EV",
            brand = "Audi",
            type = VehicleType.BEV,
            tag = "100% Elétrico",
            batteryCapacityKwh = 100.0,
            electricConsumptionKwh100km = 18.20,
            gasolineConsumptionKmL = 0.0,
            maxAcChargeKw = 11.0,
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        )
    )

    /**
     * Finds smart matching suggestions based on what the user starts typing.
     */
    fun findSmartSuggestion(input: String): Vehicle? {
        if (input.trim().length < 2) return null
        val matches = VehicleSearchEngine.search(defaultVehicles, input)
        return matches.firstOrNull()
    }

    /**
     * Synchronizes and returns latest vehicle database.
     */
    suspend fun fetchLatestVehicleDatabase(): List<Vehicle> {
        kotlinx.coroutines.delay(800)
        return defaultVehicles
    }
}

fun extractImageUrl(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""
    
    // Check if HTML <img ... src="..." ...>
    val imgTagRegex = Regex("""<img[^>]+src\s*=\s*['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE)
    val match = imgTagRegex.find(trimmed)
    if (match != null && match.groupValues.size > 1) {
        return match.groupValues[1]
    }
    
    // Check markdown ![...](url)
    val mdRegex = Regex("""!\[.*?\]\((https?://[^\s)]+)\)""")
    val mdMatch = mdRegex.find(trimmed)
    if (mdMatch != null && mdMatch.groupValues.size > 1) {
        return mdMatch.groupValues[1]
    }
    
    // Extract first URL
    val urlRegex = Regex("""(https?://[^\s"'<>]+)""")
    val urlMatch = urlRegex.find(trimmed)
    if (urlMatch != null) {
        return urlMatch.groupValues[1]
    }
    
    return trimmed
}

fun generateHtmlImgSnippet(imageUrl: String, vehicleName: String): String {
    return """<img src="$imageUrl" alt="$vehicleName" width="320" style="border-radius:12px; object-fit:cover;" />"""
}
