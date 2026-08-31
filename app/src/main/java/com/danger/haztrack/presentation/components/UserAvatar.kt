package com.danger.haztrack.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.danger.haztrack.R

/**
 * Renders the signed-in user's profile picture (e.g. from a Google account) when available,
 * falling back to a tonal circle with the user's initial otherwise.
 */
@Composable
fun UserAvatar(
    photoUrl: String?,
    initial: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
) {
    if (photoUrl != null) {
        AsyncImage(
            model = photoUrl,
            contentDescription = stringResource(R.string.profile_photo_content_description),
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style = textStyle,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
