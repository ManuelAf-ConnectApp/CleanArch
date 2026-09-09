package com.connectapp.presentation.privacy_policy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.privacy_policy_content
import org.jetbrains.compose.resources.stringResource

/**
 * Static content, no ViewModel (specs/008-complete-edit-profile-privacy/research.md D2's
 * companion decision, same YAGNI reasoning as OrderDetailScreen in 009): this screen has no
 * state and no domain/data dependency, so a ViewModel would be pure boilerplate.
 *
 * No local back button: the app shell's `CustomTopBar` already renders one for every route with
 * `showBackButton` (`NavigationRoute.kt`), `PrivacyPolicyRoute` included — a second one here
 * would just duplicate it.
 */
@Composable
fun PrivacyPolicyScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(privacy_policy_content),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
