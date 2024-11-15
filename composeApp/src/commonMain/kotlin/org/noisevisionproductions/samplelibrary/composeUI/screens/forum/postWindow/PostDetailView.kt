package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.auth.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.CustomTopAppBar
import org.noisevisionproductions.samplelibrary.composeUI.components.DefaultAvatar
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.AvatarManager.UserAvatar
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentButton
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentInputField
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentItem
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.interfaces.formatTimeAgo
import org.noisevisionproductions.samplelibrary.utils.dataClasses.CommentState
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

@Composable
fun PostDetailView(
    postId: String,
    postViewModel: PostViewModel,
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    onBack: () -> Unit,
    likeManager: LikeManager
) {
    val postState by postViewModel.getPostById(postId).collectAsState(initial = null)
    val categoryName by postViewModel.categoryNames.collectAsState(initial = emptyMap())
    val username by userViewModel.username.collectAsState()
    val avatarUrls by userViewModel.avatarUrls.collectAsState(initial = emptyMap())
    val commentState by commentViewModel.getCommentsStateForPost(postId).collectAsState()
    val commentText by commentViewModel.commentText.collectAsState()
    val isCommentFieldVisible by commentViewModel.isCommentFieldVisible.collectAsState()
    val isLoadingComments by commentViewModel.isLoadingCommentsForPost(postId)
        .collectAsState(initial = false)

    LaunchedEffect(postState) {
        postState?.let { post ->
            postViewModel.cacheManager.getCachedCategory(post.categoryId)
            commentViewModel.loadComments(post.postId)
            userViewModel.fetchAvatarUrl(post.userId)
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = postState?.title ?: "Szczegóły posta",
                onNavigateBack = onBack
            )
        }
    ) { innerPadding ->
        if (postState == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val post = postState!!
            val creatorAvatarUrl = avatarUrls[post.userId] ?: ""

            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PostHeaderSection(
                        post = post,
                        categoryName = categoryName[post.categoryId] ?: "Unknown Category",
                        username = username,
                        avatarUrl = creatorAvatarUrl
                    )
                }

                item {
                    PostContentSection(
                        post = post,
                        commentViewModel = commentViewModel,
                        postViewModel = postViewModel,
                        commentText = commentText,
                        isCommentFieldVisible = isCommentFieldVisible,
                        likeManager = likeManager
                    )
                }

                if (isLoadingComments) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (commentState.comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Brak komentarzy pod tym postem.",
                                style = MaterialTheme.typography.body1,
                                color = colors.textColorMain
                            )
                        }
                    }
                } else {
                    items(commentState.comments) { comment ->
                        CommentItem(
                            comment = comment,
                            userViewModel = userViewModel,
                            post = post,
                            commentViewModel = commentViewModel,
                            likeManager = likeManager
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostHeaderSection(
    post: PostModel,
    categoryName: String,
    username: String?,
    avatarUrl: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            if (avatarUrl.isNotEmpty()) {
                UserAvatar(
                    avatarUrl = avatarUrl,
                    size = 70.dp,
                    onClick = { }
                )
            } else {
                DefaultAvatar()
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                // Górna część avatar -> CategoryName i Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(30.dp))

                    Text(
                        text = formatTimeAgo(post.timestamp),
                        style = MaterialTheme.typography.caption
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dolna część avatar -> Username
                Text(
                    text = username ?: "username",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

@Composable
fun PostContentSection(
    post: PostModel,
    commentViewModel: CommentViewModel,
    postViewModel: PostViewModel,
    likeManager: LikeManager,
    commentText: String,
    isCommentFieldVisible: Boolean
) {
    val commentState by commentViewModel.getCommentsStateForPost(post.postId)
        .collectAsState(initial = CommentState())

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = post.title,
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = post.content,
            style = MaterialTheme.typography.body1
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PostLikeButton(
                post = post,
                postViewModel = postViewModel,
                likeManager = likeManager
            )

            Spacer(modifier = Modifier.width(8.dp))

            CommentButton(
                commentCount = commentState.totalCount,
                onClick = { commentViewModel.toggleCommentFieldVisibility() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input for adding comments
        if (isCommentFieldVisible) {
            CommentInputField(
                commentText = commentText,
                onCommentTextChanged = { commentViewModel.updateCommentText(it) },
                onSendClick = { commentViewModel.addComment(post.postId) }
            )

        }
    }
}

@Composable
fun PostLikeButton(post: PostModel, postViewModel: PostViewModel, likeManager: LikeManager) {
    val postLikeStates by likeManager.postLikeStates.collectAsState()
    val likeState = postLikeStates[post.postId]

    val isLiked = likeState?.isLiked ?: post.isLiked
    val likesCount = likeState?.likesCount ?: post.likesCount

    Button(
        onClick = { postViewModel.togglePostLike(post.postId) },
        colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor),
        shape = RoundedCornerShape(30.dp)
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isLiked) "Liked" else "Like",
            tint = if (isLiked) Color.Red else colors.backgroundWhiteColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$likesCount",
            color = colors.backgroundWhiteColor
        )
    }
}
