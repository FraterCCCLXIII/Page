package com.pageos.launcher.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pageos.launcher.BuildConfig
import com.pageos.launcher.R
import com.pageos.launcher.assistant.SystemSetting
import com.pageos.launcher.data.AppLayout
import com.pageos.launcher.data.ThemeMode
import com.pageos.launcher.launcher.DefaultLauncher
import com.pageos.launcher.notifications.PageNotificationListener
import com.pageos.launcher.ui.PageViewModel
import com.pageos.launcher.ui.components.PageButton
import com.pageos.launcher.ui.components.PageButtonVariant
import com.pageos.launcher.ui.components.PageScreenHeader
import com.pageos.launcher.ui.components.PageText
import com.pageos.launcher.ui.components.PageTextRole
import com.pageos.launcher.ui.theme.PageTheme

@Composable
fun SettingsScreen(
    viewModel: PageViewModel,
    onBack: () -> Unit,
    onOpenSetup: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val spacing = PageTheme.spacing
    val context = LocalContext.current
    val showIcons by viewModel.showAppIcons.collectAsStateWithLifecycle()
    val showCommandBar by viewModel.showCommandBar.collectAsStateWithLifecycle()
    val appLayout by viewModel.appLayout.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notificationsEnabled = PageNotificationListener.isEnabled(context)
    val comingSoon = stringResource(R.string.coming_soon)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenPadding),
    ) {
        PageScreenHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
        )

        SettingsToggleRow(
            title = stringResource(R.string.settings_show_icons),
            checked = showIcons,
            onCheckedChange = viewModel::setShowAppIcons,
        )
        SettingsToggleRow(
            title = stringResource(R.string.settings_show_command_bar),
            checked = showCommandBar,
            onCheckedChange = viewModel::setShowCommandBar,
        )
        SettingsToggleRow(
            title = stringResource(R.string.settings_tile_view),
            checked = appLayout == AppLayout.TILES,
            onCheckedChange = { useTiles ->
                viewModel.setAppLayout(if (useTiles) AppLayout.TILES else AppLayout.LIST)
            },
        )

        SectionTitle(stringResource(R.string.settings_appearance))
        ThemeModeSelector(
            selected = themeMode,
            onSelect = viewModel::setThemeMode,
        )

        SectionTitle(stringResource(R.string.settings_default_apps))
        SettingsRow(
            title = stringResource(R.string.settings_recommended_apps),
            subtitle = stringResource(R.string.settings_recommended_apps_subtitle),
            onClick = onOpenSetup,
        )
        SettingsRow(
            title = stringResource(R.string.settings_default_apps),
            subtitle = comingSoon,
            onClick = { onMessage(comingSoon) },
        )
        SettingsRow(
            title = stringResource(R.string.settings_hidden_apps),
            subtitle = comingSoon,
            onClick = { onMessage(comingSoon) },
        )

        SectionTitle(stringResource(R.string.settings_focus_mode))
        SettingsRow(
            title = stringResource(R.string.settings_notification_digest),
            subtitle = if (notificationsEnabled) "Enabled" else "Tap to grant access",
            onClick = {
                if (!viewModel.openNotificationAccessSettings()) onMessage(comingSoon)
            },
        )
        SettingsRow(
            title = stringResource(R.string.settings_focus_mode),
            subtitle = comingSoon,
            onClick = { onMessage(comingSoon) },
        )

        SectionTitle(stringResource(R.string.settings_system))
        var isDefaultLauncher by remember { mutableStateOf(viewModel.isDefaultLauncher()) }
        val roleLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            isDefaultLauncher = viewModel.isDefaultLauncher()
        }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            isDefaultLauncher = viewModel.isDefaultLauncher()
        }
        SettingsRow(
            title = if (isDefaultLauncher) {
                stringResource(R.string.settings_default_launcher_active)
            } else {
                stringResource(R.string.settings_set_default_launcher)
            },
            subtitle = if (isDefaultLauncher) {
                stringResource(R.string.settings_default_launcher_active_subtitle)
            } else {
                stringResource(R.string.settings_set_default_launcher_subtitle)
            },
            onClick = { roleLauncher.launch(DefaultLauncher.requestIntent(context)) },
        )
        SystemSetting.entries.forEach { setting ->
            SettingsRow(
                title = setting.displayName(),
                onClick = { viewModel.openSystemSetting(setting) },
            )
        }

        SectionTitle(stringResource(R.string.settings_about))
        SettingsRow(
            title = stringResource(R.string.settings_about),
            subtitle = "Page ${BuildConfig.VERSION_NAME} — a calm, private launcher.",
            onClick = { viewModel.openSystemSetting(SystemSetting.APP_DETAILS) },
        )

        Spacer(Modifier.height(spacing.xxl))
    }
}

private fun SystemSetting.displayName(): String = when (this) {
    SystemSetting.WIFI -> "Wi-Fi settings"
    SystemSetting.BLUETOOTH -> "Bluetooth settings"
    SystemSetting.NOTIFICATIONS -> "Notification settings"
    SystemSetting.BATTERY -> "Battery settings"
    SystemSetting.ACCESSIBILITY -> "Accessibility settings"
    SystemSetting.APP_DETAILS -> "Page app settings"
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(PageTheme.spacing.lg))
    PageText(
        text = text.uppercase(),
        role = PageTextRole.Label,
        modifier = Modifier.padding(bottom = PageTheme.spacing.xs),
    )
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    val spacing = PageTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = spacing.rowVertical),
    ) {
        PageText(
            text = title,
            role = PageTextRole.Body,
        )
        if (subtitle != null) {
            PageText(
                text = subtitle,
                role = PageTextRole.BodySecondary,
            )
        }
    }
}

/**
 * A calm segmented selector for the dark/light appearance. The active mode is a
 * filled [PageButton]; the others are outlined, so it reads as one consistent
 * control in either theme.
 */
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val spacing = PageTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        themeModeOptions.forEach { (mode, labelRes) ->
            PageButton(
                label = stringResource(labelRes),
                onClick = { onSelect(mode) },
                modifier = Modifier.weight(1f),
                variant = if (mode == selected) {
                    PageButtonVariant.Primary
                } else {
                    PageButtonVariant.Secondary
                },
            )
        }
    }
}

private val themeModeOptions: List<Pair<ThemeMode, Int>> = listOf(
    ThemeMode.SYSTEM to R.string.theme_system,
    ThemeMode.LIGHT to R.string.theme_light,
    ThemeMode.DARK to R.string.theme_dark,
)

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = PageTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.rowVertical),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PageText(
            text = title,
            role = PageTextRole.Body,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.onBackground,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }
}
