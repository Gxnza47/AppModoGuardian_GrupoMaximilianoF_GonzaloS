package com.example.modoguardian_grupomg.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modoguardian_grupomg.GoogleAuthHelper
import com.example.modoguardian_grupomg.model.LoginUiState
import com.example.modoguardian_grupomg.ui.theme.ColorExito
import com.example.modoguardian_grupomg.ui.theme.ModoGuardian_GrupoMGTheme
import com.example.modoguardian_grupomg.ui.utils.obtenerWindowSizeClass
import com.example.modoguardian_grupomg.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: LoginViewModel, onCrearCuenta: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthHelper = remember { GoogleAuthHelper(context) }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRecordarCorreoChange = viewModel::onRecordarCorreoChange,
        onSimularSinConexionChange = viewModel::onSimularSinConexionChange,
        onLogin = viewModel::login,
        onGoogleLogin = {
            coroutineScope.launch {
                val result = googleAuthHelper.signInWithGoogle()
                result.onSuccess { email ->
                    Toast.makeText(context, "Bienvenido $email", Toast.LENGTH_SHORT).show()
                    viewModel.loginConGoogle(email)
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        error.message ?: "Error al iniciar con Google",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        },
        onCrearCuenta = onCrearCuenta
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onRecordarCorreoChange: (Boolean) -> Unit = {},
    onSimularSinConexionChange: (Boolean) -> Unit = {},
    onLogin: () -> Unit = {},
    onGoogleLogin: () -> Unit = {},
    onCrearCuenta: () -> Unit = {}
) {
    val compact = obtenerWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(if (compact) 20.dp else 32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (compact) {
                Column(
                    Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    GuardianBrand()
                    LoginForm(
                        state, onEmailChange, onPasswordChange, onRecordarCorreoChange,
                        onSimularSinConexionChange, onLogin, onGoogleLogin, onCrearCuenta
                    )
                }
            } else {
                Card(
                    Modifier
                        .widthIn(max = 1040.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier.padding(32.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) { GuardianBrand() }
                        Column(Modifier.weight(1.2f)) {
                            LoginForm(
                                state, onEmailChange, onPasswordChange, onRecordarCorreoChange,
                                onSimularSinConexionChange, onLogin, onGoogleLogin, onCrearCuenta
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuardianBrand() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = "Logo de Modo Guardián",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "INSERCODE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    "SEGURIDAD INTEGRAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
            }
        }
        Row(
            Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = ColorExito, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Acceso Seguro", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(4.dp))
        Text("Modo Guardián", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text(
            "Ingresa a tu turno de monitoreo",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Demostración académica · Solo datos ficticios",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
private fun LoginForm(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRecordarCorreoChange: (Boolean) -> Unit,
    onSimularSinConexionChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val submit = { focusManager.clearFocus(); onLogin() }
    var verClave by rememberSaveable { mutableStateOf(false) }
    val forma = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            state.email, onEmailChange, modifier = Modifier.fillMaxWidth(), shape = forma,
            label = { Text("Correo electrónico") }, singleLine = true, enabled = !state.isLoading,
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            isError = state.errores.email != null,
            supportingText = { state.errores.email?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            state.password, onPasswordChange, modifier = Modifier.fillMaxWidth(), shape = forma,
            label = { Text("Contraseña") }, singleLine = true, enabled = !state.isLoading,
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { verClave = !verClave }) {
                    Icon(
                        if (verClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (verClave) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
            isError = state.errores.password != null,
            supportingText = { state.errores.password?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (state.isLoginEnabled) submit() })
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(state.recordarCorreo, onRecordarCorreoChange, enabled = !state.isLoading)
            Text("Recordar solo mi correo", style = MaterialTheme.typography.bodyMedium)
        }
        state.errorGeneral?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        state.aviso?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }

        Button(
            submit, enabled = state.isLoginEnabled, shape = forma,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Ingresando…")
            } else {
                Text("Ingresar al sistema", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(10.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }

        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                onGoogleLogin()
            },
            enabled = !state.isLoading,
            shape = forma,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "G",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.Blue
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Continuar con Google",
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            "Tus permisos se cargarán automáticamente según tu perfil",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )

        Row(
            Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "¿No tienes cuenta? ", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Crear cuenta", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(enabled = !state.isLoading, onClick = onCrearCuenta)
            )
        }

        HorizontalDivider()
        Text(
            "USUARIOS DE PRUEBA · toca uno para completar", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp
        )
        listOf(
            "Admin" to "admin@guardian.test",
            "Supervisor" to "supervisor@guardian.test",
            "Operador" to "operador@guardian.test"
        ).forEach { (rol, correo) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = !state.isLoading) { onEmailChange(correo); onPasswordChange("123456") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    rol, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(84.dp)
                )
                Text(
                    correo, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            "Contraseña para los tres: 123456", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Switch(state.simularSinConexion, onSimularSinConexionChange, enabled = !state.isLoading)
            Text("Simular desconexión", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(name = "Compact · Celular", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(name = "Medium · Horizontal", widthDp = 700, heightDp = 400, showBackground = true)
@Preview(name = "Expanded · Tablet", widthDp = 1100, heightDp = 800, showBackground = true)
@Composable
private fun LoginPreview() { ModoGuardian_GrupoMGTheme { LoginContent(LoginUiState()) } }

@Preview(name = "Error de formulario", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LoginErrorPreview() {
    ModoGuardian_GrupoMGTheme {
        LoginContent(
            LoginUiState(
                email = "correo incorrecto", password = "12",
                errores = com.example.modoguardian_grupomg.model.LoginValidator.validar("correo incorrecto", "12")
            )
        )
    }
}