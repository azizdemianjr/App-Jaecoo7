package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.AppUiState
import com.example.ui.MainViewModel
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@Composable
fun InfoScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = state.strings
    var pixCopied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = strings.infoHeaderTitle,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = VoltageSecondary
            )
            Text(
                text = strings.infoHeaderSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = VoltageOnSurfaceVariant
            )
        }

        // Card 0: Idioma / Language Settings
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_language_settings"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
            border = BorderStroke(1.dp, VoltagePrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VoltagePrimaryContainer.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = VoltagePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = strings.languageSectionTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VoltageOnSurface
                        )
                        Text(
                            text = strings.languageSectionDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = VoltageOnSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = lang == state.language
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) VoltagePrimaryContainer.copy(alpha = 0.35f) else VoltageCardBg,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) VoltagePrimary else VoltageOutline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setLanguage(lang) }
                                .testTag("info_lang_btn_${lang.code}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(lang.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = lang.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) VoltagePrimary else VoltageOnSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 1: Sobre o Aplicativo
        Card(
            modifier = Modifier.fillMaxWidth().testTag("card_about"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
            border = BorderStroke(1.dp, VoltageCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VoltagePrimaryContainer.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = VoltagePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = strings.aboutCardTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface
                    )
                }

                Text(
                    text = strings.aboutCardText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoltageOnSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }

        // Card 2: Desenvolvedor & Apoio
        Card(
            modifier = Modifier.fillMaxWidth().testTag("card_developer"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
            border = BorderStroke(1.dp, VoltageCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VoltageSecondaryContainer.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = VoltageSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = strings.devCardTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface
                    )
                }

                Text(
                    text = strings.devCardText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoltageOnSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }

        // Card 3: Apoie o Projeto (PIX)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("card_support"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
            border = BorderStroke(1.dp, VoltageCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VoltagePrimaryContainer.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = VoltagePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = strings.supportCardTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface
                    )
                }

                Text(
                    text = strings.supportCardText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoltageOnSurfaceVariant,
                    lineHeight = 22.sp
                )

                Text(
                    text = strings.pixKeyLabel,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoltageOnSurfaceVariant.copy(alpha = 0.8f)
                )

                // PIX Container with Copiar Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VoltageInputBg,
                    border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.pixKey,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = VoltageOnSurface,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Chave PIX", state.pixKey)
                                clipboard.setPrimaryClip(clip)
                                pixCopied = true
                                viewModel.showToast(strings.toastPixCopied)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pixCopied) VoltageSuccess else VoltagePrimaryContainer
                            ),
                            modifier = Modifier.testTag("btn_copy_pix")
                        ) {
                            Icon(
                                imageVector = if (pixCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (pixCopied) VoltageOnSuccess else VoltageOnPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (pixCopied) strings.btnCopied else strings.btnCopy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pixCopied) VoltageOnSuccess else VoltageOnPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Card: Atualização do Catálogo de Veículos
        Card(
            modifier = Modifier.fillMaxWidth().testTag("card_sync_vehicles"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VoltageSurfaceContainerLow),
            border = BorderStroke(1.dp, VoltageSecondary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VoltageSecondaryContainer.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = VoltageSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = strings.cloudCatalogTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VoltageOnSurface
                        )
                        Text(
                            text = "${state.vehiclesList.size} ${if (state.language == AppLanguage.EN_US) "models loaded" else "modelos carregados"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoltageOnSurfaceVariant
                        )
                    }
                }

                Text(
                    text = strings.cloudCatalogDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoltageOnSurfaceVariant,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = { viewModel.syncVehiclesFromCloud() },
                    enabled = !state.isSyncingCatalog,
                    modifier = Modifier.fillMaxWidth().testTag("btn_sync_vehicles_info"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltageSecondaryContainer,
                        contentColor = VoltageOnSecondary
                    )
                ) {
                    if (state.isSyncingCatalog) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = VoltageOnSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.searchingUpdates, color = VoltageOnSecondary, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = VoltageOnSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.btnCheckUpdates, color = VoltageOnSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // App Version Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${if (state.language == AppLanguage.EN_US) "Version" else "Versão"} ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodySmall,
                color = VoltageOnSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

