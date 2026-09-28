package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VehicleType
import com.example.ui.AppUiState
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.*
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.ChargingTimeScreen
import com.example.ui.screens.ChartsScreen
import com.example.ui.screens.EconomyScreen
import com.example.ui.screens.InfoScreen
import com.example.ui.screens.OdometerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Toast listener
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopHeaderBar(
                state = uiState,
                onTitleClick = { viewModel.openChangeVehicleDialog(true) },
                onEditVehicleClick = { viewModel.openEditParametersForVehicle(uiState.selectedVehicle) },
                onLanguageClick = { viewModel.openLanguageDialog(true) }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentTab = uiState.currentTab,
                onTabSelected = { viewModel.setTab(it) },
                language = uiState.language,
                isPhev = true
            )
        },
        containerColor = VoltageSurface,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VoltageSurface)
        ) {
            Crossfade(targetState = uiState.currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    NavigationTab.CALCULATOR -> CalculatorScreen(state = uiState, viewModel = viewModel)
                    NavigationTab.ECONOMY -> EconomyScreen(state = uiState, viewModel = viewModel)
                    NavigationTab.ODOMETER -> OdometerScreen(state = uiState, viewModel = viewModel)
                    NavigationTab.CHARTS -> ChartsScreen(state = uiState, viewModel = viewModel)
                    NavigationTab.CHARGING_TIME -> ChargingTimeScreen(state = uiState, viewModel = viewModel)
                    NavigationTab.INFO -> InfoScreen(state = uiState, viewModel = viewModel)
                }
            }
        }
    }

    // Modal Dialogs
    if (uiState.isLanguageDialogOpen) {
        LanguageSelectorDialog(
            currentLanguage = uiState.language,
            onSelectLanguage = { viewModel.setLanguage(it) },
            onDismiss = { viewModel.openLanguageDialog(false) }
        )
    }

    if (uiState.isChangeVehicleDialogOpen) {
        VehicleSelectorDialog(
            currentVehicle = uiState.selectedVehicle,
            vehiclesList = uiState.vehiclesList,
            isSyncing = uiState.isSyncingCatalog,
            lastSyncTimestamp = uiState.lastSyncTimestamp,
            onSelectVehicle = { viewModel.selectVehicle(it) },
            onOpenAddVehicle = {
                viewModel.openChangeVehicleDialog(false)
                viewModel.openAddVehicleDialog(true)
            },
            onEditVehicle = {
                viewModel.openChangeVehicleDialog(false)
                viewModel.openEditParametersForVehicle(it)
            },
            onSyncVehicles = { viewModel.syncVehiclesFromCloud() },
            onDeleteVehicle = { viewModel.deleteVehicle(it) },
            onRestoreDefaults = { viewModel.restoreDefaultVehicles() },
            onOpenCustomizer = {
                viewModel.openChangeVehicleDialog(false)
                viewModel.openEditParametersDialog(true)
            },
            onDismiss = { viewModel.openChangeVehicleDialog(false) }
        )
    }

    if (uiState.isSyncSummaryDialogOpen) {
        val result = uiState.syncSummaryResult
        if (result != null) {
            VehicleSyncSummaryDialog(
                syncResult = result,
                onSelectVehicle = { viewModel.selectVehicle(it) },
                onDismiss = { viewModel.openSyncSummaryDialog(false) }
            )
        }
    }

    if (uiState.isAddVehicleDialogOpen) {
        AddVehicleDialog(
            onAddVehicle = { viewModel.addNewVehicle(it) },
            onDismiss = { viewModel.openAddVehicleDialog(false) }
        )
    }

    if (uiState.isEditParametersDialogOpen) {
        val vehicleToEdit = uiState.vehicleBeingEdited ?: uiState.selectedVehicle
        EditParametersDialog(
            vehicle = vehicleToEdit,
            onSaveParameters = { name, battery, elec, gas, ac, img ->
                viewModel.updateVehicleCustomValues(name, battery, elec, gas, ac, img, vehicleToEdit.id)
            },
            onResetToFactory = {
                viewModel.resetSingleVehicleToFactory(vehicleToEdit.id)
            },
            onDismiss = { viewModel.openEditParametersDialog(false) }
        )
    }

    if (uiState.isExportReportDialogOpen) {
        ExportReportDialog(
            state = uiState,
            onDismiss = { viewModel.openExportReportDialog(false) }
        )
    }
}
