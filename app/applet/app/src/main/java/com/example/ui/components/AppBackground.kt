package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import java.io.File

@Composable
fun AppBackground(
    preset: String,
    customImagePath: String?,
    dim: Float = 0.12f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        when (preset) {
            "ROMANTIC_BW" -> {
                Image(
                    painter = painterResource(id = R.drawable.app_logo_zava),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(dim),
                    contentScale = ContentScale.Crop
                )
            }
            "CUSTOM_IMAGE" -> {
                if (!customImagePath.isNullOrBlank() && File(customImagePath).exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(customImagePath))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(dim),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            "SOFT_GRADIENT" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                                )
                            )
                        )
                )
            }
            else -> {
                // "NONE" - clean solid theme background
            }
        }

        content()
    }
}
