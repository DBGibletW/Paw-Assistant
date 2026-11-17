package com.pawbyte.pawassistant.ui

import android.util.Patterns
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

private enum class AuthMode {
    LOGIN,
    REGISTER,
    RESET_PASSWORD
}

@Composable
fun AuthScreen() {
    val auth = remember { FirebaseAuth.getInstance() }

    var mode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun switchMode(target: AuthMode) {
        mode = target
        errorMessage = null
        successMessage = null
        password = ""
        confirmPassword = ""
        passwordVisible = false
        confirmPasswordVisible = false
    }

    fun submitLogin() {
        val trimmedEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Lütfen geçerli bir e-posta adresi girin."
            return
        }
        if (password.isEmpty()) {
            errorMessage = "Lütfen şifrenizi girin."
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        auth.signInWithEmailAndPassword(trimmedEmail, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (!task.isSuccessful) {
                    errorMessage = mapFirebaseAuthError(task.exception)
                }
            }
    }

    fun submitRegister() {
        val trimmedEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Lütfen geçerli bir e-posta adresi girin."
            return
        }
        if (password.length < 6) {
            errorMessage = "Şifre en az 6 karakter olmalıdır."
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Girdiğiniz şifreler birbiriyle eşleşmiyor."
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        auth.createUserWithEmailAndPassword(trimmedEmail, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (!task.isSuccessful) {
                    errorMessage = mapFirebaseAuthError(task.exception)
                }
            }
    }

    fun submitResetPassword() {
        val trimmedEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Lütfen geçerli bir e-posta adresi girin."
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        auth.sendPasswordResetEmail(trimmedEmail)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    successMessage = "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi."
                } else {
                    errorMessage = mapFirebaseAuthError(task.exception)
                }
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AuthAssistantLogo(modifier = Modifier.size(44.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Paw Assistant",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (mode) {
                        AuthMode.LOGIN -> "Devam etmek için hesabınıza giriş yapın"
                        AuthMode.REGISTER -> "Yeni bir Paw Assistant hesabı oluşturun"
                        AuthMode.RESET_PASSWORD -> "Şifrenizi sıfırlamak için e-posta adresinizi girin"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            enabled = !isLoading,
                            singleLine = true,
                            label = { Text("E-posta") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = if (mode == AuthMode.RESET_PASSWORD) {
                                    ImeAction.Done
                                } else {
                                    ImeAction.Next
                                }
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (mode == AuthMode.RESET_PASSWORD && !isLoading) {
                                        submitResetPassword()
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (mode != AuthMode.RESET_PASSWORD) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                enabled = !isLoading,
                                singleLine = true,
                                label = { Text("Şifre") },
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        modifier = Modifier.semantics {
                                            contentDescription = if (passwordVisible) {
                                                "Şifreyi gizle"
                                            } else {
                                                "Şifreyi göster"
                                            }
                                        }
                                    ) {
                                        PasswordEyeIcon(
                                            visible = passwordVisible,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (mode == AuthMode.LOGIN) {
                                        ImeAction.Done
                                    } else {
                                        ImeAction.Next
                                    }
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (mode == AuthMode.LOGIN && !isLoading) {
                                            submitLogin()
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (mode == AuthMode.REGISTER) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                enabled = !isLoading,
                                singleLine = true,
                                label = { Text("Şifre tekrar") },
                                visualTransformation = if (confirmPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                                        modifier = Modifier.semantics {
                                            contentDescription = if (confirmPasswordVisible) {
                                                "Şifreyi gizle"
                                            } else {
                                                "Şifreyi göster"
                                            }
                                        }
                                    ) {
                                        PasswordEyeIcon(
                                            visible = confirmPasswordVisible,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (!isLoading) submitRegister()
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (mode == AuthMode.LOGIN) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                TextButton(
                                    onClick = { switchMode(AuthMode.RESET_PASSWORD) },
                                    enabled = !isLoading
                                ) {
                                    Text("Şifremi unuttum")
                                }
                            }
                        }

                        errorMessage?.let { error ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        successMessage?.let { message ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = message,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                when (mode) {
                                    AuthMode.LOGIN -> submitLogin()
                                    AuthMode.REGISTER -> submitRegister()
                                    AuthMode.RESET_PASSWORD -> submitResetPassword()
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Lütfen bekleyin...")
                            } else {
                                Text(
                                    text = when (mode) {
                                        AuthMode.LOGIN -> "Giriş yap"
                                        AuthMode.REGISTER -> "Hesap oluştur"
                                        AuthMode.RESET_PASSWORD -> "Sıfırlama e-postası gönder"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (mode) {
                    AuthMode.LOGIN -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hesabınız yok mu?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = { switchMode(AuthMode.REGISTER) },
                                enabled = !isLoading
                            ) {
                                Text("Kayıt ol")
                            }
                        }
                    }

                    AuthMode.REGISTER -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Zaten hesabınız var mı?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = { switchMode(AuthMode.LOGIN) },
                                enabled = !isLoading
                            ) {
                                Text("Giriş yap")
                            }
                        }
                    }

                    AuthMode.RESET_PASSWORD -> {
                        TextButton(
                            onClick = { switchMode(AuthMode.LOGIN) },
                            enabled = !isLoading
                        ) {
                            Text("Giriş ekranına dön")
                        }
                    }
                }
            }
        }
    }
}

private fun mapFirebaseAuthError(exception: Exception?): String {
    if (exception is FirebaseNetworkException) {
        return "İnternet bağlantısı kurulamadı. Lütfen bağlantınızı kontrol edin."
    }

    if (exception is FirebaseAuthException) {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Lütfen geçerli bir e-posta adresi girin."
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                "E-posta adresi veya şifre hatalı."
            "ERROR_USER_NOT_FOUND" ->
                "Bu e-posta adresine kayıtlı bir hesap bulunamadı."
            "ERROR_EMAIL_ALREADY_IN_USE" ->
                "Bu e-posta adresi ile zaten bir hesap kayıtlı."
            "ERROR_WEAK_PASSWORD" ->
                "Şifreniz en az 6 karakter uzunluğunda olmalıdır."
            "ERROR_USER_DISABLED" ->
                "Bu hesap devre dışı bırakılmış."
            "ERROR_TOO_MANY_REQUESTS" ->
                "Çok fazla deneme yapıldı. Lütfen biraz bekleyip tekrar deneyin."
            else -> "İşlem tamamlanamadı. Lütfen bilgilerinizi kontrol edip tekrar deneyin."
        }
    }

    return "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin."
}

@Composable
private fun AuthAssistantLogo(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val cy = h * 0.50f

        val topPetal = Path().apply {
            moveTo(cx, h * 0.08f)
            cubicTo(w * 0.65f, h * 0.08f, w * 0.68f, h * 0.24f, w * 0.61f, h * 0.38f)
            lineTo(cx, h * 0.46f)
            lineTo(w * 0.39f, h * 0.38f)
            cubicTo(w * 0.32f, h * 0.24f, w * 0.35f, h * 0.08f, cx, h * 0.08f)
            close()
        }
        drawPath(topPetal, color = Color(0xFF4285F4))

        val rightPetal = Path().apply {
            moveTo(w * 0.92f, cy)
            cubicTo(w * 0.92f, h * 0.65f, w * 0.76f, h * 0.68f, w * 0.62f, h * 0.61f)
            lineTo(w * 0.54f, cy)
            lineTo(w * 0.62f, h * 0.39f)
            cubicTo(w * 0.76f, h * 0.32f, w * 0.92f, h * 0.35f, w * 0.92f, cy)
            close()
        }
        drawPath(rightPetal, color = Color(0xFF34A853))

        val bottomPetal = Path().apply {
            moveTo(cx, h * 0.92f)
            cubicTo(w * 0.35f, h * 0.92f, w * 0.32f, h * 0.76f, w * 0.39f, h * 0.62f)
            lineTo(cx, h * 0.54f)
            lineTo(w * 0.61f, h * 0.62f)
            cubicTo(w * 0.68f, h * 0.76f, w * 0.65f, h * 0.92f, cx, h * 0.92f)
            close()
        }
        drawPath(bottomPetal, color = Color(0xFFFBBC04))

        val leftPetal = Path().apply {
            moveTo(w * 0.08f, cy)
            cubicTo(w * 0.08f, h * 0.35f, w * 0.24f, h * 0.32f, w * 0.38f, h * 0.39f)
            lineTo(w * 0.46f, cy)
            lineTo(w * 0.38f, h * 0.61f)
            cubicTo(w * 0.24f, h * 0.68f, w * 0.08f, h * 0.65f, w * 0.08f, cy)
            close()
        }
        drawPath(leftPetal, color = Color(0xFFEA4335))

        drawCircle(
            color = Color.White,
            radius = w * 0.09f,
            center = Offset(cx, cy)
        )
    }
}

@Composable
private fun PasswordEyeIcon(
    visible: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = size.minDimension * 0.10f

        val eyePath = Path().apply {
            moveTo(w * 0.10f, h * 0.50f)
            cubicTo(w * 0.26f, h * 0.22f, w * 0.74f, h * 0.22f, w * 0.90f, h * 0.50f)
            cubicTo(w * 0.74f, h * 0.78f, w * 0.26f, h * 0.78f, w * 0.10f, h * 0.50f)
            close()
        }
        drawPath(
            path = eyePath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawCircle(
            color = color,
            radius = w * 0.13f,
            center = Offset(w * 0.50f, h * 0.50f)
        )

        if (!visible) {
            drawLine(
                color = color,
                start = Offset(w * 0.18f, h * 0.82f),
                end = Offset(w * 0.82f, h * 0.18f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
