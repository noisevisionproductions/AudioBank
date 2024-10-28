package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_create
import samplelibrary.composeapp.generated.resources.icon_error
import samplelibrary.composeapp.generated.resources.icon_filters


@Composable
fun RowWithSearchBar(
    placeholderText: String,
    onSearchTextChanged: (String) -> Unit,
    onChangeContent: () -> Unit,
    filters: @Composable () -> Unit,
    tags: List<String>? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Row(
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
                    onClick = { onChangeContent() },
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

    val expandedHeight by animateDpAsState(targetValue = if (isExpanded) 140.dp else 0.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(expandedHeight)
            .padding(8.dp)
    ) {
        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters()
            }

            tags?.let {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                )
                {
                    items(tags) { tag ->
                        TagItem(tag)
                    }
                }
            }
        }
    }
}

@Composable
fun TagItem(tag: String) {
    Box(
        modifier = Modifier
            .background(Color.LightGray, shape = RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Text(text = tag)
    }
}

@Composable
fun DropDownMenuWithItems(label: String, options: List<String>, onItemSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(label) }

    Column {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, colors.barColor, RoundedCornerShape(16.dp))
                .background(Color.Transparent)
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Text(
                text = selectedItem,
                color = colors.barColor,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colors.backgroundDarkGrayColor)
        ) {
            DropdownMenuItem(
                onClick = {
                    selectedItem = label // Reset to default label
                    expanded = false
                    onItemSelected("") // Notify that filters should be cleared
                },
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                Text(
                    text = "Wyczyść filtry",
                    color = colors.textColorMain,
                )
            }
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        selectedItem = option
                        expanded = false
                        onItemSelected(option)
                    },
                    modifier = Modifier
                        .background(Color.Transparent)
                ) {
                    Text(
                        text = option,
                        color = colors.textColorMain,
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollingText(fileName: String, modifier: Modifier = Modifier) {
    var textWidth by remember { mutableStateOf(0f) }
    var containerWidth by remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(fileName, textWidth, containerWidth) {
        if (textWidth > containerWidth) {
            offsetX.snapTo(0f)
            offsetX.animateTo(
                targetValue = containerWidth - textWidth,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (textWidth * 15).toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            offsetX.snapTo(0f)
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
            .padding(end = 5.dp, start = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        containerWidth = constraints.maxWidth.toFloat()

        Box(modifier = Modifier.clipToBounds()) {
            val textMeasurer = rememberTextMeasurer()
            val textLayoutResult = remember(fileName) {
                textMeasurer.measure(
                    text = AnnotatedString(fileName),
                    style = TextStyle(fontSize = 16.sp)
                )
            }
            textWidth = textLayoutResult.size.width.toFloat()

            Text(
                text = fileName,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                fontSize = 16.sp,
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.toInt(), 0) }
                    .width(with(LocalDensity.current) { textWidth.toDp() })
            )
        }
    }
}

@Composable
fun ShowDialogAlert(onConfirm: () -> Unit, onDismiss: () -> Unit, contentQuestion: String) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Anulować zmiany?") },
        text = { Text(text = contentQuestion) },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text(
                    "Tak",
                    style = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = colors.backgroundWhiteColor
                    )
                )
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor)
            ) {
                Text(
                    "Nie",
                    style = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = colors.backgroundWhiteColor
                    )
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CreateErrorMessage(
    errorMessage: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ErrorIcon()
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = errorMessage,
            color = Color.Red,
            modifier = Modifier
                .padding(start = 5.dp)
                .weight(0.05f),
            fontSize = 16.sp,
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun ErrorIcon() {
    Image(
        painter = painterResource(Res.drawable.icon_error),
        contentDescription = "Error",
        colorFilter = ColorFilter.tint(Color.Red),
        modifier = Modifier.size(15.dp)
    )
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    isEnabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.body1
            )
        },
        singleLine = true,
        isError = isError,
        enabled = isEnabled,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = colors.backgroundWhiteColor,
            focusedBorderColor = colors.textColorMain,
            unfocusedBorderColor = colors.textColorMain,
            focusedLabelColor = colors.textColorMain,
            unfocusedLabelColor = colors.textColorMain,
            cursorColor = colors.textColorMain
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(30.dp)
    )
}

@Composable
fun CustomTopAppBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = onNavigateBack?.let {
            {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Powrót do porzedniego menu"
                    )
                }
            }
        },
        actions = actions,
        backgroundColor = colors.primaryBackgroundColor,
        contentColor = colors.textColorMain
    )
}

@Composable
expect fun PropertiesMenu(
    fileUrl: String,
    fileName: String,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit,
    alignRight: Boolean
)

@Composable
expect fun PlatformAvatarImage(avatarPath: String)