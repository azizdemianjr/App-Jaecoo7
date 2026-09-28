package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationTab
import com.example.ui.localization.AppDictionary
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*

data class NavItem(
    val tab: NavigationTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun BottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    language: AppLanguage = AppLanguage.PT_BR,
    isPhev: Boolean = true,
    modifier: Modifier = Modifier
) {
    val strings = AppStrings.get(language)
    val navItems = buildList {
        add(
            NavItem(
                tab = NavigationTab.CALCULATOR,
                label = strings.navCalculator,
                selectedIcon = Icons.Filled.Calculate,
                unselectedIcon = Icons.Outlined.Calculate,
                testTag = "nav_calculator"
            )
        )
        add(
            NavItem(
                tab = NavigationTab.ECONOMY,
                label = strings.navEconomy,
                selectedIcon = Icons.Filled.Savings,
                unselectedIcon = Icons.Outlined.Savings,
                testTag = "nav_economy"
            )
        )
        add(
            NavItem(
                tab = NavigationTab.ODOMETER,
                label = strings.navOdometer,
                selectedIcon = Icons.Filled.Speed,
                unselectedIcon = Icons.Outlined.Speed,
                testTag = "nav_odometer"
            )
        )
        add(
            NavItem(
                tab = NavigationTab.CHARGING_TIME,
                label = strings.navTime,
                selectedIcon = Icons.Filled.ElectricBolt,
                unselectedIcon = Icons.Outlined.ElectricBolt,
                testTag = "nav_time"
            )
        )
        add(
            NavItem(
                tab = NavigationTab.INFO,
                label = strings.navInfo,
                selectedIcon = Icons.Filled.Info,
                unselectedIcon = Icons.Outlined.Info,
                testTag = "nav_info"
            )
        )
    }

    val isCompactLayout = navItems.size >= 5

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VoltageSurfaceContainerLowest)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            color = Color(0xFF1B283A),
            thickness = 0.8.dp
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 14.dp)
        ) {
            val scrollState = rememberScrollState()
            val minItemWidth = 80.dp
            val totalRequiredWidth = minItemWidth * navItems.size + 16.dp
            val isScrollable = maxWidth < totalRequiredWidth

            // Rola automaticamente para a aba selecionada em telas roláveis
            LaunchedEffect(currentTab, isScrollable) {
                if (isScrollable) {
                    val index = navItems.indexOfFirst { it.tab == currentTab }
                    if (index >= 0) {
                        val maxScroll = scrollState.maxValue
                        if (maxScroll > 0) {
                            val targetScroll = (maxScroll.toFloat() * (index.toFloat() / (navItems.size - 1).coerceAtLeast(1))).toInt()
                            scrollState.animateScrollTo(targetScroll)
                        }
                    }
                }
            }

            Row(
                modifier = if (isScrollable) {
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 8.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                },
                horizontalArrangement = if (isScrollable) Arrangement.spacedBy(8.dp) else Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = item.tab == currentTab
                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) VoltagePrimaryContainer.copy(alpha = 0.85f) else Color.Transparent,
                        label = "pill_bg"
                    )
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) VoltageOnPrimaryContainer else VoltageOnSurfaceVariant,
                        label = "icon_color"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) VoltagePrimary else VoltageOnSurfaceVariant.copy(alpha = 0.7f),
                        label = "text_color"
                    )

                    Column(
                        modifier = if (isScrollable) {
                            Modifier
                                .widthIn(min = 78.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onTabSelected(item.tab) }
                                .testTag(item.testTag)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        } else {
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onTabSelected(item.tab) }
                                .testTag(item.testTag)
                                .padding(horizontal = 2.dp, vertical = 4.dp)
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(pillBgColor)
                                .padding(
                                    horizontal = if (isCompactLayout && !isScrollable) 10.dp else 14.dp,
                                    vertical = 3.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = iconColor,
                                modifier = Modifier.size(if (isCompactLayout && !isScrollable) 22.dp else 24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            fontSize = if (isCompactLayout && !isScrollable) 10.sp else 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
