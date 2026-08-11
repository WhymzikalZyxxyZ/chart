package xyz.zyxwonderland.chart.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF00695C) // teal-800, clinical/medical association
private val OnPrimary = Color(0xFFFFFFFF)
private val Secondary = Color(0xFF5C6BC0) // indigo-400
private val Background = Color(0xFF0D1412)
private val Surface = Color(0xFF17211E)
private val OnSurface = Color(0xFFE2E8F0)

private val DarkColors =
    darkColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        secondary = Secondary,
        background = Background,
        surface = Surface,
        onSurface = OnSurface,
        onBackground = OnSurface,
    )

private val LightColors =
    lightColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        secondary = Secondary,
    )

@Composable
fun ChartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
