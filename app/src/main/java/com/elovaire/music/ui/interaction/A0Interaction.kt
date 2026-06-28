package elovaire.music.droidbeauty.app.ui.interaction

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import elovaire.music.droidbeauty.app.ui.motion.MotionScale
import elovaire.music.droidbeauty.app.ui.motion.a0PressScaleMotion
import elovaire.music.droidbeauty.app.ui.motion.rememberMotionSpecs

@Immutable
data class A0InteractionSpecs(
    val chromePressScale: Float = MotionScale.ChromePressed,
    val mediaPressScale: Float = MotionScale.MediaPressed,
)

object A0Interaction {
    val specs = A0InteractionSpecs()
}

@Composable
fun rememberA0InteractionSource(): MutableInteractionSource {
    return remember { MutableInteractionSource() }
}

fun Modifier.a0PressScale(
    enabled: Boolean = true,
    pressedScale: Float = A0Interaction.specs.chromePressScale,
    animationSpec: FiniteAnimationSpec<Float>? = null,
    interactionSource: MutableInteractionSource? = null,
    label: String = "a0PressScale",
): Modifier = composed {
    if (!enabled) return@composed this
    val motionSpecs = rememberMotionSpecs()
    val resolvedInteractionSource = interactionSource ?: rememberA0InteractionSource()
    val pressed by resolvedInteractionSource.collectIsPressedAsState()
    a0PressScaleMotion(
        pressed = pressed,
        pressedScale = pressedScale,
        pressSpec = motionSpecs.pressDown(),
        releaseSpec = animationSpec ?: motionSpecs.chromeRelease(),
        label = label,
    )
}

fun Modifier.consumePointersWithoutSemantics(): Modifier {
    return clearAndSetSemantics {}
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Final)
                    event.changes.forEach { change ->
                        if (!change.isConsumed) {
                            change.consume()
                        }
                    }
                }
            }
        }
}
