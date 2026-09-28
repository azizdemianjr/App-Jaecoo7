package com.example.data

import java.text.Normalizer
import java.util.Locale

object VehicleSearchEngine {

    /**
     * Normalizes text by removing accents, diacritics, extra spaces and punctuation.
     * e.g., "Híbrido Elétrico - DM-i" -> "hibrido eletrico dmi"
     */
    fun normalize(text: String): String {
        if (text.isBlank()) return ""
        val normalized = Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        val withoutAccents = normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return withoutAccents.trim()
    }

    /**
     * Produces a clean alphanumeric string for fuzzy matching (removes hyphens, dots, spaces).
     * e.g., "Song Plus DM-i" -> "songplusdmi", "Tank-300" -> "tank300", "ë-C3" -> "ec3"
     */
    fun simplify(text: String): String {
        return normalize(text).replace(Regex("[^a-z0-9]"), "")
    }

    /**
     * Calculates relevance score for a vehicle against a search query.
     * Higher score = better match. 0 = no match.
     */
    fun score(vehicle: Vehicle, query: String): Int {
        val qRaw = query.trim()
        if (qRaw.isEmpty()) return 100

        val qNorm = normalize(qRaw)
        val qTokens = qNorm.split(Regex("\\s+")).filter { it.isNotBlank() }
        val qSimple = simplify(qRaw)

        val nameNorm = normalize(vehicle.name)
        val brandNorm = normalize(vehicle.brand)
        val tagNorm = normalize(vehicle.tag)
        val typeNorm = normalize(vehicle.type.label)
        val combinedNorm = "$brandNorm $nameNorm $tagNorm $typeNorm ${vehicle.brand} ${vehicle.name}"

        val nameSimple = simplify(vehicle.name)
        val brandSimple = simplify(vehicle.brand)
        val combinedSimple = "$brandSimple$nameSimple"

        var totalScore = 0

        // Exact matches
        if (nameNorm.equals(qNorm, ignoreCase = true)) return 1000
        if (combinedNorm.contains(qNorm, ignoreCase = true)) totalScore += 300
        if (nameNorm.startsWith(qNorm, ignoreCase = true)) totalScore += 250
        if (nameSimple.contains(qSimple)) totalScore += 200
        if (combinedSimple.contains(qSimple)) totalScore += 180

        // Match on individual tokens
        var allTokensMatch = true
        for (token in qTokens) {
            val tokenSimple = simplify(token)
            val matchesToken = combinedNorm.contains(token) || 
                               combinedSimple.contains(tokenSimple) ||
                               matchesSynonyms(vehicle, token)
            
            if (matchesToken) {
                totalScore += 50
                if (brandNorm.contains(token)) totalScore += 40
                if (nameNorm.contains(token)) totalScore += 60
            } else {
                allTokensMatch = false
            }
        }

        if (allTokensMatch && qTokens.isNotEmpty()) {
            totalScore += 150
        }

        // Fuzzy Brand matching
        if (brandNorm.startsWith(qNorm)) totalScore += 100

        return totalScore
    }

