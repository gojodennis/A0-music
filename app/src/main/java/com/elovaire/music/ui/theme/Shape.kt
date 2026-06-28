package elovaire.music.droidbeauty.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
object A0Radii {
    val topBar: Dp = 0.dp
    val dock: Dp = 20.dp
    val module: Dp = 14.dp
    val card: Dp = 14.dp
    val tile: Dp = 14.dp
    val button: Dp = 16.dp
    val artwork: Dp = 8.dp
    val artworkSmall: Dp = 4.dp
    val input: Dp = 16.dp
    val dialog: Dp = 16.dp
    val pill: Dp = 999.dp
}

fun a0Shapes(): Shapes {
    return Shapes(
        extraSmall = RoundedCornerShape(A0Radii.tile),
        small = RoundedCornerShape(A0Radii.tile),
        medium = RoundedCornerShape(A0Radii.card),
        large = RoundedCornerShape(A0Radii.module),
        extraLarge = RoundedCornerShape(A0Radii.dock),
    )
}
