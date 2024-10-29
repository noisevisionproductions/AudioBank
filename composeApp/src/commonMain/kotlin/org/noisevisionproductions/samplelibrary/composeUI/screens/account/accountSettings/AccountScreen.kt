package org.noisevisionproductions.samplelibrary.composeUI.screens.account.accountSettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.composeUI.CustomTopAppBar
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.AvatarManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.ErrorDialogManager
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.UserModel

@Composable
fun AccountEditScreen(
    accountViewModel: AccountViewModel,
    errorDialogManager: ErrorDialogManager,
    onNavigateBack: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val userState by accountViewModel.userState.collectAsState()
    val likedPosts by accountViewModel.likedPosts.collectAsState()
    val createdPosts by accountViewModel.createdPosts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColorForNewContent)
    ) {
        CustomTopAppBar(
            title = "Ustawienia konta",
            onNavigateBack = onNavigateBack
        )

        errorDialogManager.ShowErrorDialog()

        when (val state = userState) {
            is UserState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = colors.primaryBackgroundColor
                )
            }

            is UserState.Success -> {
                AccountContent(
                    userModel = state.user,
                    likedPosts = likedPosts,
                    createdPosts = createdPosts,
                    accountViewModel = accountViewModel,
                    onRemoveLikedPost = { accountViewModel.removeLikedPost(it) },
                    onPostClick = onPostClick
                )
            }
        }
    }
}

@Composable
fun AccountContent(
    userModel: UserModel,
    likedPosts: List<PostModel>,
    createdPosts: List<PostModel>,
    accountViewModel: AccountViewModel,
    onRemoveLikedPost: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileSection(
                userModel = userModel,
                accountViewModel = accountViewModel
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            StatisticSection(userModel = userModel)
        }

        item {
            PostsSection(
                title = "Utworzone posty",
                posts = createdPosts,
                emptyMessage = "Nie utworzono jeszcze żadnych postów",
                onPostClick = onPostClick
            )
        }

        // Liked Posts Section
        item {
            PostsSection(
                title = "Polubione posty",
                posts = likedPosts,
                emptyMessage = "Nie polubiono jeszcze żadnych postów",
                onPostClick = onPostClick,
                onRemove = onRemoveLikedPost
            )
        }
    }
}

@Composable
fun ProfileSection(
    userModel: UserModel,
    accountViewModel: AccountViewModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarManager.UserAvatar(
                avatarUrl = userModel.avatarUrl,
                onClick = { accountViewModel.pickAvatar() },
                showEditButton = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userModel.username,
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dołączono: ${userModel.registrationDate.substringBeforeLast("T")}",
                style = MaterialTheme.typography.h4,
            )
        }
    }
}

@Composable
fun StatisticSection(userModel: UserModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statystyki",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem("Polubione posty", userModel.likedPosts.size.toString())
                StatisticItem("Polubione komentarze", userModel.likedComments.size.toString())
            }
        }
    }
}

@Composable
fun StatisticItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.h5,
            color = colors.primaryBackgroundColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
fun PostsSection(
    title: String,
    posts: List<PostModel>,
    emptyMessage: String,
    onPostClick: (String) -> Unit,
    onRemove: ((String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (posts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            posts.forEach { post ->
                PostCard(
                    post = post,
                    onRemove = onRemove?.let { { it(post.postId) } },
                    onClick = { onPostClick(post.postId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PostCard(
    post: PostModel,
    onRemove: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "przez ${post.username} • ${post.timestamp.substringBeforeLast(" ")}",
                    style = MaterialTheme.typography.caption.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
            }

            onRemove?.let {
                IconButton(
                    onClick = it,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Usuń z polubionych",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
