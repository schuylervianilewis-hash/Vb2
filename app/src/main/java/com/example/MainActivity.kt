package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.backup.VianBackupManager
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogTag
import com.example.diagnostics.ui.LogKeeperScreen
import com.example.onboarding.OnboardingManager
import com.example.onboarding.WelcomeOnboardingScreen
import com.example.settings.CustomShortcutsManager
import com.example.settings.GeneralSettingsScreen
import com.example.settings.KeyboardSettings
import com.example.settings.KeyboardSettingsManager
import com.example.settings.ShortcutItem
import com.example.ui.theme.MyApplicationTheme
import com.example.vault.personal.PersonalVaultPlaceholderTab
import com.example.vault.ui.VaultSettingsTab
import com.example.voice.ui.VoiceModelManagementTab

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LogKeeper.log(LogTag.SYSTEM, LogLevel.INFO, "MainActivity launched")
        
        val initialDestination = intent?.getStringExtra("destination")

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val onboardingManager = remember { OnboardingManager(context) }
                val isCompleted = onboardingManager.isOnboardingCompleted()
                
                var showOnboarding by remember { 
                    mutableStateOf(!isCompleted && initialDestination == null) 
                }
                var showDirectLogKeeper by remember { 
                    mutableStateOf(initialDestination == "log_keeper") 
                }
                var showDirectAppearanceTester by remember {
                    mutableStateOf(false)
                }

                if (showDirectLogKeeper) {
                    LogKeeperScreen(onNavigateBack = { 
                        showDirectLogKeeper = false
                        if (!onboardingManager.isOnboardingCompleted()) {
                            showOnboarding = true
                        }
                    })
                } else if (showDirectAppearanceTester) {
                    com.example.ui.AppearanceTesterScreen(onNavigateBack = {
                        showDirectAppearanceTester = false
                        if (!onboardingManager.isOnboardingCompleted()) {
                            showOnboarding = true
                        }
                    })
                } else if (showOnboarding) {
                    WelcomeOnboardingScreen(
                        onboardingManager = onboardingManager,
                        onOpenAppearanceTester = { showDirectAppearanceTester = true },
                        onOpenSettings = { showOnboarding = false },
                        onOpenLogKeeper = { showDirectLogKeeper = true }
                    )
                } else {
                    MainSettingsScreen(
                        onOpenOnboarding = { showOnboarding = true },
                        onOpenLogKeeper = { showDirectLogKeeper = true }
                    )
                }
            }
        }
    }
}

enum class SettingsSubPage(val title: String, val icon: ImageVector) {
    ROOT("Vian Board", Icons.Default.Settings),
    APPEARANCE("Appearance & Key Styling", Icons.Default.Palette),
    TYPING_BEHAVIOR("Typing Behavior & Rules", Icons.Default.Tune),
    LOG_KEEPER("Log Keeper", Icons.Default.ListAlt),
    APPEARANCE_TESTER("Appearance & Layout Tester", Icons.Default.Keyboard)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsScreen(
    onOpenOnboarding: () -> Unit = {},
    onOpenLogKeeper: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(SettingsSubPage.ROOT) }
    var isKeyboardEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = imm.enabledInputMethodList
        val packageName = context.packageName
        isKeyboardEnabled = enabledMethods.any { it.packageName == packageName }
    }

    if (currentPage == SettingsSubPage.LOG_KEEPER) {
        LogKeeperScreen(onNavigateBack = { currentPage = SettingsSubPage.ROOT })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentPage == SettingsSubPage.ROOT) "Vian Board" else currentPage.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    if (currentPage != SettingsSubPage.ROOT) {
                        IconButton(onClick = { currentPage = SettingsSubPage.ROOT }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(
                            Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentPage) {
                SettingsSubPage.ROOT -> SettingsRootList(
                    isKeyboardEnabled = isKeyboardEnabled,
                    onEnableClicked = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
                    onSelectClicked = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showInputMethodPicker()
                    },
                    onNavigate = { currentPage = it }
                )
                SettingsSubPage.APPEARANCE -> com.example.settings.AppearanceSettingsScreen(
                    onNavigateBack = { currentPage = SettingsSubPage.ROOT }
                )
                SettingsSubPage.TYPING_BEHAVIOR -> com.example.settings.TypingBehaviorScreen(
                    onNavigateBack = { currentPage = SettingsSubPage.ROOT }
                )
                SettingsSubPage.LOG_KEEPER -> {
                    // Handled above
                }
                SettingsSubPage.APPEARANCE_TESTER -> com.example.ui.AppearanceTesterScreen(
                    onNavigateBack = { currentPage = SettingsSubPage.ROOT }
                )
            }
        }
    }
}

