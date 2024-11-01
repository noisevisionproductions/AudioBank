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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.CustomTopAppBar
import org.noisevisionproductions.samplelibrary.composeUI.DropDownMenuWithItems
import org.noisevisionproductions.samplelibrary.composeUI.RowWithSearchBar
import org.noisevisionproductions.samplelibrary.composeUI.CustomAlertDialog
import org.noisevisionproductions.samplelibrary.composeUI.CustomColors
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postCreating.CreateNewPost
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postCreating.CreatePostViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow.PostDetailView
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow.PostViewModel
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.database.PostsRepository
import org.noisevisionproductions.samplelibrary.interfaces.formatTimeAgo
import org.noisevisionproductions.samplelibrary.interfaces.poppinsFontFamily
import org.noisevisionproductions.samplelibrary.utils.UiState
import org.noisevisionproductions.samplelibrary.utils.fragmentNavigation.NavigationViewModel
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

@Composable
fun ForumNavigationHost(
    postViewModel: PostViewModel,
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    authService: AuthService,
    forumRepository: ForumRepository,
    likeManager: LikeManager,
    navigationViewModel: NavigationViewModel,
    postsRepository: PostsRepository
) {
    var currentScreen by remember { mutableStateOf<ForumScreenNavigation>(ForumScreenNavigation.PostList) }

    val navigationEvent by navigationViewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { postId ->
            currentScreen = ForumScreenNavigation.PostDetail(postId)
            navigationViewModel.navigationHandled()
        }
    }

    when (currentScreen) {
        is ForumScreenNavigation.PostList -> {
            ForumContent(
                postViewModel = postViewModel,
                userViewModel = userViewModel,
                commentViewModel = commentViewModel,
                onNavigateToCreate = { currentScreen = ForumScreenNavigation.CreatePost },
                onNavigateToDetail = { post ->
                    currentScreen = ForumScreenNavigation.PostDetail(post.postId)
                }
            )
        }

        is ForumScreenNavigation.CreatePost -> {
            CreatePostScreen(
                createPostViewModel = CreatePostViewModel(
                    forumRepository = forumRepository,
                    authService = authService,
                    postsRepository = postsRepository
                ),
                userViewModel = userViewModel,
                onNavigateBack = { currentScreen = ForumScreenNavigation.PostList }
            )
        }

        is ForumScreenNavigation.PostDetail -> {
            val postId = (currentScreen as ForumScreenNavigation.PostDetail).postId
            PostDetailView(
                postId = postId,
                postViewModel = postViewModel,
                userViewModel = userViewModel,
                commentViewModel = commentViewModel,
                onBack = { currentScreen = ForumScreenNavigation.PostList },
                likeManager = likeManager
            )
        }
    }
}

@Composable
fun ForumContent(
    postViewModel: PostViewModel,
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (PostModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundGrayColor)
    ) {
        RowWithSearchBar(
            placeholderText = "Wyszukaj wątki",
            onSearchTextChanged = { query ->
                postViewModel.filterPosts(query)
            },
            onChangeContent = onNavigateToCreate,
            filters = {
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
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(colors.backgroundWhiteColor)
        ) {
            MainContentWithForum(
                onPostClick = onNavigateToDetail,
                postViewModel = postViewModel,
                userViewModel = userViewModel,
                commentViewModel = commentViewModel
            )
        }
    }
}

