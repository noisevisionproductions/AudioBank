package org.noisevisionproductions.samplelibrary.composeUI.screens.loginAndRegister

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.auth.LoginViewModel
import org.noisevisionproductions.samplelibrary.errors.validation.ValidationResult
import org.noisevisionproductions.samplelibrary.composeUI.components.BackgroundWithCircles
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.utils.ClickableTextWithBackgroundForLoginAndRegister
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.phone_and_person_icon

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginClick: (String, String) -> Unit,
    onRegistrationActivityClick: () -> Unit,
    onForgotPasswordClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val formState by viewModel.formState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundGrayColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        BackgroundWithCircles(colors.backgroundDarkGrayColor)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Witaj spowrotem!",
                style = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    color = colors.textColorMain,
                    fontWeight = FontWeight.Bold
                )
            )
            Image(
                painterResource(Res.drawable.phone_and_person_icon),
                contentDescription = "welcome",
                modifier = Modifier
                    .padding(25.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colors.backgroundWhiteColor,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(4.dp)
            ) {
                TextField(
                    value = formState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    isError = formState.emailError is ValidationResult.Invalid,
                    label = { Text("Wpisz swój adres e-mail") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(
                                1.dp,
                                if (formState.emailError is ValidationResult.Invalid) Color.Red else Color.Transparent
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedLabelColor = colors.textColorMain,
                        unfocusedLabelColor = colors.textColorMain,
                        cursorColor = colors.textColorMain
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                if (formState.emailError is ValidationResult.Invalid) {
                    Text(
                        text = (formState.emailError as ValidationResult.Invalid).message,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp, top = 56.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(35.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colors.backgroundWhiteColor,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(4.dp)
            ) {
                TextField(
                    value = formState.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    isError = formState.passwordError is ValidationResult.Invalid,
                    label = { Text("Wpisz hasło") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(
                                1.dp,
                                if (formState.passwordError is ValidationResult.Invalid) Color.Red else Color.Transparent
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedLabelColor = colors.textColorMain,
                        unfocusedLabelColor = colors.textColorMain,
                        cursorColor = colors.textColorMain
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { onLoginClick(formState.email, formState.password) }
                    )
                )
                if (formState.passwordError is ValidationResult.Invalid) {
                    Text(
                        text = (formState.passwordError as ValidationResult.Invalid).message,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp, top = 56.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(50.dp))

            ClickableTextWithBackgroundForLoginAndRegister(
                text = "Nie pamiętasz hasła?",
                onClick = { onForgotPasswordClick() }
            )

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = { onLoginClick(formState.email, formState.password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor)
            ) {
                Text(
                    "Zaloguj się",
                    style = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = colors.backgroundWhiteColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(35.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Nie posiadasz konta w AudioBank?",
                    style = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                ClickableTextWithBackgroundForLoginAndRegister(
                    text = "Zarejestruj się",
                    onClick = { onRegistrationActivityClick() }
                )
            }
        }
    }
}
