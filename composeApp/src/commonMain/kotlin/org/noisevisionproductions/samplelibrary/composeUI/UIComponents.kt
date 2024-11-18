package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.components.topShadow
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.interfaces.poppinsFontFamily
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
    tags: List<String>? = null,
    selectedTags: Set<String> = emptySet(),
    onTagSelected: (String) -> Unit = {},
    onResetTags: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Create new button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.primaryBackgroundColorLight, CircleShape)
                    .clickable(onClick = onChangeContent),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_create),
                    contentDescription = "Create new",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(colors.textColorMain)
                )
            }

            // Search field
            BasicTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    onSearchTextChanged(it)
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = colors.textColorMain
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(colors.backgroundWhiteColor, RoundedCornerShape(24.dp)),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = colors.hintTextColorMedium,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchText.isEmpty()) {
                                Text(
                                    text = placeholderText,
                                    color = colors.hintTextColorLight,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )

            // Filter button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isExpanded) colors.primaryBackgroundColor else colors.primaryBackgroundColorLight,
                        CircleShape
                    )
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(Res.drawable.icon_filters),
                    contentDescription = "Filters",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(
                        if (isExpanded) colors.backgroundWhiteColor else colors.textColorMain
                    )
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters()
                }

                if (!tags.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onResetTags,
                            modifier = Modifier
                                .size(32.dp)
                                .background(colors.backgroundWhiteColor, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear tags",
                                tint = colors.textColorMain,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        LazyRow(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tags) { tag ->
                                TagItem(
                                    tag = tag,
                                    isSelected = selectedTags.contains(tag),
                                    onTagClick = onTagSelected
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagItem(
    tag: String,
    isSelected: Boolean,
    onTagClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) colors.primaryBackgroundColor
                else colors.backgroundWhiteColor
            )
            .clickable { onTagClick(tag) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = tag,
            fontSize = 14.sp,
            color = if (isSelected) colors.backgroundWhiteColor
            else colors.textColorMain
        )
    }
}

@Composable
fun DropDownMenuWithItems(
    defaultLabel: String,
    options: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(defaultLabel) }

    Box {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selectedItem == defaultLabel)
                        colors.backgroundWhiteColor
                    else
                        colors.primaryBackgroundColor
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = selectedItem,
                    fontSize = 14.sp,
                    color = if (selectedItem == defaultLabel)
                        colors.textColorMain
                    else
                        colors.backgroundWhiteColor
                )
                Icon(
                    imageVector = if (expanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = if (selectedItem == defaultLabel)
                        colors.textColorMain
                    else
                        colors.backgroundWhiteColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.backgroundWhiteColor)
        ) {
            DropdownMenuItem(
                onClick = {
                    selectedItem = defaultLabel
                    expanded = false
                    onItemSelected("")
                }
            ) {
                Text(
                    text = "Wyczyść filtry",
                    fontSize = 14.sp,
                    color = colors.textColorMain
                )
            }

            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        selectedItem = option
                        expanded = false
                        onItemSelected(option)
                    }
                ) {
                    Text(
                        text = option,
                        fontSize = 14.sp,
                        color = colors.textColorMain
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
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.toInt(), 0) }
                    .width(with(LocalDensity.current) { textWidth.toDp() })
            )
        }
    }
}

@Composable
fun CustomAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    contentQuestion: String
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title) },
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.backgroundWhiteColor, RoundedCornerShape(12.dp))
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEnabled,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = colors.textColorMain
            ),
            cursorBrush = SolidColor(colors.primaryBackgroundColor),
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction,
                keyboardType = keyboardType
            ),
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = label,
                            color = colors.hintTextColorMedium,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun CustomTopAppBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .topShadow(),
        color = colors.backgroundWhiteColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavigateBack != null) {
                IconButton(
                    onClick = { onBackPressed?.invoke() ?: onNavigateBack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = colors.primaryBackgroundColorLight,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textColorMain,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = poppinsFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = colors.textColorMain
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

@Composable
expect fun PropertiesMenu(
    fileUrl: String,
    fileName: String,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit,
    alignRight: Boolean,
    modifier: Modifier = Modifier
)

@Composable
expect fun PlatformAvatarImage(avatarPath: String)