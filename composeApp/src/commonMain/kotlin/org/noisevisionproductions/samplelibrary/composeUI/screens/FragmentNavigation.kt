package org.noisevisionproductions.samplelibrary.composeUI.screens

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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.FiltersAndTagsWindow
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.ForumFragment
import org.noisevisionproductions.samplelibrary.interfaces.getTagsFromJsonFile
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_create
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
                FragmentsTabs.Tab1 -> /*DynamicListWithSamples("samples")*/ ForumFragment()
                FragmentsTabs.Tab2 -> DynamicListWithSamples("acapella")
                FragmentsTabs.Tab3 -> ForumFragment()
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
    onSearchTextChanged: (String) -> Unit,
    onChangeContent: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Row( // pasek z wyszukiwaniem dzwięków
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_create),
            contentDescription = "Create new",
            modifier = Modifier
                .background(colors.backgroundGrayColor)
                .size(50.dp)
                .clickable(
                    onClick = { onChangeContent() }, // Funkcja odpowiedzialna za zmianę kontentu
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                )
        )

        // Pole wyszukiwania
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(25.dp))
        ) {
            TextField(
                value = searchText,
                onValueChange = {
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

        // Ikona do rozwijania filtrów
        Image(
            painterResource(Res.drawable.icon_filters),
            contentDescription = "Filters",
            colorFilter = ColorFilter.tint(colors.textColorMain),
            modifier = Modifier
                .background(colors.backgroundGrayColor)
                .size(50.dp)
                .clickable(
                    onClick = {
                        isExpanded = !isExpanded
                    }, // Zmienna odpowiedzialna za rozwijanie filtrów
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }

    // Animacja rozwijania filtrów
    val expandedHeight by animateDpAsState(targetValue = if (isExpanded) 140.dp else 0.dp)
    val tags = getTagsFromJsonFile()

    // Okno z filtrami oraz tagami
    FiltersAndTagsWindow(isExpanded, expandedHeight, tags = tags)
}