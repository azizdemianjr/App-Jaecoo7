package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppUiState
import com.example.ui.formatCurrency
import com.example.ui.formatNumber
import com.example.ui.theme.*

@Composable
fun ExportReportDialog(
    state: AppUiState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copyStatus by remember { mutableStateOf<String?>(null) }

    val reportText = remember(state) {
        buildString {
            appendLine("==========================================")
            val titleType = when {
                state.isElectricOnly -> "RELATÓRIO DE RECARGA 100% ELÉTRICO"
                state.isHybridConventional -> "RELATÓRIO DE CONSUMO HÍBRIDO (HEV)"
                else -> "RELATÓRIO DE RECARGA & ECONOMIA PHEV"
            }
            appendLine(" $titleType")
            appendLine(" Veículo: ${state.selectedVehicle.name} (${state.selectedVehicle.type.label})")
            appendLine(" Data: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine("==========================================")
            appendLine("")

            if (state.isHybridConventional) {
                appendLine("--- PARÂMETROS DE CONSUMO ---")
                appendLine("• Preço da Gasolina: ${formatCurrency(state.gasolinePrice)} / Litro")
                appendLine("• Consumo do ${state.selectedVehicle.name}: ${state.selectedVehicle.gasolineConsumptionKmL} km/L")
                val baseIceKmLStr = if (state.customComparisonGasKmL % 1.0 == 0.0) "${state.customComparisonGasKmL.toInt()}" else String.format(java.util.Locale.US, "%.1f", state.customComparisonGasKmL).replace('.', ',')
                appendLine("• Carro Combustão Padrão (Comparativo): $baseIceKmLStr km/L")
                appendLine("")
                appendLine("--- CUSTO POR QUILÔMETRO ---")
                appendLine("• Híbrido (${state.selectedVehicle.name}): ${formatCurrency(state.costHevPerKm)}/km (${formatCurrency(state.costHevPer100Km)} / 100km)")
                appendLine("• Carro 100% a Combustão: ${formatCurrency(state.costIceBenchmarkPerKm)}/km (${formatCurrency(state.costIceBenchmarkPer100Km)} / 100km)")
                appendLine("• Vantagem por 100 km: Economia de ${formatCurrency((state.costIceBenchmarkPer100Km - state.costHevPer100Km).coerceAtLeast(0.0))} (${state.hevSavingsPercentVsIce.toInt()}%)")
                appendLine("")
                appendLine("--- PROJEÇÃO DE ECONOMIA (${state.monthlyKm.toInt()} km/mês) ---")
                appendLine("• Economia Mensal em Combustível: ${formatCurrency(state.monthlyHevSavingsVsIce)}/mês")
                appendLine("• Litros Poupados por Mês: ~${state.hevMonthlyLitersSaved.toInt()} Litros")
                appendLine("• Projeção de Economia Anual: ${formatCurrency(state.annualHevSavingsVsIce)}/ano")
                appendLine("• Informação de Recarga: Bateria auto-recarregável (HEV). Não utiliza tomada ou recarga externa.")
            } else if (state.isElectricOnly) {
                appendLine("--- PREÇOS CONSIDERADOS ---")
                appendLine("• Energia em Casa: ${formatCurrency(state.homeEnergyPrice)} / kWh")
                appendLine("• Energia no Posto: ${formatCurrency(state.publicEnergyPrice)} / kWh")
                appendLine("• Gasolina (Comparativo Combustão): ${formatCurrency(state.gasolinePrice)} / Litro")
                appendLine("")
                appendLine("--- CUSTO POR QUILÔMETRO ---")
                appendLine("• Em Casa: ${formatCurrency(state.costHomePerKm)}/km (${formatCurrency(state.costHomePer100Km)} / 100km)")
                appendLine("• Posto Público: ${formatCurrency(state.costPublicPerKm)}/km (${formatCurrency(state.costPublicPer100Km)} / 100km)")
                val kmLStr = if (state.benchmarkGasolineConsumptionKmL % 1.0 == 0.0) {
                    "${state.benchmarkGasolineConsumptionKmL.toInt()} km/L"
                } else {
                    "${String.format(java.util.Locale.US, "%.1f", state.benchmarkGasolineConsumptionKmL).replace('.', ',')} km/L"
                }
                appendLine("• Carro a Gasolina (base $kmLStr): ${formatCurrency(state.costGasPerKm)}/km (${formatCurrency(state.costGasPer100Km)} / 100km)")
                appendLine("")
                appendLine("--- PROJEÇÃO DE ECONOMIA (${state.monthlyKm.toInt()} km/mês) ---")
                appendLine("• Recarga em Casa vs Carro a Gasolina: ${formatCurrency(state.monthlyHomeSavingsVsGas)}/mês (${formatCurrency(state.annualHomeSavingsVsGas)}/ano) - ${state.savingsHomeVsGasPercent.toInt()}% mais barato")
                appendLine("• Posto Público vs Carro a Gasolina: ${formatCurrency(state.monthlyPublicSavingsVsGas)}/mês (${formatCurrency(state.annualPublicSavingsVsGas)}/ano)")
                appendLine("• Economia Casa vs Posto Público: ${formatCurrency(state.monthlyHomeSavingsVsPublic)}/mês (${formatCurrency(state.annualHomeSavingsVsPublic)}/ano)")
                appendLine("")
                appendLine("--- ESTIMATIVA DE RECARGA (${state.costSocInitial}% -> ${state.costSocFinal}%) ---")
                appendLine("• Energia Requerida (+10% perda): ${formatNumber(state.energyRequiredKwh)} kWh")
                appendLine("• Custo em Casa: ${formatCurrency(state.costRechargeHome)}")
                appendLine("• Custo no Posto: ${formatCurrency(state.costRechargePublic)}")
            } else {
                appendLine("--- PREÇOS CONSIDERADOS ---")
                appendLine("• Energia em Casa: ${formatCurrency(state.homeEnergyPrice)} / kWh")
                appendLine("• Energia no Posto: ${formatCurrency(state.publicEnergyPrice)} / kWh")
                appendLine("• Gasolina: ${formatCurrency(state.gasolinePrice)} / Litro")
                appendLine("")
                appendLine("--- CUSTO POR QUILÔMETRO ---")
                appendLine("• Em Casa: ${formatCurrency(state.costHomePerKm)}/km (${formatCurrency(state.costHomePer100Km)} / 100km)")
                appendLine("• Posto Público: ${formatCurrency(state.costPublicPerKm)}/km (${formatCurrency(state.costPublicPer100Km)} / 100km)")
                appendLine("• Gasolina: ${formatCurrency(state.costGasPerKm)}/km (${formatCurrency(state.costGasPer100Km)} / 100km)")
                appendLine("")
                appendLine("--- TETO MÁXIMO DE VIABILIDADE ---")
                appendLine("• Limite no Posto: ${formatCurrency(state.ceilingPricePublicKwh)} / kWh")
                appendLine("  (Acima deste valor, compensa rodar na gasolina)")
                appendLine("")
                appendLine("--- PROJEÇÃO DE ECONOMIA (${state.monthlyKm.toInt()} km/mês) ---")
                appendLine("• Recarga em Casa (vs Gasolina): ${formatCurrency(state.monthlyHomeSavingsVsGas)}/mês (${formatCurrency(state.annualHomeSavingsVsGas)}/ano)")
                appendLine("• Posto Público (vs Gasolina): ${formatCurrency(state.monthlyPublicSavingsVsGas)}/mês (${formatCurrency(state.annualPublicSavingsVsGas)}/ano)")
                appendLine("")
                appendLine("--- ESTIMATIVA DE RECARGA (${state.costSocInitial}% -> ${state.costSocFinal}%) ---")
                appendLine("• Energia Requerida (+10% perda): ${formatNumber(state.energyRequiredKwh)} kWh")
                appendLine("• Custo em Casa: ${formatCurrency(state.costRechargeHome)}")
                appendLine("• Custo no Posto: ${formatCurrency(state.costRechargePublic)}")
            }
            appendLine("==========================================")
            appendLine("Gerado por Calculadora de Recarga & Economia (PHEV, HEV & EV)")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = VoltagePrimary
                )
                Text(
                    text = "Exportar & Compartilhar",
                    style = MaterialTheme.typography.titleLarge,
                    color = VoltageOnSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Relatório pronto para envio por WhatsApp, Telegram ou impressão:",
                    fontSize = 12.sp,
                    color = VoltageOnSurfaceVariant
                )

                // Report Preview Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = VoltageInputBg,
                    border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = reportText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = VoltageOnSurface,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Enviar Relatório"))
                        },
                        modifier = Modifier.weight(1f).testTag("btn_share_report"),
                        colors = ButtonDefaults.buttonColors(containerColor = VoltagePrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = VoltageOnPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar", color = VoltageOnPrimaryContainer)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("PHEV Report", reportText))
                            copyStatus = "Relatório copiado para a área de transferência!"
                        },
                        modifier = Modifier.weight(1f).testTag("btn_copy_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = VoltageSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar", color = VoltageSecondary)
                    }
                }

                if (copyStatus != null) {
                    Text(
                        text = copyStatus!!,
                        fontSize = 11.sp,
                        color = VoltageSuccess,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = VoltageOnSurfaceVariant)
            }
        },
        containerColor = VoltageSurfaceContainer
    )
}
