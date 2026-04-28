package com.stephenbrines.trailweight.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.Backpack,
        title = "Your gear, organized",
        body = "Add everything you own to your gear inventory. Paste a product URL and the name and weight are fetched automatically from REI, Zpacks, Gossamer Gear, and more.",
    ),
    OnboardingPage(
        icon = Icons.Default.Scale,
        title = "Go ultralight",
        body = "Build pack lists for each trip. Track base weight, worn weight, and consumables in real time. See your ultralight classification update as you pack.",
    ),
    OnboardingPage(
        icon = Icons.Default.Map,
        title = "Plan smarter",
        body = "Get gear recommendations based on your route elevation, season, and terrain type. Plan every resupply box for long trails with mile markers and shipping details.",
    ),
    OnboardingPage(
        icon = Icons.Default.Download,
        title = "Import from Lighterpack",
        body = "Already on Lighterpack? Export your list as a CSV and import it here in seconds. Everything stays on your device — no account, no cloud required.",
    ),
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]
    val isLast = currentPage == pages.lastIndex

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Page indicator
        Row(
            Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            pages.indices.forEach { i ->
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .size(if (i == currentPage) 24.dp else 8.dp, 8.dp)
                )
            }
        }

        // Page content
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },
            modifier = Modifier.weight(1f),
            label = "onboarding",
        ) { idx ->
            val p = pages[idx]
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = p.icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.height(40.dp))
                Text(
                    p.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    p.body,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                )
            }
        }

        // Actions
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    if (isLast) onComplete() else currentPage++
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isLast) "Get Started" else "Continue")
            }
            if (!isLast) {
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
