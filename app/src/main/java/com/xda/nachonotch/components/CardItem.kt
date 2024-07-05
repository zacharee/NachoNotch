package com.xda.nachonotch.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CardItem(
    onClick: () -> Unit,
    icon: Pair<Painter, String>,
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
    widget: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon.first,
                contentDescription = icon.second,
            )

            Spacer(Modifier.size(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.size(4.dp))

                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            widget?.let {
                Spacer(modifier = Modifier.size(16.dp))

                it.invoke()
            }
        }
    }
}
