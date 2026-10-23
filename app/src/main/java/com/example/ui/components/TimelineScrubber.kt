package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanCardBorder
import com.example.ui.theme.PetroleumAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TimelineScrubber(
    selectedHour: Float,
    maxHours: Float,
    isPlaying: Boolean,
    onHourChanged: (Float) -> Unit,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = OceanCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_scrubber")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = onTogglePlayback,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isPlaying) PetroleumAmber else CyanNeon,
                            contentColor = OceanAbyss
                        ),
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("playback_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { onHourChanged((selectedHour - 1f).coerceAtLeast(0f)) },
                        modifier = Modifier.size(36.dp).testTag("step_backward_button")
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Step Back", tint = TextPrimary)
                    }

                    IconButton(
                        onClick = { onHourChanged((selectedHour + 1f).coerceAtMost(maxHours)) },
                        modifier = Modifier.size(36.dp).testTag("step_forward_button")
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = "Step Forward", tint = TextPrimary)
                    }

                    IconButton(
                        onClick = { onHourChanged(0f) },
                        modifier = Modifier.size(36.dp).testTag("restart_timeline_button")
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Reset", tint = TextMuted)
                    }
                }

                Surface(
                    color = OceanAbyss,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OceanCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "T+${String.format("%04.1f", selectedHour)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                        Text(
                            text = " / ${String.format("%.0f", maxHours)}h",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = selectedHour,
                onValueChange = onHourChanged,
                valueRange = 0f..maxHours,
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanNeon,
                    inactiveTrackColor = OceanAbyss
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timeline_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0h (Release)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                Text("+${String.format("%.0f", maxHours / 2f)}h", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                Text("+${String.format("%.0f", maxHours)}h Forecast", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}