    /**
     * Checks automotive market synonyms and abbreviations commonly used in Brazil.
     */
    private fun matchesSynonyms(vehicle: Vehicle, token: String): Boolean {
        val t = simplify(token)
        return when (t) {
            "ev", "bev", "eletrico", "eletricos", "100", "bateria" -> vehicle.type == VehicleType.BEV
            "phev", "plugin", "plug", "dmi", "dm", "hi4", "hi4t", "4xe", "cdm", "idm" -> vehicle.type == VehicleType.PHEV
            "hev", "hibrido", "hibridos", "auto", "autorrecarregavel", "ehev" -> vehicle.type == VehicleType.HEV
            "h6" -> vehicle.name.contains("H6", ignoreCase = true)
            "h6gt", "gt" -> vehicle.name.contains("GT", ignoreCase = true)
            "dolphin" -> vehicle.name.contains("Dolphin", ignoreCase = true)
            "song" -> vehicle.name.contains("Song", ignoreCase = true)
            "king" -> vehicle.name.contains("King", ignoreCase = true)
            "seal" -> vehicle.name.contains("Seal", ignoreCase = true)
            "tiggo", "chery", "caoa" -> vehicle.brand.contains("Chery", ignoreCase = true) || vehicle.name.contains("Tiggo", ignoreCase = true)
            "omoda", "e5" -> vehicle.brand.contains("Omoda", ignoreCase = true) || vehicle.name.contains("Omoda", ignoreCase = true)
            "jaecoo", "j7", "j8" -> vehicle.brand.contains("Jaecoo", ignoreCase = true) || vehicle.name.contains("Jaecoo", ignoreCase = true)
            "haval", "gwm", "ora" -> vehicle.brand.contains("Haval", ignoreCase = true) || vehicle.brand.contains("GWM", ignoreCase = true)
            "corolla", "toyota", "cross" -> vehicle.brand.contains("Toyota", ignoreCase = true) || vehicle.name.contains("Corolla", ignoreCase = true)
            "civic", "crv", "accord", "honda" -> vehicle.brand.contains("Honda", ignoreCase = true) || vehicle.name.contains("Civic", ignoreCase = true)
            "zeekr", "zeeker" -> vehicle.brand.contains("Zeekr", ignoreCase = true)
            "volvo", "ex30", "xc60", "xc40" -> vehicle.brand.contains("Volvo", ignoreCase = true)
            "gac", "aion", "aiony", "aiones", "aionv", "aions", "hyptec", "hyper", "emkoo", "empow", "gs8", "e9", "yplus" -> 
                vehicle.brand.contains("GAC", ignoreCase = true) || 
                vehicle.brand.contains("Aion", ignoreCase = true) || 
                vehicle.brand.contains("Hyptec", ignoreCase = true) || 
                vehicle.name.contains("GAC", ignoreCase = true) || 
                vehicle.name.contains("Aion", ignoreCase = true) ||
                vehicle.name.contains("Hyptec", ignoreCase = true) ||
                vehicle.name.contains("GS8", ignoreCase = true) ||
                vehicle.name.contains("Emkoo", ignoreCase = true) ||
                vehicle.name.contains("Empow", ignoreCase = true)
            "jetour" -> vehicle.brand.contains("Jetour", ignoreCase = true)
            "leapmotor", "leap" -> vehicle.brand.contains("Leapmotor", ignoreCase = true)
            "neta" -> vehicle.brand.contains("Neta", ignoreCase = true)
            "bmw" -> vehicle.brand.contains("BMW", ignoreCase = true)
            "mercedes", "benz" -> vehicle.brand.contains("Mercedes", ignoreCase = true)
            "audi", "etron" -> vehicle.brand.contains("Audi", ignoreCase = true) || vehicle.name.contains("e-tron", ignoreCase = true)
            "porsche", "taycan", "macan" -> vehicle.brand.contains("Porsche", ignoreCase = true)
            "kwid", "megane", "renault" -> vehicle.brand.contains("Renault", ignoreCase = true) || vehicle.name.contains("Kwid", ignoreCase = true)
            "kia", "niro", "ev5", "ev6", "ev9" -> vehicle.brand.contains("Kia", ignoreCase = true)
            "hyundai", "ioniq" -> vehicle.brand.contains("Hyundai", ignoreCase = true) || vehicle.name.contains("Ioniq", ignoreCase = true)
            "blazer", "equinox", "bolt", "chevrolet", "gm" -> vehicle.brand.contains("Chevrolet", ignoreCase = true)
            "mach", "mache", "mustang", "maverick", "ford" -> vehicle.brand.contains("Ford", ignoreCase = true)
            else -> false
        }
    }

    /**
     * Filters and intelligently sorts a list of vehicles by relevance.
     */
    fun search(vehicles: List<Vehicle>, query: String, categoryFilter: VehicleType? = null): List<Vehicle> {
        val filteredByType = if (categoryFilter != null) {
            vehicles.filter { it.type == categoryFilter }
        } else {
            vehicles
        }

        if (query.isBlank()) {
            return filteredByType
        }

        return filteredByType
            .map { vehicle -> vehicle to score(vehicle, query) }
            .filter { (_, score) -> score > 0 }
            .sortedByDescending { (_, score) -> score }
            .map { (vehicle, _) -> vehicle }
    }
}