@Composable
fun CreatePostScreen(
    createPostViewModel: CreatePostViewModel,
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundWhiteColor)
    ) {
        CustomTopAppBar(
            title = "Nowy post",
            onNavigateBack = onNavigateBack
        )

        CreateNewPost(
            onPostCreated = onNavigateBack,
            viewModel = createPostViewModel,
            userViewModel = userViewModel
        )
    }

    if (showDialog) {
        CustomAlertDialog(
            onConfirm = {
                showDialog = false
                onNavigateBack()
            },
            onDismiss = { showDialog = false },
            contentQuestion = "Czy na pewno chcesz anulować wprowadzanie nowego postu?",
            title = "Anulowanie"
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
            fontSize = 14.sp,
            textAlign = TextAlign.Left
        )
        Text(
            text = "Znajdziesz tutaj aktywne posty, które zawierają ostatnią aktywność",
            style = MaterialTheme.typography.h5,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
    val filteredPosts by postViewModel.filteredPosts.collectAsState()
    val categoryNames by postViewModel.categoryNames.collectAsState()

    // Handle loading state
    when (uiState) {
        is UiState.Loading -> {
            LoadingIndicator()
        }

        is UiState.Error -> {
            ErrorMessage(message = (uiState as UiState.Error).message)
        }

        is UiState.Success -> {
            if (filteredPosts.isEmpty()) {
                EmptyPostsMessage()
            } else {
                PostsList(
                    filteredPosts = filteredPosts,
                    categoryNames = categoryNames,
                    isLoadingMore = isLoadingMore,
                    commentViewModel = commentViewModel,
                    userViewModel = userViewModel,
                    onPostClick = onPostClick,
                    onLoadMore = { postViewModel.onScrollToEnd() }
                )
            }
        }
    }
}

@Composable
private fun PostsList(
    filteredPosts: List<PostModel>,
    categoryNames: Map<String, String>,
    isLoadingMore: Boolean,
    commentViewModel: CommentViewModel,
    userViewModel: UserViewModel,
    onPostClick: (PostModel) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        itemsIndexed(filteredPosts.distinct()) { index, post ->
            PostItem(
                post = post,
                categoryNames = categoryNames,
                commentViewModel = commentViewModel,
                userViewModel = userViewModel,
                onPostClick = onPostClick
            )

            if (index == filteredPosts.size - 1 && !isLoadingMore) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }

        if (isLoadingMore) {
            item {
                LoadMoreIndicator()
            }
        }
    }
}

@Composable
private fun PostItem(
    post: PostModel,
    categoryNames: Map<String, String>,
    commentViewModel: CommentViewModel,
    userViewModel: UserViewModel,
    onPostClick: (PostModel) -> Unit
) {
    LaunchedEffect(post.postId) {
        commentViewModel.loadComments(post.postId)
    }

    val commentState by commentViewModel.getCommentsStateForPost(post.postId).collectAsState()
    val lastComment = commentViewModel.getLastCommentForPost(post.postId)

    Column {
        PostModelItem(
            post = post,
            categoryName = categoryNames[post.categoryId] ?: "Unknown Category",
            onClick = { onPostClick(post) },
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
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Error: $message", color = Color.Red)
    }
}

@Composable
private fun EmptyPostsMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Brak postów do wyświetlenia")
    }
}

@Composable
private fun LoadMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
                style = MaterialTheme.typography.body1,
                fontSize = 11.sp,
                lineHeight = 11.sp,
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
                            style = TextStyle(
                                fontFamily = poppinsFontFamily(),
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = CustomColors.black60,
                        )
                        Text(
                            text = post.username,
                            style = TextStyle(
                                fontFamily = poppinsFontFamily(),
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = CustomColors.primary60,
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
                        style = TextStyle(
                            fontFamily = poppinsFontFamily(),
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = CustomColors.black60,
                    )
                }
                Row {
                    Text(
                        text = "Odpowiedzi: ",
                        style = TextStyle(
                            fontFamily = poppinsFontFamily(),
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = CustomColors.black60,
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
                            style = TextStyle(
                                fontFamily = poppinsFontFamily(),
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = CustomColors.primary60,
                        )
                    }
                    Text(
                        text = " | W: ",
                        style = TextStyle(
                            fontFamily = poppinsFontFamily(),
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = CustomColors.black60,
                    )
                    Text(
                        text = categoryName,
                        style = TextStyle(
                            fontFamily = poppinsFontFamily(),
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = CustomColors.primary60,
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
                    style = TextStyle(
                        fontFamily = poppinsFontFamily(),
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    color = CustomColors.black60,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = textSize
                )
                Text(
                    text = lastComment?.timestamp?.let { formatTimeAgo(it) } ?: "-",
                    style = TextStyle(
                        fontFamily = poppinsFontFamily(),
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    color = CustomColors.primary60,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.Start,
            ) {

                Text(
                    text = "Od:",
                    style = TextStyle(
                        fontFamily = poppinsFontFamily(),
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    color = CustomColors.black60,
                )
                Text(
                    text = lastComment?.username ?: "-",
                    style = TextStyle(
                        fontFamily = poppinsFontFamily(),
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    color = CustomColors.primary60,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
