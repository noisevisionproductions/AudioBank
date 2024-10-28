package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.noisevisionproductions.samplelibrary.interfaces.poppinsFontFamily

/**
 *
 * Kiedy używać poszczególnych stylów?
 * Thin i Extra Light – używaj ich do tekstów pomocniczych, takich jak podpisy lub mniejsze etykiety (caption), aby nie przyciągały zbyt dużo uwagi.
 * Light – świetnie nadaje się do podtytułów lub opisów (subtitle1, body2), gdzie chcesz mieć lekki, czytelny tekst.
 * Normal (Regular) – używaj go jako podstawowy styl tekstu dla treści głównej (body1), aby zachować przejrzystość i czytelność.
 * Medium i Semi Bold – odpowiednie dla mniejszych nagłówków lub akcentów, np. nagłówki sekcji (h5, h6), aby je wyróżnić.
 * Bold i Extra Bold – używaj do głównych nagłówków (h3, h4), aby mocno zaznaczyć ważne sekcje.
 * Black – najlepsze do bardzo ważnych nagłówków lub komunikatów (h1), które mają przyciągnąć uwagę.
 *
 */
@Composable
fun MyCustomTheme(content: @Composable () -> Unit) {
    val typography = Typography(
        h1 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Black,
            fontSize = 30.sp
        ),
        h2 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        ),
        h3 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        h4 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        ),
        h5 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        ),
        h6 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        ),
        subtitle1 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Light,
            fontSize = 12.sp
        ),
        body1 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        body2 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Light,
            fontSize = 14.sp
        ),
        caption = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Thin,
            fontSize = 12.sp
        )
    )

    MaterialTheme(
        typography = typography,
        content = content
    )
}
