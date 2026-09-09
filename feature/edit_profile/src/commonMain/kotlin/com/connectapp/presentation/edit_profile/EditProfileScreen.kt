package com.connectapp.presentation.edit_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.button_cancel
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_last_name
import com.connectapp.commonresources.label_name
import com.connectapp.commonresources.label_phone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel: EditProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditProfileEffect.NavigateBack -> onNavigateBack()
                EditProfileEffect.NavigateToProfile -> onSaved()
            }
        }
    }

    EditProfileContent(modifier = modifier, state = state, onIntent = onIntent)
}

/** Same avatar/card visual language as `ProfileScreen`, so switching between view and edit feels continuous. */
@Composable
private fun EditProfileContent(
    modifier: Modifier = Modifier,
    state: EditProfileState,
    onIntent: (EditProfileIntent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LabeledTextField(
                    value = state.firstName,
                    onValueChange = { onIntent(EditProfileIntent.FirstNameChanged(it)) },
                    label = stringResource(label_name),
                    icon = Icons.Default.Person,
                    enabled = !state.isLoading,
                )
                LabeledTextField(
                    value = state.lastName,
                    onValueChange = { onIntent(EditProfileIntent.LastNameChanged(it)) },
                    label = stringResource(label_last_name),
                    icon = Icons.Default.Person,
                    enabled = !state.isLoading,
                )
                LabeledTextField(
                    value = state.email,
                    onValueChange = { onIntent(EditProfileIntent.EmailChanged(it)) },
                    label = stringResource(label_email),
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    enabled = !state.isLoading,
                )
                LabeledTextField(
                    value = state.phone,
                    onValueChange = { onIntent(EditProfileIntent.PhoneChanged(it)) },
                    label = stringResource(label_phone),
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    enabled = !state.isLoading,
                )
            }
        }

        state.errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { onIntent(EditProfileIntent.CancelClicked) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(stringResource(button_cancel), style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = { onIntent(EditProfileIntent.SaveClicked) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(stringResource(button_save), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/** Shared by all four fields above — keeps their icon/shape/keyboard styling in one place. */
@Composable
private fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}
