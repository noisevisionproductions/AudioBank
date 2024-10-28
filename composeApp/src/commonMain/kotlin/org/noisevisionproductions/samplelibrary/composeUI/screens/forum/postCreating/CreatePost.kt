package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postCreating

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.noisevisionproductions.samplelibrary.auth.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.CreateErrorMessage
import org.noisevisionproductions.samplelibrary.composeUI.CustomOutlinedTextField
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.ShowAgreementDialog
import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel


@Composable
fun CreateNewPost(
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel,
    userViewModel: UserViewModel
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val isAnonymous by viewModel.isAnonymous.collectAsState()
    val isAgreementAccepted by viewModel.isAgreementAccepted.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isTitleError by viewModel.isTitleError.collectAsState()
    val isContentError by viewModel.isContentError.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()

    var showAgreementDialog by remember { mutableStateOf(false) }
    val username by userViewModel.username.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
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

            CategoryDropdownMenu(
                categories = categories,
                onCategorySelected = { category ->
                    viewModel.updateSelectedCategory(category)
                })
        }
        CustomOutlinedTextField(
            value = title,
            onValueChange = { viewModel.updateTitle(it) },
            label = "Tytuł",
            isError = isTitleError,
            imeAction = ImeAction.Next,

            )

        if (isTitleError) {
            CreateErrorMessage("Tytuł nie może być pusty")
        }

        CustomOutlinedTextField(
            value = content,
            onValueChange = { viewModel.updateContent(it) },
            label = "Treść postu",
            isError = isContentError,
            imeAction = ImeAction.Done,
            modifier = Modifier
                .weight(0.5f)
                .padding(top = 16.dp)
        )

        if (isContentError) {
            CreateErrorMessage("Treść posta nie może być pusta")
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
                    checked = isAnonymous,
                    onCheckedChange = { viewModel.updateIsAnonymous(it) }
                )
                Text(
                    text = "Udostępnij post anonimowo",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Drugi rząd - Checkbox i tekst "Akceptuję regulamin forum"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAgreementAccepted,
                    onCheckedChange = { viewModel.updateAgreementAccepted(it) }
                )
                Text(
                    text = "Akceptuję regulamin forum",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable {
                            showAgreementDialog = true
                        },
                    style = MaterialTheme.typography.subtitle1
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
                viewModel = viewModel,
                isFormValid = isFormValid,
                username = username,
                onPostCreated = onPostCreated
            )
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
    viewModel: CreatePostViewModel,
    isFormValid: Boolean,
    username: String?,
    onPostCreated: () -> Unit,
) {
    Button(
        onClick = {
            viewModel.createPost(username) {
                onPostCreated()
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