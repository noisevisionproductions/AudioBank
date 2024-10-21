package org.noisevisionproductions.samplelibrary.composeUI.screens.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.composeUI.components.DefaultAvatar
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.CommentViewModel
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.PostViewModel
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.UserViewModel
import org.noisevisionproductions.samplelibrary.interfaces.formatTimeAgo
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_comment

@Composable
fun PostDetailView(
    postId: String,
    postViewModel: PostViewModel,
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    onBack: () -> Unit
) {
    val post by postViewModel.getPostById(postId).collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val authService = AuthService()
    val categoryName = remember { mutableStateOf("") }
    val username by userViewModel.username.collectAsState()
    val isLoading = remember { mutableStateOf(true) }

    val commentState by commentViewModel.getCommentsStateForPost(postId).collectAsState()
    val commentText by commentViewModel.commentText.collectAsState()
    val isCommentFieldVisible = remember { mutableStateOf(false) }
    val replyFieldVisibilityMap = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(post) {
        if (post != null) {
            categoryName.value = postViewModel.getCategoryName(post!!.categoryId)
            commentViewModel.loadComments(post!!.postId)
            isLoading.value = false
        }
    }

    if (isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = { // Przycisk "floating" typu FAB
                FloatingActionButton(
                    onClick = { onBack() }, // Funkcja powrotu
                    backgroundColor = colors.primaryBackgroundColor, // Kolor tła
                    contentColor = colors.backgroundWhiteColor // Kolor treści
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ikona powrotu
                        contentDescription = "Wróć"
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Górna część z avatarem, kategorią, nazwą użytkownika i timestamp
                    PostHeaderSection(post!!, categoryName.value, username)
                }

                item {
                    // Środkowa część z treścią posta i przyciskami
                    PostContentSection(
                        post = post!!,
                        coroutineScope = coroutineScope,
                        commentViewModel = commentViewModel,
                        postViewModel = postViewModel,
                        commentText = commentText,
                        authService = authService,
                        username = username,
                        isCommentFieldVisible = isCommentFieldVisible
                    )
                }

                // Sekcja komentarzy powinna być przewijana
                items(commentState.comments) { comment ->
                    val showReplyField = remember {
                        mutableStateOf(replyFieldVisibilityMap[comment.commentId] ?: false)
                    }
                    CommentItem(
                        comment = comment,
                        userViewModel = userViewModel,
                        post = post!!,
                        commentViewModel = commentViewModel,
                        showReplyField = showReplyField
                    )

                    LaunchedEffect(showReplyField.value) {
                        replyFieldVisibilityMap[comment.commentId] = showReplyField.value
                    }
                }
            }
        }
    }
}

@Composable
fun PostHeaderSection(post: PostModel, categoryName: String, username: String?) {
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

            DefaultAvatar()

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
    coroutineScope: CoroutineScope,
    commentViewModel: CommentViewModel,
    postViewModel: PostViewModel,
    commentText: String,
    authService: AuthService,
    username: String?,
    isCommentFieldVisible: MutableState<Boolean>
) {
    val commentState by commentViewModel.getCommentsStateForPost(post.postId).collectAsState()

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
            Button(
                onClick = {
                    postViewModel.toggleLike(post.postId)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (post.isLiked) "Liked" else "Like",
                    tint = if (post.isLiked) Color.Red else colors.backgroundWhiteColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likesCount}",
                    color = colors.backgroundWhiteColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { isCommentFieldVisible.value = !isCommentFieldVisible.value },
                colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.icon_comment),
                    contentDescription = "Comment",
                    tint = colors.backgroundWhiteColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${commentState.totalCount}",
                    color = colors.backgroundWhiteColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input for adding comments
        if (isCommentFieldVisible.value) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentViewModel.updateCommentText(it) },
                    label = { Text("Dodaj komentarz") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = colors.backgroundWhiteColor,
                        focusedBorderColor = colors.textColorMain,
                        unfocusedBorderColor = colors.textColorMain,
                        focusedLabelColor = colors.textColorMain,
                        unfocusedLabelColor = colors.textColorMain,
                        cursorColor = colors.textColorMain
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Go
                    ),
                    shape = RoundedCornerShape(30.dp)
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val userId = authService.getCurrentUserId() ?: "unknown"
                            commentViewModel.addComment(
                                post.postId,
                                userId,
                                username ?: "unknown"
                            )
                        }
                        isCommentFieldVisible.value = false
                    },
                    enabled = commentText.isNotEmpty(),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(top = 8.dp)
                        .aspectRatio(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Wyślij komentarz",
                        tint = colors.backgroundWhiteColor
                    )
                }
            }
        }
    }
}

