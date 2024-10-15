package org.noisevisionproductions.samplelibrary.composeUI.screens.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.composeUI.components.DefaultAvatar
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.PostViewModel
import org.noisevisionproductions.samplelibrary.composeUI.viewModels.UserViewModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

@Composable
fun PostDetailView(
    post: PostModel,
    postViewModel: PostViewModel,
    userViewModel: UserViewModel, onBack: () -> Unit
) {
    val username by userViewModel.username.collectAsState()
    val categoryName = remember { mutableStateOf("") }

    LaunchedEffect(post.categoryId) {
        categoryName.value = postViewModel.getCategoryName(post.categoryId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Button(
                    onClick = { onBack() }
                ) {
                    Text(text = "Wróć")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                DefaultAvatar()

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = categoryName.value,
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = username ?: "username",
                        style = MaterialTheme.typography.caption
                    )
                }

                Text(
                    text = post.timestamp.substringBefore(" "),
                    style = MaterialTheme.typography.caption
                )
            }

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
                Button(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Like"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Like")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Comment"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Comment")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("Dodaj komentarz") },
                singleLine = true,
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
        }

    }

}