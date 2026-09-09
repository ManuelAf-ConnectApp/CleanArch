package com.connectapp.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.connectapp.commonresources.*
import org.jetbrains.compose.resources.StringResource

data class DashboardItem(val title: StringResource, val subtitle: StringResource, val icon: ImageVector)

val dashboardItems = listOf(
    DashboardItem(home_projects_title, home_projects_subtitle, Icons.Default.Folder),
    DashboardItem(home_tasks_title, home_tasks_subtitle, Icons.Default.CheckCircle),
    DashboardItem(home_messages_title, home_messages_subtitle, Icons.Default.Mail),
    DashboardItem(settings, home_settings_subtitle, Icons.Default.Settings)
)

data class HomeState(
    val items: List<DashboardItem> = dashboardItems,
)
