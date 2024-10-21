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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.noisevisionproductions.samplelibrary.composeUI.DropDownMenuWithItems
import org.noisevisionproductions.samplelibrary.composeUI.RowWithSearchBar
import org.noisevisionproductions.samplelibrary.composeUI.ShowDialogAlert
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.CommentViewModel
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.PostViewModel
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.UserViewModel
import org.noisevisionproductions.samplelibrary.database.ForumService
import org.noisevisionproductions.samplelibrary.interfaces.formatTimeAgo
import org.noisevisionproductions.samplelibrary.interfaces.getTagsFromJsonFile
import org.noisevisionproductions.samplelibrary.utils.UiState
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

@Composable
fun ForumFragment() {
    val postViewModel = PostViewModel()
    val userViewModel = UserViewModel()
    val commentViewModel = CommentViewModel()
    val forumService = remember { ForumService() }

    var selectedPost by rememberSaveable { mutableStateOf<PostModel?>(null) }
    var isCreatingPost by rememberSaveable { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val onSearchTextChanged: (String) -> Unit = { query ->
    }

    val tags = getTagsFromJsonFile()

    val filters: @Composable () -> Unit = {
        DropDownMenuWithItems(
            label = "Kategorie",
            options = listOf("Muzyka", "Opinie"),
            onItemSelected = { category ->
                postViewModel.setSelectedCategory(category)
            }
        )
        DropDownMenuWithItems(
            label = "Sortowanie",
            options = listOf("Najnowsze", "Najstarsze"),
            onItemSelected = { sortingOption ->
                postViewModel.setSelectedSortingOption(sortingOption)
            }
        )
    }

    fun togglePostCreation() {
        if (isCreatingPost) {
            showDialog = true
        } else {
            isCreatingPost = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colors.backgroundGrayColor)
    ) {
        RowWithSearchBar(
            "Wyszukaj wątki",
            onSearchTextChanged = onSearchTextChanged,
            onChangeContent = {
                togglePostCreation()
            },
            filters = filters,
            tags = tags
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(colors.backgroundWhiteColor)
        ) {
            when {
                isCreatingPost -> {
                    CreateNewPost(
                        onPostCreated = { isCreatingPost = false },
                        forumService = forumService
                    )
                }

                selectedPost != null -> {
                    PostDetailView(
                        postId = selectedPost!!.postId,
                        onBack = { selectedPost = null },
                        postViewModel = postViewModel,
                        userViewModel = userViewModel,
                        commentViewModel = commentViewModel
                    )
                }

                else -> {
                    MainContentWithForum(
                        onPostClick = { post ->
                            selectedPost = post
                        },
                        postViewModel = postViewModel,
                        userViewModel = userViewModel,
                        commentViewModel = commentViewModel
                    )
                }
            }
        }
    }
    if (showDialog) {
        ShowDialogAlert(
            onConfirm = {
                isCreatingPost = false
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
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
    )
    {
        Text(
            text = "Najnowsza aktywność na forum",
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp),
            color = colors.textColorMain,
            fontSize = 16.sp,
            textAlign = TextAlign.Left
        )
        Text(
            text = "Znajdziesz tutaj aktywne posty, które zawierają ostatnią aktywność",
            style = MaterialTheme.typography.body1,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = colors.hintTextColorDark,
            textAlign = TextAlign.Left,
            lineHeight = 16.sp
        )
        Divider(
            color = colors.textColorMain,
            thickness = 2.dp,
            modifier = Modifier.padding(horizontal = 10.dp)
                .padding(top = 10.dp)
        )

        PostListView(
            postViewModel = postViewModel,
            onPostClick = onPostClick,
            userViewModel = userViewModel,
            commentViewModel = commentViewModel
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
    commentViewModel: CommentViewModel,
    onPostClick: (PostModel) -> Unit,
    userViewModel: UserViewModel
) {
    val uiState by postViewModel.uiState.collectAsState()
    val isLoadingMore by postViewModel.isLoadingMore.collectAsState()

    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${(uiState as UiState.Error).message}", color = Color.Red)
            }
        }

        is UiState.Success -> {
            val postsWithCategories = (uiState as UiState.Success).posts

            if (postsWithCategories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Brak dostępnych postów")
                }
            } else {
                LazyColumn {
                    itemsIndexed(postsWithCategories) { index, postsWithCategory ->

                        LaunchedEffect(postsWithCategory.post.postId) {
                            commentViewModel.loadComments(postsWithCategory.post.postId)
                        }

                        val commentState by commentViewModel.getCommentsStateForPost(
                            postsWithCategory.post.postId
                        ).collectAsState()

                        val lastComment =
                            commentViewModel.getLastCommentForPost(postsWithCategory.post.postId)

                        Column {
                            PostModelItem(
                                post = postsWithCategory.post,
                                categoryName = postsWithCategory.categoryName,
                                onClick = { onPostClick(postsWithCategory.post) },
                                userViewModel = userViewModel,
                                commentCount = commentState.totalCount,
                                isLoadingComments = commentState.isLoading,
                                lastComment = lastComment
                            )
                            Divider(
                                color = colors.hintTextColorLight,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                        }

                        if (index == postsWithCategories.size - 1 && !isLoadingMore) {
                            postViewModel.onScrollToEnd()
                        }
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostModelItem(
    post: PostModel,
    categoryName: String,
    onClick: () -> Unit,
    userViewModel: UserViewModel,
    lastComment: CommentModel?,
    commentCount: Int,
    isLoadingComments: Boolean
) {
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()
    val textSize = 14.sp

    val date = post.timestamp.substringBefore(" ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .size(100.dp)
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
                style = MaterialTheme.typography.h6,
                fontSize = 15.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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
                    Row {
                        Text(
                            text = "Zapostował: ",
                            color = colors.hintTextColorMedium,
                            fontSize = textSize
                        )
                        Text(
                            text = post.username,
                            color = colors.hintTextColorLight,
                            fontSize = textSize
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                Row {
                    Text(
                        text = date,
                        color = colors.hintTextColorMedium,
                        fontSize = textSize
                    )
                }
                Row {
                    Text(
                        text = "Odpowiedzi: ",
                        color = colors.hintTextColorMedium,
                        fontSize = textSize
                    )

                    if (isLoadingComments) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colors.hintTextColorLight
                        )
                    } else {
                        Text(
                            text = commentCount.toString(),
                            color = colors.hintTextColorLight,
                            fontSize = textSize
                        )
                    }
                    Text(
                        text = " | W: ",
                        color = colors.hintTextColorMedium,
                        fontSize = textSize
                    )
                    Text(
                        text = categoryName,
                        color = colors.hintTextColorLight,
                        fontSize = textSize
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Najnowszy komentarz:",
                    style = MaterialTheme.typography.caption,
                    color = colors.hintTextColorMedium,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = textSize
                )
                Text(
                    text = lastComment?.timestamp?.let { formatTimeAgo(it) } ?: "-",
                    style = MaterialTheme.typography.caption,
                    color = colors.hintTextColorLight,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = textSize
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.Start,
            ) {

                Text(
                    text = "Od:",
                    style = MaterialTheme.typography.caption,
                    color = colors.hintTextColorMedium,
                    fontSize = textSize
                )
                Text(
                    text = lastComment?.username ?: "-",
                    style = MaterialTheme.typography.caption,
                    color = colors.hintTextColorLight,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = textSize
                )
            }
        }
    }
}
