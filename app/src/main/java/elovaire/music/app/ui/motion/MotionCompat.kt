package elovaire.music.droidbeauty.app.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.TransformOrigin

object A0AlbumMotion {
    fun forwardEnter(transformOrigin: TransformOrigin): EnterTransition =
        A0Motion.albumDetailForwardEnter(transformOrigin)

    fun forwardExit(): ExitTransition = A0Motion.albumDetailForwardExit()

    fun backEnter(): EnterTransition = A0Motion.albumDetailBackEnter()

    fun backExit(transformOrigin: TransformOrigin): ExitTransition =
        A0Motion.albumDetailBackExit(transformOrigin)
}

@Composable
fun rememberSystemAnimationScale(): Float = LocalMotionRuntime.current.durationScale

@Composable
fun SyncA0MotionScale() {
    val scale = rememberSystemAnimationScale()
    SideEffect {
        A0Motion.updateSystemDurationScale(scale)
    }
}
