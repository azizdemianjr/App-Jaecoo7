package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*

@Composable
fun LanguageSelectorDialog(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = VoltageSurfaceContainerLow,
            border = BorderStroke(1.dp, VoltageOutline.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_language_selector")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = VoltagePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (currentLanguage == AppLanguage.EN_US) "Select Language" else "Selecione o Idioma",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VoltageOnSurface
                    )
                }

                HorizontalDivider(color = VoltageOutline.copy(alpha = 0.2f))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppLanguage.entries.forEach { language ->
                        val isSelected = language == currentLanguage
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) VoltagePrimaryContainer.copy(alpha = 0.35f) else VoltageCardBg,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) VoltagePrimary else VoltageOutline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectLanguage(language) }
                                .testTag("lang_option_${language.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = language.flag,
                                        fontSize = 24.sp
                                    )
                                    Column {
                                        Text(
                                            text = language.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) VoltagePrimary else VoltageOnSurface
                                        )
                                        Text(
                                            text = if (language == AppLanguage.PT_BR) "Português (Brasil)" else "English (United States)",
                                            fontSize = 12.sp,
                                            color = VoltageOnSurfaceVariant
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = VoltagePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_lang_dialog")
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.EN_US) "Close" else "Fechar",
                            color = VoltageOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
