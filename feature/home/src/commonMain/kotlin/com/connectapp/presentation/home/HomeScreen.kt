package com.connectapp.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.close
import com.connectapp.commonresources.home_welcome_subtitle
import com.connectapp.commonresources.home_welcome_title
import com.connectapp.commonresources.nav_item_clicked
import com.connectapp.presentation.home.component.HomeCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onShowToast: (message: String, actionLabel: String) -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    // Resolved up front, in composition (stringResource is @Composable and can't be called from
    // inside the LaunchedEffect below, which runs outside composition once the effect fires).
    val closeLabel = stringResource(close)
    val itemClickedMessages = state.items.associateWith { item ->
        stringResource(nav_item_clicked, stringResource(item.title))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowToast -> {
                    onShowToast(itemClickedMessages[effect.item].orEmpty(), closeLabel)
                }
            }
        }
    }

    // No statusBarsPadding() here — MainScreen's Scaffold (the app shell) already reserves that
    // space once for every screen; adding it again here would double it under the TopBar.
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        HomeScreenContent(
            modifier = modifier,
            state = state,
            onIntent = onIntent
        )
    }
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(home_welcome_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(home_welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(state.items) { item ->
            HomeCard(
                item = item,
                onClick = {
                    onIntent(HomeIntent.ItemClicked(item))
                }
            )
        }
    }
}

