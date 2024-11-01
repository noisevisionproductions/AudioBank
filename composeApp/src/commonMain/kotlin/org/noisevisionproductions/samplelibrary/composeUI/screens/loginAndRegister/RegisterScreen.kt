package org.noisevisionproductions.samplelibrary.composeUI.screens.loginAndRegister

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import org.noisevisionproductions.samplelibrary.auth.RegisterViewModel
import org.noisevisionproductions.samplelibrary.errors.validation.ValidationResult
import org.noisevisionproductions.samplelibrary.composeUI.components.BackgroundWithCircles
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.utils.ClickableTextWithBackgroundForLoginAndRegister

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterClick: (String, String, String, String) -> Unit,
    onLoginActivityClick: () -> Unit,
    onRegulationsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
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
                text = "Witaj w AudioBank!",
                style = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    color = colors.textColorMain,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Wszystkie dźwięki, których poszukujesz!",
                style = LocalTextStyle.current.copy(
                    fontSize = 20.sp,
                    color = colors.textColorMain
                )
            )

            Spacer(modifier = Modifier.height(45.dp))

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
                    value = formState.nickname,
                    onValueChange = { viewModel.updateNickname(it) },
                    isError = formState.nicknameError is ValidationResult.Invalid,
                    label = { Text("Wpisz nazwę użytkownika") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(
                                1.dp,
                                if (formState.nicknameError is ValidationResult.Invalid) Color.Red else Color.Transparent
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
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                if (formState.nicknameError is ValidationResult.Invalid) {
                    Text(
                        text = (formState.nicknameError as ValidationResult.Invalid).message,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp, top = 56.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(25.dp))

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
                        focusedLabelColor = colors.textColorMain,
                        errorIndicatorColor = Color.Transparent,
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
                        modifier = Modifier
                            .padding(start = 16.dp, top = 56.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(25.dp))

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
                        imeAction = ImeAction.Next
                    )
                )
                if (formState.passwordError is ValidationResult.Invalid) {
                    Text(
                        text = (formState.passwordError as ValidationResult.Invalid).message,
                        color = Color.Red,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 56.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(25.dp))

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
                    value = formState.confirmPassword,
                    onValueChange = { viewModel.updateConfirmPassword(it) },
                    isError = formState.confirmPasswordError is ValidationResult.Invalid,
                    label = { Text("Potwierdź hasło") },
                    modifier = Modifier.fillMaxWidth()
                        .border(
                            BorderStroke(
                                1.dp,
                                if (formState.confirmPasswordError is ValidationResult.Invalid) Color.Red else Color.Transparent
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
                        onGo = {
                            onRegisterClick(
                                formState.nickname,
                                formState.email,
                                formState.password,
                                formState.confirmPassword
                            )
                        }
                    )
                )
                if (formState.confirmPasswordError is ValidationResult.Invalid) {
                    Text(
                        text = (formState.confirmPasswordError as ValidationResult.Invalid).message,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp, top = 56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    onRegisterClick(
                        formState.nickname,
                        formState.email,
                        formState.password,
                        formState.confirmPassword
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = colors.primaryBackgroundColor)
            ) {
                Text(
                    "Zarejestruj się",
                    style = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = colors.backgroundWhiteColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Tworząc konto, akceptujesz nasz",
                    style = LocalTextStyle.current.copy(fontSize = 12.sp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                ClickableTextWithBackgroundForLoginAndRegister(
                    text = "Regulamin",
                    onClick = { onRegulationsClick() },
                    fontSize = 12
                )
                Text(
                    text = "oraz",
                    style = LocalTextStyle.current.copy(fontSize = 12.sp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                ClickableTextWithBackgroundForLoginAndRegister(
                    text = "Politykę prywatności",
                    onClick = { onPrivacyPolicyClick() },
                    fontSize = 12
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Masz już konto w AudioBank?",
                    style = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                ClickableTextWithBackgroundForLoginAndRegister(
                    text = "Zaloguj się",
                    onClick = { onLoginActivityClick() }
                )
            }
        }
    }
}
