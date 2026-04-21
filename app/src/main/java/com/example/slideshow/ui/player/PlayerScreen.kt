package com.example.slideshow.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.slideshow.util.ImmersiveMode
import com.example.slideshow.util.KeepScreenOn

private const val SWIPE_THRESHOLD_PX = 120f

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = viewModel(),
) {
    BackHandler(onBack = onBack)
    KeepScreenOn()
    ImmersiveMode()

    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.loading -> CircularProgressIndicator(color = Color.White)
            state.error != null -> Text(
                text = state.error!!,
                color = Color.White,
                modifier = Modifier.padding(24.dp),
            )
            else -> SlideshowStage(state, viewModel)
        }
    }
}

@Composable
private fun SlideshowStage(state: PlayerUiState, viewModel: PlayerViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { viewModel.togglePlay() })
            }
            .pointerInput(Unit) {
                var total = 0f
                detectHorizontalDragGestures(
                    onDragStart = { total = 0f },
                    onDragCancel = { total = 0f },
                    onDragEnd = {
                        when {
                            total > SWIPE_THRESHOLD_PX -> viewModel.previous()
                            total < -SWIPE_THRESHOLD_PX -> viewModel.next()
                        }
                        total = 0f
                    },
                ) { change, dragAmount ->
                    total += dragAmount
                    change.consume()
                }
            },
    ) {
        Crossfade(
            targetState = state.current,
            animationSpec = tween(durationMillis = 400),
            label = "slide",
            modifier = Modifier.fillMaxSize(),
        ) { uri ->
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (!state.isPlaying) {
            Icon(
                imageVector = Icons.Filled.Pause,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp),
            )
        }
    }
}
