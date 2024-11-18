package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.noisevisionproductions.samplelibrary.composeUI.CustomColors.black100
import org.noisevisionproductions.samplelibrary.composeUI.CustomColors.black50
import org.noisevisionproductions.samplelibrary.composeUI.CustomColors.black60
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

object CustomColors {
    val black100 = Color(0xFF000000)
    val black90 = Color(0xE6000000)
    val black80 = Color(0xCC000000)
    val black70 = Color(0xB3000000)
    val black60 = Color(0x99000000)
    val black50 = Color(0x80000000)
    val black40 = Color(0x66000000)
    val black30 = Color(0x4D000000)
    val black20 = Color(0x33000000)
    val black10 = Color(0x1A000000)
    val black0 = Color(0x00000000)
    val primary60 = Color(0x991F3F3F)
    val primary100 = Color(0xFF1F3F3F)
}

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
            color = black60
        ),
        h6 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Bold,
            color = black100
        ),
        subtitle1 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Light,
            fontSize = 12.sp
        ),
        body1 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Normal,
            color = black100
        ),
        body2 = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontWeight = FontWeight.Normal,
            color = black50
        )
    )

    MaterialTheme(
        typography = typography,
        content = content
    )
}
