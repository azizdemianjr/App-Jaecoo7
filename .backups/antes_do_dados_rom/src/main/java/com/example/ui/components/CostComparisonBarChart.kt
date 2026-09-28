package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.formatCurrency
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import kotlin.math.max

data class BarItem(
    val label: String,
    val value: Double,
    val color: Color,
    val testTag: String
)

@Composable
fun CostComparisonBarChart(
    costHome100km: Double,
    costPublic100km: Double,
    costGas100km: Double,
    modifier: Modifier = Modifier,
    language: AppLanguage = AppLanguage.PT_BR,
    customItems: List<BarItem>? = null
) {
    val strings = AppStrings.get(language)
    val items = customItems ?: buildList {
        add(BarItem(label = strings.legendHome, value = costHome100km, color = VoltagePrimary, testTag = "bar_home"))
        add(BarItem(label = strings.legendPublic, value = costPublic100km, color = VoltageSecondary, testTag = "bar_public"))
        if (costGas100km > 0) {
            add(BarItem(label = strings.legendGas, value = costGas100km, color = VoltageTertiary, testTag = "bar_gas"))
        }
    }

    val maxBarValue = items.maxOfOrNull { it.value } ?: 50.0
    val maxValue = max(30.0, maxBarValue * 1.18)
    val yGridSteps = listOf(
        maxValue * 0.9,
        maxValue * 0.7,
        maxValue * 0.5,
        maxValue * 0.3,
        maxValue * 0.1
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cost_comparison_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VoltageCardBg),
        border = BorderStroke(1.dp, VoltageCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = VoltageSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${strings.chartTitle} (${language.currencySymbol})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VoltageOnSurface,
                    lineHeight = 20.sp
                )
            }

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(vertical = 8.dp)
            ) {
                // Background Grid Lines & Y-Labels
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    yGridSteps.forEach { step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${language.currencySymbol} ${step.toInt()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = VoltageOnSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.width(36.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            HorizontalDivider(
                                color = VoltageOutline.copy(alpha = 0.25f),
                                thickness = 1.dp
                            )
                        }
                    }
                }

                // Bars Row
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 40.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    items.forEach { item ->
                        val animFraction by animateFloatAsState(
                            targetValue = if (item.value <= 0.0) 0.025f else (item.value / maxValue).toFloat().coerceIn(0.05f, 1f),
                            animationSpec = tween(durationMillis = 600),
                            label = "bar_height"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            // Value text above bar
                            Text(
                                text = formatCurrency(item.value, language),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = item.color,
                                modifier = Modifier.padding(bottom = 4.dp),
                                maxLines = 1
                            )

                            // Bar
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 28.dp, max = 44.dp)
                                    .fillMaxWidth(0.55f)
                                    .fillMaxHeight(animFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(item.color)
                                    .testTag(item.testTag)
                            )
                        }
                    }
                }
            }

            // X-Axis Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = VoltageOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

