package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

val colors = Colors()

@Composable
@Preview
fun App() {
    BarWithFragmentsList()
    BottomSheetConfiguration()
}

@Composable
fun BarWithFragmentsList() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column {
            Spacer(// miejsce nad paskiem
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .background(color = colors.primaryBackgroundColor)
            )
            var currentScreen by remember { mutableStateOf(FragmentsTabs.Tab1) }
            val tabTitles = listOf("Dźwięki & pętle", "acapella", "Forum", "Czat", "Pomoc")

            // pasek wyboru fragmentów
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(elevation = 10.dp, shape = RectangleShape, clip = false)
                    .background(colors.barColor),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in tabTitles.indices) {
                    TabItem(
                        text = tabTitles[i],
                        isSelected = when (i) {
                            0 -> currentScreen == FragmentsTabs.Tab1
                            1 -> currentScreen == FragmentsTabs.Tab2
                            2 -> currentScreen == FragmentsTabs.Tab3
                            3 -> currentScreen == FragmentsTabs.Tab4
                            4 -> currentScreen == FragmentsTabs.Tab5
                            else -> false
                        },
                        onClick = {
                            currentScreen = when (i) {
                                0 -> FragmentsTabs.Tab1
                                1 -> FragmentsTabs.Tab2
                                2 -> FragmentsTabs.Tab3
                                3 -> FragmentsTabs.Tab4
                                4 -> FragmentsTabs.Tab5
                                else -> FragmentsTabs.Tab1
                            }
                        },
                        isFirst = i == 0,
                        isLast = i == tabTitles.size - 1
                    )
                }
            }
            when (currentScreen) {
                FragmentsTabs.Tab1 -> DynamicListWithSamples()
                FragmentsTabs.Tab2 -> DynamicListWithSamples()
                FragmentsTabs.Tab3 -> DynamicListWithSamples()
                FragmentsTabs.Tab4 -> DynamicListWithSamples()
                FragmentsTabs.Tab5 -> DynamicListWithSamples()
            }
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    Box(
        modifier = Modifier
            .padding(PaddingValues(top = 4.dp))
            .fillMaxHeight()
            .padding(
                start = if (isFirst) 8.dp else 0.dp,
                end = if (isLast) 8.dp else 0.dp
            )
            .clip(
                if (isSelected)
                    RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                else RoundedCornerShape(0.dp)
            )
            .background(if (isSelected) colors.backgroundWhiteColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) colors.textColorChosenBar else colors.textColorUnChosenBar,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun RowWithSearchBar(
    labelText: String,
    placeholderText: String,
    onSearchTextChanged: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val expandedHeight by animateDpAsState(targetValue = if (isExpanded) 100.dp else 0.dp)

    Row( // pasek z wyszukiwaniem dzwięków
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = labelText,
            color = Color.Black,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(5.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(25.dp))
                .background(colors.backgroundGrayColor)
        ) {
            // wyszukiwarka
            TextField(
                value = searchText,
                onValueChange = {//TODO: obsługa zmian tekstu
                    searchText = it
                    onSearchTextChanged(it)
                },
                singleLine = true,
                placeholder = {
                    Text(placeholderText)
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search icon")
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colors.backgroundDarkGrayColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.backgroundGrayColor)
                .clickable(
                    onClick = { isExpanded = !isExpanded },
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Text(
                //TODO: zamienic na vector?
                text = "+",
                color = colors.textColorMain,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    // okno z filtrami
    FiltersWindow(isExpanded, expandedHeight)
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun UniversalBottomSheet(
    scaffoldState: BottomSheetScaffoldState,
    title: String,
    content: @Composable () -> Unit,
    onSliderChange: (Float) -> Unit
) {
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                // Nagłówek BottomSheet, tytuł, kolory
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primaryBackgroundColor)
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            color = colors.barColor,
                            thickness = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Text(
                            color = colors.barColor,
                            text = title,  // Dynamiczny tytuł
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(8.dp)
                        )
                        Divider(
                            color = colors.barColor,
                            thickness = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }

                // Dynamiczny kontent przekazywany do bottom sheet
                content()

                // Slider na końcu - nie zmienia się
                Row {
                    MusicPlayerSlider(
                        progress = 0.5f,
                        onValueChange = onSliderChange  // Dynamiczna obsługa slidera
                    )
                }
            }
        },
        sheetPeekHeight = 64.dp
    ) {
        // Zawartość fragmentu poza bottom sheet
        BarWithFragmentsList()
    }
}

@Composable
fun MusicPlayerSlider(
    progress: Float,
    onValueChange: (Float) -> Unit
) {
    Slider(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF89c1c1))
            .padding(horizontal = 30.dp, vertical = 10.dp),
        value = progress,
        onValueChange = { newValue ->
            onValueChange(newValue)
        },
        valueRange = 0f..50f,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color(0xFF0F3F3F),
            inactiveTrackColor = Color(0xFF4C4C4C)
        ),
    )
}

