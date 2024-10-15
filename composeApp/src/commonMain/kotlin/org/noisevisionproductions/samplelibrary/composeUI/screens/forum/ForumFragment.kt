package org.noisevisionproductions.samplelibrary.composeUI.screens.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.ShowDialogAlert
import org.noisevisionproductions.samplelibrary.composeUI.screens.RowWithSearchBar
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.PostViewModel
import org.noisevisionproductions.samplelibrary.database.ForumService
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

@Composable
fun ForumFragment() {
    val postViewModel = PostViewModel()
    val userViewModel = UserViewModel()
    val forumService = ForumService()
    var selectedPost by remember { mutableStateOf<PostModel?>(null) }
    var isContentReplaced by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val onSearchTextChanged: (String) -> Unit = { query ->
    }

    fun toggleContent() {
        if (isContentReplaced) {
            showDialog = true
        } else {
            isContentReplaced = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colors.backgroundGrayColor)
    ) {
        RowWithSearchBar(
            "Forum",
            "Wyszukaj wątki",
            onSearchTextChanged = onSearchTextChanged,
            onChangeContent = {
                toggleContent()
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(colors.backgroundWhiteColor)
        ) {
            if (isContentReplaced) {
                CreateNewPost(
                    onPostCreated = { isContentReplaced = false },
                    forumService = forumService
                )
            } else {
                if (selectedPost != null) {
                    PostDetailView(
                        post = selectedPost!!, onBack = { selectedPost = null },
                        postViewModel = postViewModel,
                        userViewModel = userViewModel
                    )
                } else {
                    MainContentWithForum(
                        onPostClick = { post ->
                            selectedPost = post
                        },
                        postViewModel = postViewModel,
                        userViewModel = userViewModel
                    )
                }
            }
        }
    }
    if (showDialog) {
        ShowDialogAlert(
            onConfirm = {
                isContentReplaced = false
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            },
            contentQuestion = "Czy na pewno chcesz anulować wprowadzanie nowego postu?"
        )
    }
}

@Composable
fun MainContentWithForum(
    onPostClick: (PostModel) -> Unit,
    postViewModel: PostViewModel,
    userViewModel: UserViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = "Najnowsza aktywność na forum",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            color = colors.textColorMain,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Znajdziesz tutaj aktywne posty, które zawierają ostatnią aktywność",
            style = MaterialTheme.typography.body1,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            color = colors.hintTextColorLight,
            textAlign = TextAlign.Center
        )
        Divider(
            color = colors.textColorMain,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        PostListView(
            postViewModel = postViewModel,
            onPostClick = onPostClick,
            userViewModel = userViewModel
        )
    }
}


@Composable
fun ShowAgreementDialog(onAgreementDialogShown: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onAgreementDialogShown() },
        title = { Text("Regulamin forum") },
        text = {
            Text("Tutaj znajduje się treść regulaminu forum. Możesz dodać tutaj pełny tekst regulaminu.")
        },
        confirmButton = {
            Button(
                onClick = { onAgreementDialogShown() }
            ) {
                Text("Akceptuję")
            }
        }

    )
}

@Composable
fun PostListView(
    postViewModel: PostViewModel,
    onPostClick: (PostModel) -> Unit,
    userViewModel: UserViewModel
) {
    val posts by postViewModel.posts.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()
    val error by postViewModel.error.collectAsState()

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $error", color = Color.Red)
            }
        }

        posts.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No posts available")
            }
        }

        else -> {
            LazyColumn {
                items(posts) { post ->
                    Column {
                        PostModelItem(
                            post = post,
                            onClick = { onPostClick(post) },
                            userViewModel = userViewModel,
                            postViewModel = postViewModel
                        )
                        Divider(
                            color = colors.dividerLightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostModelItem(
    post: PostModel,
    onClick: () -> Unit,
    userViewModel: UserViewModel,
    postViewModel: PostViewModel
) {
    val username by userViewModel.username.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()

    val date = post.timestamp.substringBefore(" ")
    val categoryName = remember { mutableStateOf("") }
    val isCategoryLoading = remember { mutableStateOf(true) }

    LaunchedEffect(post.categoryId) {
        isCategoryLoading.value = true
        categoryName.value = postViewModel.getCategoryName(post.categoryId)
        isCategoryLoading.value = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .size(70.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.weight(1f))

            when {
                isLoading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = colors.backgroundGrayColor
                    )
                }

                error != null -> {
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.caption,
                        color = Color.Red
                    )
                }

                else -> {
                    Text(
                        text = "Autor: ${username ?: "Nieznany"}",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                if (isCategoryLoading.value) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = colors.backgroundGrayColor
                    )
                } else {
                    Text(
                        text = "$date | W: ${categoryName.value}",
                        style = MaterialTheme.typography.caption,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 10.dp, end = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Najnowszy post:",
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = "Brak",
                    style = MaterialTheme.typography.caption,
                    color = colors.hintTextColorLight
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.Start,
            ) {

                Text(
                    text = "Od: ",
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = "User",
                    style = MaterialTheme.typography.caption,
                    color = colors.hintTextColorLight
                )

            }
        }

    }
}