@Composable
fun SettingsRootList(
    isKeyboardEnabled: Boolean,
    onEnableClicked: () -> Unit,
    onSelectClicked: () -> Unit,
    onNavigate: (SettingsSubPage) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Setup banner if keyboard is not active
        if (!isKeyboardEnabled) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Keyboard Setup Required", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onEnableClicked,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) { Text("1. Enable", fontSize = 12.sp) }
                            OutlinedButton(
                                onClick = onSelectClicked,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) { Text("2. Select", fontSize = 12.sp) }
                        }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        val items = listOf(
            SettingsSubPage.APPEARANCE,
            SettingsSubPage.TYPING_BEHAVIOR,
            SettingsSubPage.LOG_KEEPER,
            SettingsSubPage.APPEARANCE_TESTER
        )

        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(item) }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    item.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
fun GeneralSettingsTab(
    settings: KeyboardSettings,
    isKeyboardEnabled: Boolean,
    onEnableClicked: () -> Unit,
    onSelectClicked: () -> Unit,
    onSettingsChanged: (KeyboardSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Layout & Height Dimensions
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Keyboard Layout & Sizing", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Height Scale: ${"%.2f".format(settings.heightScale)}x", fontSize = 13.sp)
                    Slider(
                        value = settings.heightScale,
                        onValueChange = { onSettingsChanged(settings.copy(heightScale = it)) },
                        valueRange = 0.8f..1.4f,
                        steps = 5
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bottom Inset Padding: ${settings.bottomInsetPaddingDp} dp", fontSize = 13.sp)
                    Slider(
                        value = settings.bottomInsetPaddingDp.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(bottomInsetPaddingDp = it.toInt())) },
                        valueRange = 0f..32f,
                        steps = 8
                    )
                }
            }
        }

        // Feature Toggles
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Typing Features", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingToggleRow(
                        title = "Number Row",
                        subtitle = "Display dedicated number row on top of keyboard",
                        checked = settings.showNumberRow,
                        onCheckedChange = { onSettingsChanged(settings.copy(showNumberRow = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "Smart Multiply Morph (x -> ×)",
                        subtitle = "Automatically converts 'x' to '×' between numbers (e.g. 1920x1080 -> 1920×1080)",
                        checked = settings.smartMultiplyMorph,
                        onCheckedChange = { onSettingsChanged(settings.copy(smartMultiplyMorph = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "Double-Space Period",
                        subtitle = "Inserts a period and space when double-tapping spacebar",
                        checked = settings.doubleSpacePeriod,
                        onCheckedChange = { onSettingsChanged(settings.copy(doubleSpacePeriod = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on keypress",
                        checked = settings.hapticFeedbackEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(hapticFeedbackEnabled = it)) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingToggleRow(
                        title = "Sound on Keypress",
                        subtitle = "Play auditory feedback clicks",
                        checked = settings.soundOnKeyPress,
                        onCheckedChange = { onSettingsChanged(settings.copy(soundOnKeyPress = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ShortcutsTab(
    configuredShortcuts: List<ShortcutItem>,
    onShortcutsChanged: (List<ShortcutItem>) -> Unit
) {
    val available = CustomShortcutsManager.AVAILABLE_SHORTCUTS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Top Bar Shortcuts Palette", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Select up to 5 shortcuts to appear directly on the keyboard's top utility strip.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Keyboard Strip Preview (5 slots)", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until 5) {
                            val item = configuredShortcuts.getOrNull(i)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item?.label ?: "Empty",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Available Shortcut Palette", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(available) { item ->
            val isSelected = configuredShortcuts.any { it.id == item.id }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val current = configuredShortcuts.toMutableList()
                        if (isSelected) {
                            current.removeAll { it.id == item.id }
                        } else {
                            if (current.size < 5) {
                                current.add(item)
                            }
                        }
                        onShortcutsChanged(current)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.label, fontWeight = FontWeight.Medium)
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun ModularBackupTab(
    backupManager: VianBackupManager,
    onBackupRestored: () -> Unit
) {
    val context = LocalContext.current

    // Export state
    var exportSettings by remember { mutableStateOf(true) }
    var exportShortcuts by remember { mutableStateOf(true) }
    var exportVoice by remember { mutableStateOf(true) }
    var exportPersonalVault by remember { mutableStateOf(true) }
    var exportSecurityVault by remember { mutableStateOf(false) }
    var vaultExportPassword by remember { mutableStateOf("") }

    // Restore inspection state
    var inspectionResult by remember { mutableStateOf<VianBackupManager.ZipInspectionResult?>(null) }
    var restoreSettings by remember { mutableStateOf(true) }
    var restoreShortcuts by remember { mutableStateOf(true) }
    var restoreVoice by remember { mutableStateOf(true) }
    var restoreVault by remember { mutableStateOf(false) }
    var vaultRestorePassword by remember { mutableStateOf("") }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                val selection = VianBackupManager.BackupSelection(
                    includeSettings = exportSettings,
                    includeShortcuts = exportShortcuts,
                    includeVoiceConfig = exportVoice,
                    includePersonalVault = exportPersonalVault,
                    includeSecurityVault = exportSecurityVault,
                    vaultPassword = if (exportSecurityVault && vaultExportPassword.isNotEmpty()) vaultExportPassword.toCharArray() else null
                )
                val success = backupManager.createModularZipBackup(selection, outputStream)
                if (success) {
                    Toast.makeText(context, "Modular ZIP backup created!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to create backup ZIP", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedRestoreUri = uri
            val result = backupManager.inspectZipBackup(uri)
            inspectionResult = result
            if (result != null) {
                restoreSettings = result.hasSettings
                restoreShortcuts = result.hasShortcuts
                restoreVoice = result.hasVoiceConfig
                restoreVault = result.hasSecurityVault
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Modular ZIP Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Export structured ZIP archives with password-gated security vault protection, or import backups (including legacy HeliBoard archives).",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Create Backup
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Create Modular ZIP Backup", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "Keyboard Preferences",
                        subtitle = "Include layout heights, haptics & styling",
                        checked = exportSettings,
                        onCheckedChange = { exportSettings = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    SettingToggleRow(
                        title = "Text Shortcuts",
                        subtitle = "Include custom top bar shortcuts",
                        checked = exportShortcuts,
                        onCheckedChange = { exportShortcuts = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    SettingToggleRow(
                        title = "Voice Input Config",
                        subtitle = "Include Whisper & VAD settings",
                        checked = exportVoice,
                        onCheckedChange = { exportVoice = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    SettingToggleRow(
                        title = "Personal Vault",
                        subtitle = "Include private scratchpads & notes",
                        checked = exportPersonalVault,
                        onCheckedChange = { exportPersonalVault = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    SettingToggleRow(
                        title = "Security Vault (KeePass)",
                        subtitle = "AES-256-GCM encrypted credentials (requires password)",
                        checked = exportSecurityVault,
                        onCheckedChange = { exportSecurityVault = it }
                    )

                    if (exportSecurityVault) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = vaultExportPassword,
                            onValueChange = { vaultExportPassword = it },
                            label = { Text("Vault Master Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            createBackupLauncher.launch("vian_board_backup_$timestamp.zip")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Modular ZIP")
                    }
                }
            }
        }

        // Section 2: Restore Backup
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Restore / Import ZIP Backup", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Supports native Vian Board modular ZIPs and legacy HeliBoard backup archives.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { openBackupLauncher.launch(arrayOf("application/zip", "*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select ZIP Backup File")
                    }

                    val inspect = inspectionResult
                    if (inspect != null && selectedRestoreUri != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val typeLabel = if (inspect.isHeliBoardBackup) "HeliBoard Backup Archive" else if (inspect.isVianBoardBackup) "Vian Board Modular Archive" else "Generic ZIP Archive"
                                Text("Archive: $typeLabel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                if (inspect.timestamp != null) {
                                    Text("Created: ${inspect.timestamp}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (inspect.hasSettings) {
                                    SettingToggleRow("Restore Settings", "Apply keyboard configuration", restoreSettings) { restoreSettings = it }
                                }
                                if (inspect.hasShortcuts) {
                                    SettingToggleRow("Restore Shortcuts", "Apply shortcut mappings", restoreShortcuts) { restoreShortcuts = it }
                                }
                                if (inspect.hasSecurityVault) {
                                    SettingToggleRow("Restore Security Vault", "Decrypted with master password", restoreVault) { restoreVault = it }
                                    if (restoreVault) {
                                        OutlinedTextField(
                                            value = vaultRestorePassword,
                                            onValueChange = { vaultRestorePassword = it },
                                            label = { Text("Enter Vault Master Password") },
                                            visualTransformation = PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val uri = selectedRestoreUri ?: return@Button
                                        val success = backupManager.restoreFromZipBackup(
                                            uri = uri,
                                            restoreSettings = restoreSettings,
                                            restoreShortcuts = restoreShortcuts,
                                            restoreVoice = restoreVoice,
                                            restoreVault = restoreVault,
                                            vaultPassword = if (vaultRestorePassword.isNotEmpty()) vaultRestorePassword.toCharArray() else null
                                        )
                                        if (success) {
                                            onBackupRestored()
                                        } else {
                                            Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Apply Selected Restore")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

