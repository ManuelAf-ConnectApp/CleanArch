package com.connectapp.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onShowToast: (message: String, actionLabel: String) -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Resolved up front, in composition (stringResource is @Composable and can't be called from
    // inside the LaunchedEffect below, which runs outside composition once the effect fires).
    val closeLabel = stringResource(close)
    val notificationsDisabledNotice = stringResource(notifications_disabled_os_notice)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToLogin -> onLogout()
                SettingsEffect.NavigateToEditProfile -> onNavigateToEditProfile()
                SettingsEffect.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
                SettingsEffect.ShowNotificationsDisabledNotice ->
                    onShowToast(notificationsDisabledNotice, closeLabel)
            }
        }
    }

    // specs/015-notification-permissions US3: re-syncs the notifications toggle with the real OS
    // permission whenever this screen comes back to the foreground (e.g. returning from Ajustes).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onIntent(SettingsIntent.ScreenResumed)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ProfileHeader(
                name = state.userName,
                email = state.userEmail,
                onEditClick = { viewModel.onIntent(SettingsIntent.EditProfileClicked) }
            )
        }

        item { SectionHeader(title = stringResource(settings_section_account)) }
        item {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.Person,
                    title = stringResource(settings_item_personal_info),
                    onClick = { viewModel.onIntent(SettingsIntent.EditProfileClicked) }
                )
                // No onClick: there is no Security screen to navigate to yet — SettingsRow
                // shows this without a chevron/ripple so it doesn't look tappable for no reason.
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = stringResource(settings_item_security),
                )
            }
        }

        item { SectionHeader(title = stringResource(settings_section_preferences)) }
        item {
            SettingsGroup {
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(settings_item_notifications),
                    checked = state.notificationsEnabled,
                    onCheckedChange = { viewModel.onIntent(SettingsIntent.NotificationsToggled(it)) }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(settings_item_dark_mode),
                    checked = state.darkModeEnabled,
                    onCheckedChange = { viewModel.onIntent(SettingsIntent.DarkModeToggled(it)) }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.BugReport,
                    title = stringResource(settings_item_analytics_consent),
                    checked = state.analyticsConsentEnabled,
                    onCheckedChange = { viewModel.onIntent(SettingsIntent.AnalyticsConsentToggled(it)) }
                )
            }
        }

        item { SectionHeader(title = stringResource(settings_section_more)) }
        item {
            SettingsGroup {
                // No onClick: purely informational (shows the app version) — not a navigation target.
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = stringResource(settings_item_about),
                    subtitle = stringResource(settings_item_version, state.appVersion),
                )
                SettingsRow(
                    icon = Icons.Default.Description,
                    title = stringResource(settings_item_privacy),
                    onClick = { viewModel.onIntent(SettingsIntent.PrivacyPolicyClicked) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            if (state.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = { viewModel.onIntent(SettingsIntent.LogoutClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(logout))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(edit_profile))
            }
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp), content = content)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

/**
 * [onClick] is nullable — a row with nothing to navigate to (e.g. purely informational, or a
 * not-yet-built destination) renders without the trailing chevron/ripple, so its affordance
 * doesn't lie about being tappable.
 */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val rowContent = @Composable {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            content = rowContent,
        )
    } else {
        rowContent()
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
