package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
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
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.interfaces.getTagsFromJsonFile
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_filters

@Composable
fun BarWithFragmentsList() {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundWhiteColor)
        ) {
            Spacer(// miejsce nad paskiem
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .background(color = colors.primaryBackgroundColor)
            )
            var currentScreen by remember { mutableStateOf(FragmentsTabs.Tab1) }
            val tabTitles = listOf("Dźwięki & pętle", "acapella", "Forum", "Czat", "Pomoc")

            // Pasek wyboru fragmentów
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
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
                FragmentsTabs.Tab1 -> DynamicListWithSamples("samples/rimshots")
                FragmentsTabs.Tab2 -> DynamicListWithSamples("acapella")
                FragmentsTabs.Tab3 -> DynamicListWithSamples("acapella")
                FragmentsTabs.Tab4 -> DynamicListWithSamples("acapella")
                FragmentsTabs.Tab5 -> DynamicListWithSamples("acapella")
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
            /*
                            .background(colors.backgroundGrayColor)
            */
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

        Image(
            painterResource(Res.drawable.icon_filters),
            contentDescription = "Filters",
            colorFilter = ColorFilter.tint(colors.textColorMain),
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.backgroundGrayColor)
                .size(50.dp)
                .clickable(
                    onClick = { isExpanded = !isExpanded },
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }
    val expandedHeight by animateDpAsState(targetValue = if (isExpanded) 140.dp else 0.dp)
    val tags = getTagsFromJsonFile()

    // okno z filtrami oraz tagami
    FiltersAndTagsWindow(isExpanded, expandedHeight, tags = tags)
}