package com.example.data

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipInputStream

object XlsxParser {

    /**
     * Checks if the byte stream starts with the PK (ZIP) header magic bytes (0x50, 0x4B, 0x03, 0x04).
     * Excel .xlsx files are ZIP archives containing XML sheets and shared strings.
     */
    fun isXlsx(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        return bytes[0] == 0x50.toByte() &&
                bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() &&
                bytes[3] == 0x04.toByte()
    }

    /**
     * Parses an .xlsx Excel workbook from an InputStream and extracts the grid of rows as strings.
     */
    fun parseWorkbook(inputStream: InputStream): List<List<String>> {
        val zipEntries = mutableMapOf<String, ByteArray>()
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name
                if (name == "xl/sharedStrings.xml" ||
                    name.startsWith("xl/worksheets/sheet")
                ) {
                    zipEntries[name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (zipEntries.isEmpty()) return emptyList()

        // 1. Parse shared strings table (xl/sharedStrings.xml)
        val sharedStrings = mutableListOf<String>()
        val sharedStringsBytes = zipEntries["xl/sharedStrings.xml"]
        if (sharedStringsBytes != null) {
            parseSharedStrings(sharedStringsBytes, sharedStrings)
        }

        // 2. Locate first sheet (usually xl/worksheets/sheet1.xml)
        val sheetEntryKey = zipEntries.keys
            .filter { it.startsWith("xl/worksheets/sheet") && it.endsWith(".xml") }
            .sorted()
            .firstOrNull() ?: return emptyList()

        val sheetBytes = zipEntries[sheetEntryKey] ?: return emptyList()
        return parseSheet(sheetBytes, sharedStrings)
    }

    private fun parseSharedStrings(bytes: ByteArray, outStrings: MutableList<String>) {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(ByteArrayInputStream(bytes), "UTF-8")

        var eventType = parser.eventType
        var inSi = false
        var currentString = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name.equals("si", ignoreCase = true)) {
                        inSi = true
                        currentString = StringBuilder()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inSi) {
                        currentString.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name.equals("si", ignoreCase = true)) {
                        inSi = false
                        outStrings.add(currentString.toString())
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseSheet(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(ByteArrayInputStream(bytes), "UTF-8")

        val rows = mutableListOf<List<String>>()
        var currentRow = mutableMapOf<Int, String>()
        var currentCellRef = ""
        var currentCellType = ""
        var currentCellValue = StringBuilder()
        var inValue = false
        var maxColIndexInRow = 0

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when {
                        tagName.equals("row", ignoreCase = true) -> {
                            currentRow = mutableMapOf()
                            maxColIndexInRow = 0
                        }
                        tagName.equals("c", ignoreCase = true) -> {
                            currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            currentCellValue = StringBuilder()
                        }
                        tagName.equals("v", ignoreCase = true) || tagName.equals("t", ignoreCase = true) -> {
                            inValue = true
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inValue) {
                        currentCellValue.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    when {
                        tagName.equals("v", ignoreCase = true) || tagName.equals("t", ignoreCase = true) -> {
                            inValue = false
                        }
                        tagName.equals("c", ignoreCase = true) -> {
                            val colIndex = colRefToIndex(currentCellRef)
                            val rawVal = currentCellValue.toString().trim()
                            val resolvedVal = when (currentCellType) {
                                "s" -> {
                                    val idx = rawVal.toIntOrNull()
                                    if (idx != null && idx in sharedStrings.indices) {
                                        sharedStrings[idx]
                                    } else {
                                        rawVal
                                    }
                                }
                                "b" -> if (rawVal == "1") "true" else "false"
                                else -> rawVal
                            }
                            if (colIndex >= 0) {
                                currentRow[colIndex] = resolvedVal
                                if (colIndex > maxColIndexInRow) {
                                    maxColIndexInRow = colIndex
                                }
                            }
                        }
                        tagName.equals("row", ignoreCase = true) -> {
                            if (currentRow.isNotEmpty()) {
                                val rowList = ArrayList<String>(maxColIndexInRow + 1)
                                for (c in 0..maxColIndexInRow) {
                                    rowList.add(currentRow[c] ?: "")
                                }
                                rows.add(rowList)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return rows
    }

    /**
     * Converts an Excel cell reference like "A1", "F2", "AA10" to a zero-based column index.
     * "A" -> 0, "B" -> 1, "Z" -> 25, "AA" -> 26, etc.
     */
    fun colRefToIndex(cellRef: String): Int {
        if (cellRef.isBlank()) return -1
        var colLetters = ""
        for (ch in cellRef) {
            if (ch.isLetter()) {
                colLetters += ch.uppercaseChar()
            } else {
                break
            }
        }
        if (colLetters.isEmpty()) return -1

        var result = 0
        for (ch in colLetters) {
            result = result * 26 + (ch - 'A' + 1)
        }
        return result - 1
    }
}
