package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.formatNumber
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

enum class ChartTimeGranularity {
    DAY,
    MONTH,
    YEAR
}

enum class ChartPowertrainFilter {
    ALL,
    HEV,
    EV
}

enum class ChartMetricType {
    FUEL_EFFICIENCY_KM_L,
    ENERGY_KWH,
    COST_PER_KM,
    EV_PERCENT
}

enum class ChartPresentationType {
    AREA,
    BAR
}

data class ChartDataPoint(
    val key: String,
    val shortLabel: String,
    val fullDateLabel: String,
    val value: Double,
    val secondaryValue: Double = 0.0,
    val totalKm: Double = 0.0,
    val evKm: Double = 0.0,
    val hevKm: Double = 0.0,
    val pureEvKm: Double = 0.0,
    val effectiveFuelKm: Double = 0.0,
    val excessEvKm: Double = 0.0,
    val fuelLiters: Double = 0.0,
    val energyKwh: Double = 0.0,
    val totalCost: Double = 0.0,
    val tripsCount: Int = 1
)

@Composable
fun EfficiencyTrendChart(
    dataPoints: List<ChartDataPoint>,
    metricType: ChartMetricType,
    presentationType: ChartPresentationType,
    selectedPoint: ChartDataPoint?,
    onPointSelected: (ChartDataPoint?) -> Unit,
    powertrainFilter: ChartPowertrainFilter = ChartPowertrainFilter.ALL,
    language: AppLanguage = AppLanguage.PT_BR,
    modifier: Modifier = Modifier
) {
    val strings = AppStrings.get(language)

    if (dataPoints.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageCardBg),
            border = BorderStroke(1.dp, VoltageCardBorder)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = VoltageOnSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = strings.chartsEmptyTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = VoltageOnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
        return
    }

    val isHevEvKmL = powertrainFilter == ChartPowertrainFilter.ALL && metricType == ChartMetricType.FUEL_EFFICIENCY_KM_L

    val themeColor = when {
        metricType == ChartMetricType.COST_PER_KM -> Color(0xFF38BDF8) // Sempre azul para todos (HEV+EV, HEV e EV)
        metricType == ChartMetricType.ENERGY_KWH -> Color(0xFFA78BFA) // Roxo para kWh
        powertrainFilter == ChartPowertrainFilter.HEV -> Color(0xFFF59E0B) // Âmbar / Laranja para HEV (km/L)
        powertrainFilter == ChartPowertrainFilter.EV -> Color(0xFFA78BFA) // Lavanda / Roxo para EV
        metricType == ChartMetricType.EV_PERCENT -> Color(0xFFA78BFA) // Lavanda / Roxo
        else -> Color(0xFF10B981) // Verde esmeralda para HEV+EV km/L combinado
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            themeColor.copy(alpha = 0.40f),
            themeColor.copy(alpha = 0.05f),
            Color.Transparent
        )
    )

    val rawValues = dataPoints.map { it.value }
    val maxRaw = rawValues.maxOrNull() ?: 10.0
    val minRaw = rawValues.minOrNull() ?: 0.0
    val maxVal = if (maxRaw <= 0.0) 10.0 else maxRaw * 1.15
    val minVal = if (minRaw < 0.0) minRaw * 1.1 else 0.0

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 650),
        label = "chart_progress"
    )

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("efficiency_trend_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF091322)),
        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Chart Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            val density = this
                            val leftPaddingPx = with(density) { 48.dp.toPx() }
                            val rightPaddingPx = with(density) { 16.dp.toPx() }
                            detectTapGestures { offset ->
                                val pointIndex = calculateNearestPointIndex(
                                    touchX = offset.x,
                                    width = size.width.toFloat(),
                                    count = dataPoints.size,
                                    leftPadding = leftPaddingPx,
                                    rightPadding = rightPaddingPx
                                )
                                if (pointIndex in dataPoints.indices) {
                                    val current = dataPoints[pointIndex]
                                    if (selectedPoint?.key == current.key) {
                                        onPointSelected(null)
                                    } else {
                                        onPointSelected(current)
                                    }
                                }
                            }
                        }
                        .pointerInput(dataPoints) {
                            val density = this
                            val leftPaddingPx = with(density) { 48.dp.toPx() }
                            val rightPaddingPx = with(density) { 16.dp.toPx() }
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, _ ->
                                    val pointIndex = calculateNearestPointIndex(
                                        touchX = change.position.x,
                                        width = size.width.toFloat(),
                                        count = dataPoints.size,
                                        leftPadding = leftPaddingPx,
                                        rightPadding = rightPaddingPx
                                    )
                                    if (pointIndex in dataPoints.indices) {
                                        onPointSelected(dataPoints[pointIndex])
                                    }
                                },
                                onDragEnd = {}
                            )
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val leftPadding = 48.dp.toPx()
                    val rightPadding = 16.dp.toPx()
                    val topPadding = 20.dp.toPx()
                    val bottomPadding = 36.dp.toPx()

                    val chartWidth = canvasWidth - leftPadding - rightPadding
                    val chartHeight = canvasHeight - topPadding - bottomPadding

                    if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

                    // 1. Gridlines horizontais e Labels do Eixo Y
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val fraction = i.toFloat() / gridSteps.toFloat()
                        val y = topPadding + chartHeight * (1f - fraction)
                        val gridValue = minVal + (maxVal - minVal) * fraction

                        // Linha de grade
                        drawLine(
                            color = Color(0xFF1E293B).copy(alpha = 0.85f),
                            start = Offset(leftPadding, y),
                            end = Offset(canvasWidth - rightPadding, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Texto no eixo Y
                        val formattedY = when (metricType) {
                            ChartMetricType.COST_PER_KM -> formatNumber(gridValue, 2, language)
                            ChartMetricType.EV_PERCENT -> "${gridValue.toInt()}%"
                            else -> formatNumber(gridValue, 1, language)
                        }

                        val textLayout = textMeasurer.measure(
                            text = formattedY,
                            style = TextStyle(
                                color = Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )

                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(
                                leftPadding - textLayout.size.width - 8.dp.toPx(),
                                y - textLayout.size.height / 2
                            )
                        )
                    }

                    // 2. Pontos no Plano Cartesiano
                    val n = dataPoints.size
                    val coords = ArrayList<Offset>(n)

                    for (i in 0 until n) {
                        val point = dataPoints[i]
                        val x = if (n == 1) {
                            leftPadding + chartWidth / 2f
                        } else {
                            leftPadding + (i.toFloat() / (n - 1).toFloat()) * chartWidth
                        }

                        val valueRange = (maxVal - minVal).coerceAtLeast(0.0001)
                        val normalizedVal = ((point.value - minVal) / valueRange).coerceIn(0.0, 1.0)
                        val animatedNormalized = (normalizedVal * animProgress).toFloat()
                        val y = topPadding + chartHeight * (1f - animatedNormalized)

                        coords.add(Offset(x, y))
                    }

                    // 3. Renderização do Gráfico (Área ou Barras)
                    if (presentationType == ChartPresentationType.BAR) {
                        val barWidth = if (n <= 5) 28.dp.toPx() else (chartWidth / (n * 1.6f)).coerceIn(8.dp.toPx(), 26.dp.toPx())
                        val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                        for (i in 0 until n) {
                            val coord = coords[i]
                            val isSelected = selectedPoint?.key == dataPoints[i].key
                            val barTop = coord.y
                            val barBottom = topPadding + chartHeight
                            val barHeight = (barBottom - barTop).coerceAtLeast(2.dp.toPx())

                            if (isHevEvKmL) {
                                // Barra de composição segmentada HEV (combustão sustentada) + EV (elétrico puro da recarga)
                                // Mesma lógica da média HEV: soma quilometragem HEV + EV regenerado/gerado (effectiveFuelKm), e o resto fica no EV puro (pureEvKm)
                                val point = dataPoints[i]
                                val totalKm = point.totalKm
                                val hevDisplayKm = if (point.effectiveFuelKm > 0 || point.pureEvKm > 0) point.effectiveFuelKm else point.hevKm
                                val evDisplayKm = if (point.effectiveFuelKm > 0 || point.pureEvKm > 0) point.pureEvKm else point.evKm
                                val hevRatio = if (totalKm > 0) (hevDisplayKm / totalKm).toFloat().coerceIn(0f, 1f) else 0f
                                val evRatio = (1f - hevRatio).coerceIn(0f, 1f)
                                val hevHeight = barHeight * hevRatio
                                val evHeight = barHeight * evRatio
                                val hevTop = barBottom - hevHeight

                                // Segmento HEV inferior (Âmbar / Laranja)
                                if (hevHeight > 0.5f) {
                                    val hevBrush = if (isSelected) {
                                        Brush.verticalGradient(listOf(Color(0xFFFDE68A), Color(0xFFF59E0B)))
                                    } else {
                                        Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                    }
                                    drawRoundRect(
                                        brush = hevBrush,
                                        topLeft = Offset(coord.x - barWidth / 2f, hevTop),
                                        size = Size(barWidth, hevHeight),
                                        cornerRadius = if (evHeight <= 0.5f) cornerRadius else CornerRadius(0f, 0f)
                                    )
                                }

                                // Segmento EV superior (Lavanda / Roxo)
                                if (evHeight > 0.5f) {
                                    val evBrush = if (isSelected) {
                                        Brush.verticalGradient(listOf(Color(0xFFDDD6FE), Color(0xFFA78BFA)))
                                    } else {
                                        Brush.verticalGradient(listOf(Color(0xFFA78BFA), Color(0xFF7C3AED)))
                                    }
                                    drawRoundRect(
                                        brush = evBrush,
                                        topLeft = Offset(coord.x - barWidth / 2f, barTop),
                                        size = Size(barWidth, evHeight),
                                        cornerRadius = if (hevHeight <= 0.5f) cornerRadius else CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }

                                // Linha de divisão sutil entre HEV e EV
                                if (hevHeight > 1.5f && evHeight > 1.5f) {
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.65f),
                                        start = Offset(coord.x - barWidth / 2f, hevTop),
                                        end = Offset(coord.x + barWidth / 2f, hevTop),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // Contorno de seleção
                                if (isSelected) {
                                    drawRoundRect(
                                        color = Color.White,
                                        topLeft = Offset(coord.x - barWidth / 2f, barTop),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = cornerRadius,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }
                            } else {
                                val barBrush = if (isSelected) {
                                    Brush.verticalGradient(
                                        listOf(Color.White, themeColor)
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(themeColor, themeColor.copy(alpha = 0.45f))
                                    )
                                }

                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(coord.x - barWidth / 2f, barTop),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = cornerRadius
                                )

                                if (isSelected) {
                                    drawRoundRect(
                                        color = Color.White,
                                        topLeft = Offset(coord.x - barWidth / 2f, barTop),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = cornerRadius,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                }
                            }
                        }
                    } else {
                        // AREA CHART com curvas suaves inspiradas no Recharts
                        if (coords.isNotEmpty()) {
                            val linePath = Path()
                            val fillPath = Path()

                            if (coords.size == 1) {
                                val pt = coords[0]
                                val barHalfWidth = 28.dp.toPx()
                                linePath.moveTo(pt.x - barHalfWidth, pt.y)
                                linePath.lineTo(pt.x + barHalfWidth, pt.y)

                                fillPath.moveTo(pt.x - barHalfWidth, topPadding + chartHeight)
                                fillPath.lineTo(pt.x - barHalfWidth, pt.y)
                                fillPath.lineTo(pt.x + barHalfWidth, pt.y)
                                fillPath.lineTo(pt.x + barHalfWidth, topPadding + chartHeight)
                                fillPath.close()
                            } else {
                                linePath.moveTo(coords[0].x, coords[0].y)
                                fillPath.moveTo(coords[0].x, topPadding + chartHeight)
                                fillPath.lineTo(coords[0].x, coords[0].y)

                                for (i in 0 until coords.size - 1) {
                                    val p0 = coords[i]
                                    val p1 = coords[i + 1]
                                    val controlPointX1 = (p0.x + p1.x) / 2f
                                    val controlPointY1 = p0.y
                                    val controlPointX2 = (p0.x + p1.x) / 2f
                                    val controlPointY2 = p1.y

                                    linePath.cubicTo(
                                        controlPointX1, controlPointY1,
                                        controlPointX2, controlPointY2,
                                        p1.x, p1.y
                                    )
                                    fillPath.cubicTo(
                                        controlPointX1, controlPointY1,
                                        controlPointX2, controlPointY2,
                                        p1.x, p1.y
                                    )
                                }
                                fillPath.lineTo(coords.last().x, topPadding + chartHeight)
                                fillPath.close()
                            }

                            // Preenchimento de Gradiente
                            drawPath(
                                path = fillPath,
                                brush = gradientBrush
                            )

                            // Traçado da Linha principal
                            drawPath(
                                path = linePath,
                                color = themeColor,
                                style = Stroke(
                                    width = 2.8.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Pontos de dados (Dots)
                            for (i in coords.indices) {
                                val pt = coords[i]
                                val isSelected = selectedPoint?.key == dataPoints[i].key

                                if (isSelected) {
                                    // Linha vertical de cursor
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.7f),
                                        start = Offset(pt.x, topPadding),
                                        end = Offset(pt.x, topPadding + chartHeight),
                                        strokeWidth = 1.2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                    )

                                    // Halo externo brilhante
                                    drawCircle(
                                        color = themeColor.copy(alpha = 0.35f),
                                        radius = 9.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 5.5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = themeColor,
                                        radius = 3.5.dp.toPx(),
                                        center = pt
                                    )
                                } else {
                                    drawCircle(
                                        color = Color(0xFF091322),
                                        radius = 4.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = themeColor,
                                        radius = 3.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }

                    // 4. Labels do Eixo X (Datas / Períodos)
                    if (n == 1) {
                        val pt = coords.firstOrNull() ?: Offset(leftPadding + chartWidth / 2f, topPadding + chartHeight)
                        val textLayout = textMeasurer.measure(
                            text = dataPoints.first().shortLabel,
                            style = TextStyle(
                                color = if (selectedPoint?.key == dataPoints.first().key) Color.White else Color(0xFF94A3B8),
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedPoint?.key == dataPoints.first().key) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(
                                (pt.x - textLayout.size.width / 2f).coerceIn(leftPadding, canvasWidth - rightPadding - textLayout.size.width),
                                topPadding + chartHeight + 10.dp.toPx()
                            )
                        )
                    } else if (n > 1) {
                        val maxAllowed = ((chartWidth / 55.dp.toPx()).toInt()).coerceAtLeast(1)
                        val maxLabels = maxAllowed.coerceIn(1, n)
                        val labelStep = (n / maxLabels).coerceAtLeast(1)

                        for (i in 0 until n step labelStep) {
                            val pt = coords[i]
                            val labelText = dataPoints[i].shortLabel

                            val textLayout = textMeasurer.measure(
                                text = labelText,
                                style = TextStyle(
                                    color = if (selectedPoint?.key == dataPoints[i].key) Color.White else Color(0xFF94A3B8),
                                    fontSize = 10.5.sp,
                                    fontWeight = if (selectedPoint?.key == dataPoints[i].key) FontWeight.Bold else FontWeight.Normal
                                )
                            )

                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(
                                    pt.x - textLayout.size.width / 2f,
                                    topPadding + chartHeight + 10.dp.toPx()
                                )
                            )
                        }

                        // Se o último ponto não caiu no step, desenha ele para ter a data final
                        if ((n - 1) % labelStep != 0) {
                            val pt = coords.last()
                            val textLayout = textMeasurer.measure(
                                text = dataPoints.last().shortLabel,
                                style = TextStyle(
                                    color = if (selectedPoint?.key == dataPoints.last().key) Color.White else Color(0xFF94A3B8),
                                    fontSize = 10.5.sp,
                                    fontWeight = if (selectedPoint?.key == dataPoints.last().key) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(
                                    (pt.x - textLayout.size.width / 2f).coerceAtMost(canvasWidth - rightPadding - textLayout.size.width),
                                    topPadding + chartHeight + 10.dp.toPx()
                                )
                            )
                        }
                    }
                }
            }

            // Legenda explicativa para a barra segmentada HEV+EV (cada item em sua própria linha)
            if (isHevEvKmL && presentationType == ChartPresentationType.BAR) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFF59E0B), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.EN_US) "HEV (combustion + regen)" else "HEV (combustão + reg.)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFDE68A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFA78BFA), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.EN_US) "EV (pure electric)" else "EV puro (recarga)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFDDD6FE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Interactive Tooltip Card (Recharts style)
            if (selectedPoint != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chart_tooltip_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPoint.fullDateLabel,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1E3A5F)
                            ) {
                                Text(
                                    text = "${selectedPoint.tripsCount} ${strings.chartsTooltipTrips}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1E3A5F), thickness = 0.6.dp)

                        val (mainDistanceText, subBreakdownText) = when (powertrainFilter) {
                            ChartPowertrainFilter.ALL -> {
                                val main = "${formatNumber(selectedPoint.totalKm, 0, language)} km"
                                val sub = if (selectedPoint.excessEvKm > 0) {
                                    "(${formatNumber(selectedPoint.pureEvKm, 0, language)} EV puro / ${formatNumber(selectedPoint.effectiveFuelKm, 0, language)} HEV comb.)"
                                } else if (selectedPoint.evKm > 0 && selectedPoint.hevKm > 0) {
                                    "(${formatNumber(selectedPoint.evKm, 0, language)} EV / ${formatNumber(selectedPoint.hevKm, 0, language)} HEV)"
                                } else if (selectedPoint.hevKm <= 0 && selectedPoint.evKm > 0) {
                                    "(100% Elétrico)"
                                } else if (selectedPoint.evKm <= 0 && selectedPoint.hevKm > 0) {
                                    "(HEV Combustão)"
                                } else {
                                    null
                                }
                                Pair(main, sub)
                            }
                            ChartPowertrainFilter.HEV -> {
                                val displayHev = if (selectedPoint.effectiveFuelKm > 0) selectedPoint.effectiveFuelKm else selectedPoint.hevKm
                                if (selectedPoint.excessEvKm > 0) {
                                    val main = "${formatNumber(displayHev, 0, language)} km comb."
                                    val sub = "(${formatNumber(selectedPoint.hevKm, 0, language)} HEV + ${formatNumber(selectedPoint.excessEvKm, 0, language)} reg.)"
                                    Pair(main, sub)
                                } else {
                                    val main = "${formatNumber(displayHev, 0, language)} km HEV"
                                    Pair(main, null)
                                }
                            }
                            ChartPowertrainFilter.EV -> {
                                val displayEv = if (selectedPoint.pureEvKm > 0) selectedPoint.pureEvKm else if (selectedPoint.evKm > 0) selectedPoint.evKm else selectedPoint.totalKm
                                val main = "${formatNumber(displayEv, 0, language)} km EV"
                                Pair(main, null)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Métrica Principal (toma apenas o espaço necessário)
                            Column(
                                modifier = Modifier.padding(end = 10.dp)
                            ) {
                                val metricTitle = when (metricType) {
                                    ChartMetricType.FUEL_EFFICIENCY_KM_L -> if (language == AppLanguage.EN_US) "Efficiency" else "Eficiência"
                                    ChartMetricType.ENERGY_KWH -> if (language == AppLanguage.EN_US) "Energy" else "Energia"
                                    ChartMetricType.COST_PER_KM -> if (language == AppLanguage.EN_US) "Cost" else "Custo"
                                    ChartMetricType.EV_PERCENT -> if (language == AppLanguage.EN_US) "EV %" else "% EV"
                                }
                                Text(
                                    text = metricTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val metricValueText = when (metricType) {
                                    ChartMetricType.FUEL_EFFICIENCY_KM_L -> {
                                        if (powertrainFilter == ChartPowertrainFilter.EV) "${formatNumber(selectedPoint.value, 1, language)} km/L eq."
                                        else "${formatNumber(selectedPoint.value, 1, language)} km/L"
                                    }
                                    ChartMetricType.ENERGY_KWH -> "${formatNumber(selectedPoint.value, 1, language)} kWh"
                                    ChartMetricType.COST_PER_KM -> "R$ ${formatNumber(selectedPoint.value, 2, language)}/km"
                                    ChartMetricType.EV_PERCENT -> "${formatNumber(selectedPoint.value, 0, language)}% EV"
                                }
                                Text(
                                    text = metricValueText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = themeColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    ),
                                    maxLines = 1
                                )
                            }

                            // Estatísticas de Apoio (Distância e Combustível - expande para todo o espaço restante)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = mainDistanceText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFCBD5E1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1
                                )
                                if (selectedPoint.fuelLiters > 0 && powertrainFilter != ChartPowertrainFilter.EV) {
                                    Text(
                                        text = "${formatNumber(selectedPoint.fuelLiters, 1, language)} L gasolina",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFF59E0B),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (selectedPoint.energyKwh > 0 && metricType != ChartMetricType.ENERGY_KWH) {
                                    Text(
                                        text = "${formatNumber(selectedPoint.energyKwh, 1, language)} kWh",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFA78BFA),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (selectedPoint.totalCost > 0) {
                                    Text(
                                        text = "Custo: R$ ${formatNumber(selectedPoint.totalCost, 2, language)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF34D399),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Detalhamento EV puro vs HEV em linha própria para nunca cortar em telas estreitas
                        if (subBreakdownText != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF070B14),
                                border = BorderStroke(0.6.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = subBreakdownText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.5.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    softWrap = true
                                )
                            }
                        }

                        // Seção de Composição HEV+EV no Tooltip (apenas quando há combinação real de propulsão)
                        val hasHevAndEv = (selectedPoint.effectiveFuelKm > 0 && selectedPoint.pureEvKm > 0) || (selectedPoint.hevKm > 0 && selectedPoint.evKm > 0)
                        if (isHevEvKmL && selectedPoint.totalKm > 0 && hasHevAndEv) {
                            HorizontalDivider(color = Color(0xFF1E3A5F), thickness = 0.6.dp)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF091322),
                                border = BorderStroke(0.6.dp, Color(0xFF1E3A5F))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    val evDisplayKm = if (selectedPoint.pureEvKm > 0 || selectedPoint.effectiveFuelKm > 0) selectedPoint.pureEvKm else selectedPoint.evKm
                                    val hevDisplayKm = if (selectedPoint.pureEvKm > 0 || selectedPoint.effectiveFuelKm > 0) selectedPoint.effectiveFuelKm else selectedPoint.hevKm
                                    val evPct = (evDisplayKm / selectedPoint.totalKm) * 100.0
                                    val hevPct = (hevDisplayKm / selectedPoint.totalKm) * 100.0

                                    // Linha 1: Título da composição
                                    Text(
                                        text = if (language == AppLanguage.EN_US) "Composition for calculation:" else "Composição para cálculo:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )

                                    // Linha 2: Valores percentuais EV e HEV
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = if (selectedPoint.excessEvKm > 0) "⚡ ${formatNumber(evPct, 0, language)}% EV puro" else "⚡ ${formatNumber(evPct, 0, language)}% EV",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFA78BFA),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF64748B),
                                                fontSize = 10.5.sp
                                            )
                                        )
                                        Text(
                                            text = if (selectedPoint.excessEvKm > 0) "⛽ ${formatNumber(hevPct, 0, language)}% HEV comb." else "⛽ ${formatNumber(hevPct, 0, language)}% HEV",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFF59E0B),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }

                                    // Linha 3: Fórmula direta sem prefixo "Fórmula:"
                                    if (selectedPoint.fuelLiters > 0) {
                                        if (selectedPoint.excessEvKm > 0) {
                                            Text(
                                                text = "(${formatNumber(evDisplayKm, 0, language)} km EV puro + ${formatNumber(hevDisplayKm, 0, language)} km HEV comb.) ÷ ${formatNumber(selectedPoint.fuelLiters, 1, language)} L = ${formatNumber(selectedPoint.value, 1, language)} km/L",
                                                style = TextStyle(
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    lineHeight = 14.sp
                                                )
                                            )
                                            Text(
                                                text = "↳ ${formatNumber(selectedPoint.hevKm, 1, language)} km HEV + ${formatNumber(selectedPoint.excessEvKm, 1, language)} km EV reg. = ${formatNumber(hevDisplayKm, 1, language)} km comb.",
                                                style = TextStyle(
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    lineHeight = 12.sp
                                                )
                                            )
                                        } else {
                                            Text(
                                                text = "(${formatNumber(evDisplayKm, 0, language)} km EV + ${formatNumber(hevDisplayKm, 0, language)} km HEV) ÷ ${formatNumber(selectedPoint.fuelLiters, 1, language)} L = ${formatNumber(selectedPoint.value, 1, language)} km/L",
                                                style = TextStyle(
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    lineHeight = 14.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun calculateNearestPointIndex(
    touchX: Float,
    width: Float,
    count: Int,
    leftPadding: Float,
    rightPadding: Float
): Int {
    if (count <= 0) return -1
    if (count == 1) return 0
    val chartWidth = (width - leftPadding - rightPadding).coerceAtLeast(1f)
    val relativeX = (touchX - leftPadding).coerceIn(0f, chartWidth)
    val fraction = relativeX / chartWidth
    val index = kotlin.math.round(fraction * (count - 1)).toInt()
    return index.coerceIn(0, count - 1)
}

