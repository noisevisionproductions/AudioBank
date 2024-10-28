package org.noisevisionproductions.samplelibrary.composeUI.screens.account.accountSettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.composeUI.CustomOutlinedTextField
import org.noisevisionproductions.samplelibrary.composeUI.CustomTopAppBar
import org.noisevisionproductions.samplelibrary.composeUI.PlatformAvatarImage
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
    val selectedImagePath by accountViewModel.selectedImagePath.collectAsState()

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
                    accountViewModel = accountViewModel,
                    onUsernameChange = { accountViewModel.updateUsername(it) },
                    onRemoveLikedPost = { accountViewModel.removeLikedPost(it) },
                    onPostClick = onPostClick
                )
            }
        }
    }
}

@Composable
private fun AccountContent(
    userModel: UserModel,
    likedPosts: List<PostModel>,
    accountViewModel: AccountViewModel,
    onUsernameChange: (String) -> Unit,
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
                accountViewModel = accountViewModel,
                onUsernameChange = onUsernameChange
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            StatisticSection(userModel = userModel)
        }

        item {
            if (likedPosts.isNotEmpty()) {
                Text(
                    "Polubione posty",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        items(
            items = likedPosts,
            key = { it.postId }
        ) { post ->
            LikedPostCard(
                post = post,
                onRemove = { onRemoveLikedPost(post.postId) },
                onClick = { onPostClick(post.postId) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProfileSection(
    userModel: UserModel,
    accountViewModel: AccountViewModel,
    onUsernameChange: (String) -> Unit,
) {
    var isEditingUsername by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf(userModel.username) }

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

            if (isEditingUsername) {
                CustomOutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Nazwa użytkownika",
                    keyboardActions = KeyboardActions(onDone = {
                        isEditingUsername = false
                        onUsernameChange(username)
                    }),
                    modifier = Modifier.fillMaxWidth()

                )
            } else {
                Text(
                    text = userModel.username,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.clickable { isEditingUsername = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dołączono: ${userModel.registrationDate}",
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
fun LikedPostCard(
    post: PostModel,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "przez ${post.username} • ${post.timestamp}",
                    style = MaterialTheme.typography.caption
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Usuń z polubionych",
                    tint = Color.Red
                )
            }
        }
    }
}