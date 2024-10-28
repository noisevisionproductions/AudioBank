package org.noisevisionproductions.samplelibrary.composeUI.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.accountSettings.AccountEditScreen
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.accountSettings.AccountViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.ErrorDialogManager
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_account_settings
import samplelibrary.composeapp.generated.resources.icon_groups
import samplelibrary.composeapp.generated.resources.icon_key
import samplelibrary.composeapp.generated.resources.icon_privacy_lock
import samplelibrary.composeapp.generated.resources.icon_settings
import samplelibrary.composeapp.generated.resources.icon_sounds_option

@Composable
fun AccountFragmentNavigationHost(
    accountViewModel: AccountViewModel,
    errorDialogManager: ErrorDialogManager
) {
    var currentScreen by remember {
        mutableStateOf<AccountScreenNavigation>(
            AccountScreenNavigation.AccountFragment
        )
    }

    when (currentScreen) {
        is AccountScreenNavigation.AccountFragment -> {
            AccountFragment(
                onNavigate = { destination ->
                    currentScreen = destination
                }
            )
        }

        is AccountScreenNavigation.AccountEditScreen -> {
            AccountEditScreen(
                accountViewModel = accountViewModel,
                errorDialogManager = errorDialogManager,
                onNavigateBack = { currentScreen = AccountScreenNavigation.AccountFragment },
                onPostClick = {}
            )
        }
    }
}

@Composable
fun AccountFragment(
    onNavigate: (AccountScreenNavigation) -> Unit
) {
    val searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundGrayColor)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 40.dp)
                .clip(RoundedCornerShape(25.dp))
        ) {
            TextField(
                value = searchText,
                onValueChange = {},
                singleLine = true,
                placeholder = { Text("Wyszukaj wątek, który potrzebujesz") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search icon")
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colors.backgroundDarkGrayColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(colors.backgroundWhiteColor)
        ) {
            MainContentWithOptions(onNavigate = onNavigate)
        }
    }
}

@Composable
fun MainContentWithOptions(onNavigate: (AccountScreenNavigation) -> Unit) {
    val options = listOf(
        "Ustawienia konta" to Res.drawable.icon_account_settings,
        "Login i Hasło" to Res.drawable.icon_key,
        "Prywatność" to Res.drawable.icon_privacy_lock,
        "Ustawienia" to Res.drawable.icon_settings,
        "Grupy" to Res.drawable.icon_groups,
        "Dźwięki" to Res.drawable.icon_sounds_option
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(options) { index, (title, icon) ->
            OptionTile(title, icon) {
                if (index == 0) {
                    onNavigate(AccountScreenNavigation.AccountEditScreen)
                }
            }
        }
    }
}

@Composable
fun OptionTile(title: String, icon: DrawableResource, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 16.dp,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.primaryBackgroundColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.body1.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = colors.textColorMain,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
