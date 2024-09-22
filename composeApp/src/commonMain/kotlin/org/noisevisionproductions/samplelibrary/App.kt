package org.noisevisionproductions.samplelibrary

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_heart


@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    var selectedTab by remember { mutableStateOf(0) } // dzieki temu pierwsza zadkładka jest wybrana domyślnie
    val tabTitles = listOf("Dźwięki & pętle", "acapella", "Forum", "Czat", "Pomoc")


    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Expanded)
    )

    // zmienna do otwierania i zamykania bottom sheet
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF89c1c1))
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            color = Color.Gray,
                            thickness = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Text(
                            text = "Tytuł", // todo: pobierac nazwe z wybranego utworu
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(8.dp)
                        )

                        Divider(
                            color = Color.Gray,
                            thickness = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF89c1c1))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "test1",
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = "test2",
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = "test3",
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = "test4",
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = "test5",
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = "test6",
                        modifier = Modifier
                            .weight(1f)
                    )
                }
                Row {
                    MusicPlayerSlider(
                        progress = 0.5f,
                        onValueChange = { newProgress ->
                            // TODO: odtwarzanie i przewijanie utorów
                        }
                    )
                }
            }
        },
        sheetPeekHeight = 64.dp
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF89C1C1))
        ) {
            Column {
                Spacer(modifier = Modifier.height(100.dp)) // miejsce nad paskiem

                // pasek wyboru fragmentów
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(elevation = 10.dp, shape = RectangleShape, clip = false)
                        .background(Color(0xFF1F3F3F)),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in tabTitles.indices) {
                        TabItem(
                            text = tabTitles[i],
                            isSelected = i == selectedTab,
                            onClick = { selectedTab = i },
                            isFirst = i == 0,
                            isLast = i == tabTitles.size - 1
                        )
                    }
                }
                DynamicListWithSamples()
            }
        }
    }
}

@Composable
fun RowWithSearchBar() {
    var isExpanded by remember { mutableStateOf(false) }

    val expandedHeight by animateDpAsState(targetValue = if (isExpanded) 100.dp else 0.dp)

    Row( // pasek z wyszukiwaniem dzwięków
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        /* Text(
             text = "Dźwięki",
             color = Color.Black,
             fontSize = 25.sp,
             fontWeight = FontWeight.Bold,
             modifier = Modifier
                 .padding(5.dp),
         )*/
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFFD2D2D2))
        ) {
            var searchText by remember { mutableStateOf("") }
            // wyszukiwarka
            TextField(
                value = searchText,
                onValueChange = { searchText = it }, //TODO: obsługa zmian tekstu
                singleLine = true,
                placeholder = {
                    Text("Wyszukaj dźwięki")
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search icon")
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color(0xc2c0c0c0),
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
                .background(Color(0xFFD2D2D2))
                .clickable(
                    onClick = { isExpanded = !isExpanded },
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Text(
                //TODO: zamienic na vector?
                text = "+",
                color = Color.Black,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    FiltersWindow(expandedHeight)
}


@Composable
fun FiltersWindow(expandedHeight: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(expandedHeight)
            .background(Color(0xFFD2D2D2))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            DropDownMenuWithItems("Lista 1")
            DropDownMenuWithItems("Lista 1")
            DropDownMenuWithItems("Lista 1")
            DropDownMenuWithItems("Lista 1")
        }
        /*Text(
            text = "Zawartość",
            modifier = Modifier
                .padding(16.dp),
            color = Color.White
        )*/
    }

}

@Composable
fun DropDownMenuWithItems(label: String) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("Wybierz") }

    Column {
        Button(onClick = { expanded = !expanded }) {
            Text(selectedItem)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 1"
                expanded = false
            }) {
                Text("Opcja 1")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 2"
                expanded = false
            }) {
                Text("Opcja 2")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 3"
                expanded = false
            }) {
                Text("Opcja 3")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 4"
                expanded = false
            }) {
                Text("Opcja 4")
            }
        }
    }
}

@Composable
fun DynamicListWithSamples() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFFD2D2D2))
    ) {
        RowWithSearchBar()

        // fragment z kontentem dzwieków
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(Color.White)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 80.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nazwa",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Czas",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Ton",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "BPM",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }

                Divider(
                    color = Color.DarkGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // TODO: to jest przyklad. tutaj beda wyswietlane dzwieki.
                val dataList = listOf(
                    "Dźwięk 1",
                    "Dźwięk 2",
                    "Dźwięk 3",
                    "Dźwięk 4",
                    "Dźwięk 4",
                    "Dźwięk 4",
                    "Dźwięk 4",
                    "Dźwięk 4",
                    "Dźwięk 4",
                    "Dźwięk 4",
                    "Dźwięk 5"
                )
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(dataList.size) { index ->
                        val song = dataList[index]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Stop",
                                modifier = Modifier.size(24.dp)
                            )

                            Text(text = "item")
                            Text(text = "03:45")
                            Text(text = "C")
                            Text(text = "120 BPM")

                            Image(painterResource(Res.drawable.icon_heart), null)
                        }

                        Divider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
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
                start = if (isFirst) 16.dp else 0.dp,
                end = if (isLast) 16.dp else 0.dp
            )
            .clip(
                if (isSelected)
                    RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                else RoundedCornerShape(0.dp)
            )
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}
