package com.example.rinconsur.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.TipografiaRinconSur

val RinconSurColorScheme = lightColorScheme(
    primary = Turquesa700,
    onPrimary = Superficie,
    primaryContainer = Turquesa100,
    onPrimaryContainer = Petroleo900,

    secondary = Ambar600,
    onSecondary = Superficie,
    secondaryContainer = Ambar100,
    onSecondaryContainer = Petroleo900,

    background = FondoAplicacion,
    onBackground = TextoPrincipal,

    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = Turquesa100,
    onSurfaceVariant = TextoSecundario,

    outline = Borde,

    error = Error,
    onError = Superficie,
    errorContainer = ErrorSuave,
    onErrorContainer = Error
)


val RinconSurDarkColorScheme = darkColorScheme(

    primary = Turquesa700Oscuro,
    onPrimary = TextoPrincipalOscuro,

    primaryContainer = Turquesa100Oscuro,
    onPrimaryContainer = TextoPrincipalOscuro,

    inversePrimary = Turquesa600Oscuro,

    secondary = Ambar600Oscuro,
    onSecondary = TextoPrincipalOscuro,

    secondaryContainer = Ambar100Oscuro,
    onSecondaryContainer = AdvertenciaOscura,

    tertiary = ExitoOscuro,
    onTertiary = Petroleo950Oscuro,

    tertiaryContainer = ExitoSuaveOscuro,
    onTertiaryContainer = ExitoOscuro,

    background = FondoAplicacionOscuro,
    onBackground = TextoPrincipalOscuro,

    surface = SuperficieOscura,
    onSurface = TextoPrincipalOscuro,
    surfaceVariant = Petroleo800Oscuro,
    onSurfaceVariant = TextoSecundarioOscuro,
    surfaceTint = Turquesa700Oscuro,


    inverseSurface = TextoPrincipalOscuro,
    inverseOnSurface = Petroleo900Oscuro,

    outline = BordeOscuro,
    outlineVariant = BordeSuaveOscuro,

    error = ErrorOscuro,
    onError = Petroleo950Oscuro,

    errorContainer = ErrorSuaveOscuro,
    onErrorContainer = ErrorOscuro,
    scrim = SombraOscura

)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)
@Composable
fun RinconSurTheme(

    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
    ) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        typography = TipografiaRinconSur,
        content = content,
        )
}