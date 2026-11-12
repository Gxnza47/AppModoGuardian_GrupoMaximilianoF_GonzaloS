package com.example.modoguardian_grupomg.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    secondary = ColorExito,
    tertiary = ColorAviso,
    background = AzulFondo,
    onBackground = TextoPrincipal,
    surface = AzulSuperficie,
    onSurface = TextoPrincipal,
    surfaceVariant = AzulSuperficieAlta,
    onSurfaceVariant = TextoSecundario,
    surfaceContainer = AzulSuperficie,
    surfaceContainerHigh = AzulSuperficieAlta,
    outline = AzulBorde,
    outlineVariant = AzulBorde,
    error = ColorCritico,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    secondary = ColorExito,
    tertiary = ColorAviso,
    background = ClaroFondo,
    onBackground = ClaroTexto,
    surface = ClaroSuperficie,
    onSurface = ClaroTexto,
    surfaceVariant = ClaroSuperficieAlta,
    onSurfaceVariant = ClaroTextoSecundario,
    surfaceContainer = ClaroSuperficie,
    surfaceContainerHigh = ClaroSuperficieAlta,
    outline = ClaroBorde,
    outlineVariant = ClaroBorde,
    error = ColorCritico,
    onError = Color.White
)

// El diseño de referencia es oscuro; el interruptor «Modo oscuro» del Perfil permite cambiarlo.
// Se desactiva el color dinámico para que la paleta Insercode se mantenga igual en todos los equipos.
@Composable
fun ModoGuardian_GrupoMGTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
