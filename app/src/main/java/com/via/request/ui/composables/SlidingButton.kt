package com.via.request.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.via.request.ui.theme.ButtonOutline
import com.via.request.ui.theme.VIARequestTheme
import kotlin.math.roundToInt

@Preview
@Composable
fun SlidingButtonPreview() {
    VIARequestTheme() {
        SlidingButton(
            originalLabel = "Slide to approve",
            finishedLabel = "Approved",
            onFinished = {}
        )
    }
}

@Preview
@Composable
fun SliderButtonPreview() {
    VIARequestTheme() {
        SliderButton(
            originalLabel = "Slide to approve",
            finishedLabel = "Approved",
            onFinished = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderButton(
    modifier: Modifier = Modifier,
    originalLabel: String,
    finishedLabel: String,
    colorLeft: Color = MaterialTheme.colorScheme.primary,
    colorRight: Color = MaterialTheme.colorScheme.secondary,
    onFinished: () -> Unit,
) {

    var isFinished by remember {
        mutableStateOf(false)
    }

    var value by remember {
        mutableFloatStateOf(0F)
    }

    val animatedValue by animateFloatAsState(value)

    val label = if (isFinished) {
        finishedLabel
    } else {
        originalLabel
    }

    val brush = brushFromPercentage(
        animatedValue,
        leftColor = colorLeft,
        rightColor = colorRight
    )

    Slider(
        modifier = modifier,
        value = animatedValue,
        onValueChange = {
            if (!isFinished) {
                value = it
            }
        },
        onValueChangeFinished = {
            if (value >= 0.9F) {
                value = 1F
                isFinished = true
                onFinished()
            } else {
                value = 0F
            }
        },
        track = {
            SlidingButtonTrack(
                label = label,
                brush = brush
            )
        },
        thumb = {
            DefaultSlidingButtonThumb(
                isFinished = isFinished
            )
        }
    )

}

/**
 * I built this one first, assuming for some reason that the native Slider wouldn't work.
 * After I had added the FINISHING TOUCHES to this, I thought about it a bit more and wondered
 * was I missing something in Slider.
 *
 * Well, I was. lol So I went back, coded it using the native Slider Composables.
 * Leaving this here because I'm still proud of it.
 */
@Composable
@Deprecated("Use SliderButton instead")
fun SlidingButton(
    modifier: Modifier = Modifier,
    originalLabel: String,
    finishedLabel: String,
    colorLeft: Color = MaterialTheme.colorScheme.primary,
    colorRight: Color = MaterialTheme.colorScheme.secondary,
    thumb: @Composable (percentage: Float) -> Unit = { DefaultSlidingButtonThumb(
        isFinished = it == 1F,
    ) },
    onFinished: () -> Unit,
) {

    val density = LocalDensity.current

    var isFinished by remember {
        mutableStateOf(false)
    }

    var offsetX by remember { mutableFloatStateOf(0f) }

    val draggableState = rememberDraggableState {
        if (!isFinished) {
            offsetX += it.coerceAtLeast(0F)
        }
    }

    val label = if (isFinished) {
        finishedLabel
    } else {
        originalLabel
    }

    BoxWithConstraints (
        modifier = modifier,
        propagateMinConstraints = true,
        contentAlignment = Alignment.CenterStart
    ) {
        val maxWidthInRelationToThumb = remember(density, maxWidth) {
            with(density) {
                (maxWidth - 64.dp).toPx()
            }
        }

        val slidingPercentage = (offsetX/maxWidthInRelationToThumb).coerceIn(0F, 1F)

        val brush = brushFromPercentage(
            slidingPercentage,
            leftColor = colorLeft,
            rightColor = colorRight
        )

        val intOffset by animateIntOffsetAsState(
            IntOffset(offsetX.roundToInt().coerceIn(
                minimumValue = 0,
                maximumValue = maxWidthInRelationToThumb.toInt()
            ), 0)
        )

        SlidingButtonTrack(
            label = label,
            brush = brush
        )

        Box(
            modifier = Modifier
                .offset {
                    intOffset
                }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX <= maxWidthInRelationToThumb) {
                            offsetX = 0F
                        } else {
                            offsetX = maxWidthInRelationToThumb
                            isFinished = true
                            onFinished()
                        }
                    }
                )
        ) {
            thumb(slidingPercentage)
        }
    }
}

@Composable
private fun SlidingButtonTrack(
    label: String,
    brush: Brush,
) {
    AnimatedContent(label) {
        Text(
            it,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ButtonOutline, shape = RoundedCornerShape(10.dp))
                .background(
                    brush = brush,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(
                    vertical = 12.dp
                )
            ,
            textAlign = TextAlign.Center,
            color = Color.White,
        )
    }
}

private fun brushFromPercentage(
    percentage: Float,
    leftColor: Color,
    rightColor: Color,
): Brush {
    return Brush.horizontalGradient(
        0F to leftColor,
        percentage to leftColor,
        percentage to rightColor,
        1F to rightColor,
    )
}

@Composable
private fun DefaultSlidingButtonThumb(
    isFinished: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(elevation = 4.dp)
            .background(color = containerColor, shape = RoundedCornerShape(10.dp))
    ) {
        AnimatedContent(isFinished, modifier = Modifier.align(Alignment.Center)) {
            val icon = if (it) {
                Icons.Rounded.CheckCircle
            } else {
                Icons.Rounded.KeyboardDoubleArrowRight
            }

            Icon(icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .align(Alignment.Center),
                tint = contentColor
            )
        }
    }
}