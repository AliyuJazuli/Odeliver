package com.hydr.odeliver

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import com.google.firebase.FirebaseException

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    navController: NavController,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: AuthViewModel
){
    var showPassword by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var usePhone by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }
    val activity = LocalContext.current as Activity
    val scope = remember { CoroutineScope(Dispatchers.Main) }
    val credentialManager = remember { CredentialManager.create(context) }
    val webClientId = "584309799224-gfchpd87k4ajrp2rfhnd50o4fmbue8qh.apps.googleusercontent.com"

    val email by viewModel.email.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val password by viewModel.password.collectAsState()
    val otp by viewModel.otp.collectAsState()

    var verificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var otpSent by remember { mutableStateOf(false) }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModel.signInWithCredential(credential) { task ->
                    if (task.isSuccessful) {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        }
                        Toast.makeText(context, "Sign in successful", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                Toast.makeText(context, "Verification Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                resendToken = token
                otpSent = true
                Toast.makeText(context, "OTP Sent", Toast.LENGTH_SHORT).show()
            }
        }
    }

   Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
           Column(Modifier.fillMaxWidth()) {
               Row {
                   IconButton(
                       modifier = Modifier.padding(top = 40.dp, start = 3.dp, end = 40.dp),
                       onClick = { navController.popBackStack() }
                   ) {
                       Icon(
                           imageVector = Icons.Default.ArrowBack,
                           contentDescription = "Back",
                           tint = MaterialTheme.colorScheme.primary
                       )
                   }

                   IconButton(
                       modifier = Modifier.padding(top = 40.dp, start = 290.dp),
                       onClick = onThemeToggle
                   ) {
                       Icon(
                           imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                           contentDescription = "Toggle Theme",
                           tint = MaterialTheme.colorScheme.primary
                       )
                   }
               }
                Text(
                    modifier = Modifier.padding(top = 20.dp, start = 20.dp),
                    text = "Login Account",
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
               Text(
                   modifier = Modifier.padding(top = 5.dp, start = 20.dp),
                   text = "Welcome Back!",
                   fontSize = 17.sp,
                   color = if (darkTheme) Color.White else Color.Black,
                   fontWeight = FontWeight.Bold
               )
               Spacer(Modifier.padding(top = 20.dp))
               Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.CenterVertically
               ) {
                   Text(
                       modifier = Modifier.padding(top = 15.dp, start = 23.dp),
                       text = if (usePhone) "Phone Number" else "Email Address",
                       fontSize = 22.sp,
                       color = if (darkTheme) Color.White else Color.Black,
                       fontWeight = FontWeight.Bold
                   )
                   Text(
                       modifier = Modifier
                           .clickable(onClick = { usePhone = !usePhone })
                           .padding(top = 10.dp, start = 23.dp, end = 10.dp),
                       text = if (usePhone) "Use Email?" else "Use Phone?",
                       fontSize = 22.sp,
                       color = MaterialTheme.colorScheme.primary,
                       fontWeight = FontWeight.Bold
                   )
               }

               if (usePhone) {
                   OutlinedTextField(
                       value = phoneNumber,
                       onValueChange = { viewModel.onPhoneNumberChange(it) },
                       placeholder = { Text(text = "Enter Phone Number", fontSize = 18.sp, fontWeight = FontWeight.Thin) },
                       colors = OutlinedTextFieldDefaults.colors(
                           focusedTextColor = Color.Black,
                           unfocusedTextColor = Color.Black,
                           focusedBorderColor = MaterialTheme.colorScheme.primary,
                           unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                           focusedContainerColor = Color(0xFFD9E1E2),
                           unfocusedContainerColor = Color(0xFFD9E1E2)
                       ),
                       singleLine = true,
                       shape = RoundedCornerShape(10.dp),
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(start = 23.dp, top = 10.dp, end = 10.dp)
                           .size(340.dp, 59.dp),
                       keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                       enabled = !otpSent
                   )
                   if (otpSent) {
                       Spacer(Modifier.padding(top = 10.dp))
                       OutlinedTextField(
                           value = otp,
                           onValueChange = { viewModel.onOtpChange(it) },
                           placeholder = { Text(text = "Enter OTP", fontSize = 18.sp, fontWeight = FontWeight.Thin) },
                           colors = OutlinedTextFieldDefaults.colors(
                               focusedTextColor = Color.Black,
                               unfocusedTextColor = Color.Black,
                               focusedBorderColor = MaterialTheme.colorScheme.primary,
                               unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                               focusedContainerColor = Color(0xFFD9E1E2),
                               unfocusedContainerColor = Color(0xFFD9E1E2)
                           ),
                           singleLine = true,
                           shape = RoundedCornerShape(10.dp),
                           modifier = Modifier
                               .fillMaxWidth()
                               .padding(start = 23.dp, top = 10.dp, end = 10.dp)
                               .size(340.dp, 59.dp),
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                       )
                       TextButton(
                           onClick = {
                               if (phoneNumber.isNotEmpty()) {
                                   val formattedNumber = if (phoneNumber.startsWith("0")) "+234${phoneNumber.substring(1)}" else phoneNumber
                                   val options = PhoneAuthOptions.newBuilder(auth)
                                       .setPhoneNumber(formattedNumber)
                                       .setTimeout(60L, TimeUnit.SECONDS)
                                       .setActivity(activity)
                                       .setCallbacks(callbacks)
                                   resendToken?.let { options.setForceResendingToken(it) }
                                   PhoneAuthProvider.verifyPhoneNumber(options.build())
                               }
                           },
                           modifier = Modifier.padding(start = 23.dp)
                       ) {
                           Text(text = "Resend OTP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                       }
                   }
               } else {
                   OutlinedTextField(
                       value = email,
                       onValueChange = { viewModel.onEmailChange(it) },
                       placeholder = { Text(text = "Enter email address", fontSize = 18.sp, fontWeight = FontWeight.Thin) },
                       colors = OutlinedTextFieldDefaults.colors(
                           focusedTextColor = Color.Black,
                           unfocusedTextColor = Color.Black,
                           focusedBorderColor = MaterialTheme.colorScheme.primary,
                           unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                           focusedContainerColor = Color(0xFFD9E1E2),
                           unfocusedContainerColor = Color(0xFFD9E1E2)
                       ),
                       keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                       singleLine = true,
                       shape = RoundedCornerShape(10.dp),
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(start = 23.dp, top = 10.dp, end = 10.dp)
                           .size(340.dp, 59.dp)
                   )
               }

               if (!otpSent && !usePhone) {
                   Spacer(Modifier.padding(top = 20.dp))
                   Row(
                       modifier = Modifier.fillMaxWidth(),
                       horizontalArrangement = Arrangement.SpaceBetween,
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Text(
                           modifier = Modifier.padding(top = 10.dp, start = 23.dp),
                           text = "Password",
                           fontSize = 22.sp,
                           color = if (darkTheme) Color.White else Color.Black,
                           fontWeight = FontWeight.Bold
                       )
                   }
                   OutlinedTextField(
                       value = password,
                       onValueChange = { viewModel.onPasswordChange(it) },
                       placeholder = { Text("Enter Password", fontSize = 18.sp, fontWeight = FontWeight.Thin) },
                       colors = OutlinedTextFieldDefaults.colors(
                           focusedTextColor = Color.Black,
                           unfocusedTextColor = Color.Black,
                           focusedBorderColor = MaterialTheme.colorScheme.primary,
                           unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                           focusedContainerColor = Color(0xFFD9E1E2),
                           unfocusedContainerColor = Color(0xFFD9E1E2)
                       ),
                       trailingIcon = {
                           IconButton(onClick = { showPassword = !showPassword }) {
                               Icon(
                                   if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                   contentDescription = null,
                                   tint = MaterialTheme.colorScheme.primary
                               )
                           }
                       },
                       visualTransformation = if (showPassword) PasswordVisualTransformation() else VisualTransformation.None,
                       keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                       singleLine = true,
                       shape = RoundedCornerShape(10.dp),
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(start = 23.dp, top = 10.dp, end = 10.dp)
                           .size(340.dp, 59.dp)
                   )
               }
               
               if (!usePhone) {
                   Spacer(Modifier.padding(top = 5.dp))
                   TextButton(
                       onClick = { navController.navigate(Screen.ForgetPassword.route) },
                       modifier = Modifier.align(Alignment.End).padding(end = 10.dp)
                   ) {
                       Text(
                           "Forgot password?",
                           fontWeight = FontWeight.Light, fontSize = 16.sp,
                           color = MaterialTheme.colorScheme.primary
                       )
                   }
               }

               Button(
                   onClick = {
                       if (!usePhone) {
                           if (email.isNotEmpty() && password.isNotEmpty()) {
                               viewModel.signInWithEmail { task ->
                                   if (task.isSuccessful) {
                                       navController.navigate(Screen.HomeScreen.route) {
                                           popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                       }
                                   } else {
                                       Toast.makeText(context, "Failed to login", Toast.LENGTH_SHORT).show()
                                   }
                               }
                           }
                       } else if (!otpSent) {
                           if (phoneNumber.isNotEmpty()) {
                               val formattedNumber = if (phoneNumber.startsWith("0")) "+234${phoneNumber.substring(1)}" else phoneNumber
                               val options = PhoneAuthOptions.newBuilder(auth)
                                   .setPhoneNumber(formattedNumber)
                                   .setTimeout(60L, TimeUnit.SECONDS)
                                   .setActivity(activity)
                                   .setCallbacks(callbacks)
                                   .build()
                               PhoneAuthProvider.verifyPhoneNumber(options)
                           }
                       } else if (otp.isNotEmpty()) {
                           val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                           viewModel.signInWithCredential(credential) { task ->
                               if (task.isSuccessful) {
                                   navController.navigate(Screen.HomeScreen.route) {
                                       popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                   }
                               }
                           }
                       }
                   },
                   modifier = Modifier
                       .padding(start = 35.dp, top = 20.dp)
                       .size(364.dp, 67.dp),
                   shape = RoundedCornerShape(10.dp),
                   elevation = ButtonDefaults.buttonElevation(10.dp),
                   colors = ButtonDefaults.buttonColors(
                       containerColor = MaterialTheme.colorScheme.primary,
                       contentColor = MaterialTheme.colorScheme.onPrimary
                   )
               ) {
                   Text(
                       text = if (usePhone) { if (!otpSent) "Send OTP" else "Verify OTP" } else { "Login" },
                       fontWeight = FontWeight.Bold,
                       fontSize = 21.sp
                   )
               }

               Text("------------- Or login with -------------", modifier = Modifier.padding(top = 35.dp).align(Alignment.CenterHorizontally))

               Button(
                   onClick = {
                       scope.launch {
                           val googleIdOption = GetGoogleIdOption.Builder()
                               .setFilterByAuthorizedAccounts(false)
                               .setServerClientId(webClientId)
                               .build()
                           val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
                           try {
                               val result = credentialManager.getCredential(request = request, context = activity)
                               val credential = result.credential
                               if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                   val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                   viewModel.signInWithGoogle(googleIdTokenCredential.idToken) { task ->
                                       if (task.isSuccessful) {
                                           navController.navigate(Screen.HomeScreen.route) {
                                               popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                           }
                                       }
                                   }
                               }
                           } catch (e: Exception) { Log.e(TAG, "Google Sign-In failed", e) }
                       }
                   },
                   modifier = Modifier
                       .padding(start = 38.dp, top = 25.dp)
                       .size(356.dp, 54.dp),
                   shape = RoundedCornerShape(10.dp),
                   elevation = ButtonDefaults.buttonElevation(10.dp),
                   colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9E1E2)),
                   border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
               ) {
                   Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                       Icon(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google Logo", modifier = Modifier.size(24.dp), tint = Color.Unspecified)
                       Spacer(modifier = Modifier.size(8.dp))
                       Text(text = "Continue With Google", fontWeight = FontWeight.Thin, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                   }
               }
               Text(
                   modifier = Modifier.align(Alignment.CenterHorizontally)
                       .padding(top = 24.dp)
                       .clickable(onClick = { navController.navigate(Screen.SignupScreen.route) }),
                   text = "Don't have an account?",
                   fontWeight = FontWeight.Black,
                   fontSize = 17.sp,
                   color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