@Composable
fun CommentSection(
    comments: List<CommentModel>,
    post: PostModel,
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    level: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        comments.forEach { comment ->
            CommentItem(
                comment = comment,
                userViewModel = userViewModel,
                post = post,
                commentViewModel = commentViewModel,
                level = level,
                showReplyField = remember { mutableStateOf(false) }
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentModel,
    userViewModel: UserViewModel,
    post: PostModel,
    commentViewModel: CommentViewModel,
    level: Int = 0,
    showReplyField: MutableState<Boolean>
) {
    val userLabel by userViewModel.label.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 16).dp)
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            DefaultAvatar()

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = comment.username, style = MaterialTheme.typography.subtitle2)
                    Text(text = comment.timestamp, style = MaterialTheme.typography.caption)
                }
                Text(
                    text = userLabel ?: "No label",
                    style = MaterialTheme.typography.caption,
                    color = colors.primaryBackgroundColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = comment.content,
            style = MaterialTheme.typography.body1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp),
                content = {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Like comment",
                        tint = colors.backgroundGrayColor
                    )
                }
            )

            IconButton(
                onClick = {
                    showReplyField.value = !showReplyField.value
                },
                modifier = Modifier
                    .size(40.dp),
                content = {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = if (showReplyField.value) "Hide reply field" else "Show reply field",
                        tint = colors.backgroundDarkGrayColor
                    )
                }
            )
        }
        if (showReplyField.value) {
            ReplyToCommentField(
                userViewModel = userViewModel,
                commentViewModel = commentViewModel,
                parentCommentId = comment.commentId,
                post = post
            ) {
                showReplyField.value = false
            }
        }

        if (comment.replies.isNotEmpty()) {
            CommentSection(
                comments = comment.replies,
                post = post,
                userViewModel = userViewModel,
                commentViewModel = commentViewModel,
                level = level + 1
            )
        }
    }
}

@Composable
fun ReplyToCommentField(
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    parentCommentId: String,
    post: PostModel,
    onReplySent: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val authService = AuthService()
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = replyText,
            onValueChange = { replyText = it },
            label = { Text("Odpowiedź na komentarz") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = colors.backgroundWhiteColor,
                focusedBorderColor = colors.textColorMain,
                unfocusedBorderColor = colors.textColorMain,
                focusedLabelColor = colors.textColorMain,
                unfocusedLabelColor = colors.textColorMain,
                cursorColor = colors.textColorMain
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Go
            ),
            shape = RoundedCornerShape(30.dp)
        )

        Button(
            onClick = {
                if (replyText.isNotEmpty()) {
                    coroutineScope.launch {
                        try {
                            val userId = authService.getCurrentUserId() ?: "unknown"
                            val username = userViewModel.username.value ?: "unknown"
                            commentViewModel.addReply(
                                postId = post.postId,
                                parentCommentId = parentCommentId,
                                replyContent = replyText,
                                userId = userId,
                                username = username
                            )
                            replyText = ""
                            onReplySent()
                        } catch (e: Exception) {
                            println("Error adding reply: ${e.message}")
                        }
                    }
                }
            },
            enabled = replyText.isNotEmpty(),
            modifier = Modifier
                .size(48.dp)
                .padding(top = 8.dp)
                .aspectRatio(1f),
            colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor),
            shape = RoundedCornerShape(30.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Wyślij komentarz",
                tint = colors.backgroundWhiteColor
            )
        }
    }
}