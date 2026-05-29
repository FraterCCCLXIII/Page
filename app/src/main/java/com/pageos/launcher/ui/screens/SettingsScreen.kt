package com.pageos.launcher.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pageos.launcher.BuildConfig
import com.pageos.launcher.R
import com.pageos.launcher.assistant.SystemSetting
import com.pageos.launcher.notifications.PageNotificationListener
import com.pageos.launcher.ui.PageViewModel
import com.pageos.launcher.ui.components.PageScreenHeader
import com.pageos.launcher.ui.theme.PageTheme

@Composable
fun SettingsScreen(
    viewModel: PageViewModel,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val spacing = PageTheme.spacing
    val context = LocalContext.current
    val showIcons by viewModel.showAppIcons.collectAsStateWithLifecycle()
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

        SectionTitle(stringResource(R.string.settings_default_apps))
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
        SettingsRow(
            title = stringResource(R.string.settings_set_default_launcher),
            onClick = { viewModel.openDefaultLauncherSettings() },
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
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

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
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
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
