package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.components.DefaultAvatar
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.auth.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.AvatarManager.UserAvatar
import org.noisevisionproductions.samplelibrary.interfaces.formatTimeAgo
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_comment

@Composable
fun CommentSection(
    comments: List<CommentModel>,
    post: PostModel,
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    likeManager: LikeManager,
    level: Int = 0,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        comments.forEach { comment ->
            CommentItem(
                comment = comment,
                userViewModel = userViewModel,
                post = post,
                commentViewModel = commentViewModel,
                likeManager = likeManager,
                level = level
            )
        }
    }
}

@Composable
fun CommentButton(commentCount: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
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
            text = "$commentCount",
            color = colors.backgroundWhiteColor
        )
    }
}

@Composable
fun CommentInputField(
    commentText: String,
    onCommentTextChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentTextChanged,
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
            onClick = onSendClick,
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
                contentDescription = "Wyślij odpowiedź na komentarz",
                tint = colors.backgroundWhiteColor
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentModel,
    post: PostModel,
    userViewModel: UserViewModel,
    commentViewModel: CommentViewModel,
    likeManager: LikeManager,
    level: Int = 0,
) {
    val replyFieldVisibilityMap by commentViewModel.replyFieldVisibilityMap.collectAsState()
    val showReplyField = replyFieldVisibilityMap[comment.commentId] ?: false
    val userLabels by userViewModel.userLabels.collectAsState()
    val userLabel = userLabels[comment.commentId] ?: "No label"

    val avatarUrls by userViewModel.avatarUrls.collectAsState()
    val avatarUrl = avatarUrls[comment.userId] ?: ""

    val commentLikeStates by likeManager.commentLikeStates.collectAsState()
    val likeState = commentLikeStates[post.postId]?.get(comment.commentId)

    val isLiked = likeState?.isLiked ?: false
    val likesCount = likeState?.likesCount ?: comment.likesCount

    LaunchedEffect(comment.userId) {
        userViewModel.fetchLabelForUser(comment.userId)
        userViewModel.fetchAvatarUrl(comment.userId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 16).dp)
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            if (avatarUrl.isNotEmpty()) {
                UserAvatar(avatarUrl = avatarUrl, size = 70.dp)
            } else {
                DefaultAvatar()
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = comment.username,
                        style = MaterialTheme.typography.subtitle2,
                        color = colors.backgroundDarkGrayColor,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(
                        text = formatTimeAgo(comment.timestamp),
                        style = MaterialTheme.typography.caption,
                        color = colors.primaryBackgroundColor
                    )
                }
                Text(
                    text = userLabel,
                    style = MaterialTheme.typography.caption,
                    color = colors.backgroundWhiteColor,
                    modifier = Modifier
                        .background(
                            color = colors.primaryBackgroundColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 3.dp, horizontal = 10.dp),
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
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { commentViewModel.toggleCommentLike(post.postId, comment.commentId) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Liked" else "Like",
                    tint = if (isLiked) Color.Red else colors.primaryBackgroundColor
                )
            }

            Text(
                text = "$likesCount",
                style = MaterialTheme.typography.subtitle2,
                color = colors.primaryBackgroundColor,
                modifier = Modifier.padding(end = 10.dp)
            )

            IconButton(
                onClick = {
                    commentViewModel.toggleReplyFieldVisibility(comment.commentId)
                },
                modifier = Modifier
                    .size(40.dp),
                content = {
                    Icon(
                        painter = painterResource(Res.drawable.icon_comment),
                        contentDescription = if (showReplyField) "Hide reply field" else "Show reply field",
                        tint = colors.primaryBackgroundColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
        if (showReplyField) {
            ReplyToCommentField(
                commentViewModel = commentViewModel,
                parentCommentId = comment.commentId,
                postId = post.postId
            )
        }

        CommentSection(
            comments = comment.replies,
            post = post,
            userViewModel = userViewModel,
            commentViewModel = commentViewModel,
            likeManager = likeManager,
            level = level + 1
        )
    }
}

@Composable
fun ReplyToCommentField(
    commentViewModel: CommentViewModel,
    parentCommentId: String,
    postId: String
) {
    val replyTextsMap by commentViewModel.replyTextsMap.collectAsState()
    val isSendingReply by commentViewModel.isSendingReply.collectAsState()

    val replyText = replyTextsMap[parentCommentId] ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = replyText,
            onValueChange = { commentViewModel.updateReplyText(parentCommentId, it) },
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
                commentViewModel.addReply(postId, parentCommentId)
            },
            enabled = replyText.isNotEmpty() && !isSendingReply,
            modifier = Modifier
                .size(48.dp)
                .padding(top = 8.dp)
                .aspectRatio(1f),
            colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor),
            shape = RoundedCornerShape(30.dp),
        ) {
            if (isSendingReply) {
                CircularProgressIndicator(
                    color = colors.backgroundWhiteColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Wyślij komentarz",
                tint = colors.backgroundWhiteColor
            )
        }
    }
}