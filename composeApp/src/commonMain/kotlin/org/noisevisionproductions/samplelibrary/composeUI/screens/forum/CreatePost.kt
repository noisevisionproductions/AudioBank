package org.noisevisionproductions.samplelibrary.composeUI.screens.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.composeUI.ErrorIcon
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.database.ForumService
import org.noisevisionproductions.samplelibrary.interfaces.showPostCreatedMessage
import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel


@Composable
fun CreateNewPost(
    onPostCreated: () -> Unit,
    forumService: ForumService
) {
    val coroutineScope = rememberCoroutineScope()
    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryModel?>(null) }
    var isAnonymousPostChecked by remember { mutableStateOf(false) }
    var isAgreementChecked by remember { mutableStateOf(false) }
    var showAgreementDialog by remember { mutableStateOf(false) }
    var isTitleError by remember { mutableStateOf(false) }
    var isContentError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val viewModel = UserViewModel()
    val username by viewModel.username.collectAsState()

    val isFormValid =
        titleText.isNotEmpty() && contentText.isNotEmpty() && isAgreementChecked && selectedCategory != null

    var categories by remember { mutableStateOf<List<CategoryModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val authService = AuthService()

    LaunchedEffect(Unit) {
        forumService.getCategories { loadedCategories ->
            categories = loadedCategories
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colors.backgroundColorForNewContent)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Stwórz post na forum",
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .padding(10.dp),
                fontSize = 20.sp,
                color = colors.textColorMain
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            ) {
                if (isLoading) {
                    LinearProgressIndicator(
                        color = colors.hintTextColorLight,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CategoryDropdownMenu(
                        categories = categories,
                        onCategorySelected = {
                            selectedCategory = it
                        })
                }
            }
        }

        OutlinedTextField(
            value = titleText,
            onValueChange = {
                titleText = it
                isTitleError = titleText.isEmpty()
            },
            label = { Text("Tytuł") },
            singleLine = true,
            isError = isTitleError,
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = colors.backgroundWhiteColor,
                focusedBorderColor = colors.textColorMain,
                unfocusedBorderColor = colors.textColorMain,
                focusedLabelColor = colors.textColorMain,
                unfocusedLabelColor = colors.textColorMain,
                cursorColor = colors.textColorMain
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(30.dp)
        )

        if (isTitleError) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ErrorIcon()
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tytuł nie może być pusty",
                    color = Color.Red,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .weight(0.05f),
                    fontSize = 16.sp
                )
            }

        }

        OutlinedTextField(
            value = contentText,
            onValueChange = {
                contentText = it
                isContentError = contentText.isEmpty()
            },
            label = { Text("Treść postu") },
            isError = isContentError,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .padding(top = 16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = colors.backgroundWhiteColor,
                focusedBorderColor = colors.textColorMain,
                unfocusedBorderColor = colors.textColorMain,
                focusedLabelColor = colors.textColorMain,
                unfocusedLabelColor = colors.textColorMain,
                cursorColor = colors.textColorMain
            ),
            shape = RoundedCornerShape(30.dp),
            maxLines = 5
        )

        if (isContentError) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ErrorIcon()
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Treść posta nie może być pusta",
                    color = Color.Red,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .weight(0.05f),
                    fontSize = 16.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.End,
        )
        {
            // Pierwszy rząd - Checkbox i tekst "Udostępnij post anonimowo"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // Wyrównanie do prawej
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAnonymousPostChecked,
                    onCheckedChange = { isAnonymousPostChecked = it }
                )
                Text(
                    text = "Udostępnij post anonimowo",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Drugi rząd - Checkbox i tekst "Akceptuję regulamin forum"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // Wyrównanie do prawej
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAgreementChecked,
                    onCheckedChange = { isAgreementChecked = it }
                )
                Text(
                    text = "Akceptuję regulamin forum",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable {
                            showAgreementDialog = true
                        },
                    style = TextStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }

        if (showAgreementDialog) {
            ShowAgreementDialog(onAgreementDialogShown = { showAgreementDialog = false })
        }

        if (errorMessage?.isNotEmpty() == true) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                style = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonForPostCreating(
                titleText = titleText,
                contentText = contentText,
                selectedCategory = selectedCategory,
                isAnonymousPostChecked = isAnonymousPostChecked,
                isFormValid = isFormValid,
                username = username,
                authService = authService,
                forumService = forumService,
                coroutineScope = coroutineScope,
                onPostCreated = onPostCreated,
                onErrorMessage = { error -> errorMessage = error })
        }
    }
}

@Composable
fun CategoryDropdownMenu(
    categories: List<CategoryModel>,
    onCategorySelected: (CategoryModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryModel?>(null) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF98a3f3))
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = selectedCategory?.name ?: "Wybierz kategorię",
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            color = colors.backgroundWhiteColor
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colors.backgroundDarkGrayColor)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    onClick = {
                        selectedCategory = category
                        onCategorySelected(category)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(Color.Transparent)
                ) {
                    Text(text = category.name)
                }
            }
        }
    }
}

@Composable
fun ButtonForPostCreating(
    titleText: String,
    contentText: String,
    selectedCategory: CategoryModel?,
    isAnonymousPostChecked: Boolean,
    isFormValid: Boolean,
    username: String?,
    authService: AuthService,
    forumService: ForumService,
    coroutineScope: CoroutineScope,
    onPostCreated: () -> Unit,
    onErrorMessage: (String) -> Unit
) {
    Button(
        onClick = {
            if (isFormValid && selectedCategory != null) {
                coroutineScope.launch {
                    val userId = authService.getCurrentUserId()

                    if (userId == null) {
                        onErrorMessage("Musisz być zalogowany, aby utworzyć post")
                        return@launch
                    }

                    val postUsername = if (isAnonymousPostChecked) {
                        "Anonim"
                    } else {
                        username ?: run {
                            println("Nie można pobrać nazwy użytkownika")
                            return@launch
                        }
                    }

                    forumService.createPost(
                        title = titleText,
                        content = contentText,
                        username = postUsername,
                        categoryId = selectedCategory.id,
                        userId = userId
                    ) { success ->
                        if (success) {
                            onErrorMessage("")
                            showPostCreatedMessage("Post utworzony")
                            onPostCreated()
                        } else {
                            onErrorMessage("Błąd podczas tworzenia postu")
                        }
                    }
                }
            } else {
                onErrorMessage("Wypełnij wszystkie pola")
            }
        },
        enabled = isFormValid,
        modifier = Modifier
            .size(100.dp, 50.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isFormValid) colors.primaryBackgroundColor else colors.primaryBackgroundColorLight
        ),
        shape = RoundedCornerShape(30.dp)
    ) {
        Text(
            "Wyślij",
            style = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                color = colors.backgroundWhiteColor
            )
        )
    }
}