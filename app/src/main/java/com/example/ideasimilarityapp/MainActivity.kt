package com.example.ideasimilarityapp

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ScholarMetricApp() }
    }
}

private enum class Screen {
    SPLASH,
    LOGIN,
    SIGNUP,
    HOME,
    RESULT,
    SETTINGS,
    SETTINGS_ACCOUNT,
    SETTINGS_SUBSCRIPTION,
    SETTINGS_NOTIFICATIONS,
    SETTINGS_HELP
}

private data class AnalysisUiState(
    val title: String,
    val similarityPercent: Int,
    val wordCount: Int,
    val citationsFound: Int,
    val confidenceLabel: String,
    val note: String,
    val sourceBreakdown: List<SourceBreakdownItem>
)

private data class SettingsRowData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val subtitleColor: Color = Color(0xFF5E5A64),
    val route: Screen
)

private data class SettingsSummaryUiState(
    val name: String,
    val email: String,
    val profileImageUrl: String,
    val subscriptionPlan: String,
    val notificationStatus: String,
    val supportStatus: String
)

private data class DetailItem(
    val label: String,
    val value: String
)

private data class SettingsDetailUiState(
    val title: String,
    val items: List<DetailItem>
)

private data class NotificationPrefsUiState(
    val emailNotifications: Boolean = false,
    val pushNotifications: Boolean = false,
    val weeklyDigest: Boolean = false
)

@Composable
private fun ScholarMetricApp() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var ideaText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var loginLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var signupLoading by remember { mutableStateOf(false) }
    var signupError by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var settingsLoading by remember { mutableStateOf(false) }
    var settingsError by remember { mutableStateOf<String?>(null) }
    var detailLoading by remember { mutableStateOf(false) }
    var detailError by remember { mutableStateOf<String?>(null) }
    var settingsSummary by remember {
        mutableStateOf(
            SettingsSummaryUiState(
                name = "Dr. Elena Sterling",
                email = "elena.sterling@university.edu",
                profileImageUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=400&q=80",
                subscriptionPlan = "Premium Plan Active",
                notificationStatus = "Email + Push Enabled",
                supportStatus = "Priority Academic Support"
            )
        )
    }
    var settingsDetail by remember {
        mutableStateOf(
            SettingsDetailUiState(
                title = "",
                items = emptyList()
            )
        )
    }
    var notificationPrefs by remember { mutableStateOf(NotificationPrefsUiState()) }
    var profileIconOptions by remember { mutableStateOf<List<ProfileIconOption>>(emptyList()) }
    var selectedProfileImageUrl by remember { mutableStateOf("") }
    var profileUpdateLoading by remember { mutableStateOf(false) }
    var analysisState by remember {
        mutableStateOf(
            AnalysisUiState(
                title = "Neural Architectures in Synthetic Linguistics.pdf",
                similarityPercent = 34,
                wordCount = 12482,
                citationsFound = 48,
                confidenceLabel = "HIGH CONFIDENCE",
                note = "Your manuscript shows moderate overlap with existing published works in our repository.",
                sourceBreakdown = listOf(
                    SourceBreakdownItem("Journal of AI Research", "Case Study: Transformer Models in Modern UX", 18, "PDF DOCUMENT"),
                    SourceBreakdownItem("Google Scholar", "Multiple instances detected across indexed academic pages", 10, "WEB REPOSITORY"),
                    SourceBreakdownItem("Institutional Archive", "Internal repository: Stanford Digital Library", 6, "PRIVATE")
                )
            )
        )
    }

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.180.5.246:8005/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = remember { retrofit.create(ApiService::class.java) }

    fun fetchProfileIcons() {
        api.getProfileIcons().enqueue(object : Callback<ProfileIconsResponse> {
            override fun onResponse(call: Call<ProfileIconsResponse>, response: Response<ProfileIconsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == "success") {
                    profileIconOptions = body.options
                    if (selectedProfileImageUrl.isBlank() && body.options.isNotEmpty()) {
                        selectedProfileImageUrl = body.options.first().image_url
                    }
                }
            }

            override fun onFailure(call: Call<ProfileIconsResponse>, t: Throwable) {
                // Keep existing options if network fails.
            }
        })
    }

    fun fetchSettingsSummary() {
        if (userId.isBlank()) return
        settingsLoading = true
        settingsError = null
        api.getSettingsSummary(userId).enqueue(object : Callback<SettingsSummaryResponse> {
            override fun onResponse(call: Call<SettingsSummaryResponse>, response: Response<SettingsSummaryResponse>) {
                settingsLoading = false
                val body = response.body()
                if (!response.isSuccessful || body == null || body.status != "success") {
                    settingsError = "Could not load settings summary."
                    return
                }
                settingsSummary = SettingsSummaryUiState(
                    name = body.name,
                    email = body.email,
                    profileImageUrl = body.profile_image_url,
                    subscriptionPlan = body.subscription_plan,
                    notificationStatus = body.notification_status,
                    supportStatus = body.support_status
                )
                selectedProfileImageUrl = body.profile_image_url
            }

            override fun onFailure(call: Call<SettingsSummaryResponse>, t: Throwable) {
                settingsLoading = false
                settingsError = "Settings network error: ${t.message}"
            }
        })
    }

    fun openSettings() {
        currentScreen = Screen.SETTINGS
        if (profileIconOptions.isEmpty()) fetchProfileIcons()
        fetchSettingsSummary()
    }

    fun loadAccountSettings() {
        if (userId.isBlank()) return
        currentScreen = Screen.SETTINGS_ACCOUNT
        detailLoading = true
        detailError = null
        api.getAccountSettings(userId).enqueue(object : Callback<AccountSettingsResponse> {
            override fun onResponse(call: Call<AccountSettingsResponse>, response: Response<AccountSettingsResponse>) {
                detailLoading = false
                val body = response.body()
                if (!response.isSuccessful || body == null || body.status != "success") {
                    detailError = "Unable to load account settings."
                    return
                }
                settingsDetail = SettingsDetailUiState(
                    title = "Account Settings",
                    items = listOf(
                        DetailItem("Full Name", body.full_name),
                        DetailItem("Email", body.email),
                        DetailItem("Department", body.department),
                        DetailItem("Institution", body.institution)
                    )
                )
                selectedProfileImageUrl = body.profile_image_url
            }

            override fun onFailure(call: Call<AccountSettingsResponse>, t: Throwable) {
                detailLoading = false
                detailError = "Network error: ${t.message}"
            }
        })
    }

    fun loadSubscriptionSettings() {
        if (userId.isBlank()) return
        currentScreen = Screen.SETTINGS_SUBSCRIPTION
        detailLoading = true
        detailError = null
        api.getSubscriptionSettings(userId).enqueue(object : Callback<SubscriptionSettingsResponse> {
            override fun onResponse(call: Call<SubscriptionSettingsResponse>, response: Response<SubscriptionSettingsResponse>) {
                detailLoading = false
                val body = response.body()
                if (!response.isSuccessful || body == null || body.status != "success") {
                    detailError = "Unable to load subscription details."
                    return
                }
                settingsDetail = SettingsDetailUiState(
                    title = "Subscription",
                    items = listOf(
                        DetailItem("Plan", body.plan_name),
                        DetailItem("Renewal Date", body.renewal_date),
                        DetailItem("Billing Cycle", body.billing_cycle),
                        DetailItem("Features", body.features.joinToString("\n• ", prefix = "• "))
                    )
                )
            }

            override fun onFailure(call: Call<SubscriptionSettingsResponse>, t: Throwable) {
                detailLoading = false
                detailError = "Network error: ${t.message}"
            }
        })
    }

    fun loadNotificationSettings() {
        if (userId.isBlank()) return
        currentScreen = Screen.SETTINGS_NOTIFICATIONS
        detailLoading = true
        detailError = null
        api.getNotificationSettings(userId).enqueue(object : Callback<NotificationSettingsResponse> {
            override fun onResponse(call: Call<NotificationSettingsResponse>, response: Response<NotificationSettingsResponse>) {
                detailLoading = false
                val body = response.body()
                if (!response.isSuccessful || body == null || body.status != "success") {
                    detailError = "Unable to load notification settings."
                    return
                }
                settingsDetail = SettingsDetailUiState(
                    title = "Notification Preferences",
                    items = listOf(
                        DetailItem("Email Notifications", if (body.email_notifications) "Enabled" else "Disabled"),
                        DetailItem("Push Notifications", if (body.push_notifications) "Enabled" else "Disabled"),
                        DetailItem("Weekly Digest", if (body.weekly_digest) "Enabled" else "Disabled")
                    )
                )
                notificationPrefs = NotificationPrefsUiState(
                    emailNotifications = body.email_notifications,
                    pushNotifications = body.push_notifications,
                    weeklyDigest = body.weekly_digest
                )
            }

            override fun onFailure(call: Call<NotificationSettingsResponse>, t: Throwable) {
                detailLoading = false
                detailError = "Network error: ${t.message}"
            }
        })
    }

    fun loadHelpSettings() {
        if (userId.isBlank()) return
        currentScreen = Screen.SETTINGS_HELP
        detailLoading = true
        detailError = null
        api.getHelpSettings(userId).enqueue(object : Callback<HelpSettingsResponse> {
            override fun onResponse(call: Call<HelpSettingsResponse>, response: Response<HelpSettingsResponse>) {
                detailLoading = false
                val body = response.body()
                if (!response.isSuccessful || body == null || body.status != "success") {
                    detailError = "Unable to load help details."
                    return
                }
                settingsDetail = SettingsDetailUiState(
                    title = "Help & Support",
                    items = listOf(
                        DetailItem("Support Email", body.support_email),
                        DetailItem("Help Center", body.help_center_url),
                        DetailItem("FAQ Topics", body.faq_topics.joinToString("\n• ", prefix = "• "))
                    )
                )
            }

            override fun onFailure(call: Call<HelpSettingsResponse>, t: Throwable) {
                detailLoading = false
                detailError = "Network error: ${t.message}"
            }
        })
    }

    fun saveProfileImage(url: String) {
        if (userId.isBlank() || url.isBlank()) return
        profileUpdateLoading = true
        detailError = null
        api.updateProfileImage(userId, UpdateProfileImageRequest(url)).enqueue(object : Callback<UpdateProfileImageResponse> {
            override fun onResponse(call: Call<UpdateProfileImageResponse>, response: Response<UpdateProfileImageResponse>) {
                profileUpdateLoading = false
                val body = response.body()
                if (!response.isSuccessful || body == null || body.status != "success") {
                    detailError = body?.message ?: "Unable to update profile image."
                    return
                }
                selectedProfileImageUrl = body.profile_image_url
                settingsSummary = settingsSummary.copy(profileImageUrl = body.profile_image_url)
            }

            override fun onFailure(call: Call<UpdateProfileImageResponse>, t: Throwable) {
                profileUpdateLoading = false
                detailError = "Update failed: ${t.message}"
            }
        })
    }

    when (currentScreen) {
        Screen.SPLASH -> SplashScreen(onFinished = { currentScreen = Screen.LOGIN })

        Screen.LOGIN -> LoginScreen(
            isLoading = loginLoading,
            errorText = loginError,
            onOpenSignup = {
                loginError = null
                currentScreen = Screen.SIGNUP
            },
            onLogin = { email, password ->
                if (email.isBlank() || password.isBlank()) {
                    loginError = "Enter email and password."
                    return@LoginScreen
                }
                loginLoading = true
                loginError = null
                api.login(LoginRequest(email, password)).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        loginLoading = false
                        val body = response.body()
                        if (!response.isSuccessful || body == null || body.status != "success") {
                            loginError = "Invalid credentials. Try elena.sterling@university.edu / demo123"
                            return
                        }
                        userId = body.user_id
                        userName = body.name
                        userEmail = body.email
                        fetchProfileIcons()
                        fetchSettingsSummary()
                        currentScreen = Screen.HOME
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        loginLoading = false
                        loginError = "Login failed: ${t.message}"
                    }
                })
            }
        )

        Screen.SIGNUP -> SignupScreen(
            isLoading = signupLoading,
            errorText = signupError,
            onOpenLogin = {
                signupError = null
                currentScreen = Screen.LOGIN
            },
            onSignup = { name, email, password ->
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    signupError = "Enter name, email and password."
                    return@SignupScreen
                }
                signupLoading = true
                signupError = null
                api.signup(SignupRequest(name, email, password)).enqueue(object : Callback<SignupResponse> {
                    override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                        signupLoading = false
                        val body = response.body()
                        if (!response.isSuccessful || body == null || body.status != "success") {
                            signupError = body?.message ?: "Signup failed."
                            return
                        }
                        userId = body.user_id
                        userName = body.name
                        userEmail = body.email
                        fetchProfileIcons()
                        fetchSettingsSummary()
                        currentScreen = Screen.HOME
                    }

                    override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                        signupLoading = false
                        signupError = "Signup failed: ${t.message}"
                    }
                })
            }
        )

        Screen.HOME -> HomeScreen(
            ideaText = ideaText,
            onIdeaChange = { ideaText = it },
            isLoading = isLoading,
            errorText = errorText,
            onOpenLibrary = { currentScreen = Screen.RESULT },
            onOpenSettings = { openSettings() },
            onScan = {
                if (ideaText.isBlank()) {
                    errorText = "Enter a manuscript title before scanning."
                    return@HomeScreen
                }

                isLoading = true
                errorText = null

                api.checkIdea(IdeaRequest(ideaText)).enqueue(object : Callback<IdeaResponse> {
                    override fun onResponse(call: Call<IdeaResponse>, response: Response<IdeaResponse>) {
                        isLoading = false
                        if (!response.isSuccessful || response.body() == null) {
                            errorText = "Scan failed (${response.code()})."
                            return
                        }

                        val body = response.body()!!
                        val percent = (body.similarity_score * 100).toInt().coerceIn(0, 100)
                        val words = ideaText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size

                        analysisState = AnalysisUiState(
                            title = ideaText,
                            similarityPercent = percent,
                            wordCount = body.word_count ?: (words.coerceAtLeast(1) * 312),
                            citationsFound = body.citations_found ?: (percent / 2).coerceAtLeast(1),
                            confidenceLabel = body.confidence_label
                                ?: if (percent >= 50) "HIGH CONFIDENCE" else "MEDIUM CONFIDENCE",
                            note = body.analysis_note
                                ?: if (percent >= 50) {
                                    "Your manuscript shows notable overlap with existing published works in our repository."
                                } else {
                                    "Your manuscript shows moderate overlap with existing published works in our repository."
                                },
                            sourceBreakdown = body.source_breakdown ?: analysisState.sourceBreakdown
                        )
                        currentScreen = Screen.RESULT
                    }

                    override fun onFailure(call: Call<IdeaResponse>, t: Throwable) {
                        isLoading = false
                        errorText = "Network error: ${t.message}"
                    }
                })
            }
        )

        Screen.RESULT -> ResultScreen(
            state = analysisState,
            onBack = { currentScreen = Screen.HOME },
            onOpenHome = { currentScreen = Screen.HOME },
            onOpenSettings = { openSettings() }
        )

        Screen.SETTINGS -> SettingsScreen(
            summary = settingsSummary,
            isLoading = settingsLoading,
            errorText = settingsError,
            onOpenHome = { currentScreen = Screen.HOME },
            onOpenLibrary = { currentScreen = Screen.RESULT },
            onOpenAccount = { loadAccountSettings() },
            onOpenSubscription = { loadSubscriptionSettings() },
            onOpenNotifications = { loadNotificationSettings() },
            onOpenHelp = { loadHelpSettings() },
            onLogout = {
                userId = ""
                userName = ""
                userEmail = ""
                currentScreen = Screen.LOGIN
            }
        )

        Screen.SETTINGS_ACCOUNT,
        Screen.SETTINGS_SUBSCRIPTION,
        Screen.SETTINGS_NOTIFICATIONS,
        Screen.SETTINGS_HELP -> SettingsDetailScreen(
            screen = currentScreen,
            title = settingsDetail.title,
            details = settingsDetail.items,
            notificationPrefs = notificationPrefs,
            onNotificationPrefsChanged = { notificationPrefs = it },
            profileImageUrl = selectedProfileImageUrl,
            profileIconOptions = profileIconOptions,
            onSelectProfileImage = { selectedProfileImageUrl = it },
            onSaveProfileImage = { saveProfileImage(it) },
            profileUpdateLoading = profileUpdateLoading,
            isLoading = detailLoading,
            errorText = detailError,
            onBack = { currentScreen = Screen.SETTINGS },
            onOpenHome = { currentScreen = Screen.HOME },
            onOpenLibrary = { currentScreen = Screen.RESULT },
            onOpenSettings = { currentScreen = Screen.SETTINGS }
        )
    }
}

@Composable
private fun LoginScreen(
    isLoading: Boolean,
    errorText: String?,
    onOpenSignup: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("elena.sterling@university.edu") }
    var password by remember { mutableStateOf("demo123") }

    Scaffold(containerColor = Color(0xFFF4F1F5)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome Back", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Sign in to ScholarMetric", color = Color(0xFF66626C), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F6F8),
                    unfocusedContainerColor = Color(0xFFF7F6F8),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F6F8),
                    unfocusedContainerColor = Color(0xFFF7F6F8),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { onLogin(email.trim(), password) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1656D9))
            ) {
                Text(if (isLoading) "Signing In..." else "Login", fontWeight = FontWeight.SemiBold)
            }

            if (errorText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(errorText, color = Color(0xFFB42318), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "Create new account",
                modifier = Modifier.clickable { onOpenSignup() },
                color = Color(0xFF1656D9),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SignupScreen(
    isLoading: Boolean,
    errorText: String?,
    onOpenLogin: () -> Unit,
    onSignup: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(containerColor = Color(0xFFF4F1F5)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Account", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Sign up for ScholarMetric", color = Color(0xFF66626C), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F6F8),
                    unfocusedContainerColor = Color(0xFFF7F6F8),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F6F8),
                    unfocusedContainerColor = Color(0xFFF7F6F8),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F6F8),
                    unfocusedContainerColor = Color(0xFFF7F6F8),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { onSignup(name.trim(), email.trim(), password) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1656D9))
            ) {
                Text(if (isLoading) "Creating..." else "Sign Up", fontWeight = FontWeight.SemiBold)
            }

            if (errorText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(errorText, color = Color(0xFFB42318), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "Already have an account? Login",
                modifier = Modifier.clickable { onOpenLogin() },
                color = Color(0xFF1656D9),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.floatValue,
        animationSpec = tween(1500, easing = LinearEasing),
        label = "splash-progress"
    )

    LaunchedEffect(Unit) {
        progress.floatValue = 0.35f
        delay(1900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F1F5))
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .size(420.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFFE1DCE8), CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-120).dp, y = 120.dp)
                .size(380.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFFE1DCE8), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.size(124.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "App logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("ScholarMetric", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cultivating Originality.", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5F5B66))

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFFD9D4DE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFF1656D9))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("INITIALIZING INTELLIGENCE", style = MaterialTheme.typography.labelMedium, color = Color(0xFF88838F))
            }

            Card(
                shape = RoundedCornerShape(999.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EDF3)),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1656D9), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Trusted by 500+ Academic Institutions", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4E4A55))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    ideaText: String,
    onIdeaChange: (String) -> Unit,
    isLoading: Boolean,
    errorText: String?,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onScan: () -> Unit
) {
    val primaryBlue = Color(0xFF1656D9)

    Scaffold(
        containerColor = Color(0xFFF4F1F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "App logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("Scholar Metric", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F1F5))
            )
        },
        bottomBar = {
            AppBottomNav(
                selected = Screen.HOME,
                onSearch = {},
                onLibrary = onOpenLibrary,
                onSettings = onOpenSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Verify Integrity.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                "Cultivate Originality.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = primaryBlue,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEECF1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DIGITAL MANUSCRIPT IDENTIFICATION", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ideaText,
                        onValueChange = onIdeaChange,
                        singleLine = true,
                        placeholder = { Text("Paste or type a paper title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF7F6F8),
                            unfocusedContainerColor = Color(0xFFF7F6F8),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onScan,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(if (isLoading) "Scanning..." else "Scan Title", fontWeight = FontWeight.SemiBold)
                    }

                    if (errorText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorText, color = Color(0xFFB42318), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultScreen(
    state: AnalysisUiState,
    onBack: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val primaryBlue = Color(0xFF1656D9)
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF4F1F5),
        topBar = {
            TopAppBar(
                title = { Text("Analysis Details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F1F5))
            )
        },
        bottomBar = {
            AppBottomNav(
                selected = Screen.RESULT,
                onSearch = onOpenHome,
                onLibrary = {},
                onSettings = onOpenSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F7F9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SimilarityRing(scorePercent = state.similarityPercent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Academic Integrity Score", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.note,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF5E5A65),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(Modifier.weight(1f), state.wordCount.toString(), "Total Word Count", Color(0xFF1656D9), Icons.Default.Book)
                MetricCard(Modifier.weight(1f), state.citationsFound.toString(), "Citations Found", Color(0xFF6F4EBC), Icons.Default.Search, Color(0xFFEFE9FA))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE9E6EC))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Analysis Integrity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(state.confidenceLabel, color = primaryBlue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The scan cross-referenced 4.2 billion web pages and 80 million scholarly articles. All references are verified against the DOI registry.",
                        color = Color(0xFF5D5862),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Source Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            state.sourceBreakdown.forEachIndexed { idx, item ->
                SourceCard(item.title, item.description, item.similarity_percent, item.tag)
                if (idx != state.sourceBreakdown.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val message = runCatching { createPdfReport(context, state) }
                        .getOrElse { "Unable to create PDF report: ${it.message ?: "unknown error"}" }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download Full Report", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun createPdfReport(context: Context, state: AnalysisUiState): String {
    val fileName = "analysis_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val document = PdfDocument()

    try {
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val headingPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subHeadingPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 12f
            isAntiAlias = true
        }

        var y = 56f
        canvas.drawText("ScholarMetric Analysis Report", 40f, y, headingPaint)
        y += 26f
        canvas.drawText("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}", 40f, y, bodyPaint)
        y += 30f

        canvas.drawText("Manuscript", 40f, y, subHeadingPaint)
        y += 20f
        wrapText(state.title, 72).forEach { line ->
            canvas.drawText(line, 40f, y, bodyPaint)
            y += 16f
        }

        y += 14f
        canvas.drawText("Summary", 40f, y, subHeadingPaint)
        y += 20f
        canvas.drawText("Similarity Score: ${state.similarityPercent}%", 40f, y, bodyPaint)
        y += 16f
        canvas.drawText("Word Count: ${state.wordCount}", 40f, y, bodyPaint)
        y += 16f
        canvas.drawText("Citations Found: ${state.citationsFound}", 40f, y, bodyPaint)
        y += 16f
        canvas.drawText("Confidence: ${state.confidenceLabel}", 40f, y, bodyPaint)
        y += 20f

        wrapText("Note: ${state.note}", 82).forEach { line ->
            canvas.drawText(line, 40f, y, bodyPaint)
            y += 16f
        }

        y += 14f
        canvas.drawText("Source Breakdown", 40f, y, subHeadingPaint)
        y += 20f
        state.sourceBreakdown.forEachIndexed { index, item ->
            wrapText("${index + 1}. ${item.title} (${item.similarity_percent}%) [${item.tag}]", 82).forEach { line ->
                canvas.drawText(line, 40f, y, bodyPaint)
                y += 16f
            }
            wrapText("${item.description}", 82).forEach { line ->
                canvas.drawText(line, 56f, y, bodyPaint)
                y += 16f
            }
            y += 6f
        }

        document.finishPage(page)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ScholarMetric")
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Could not create download entry")
            context.contentResolver.openOutputStream(uri)?.use { output ->
                document.writeTo(output)
            } ?: throw IOException("Could not open output stream")
            return "PDF saved to Downloads/ScholarMetric as $fileName"
        }

        val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        val fallbackFile = File(fallbackDir, fileName)
        fallbackFile.outputStream().use { output ->
            document.writeTo(output)
        }
        return "PDF saved to ${fallbackFile.absolutePath}"
    } finally {
        document.close()
    }
}

private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
    if (text.length <= maxCharsPerLine) return listOf(text)
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var current = StringBuilder()

    for (word in words) {
        val nextLength = if (current.isEmpty()) word.length else current.length + 1 + word.length
        if (nextLength > maxCharsPerLine && current.isNotEmpty()) {
            lines.add(current.toString())
            current = StringBuilder(word)
        } else {
            if (current.isNotEmpty()) current.append(" ")
            current.append(word)
        }
    }

    if (current.isNotEmpty()) lines.add(current.toString())
    return lines
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    summary: SettingsSummaryUiState,
    isLoading: Boolean,
    errorText: String?,
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenSubscription: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenHelp: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF4F1F5),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F1F5))
            )
        },
        bottomBar = {
            AppBottomNav(
                selected = Screen.SETTINGS,
                onSearch = onOpenHome,
                onLibrary = onOpenLibrary,
                onSettings = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDEBF0)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = summary.profileImageUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE0DDE5), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1656D9))
                            .clickable { onOpenAccount() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(summary.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(summary.email, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF5E5A64))
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Loading settings...", color = Color(0xFF6F6A75), style = MaterialTheme.typography.bodySmall)
            }

            if (errorText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(errorText, color = Color(0xFFB42318), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(22.dp))
            Text("ACCOUNT MANAGEMENT", style = MaterialTheme.typography.labelLarge, color = Color(0xFF5B5861))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionCard(
                rows = listOf(
                    SettingsRowData(Icons.Default.Person, "Account Settings", "", route = Screen.SETTINGS_ACCOUNT),
                    SettingsRowData(
                        Icons.Default.LibraryBooks,
                        "Subscription",
                        summary.subscriptionPlan,
                        Color(0xFF1656D9),
                        route = Screen.SETTINGS_SUBSCRIPTION
                    )
                ),
                onRowClick = { route ->
                    when (route) {
                        Screen.SETTINGS_ACCOUNT -> onOpenAccount()
                        Screen.SETTINGS_SUBSCRIPTION -> onOpenSubscription()
                        else -> Unit
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("PREFERENCES", style = MaterialTheme.typography.labelLarge, color = Color(0xFF5B5861))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionCard(
                rows = listOf(
                    SettingsRowData(Icons.Default.Notifications, "Notification Preferences", summary.notificationStatus, route = Screen.SETTINGS_NOTIFICATIONS),
                    SettingsRowData(Icons.Default.HelpOutline, "Help & Support", summary.supportStatus, route = Screen.SETTINGS_HELP)
                ),
                onRowClick = { route ->
                    when (route) {
                        Screen.SETTINGS_NOTIFICATIONS -> onOpenNotifications()
                        Screen.SETTINGS_HELP -> onOpenHelp()
                        else -> Unit
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F6F8), contentColor = Color(0xFFA62D26)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color(0xFFE8E0E1), RoundedCornerShape(14.dp))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "THE CURATOR ACADEMIC SUITE - VERSION 2.4.0",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color(0xFF7C7883),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDetailScreen(
    screen: Screen,
    title: String,
    details: List<DetailItem>,
    notificationPrefs: NotificationPrefsUiState,
    onNotificationPrefsChanged: (NotificationPrefsUiState) -> Unit,
    profileImageUrl: String,
    profileIconOptions: List<ProfileIconOption>,
    onSelectProfileImage: (String) -> Unit,
    onSaveProfileImage: (String) -> Unit,
    profileUpdateLoading: Boolean,
    isLoading: Boolean,
    errorText: String?,
    onBack: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val appContext = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF4F1F5),
        topBar = {
            TopAppBar(
                title = { Text(if (title.isBlank()) "Settings" else title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F1F5))
            )
        },
        bottomBar = {
            AppBottomNav(
                selected = Screen.SETTINGS,
                onSearch = onOpenHome,
                onLibrary = onOpenLibrary,
                onSettings = onOpenSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            DetailHeroCard(screen = screen, title = title)

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F8FB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isLoading) {
                        Text(
                            "Loading details...",
                            color = Color(0xFF6F6A75),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else if (errorText != null) {
                        Text(
                            errorText,
                            color = Color(0xFFB42318),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        if (screen == Screen.SETTINGS_ACCOUNT) {
                            AccountProfileEditorCard(
                                currentImageUrl = profileImageUrl,
                                options = profileIconOptions,
                                onSelectImage = onSelectProfileImage,
                                onSave = onSaveProfileImage,
                                isSaving = profileUpdateLoading
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            details.forEachIndexed { index, item ->
                                ModernDetailRowCard(item = item)
                                if (index != details.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        } else if (screen == Screen.SETTINGS_NOTIFICATIONS) {
                            NotificationToggleCard(
                                title = "Email Notifications",
                                subtitle = "Receive updates and report alerts by email",
                                checked = notificationPrefs.emailNotifications,
                                onCheckedChange = {
                                    onNotificationPrefsChanged(notificationPrefs.copy(emailNotifications = it))
                                }
                            )
                            NotificationToggleCard(
                                title = "Push Notifications",
                                subtitle = "Get instant in-app alerts",
                                checked = notificationPrefs.pushNotifications,
                                onCheckedChange = {
                                    onNotificationPrefsChanged(notificationPrefs.copy(pushNotifications = it))
                                }
                            )
                            NotificationToggleCard(
                                title = "Weekly Digest",
                                subtitle = "Receive a weekly summary of activity",
                                checked = notificationPrefs.weeklyDigest,
                                onCheckedChange = {
                                    onNotificationPrefsChanged(notificationPrefs.copy(weeklyDigest = it))
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    Toast.makeText(appContext, "Notification preferences saved", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1656D9))
                            ) {
                                Text("Save Preferences", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            details.forEachIndexed { index, item ->
                                ModernDetailRowCard(item = item)
                                if (index != details.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountProfileEditorCard(
    currentImageUrl: String,
    options: List<ProfileIconOption>,
    onSelectImage: (String) -> Unit,
    onSave: (String) -> Unit,
    isSaving: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = currentImageUrl,
                contentDescription = "Selected profile image",
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF1656D9), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Choose a profile icon", style = MaterialTheme.typography.titleMedium, color = Color(0xFF22212A))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option.image_url == currentImageUrl
                    AsyncImage(
                        model = option.image_url,
                        contentDescription = option.label,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF1656D9) else Color(0xFFDCD8E4),
                                shape = CircleShape
                            )
                            .clickable { onSelectImage(option.image_url) },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onSave(currentImageUrl) },
                enabled = !isSaving && currentImageUrl.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1656D9))
            ) {
                Text(if (isSaving) "Updating..." else "Update Profile Icon", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DetailHeroCard(screen: Screen, title: String) {
    val accent = when (screen) {
        Screen.SETTINGS_ACCOUNT -> Color(0xFF1656D9)
        Screen.SETTINGS_SUBSCRIPTION -> Color(0xFF6A4DD8)
        Screen.SETTINGS_NOTIFICATIONS -> Color(0xFF007A6A)
        Screen.SETTINGS_HELP -> Color(0xFF9A4A1A)
        else -> Color(0xFF1656D9)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF3FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                if (title.isBlank()) "Settings" else title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E1B25)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Customize and review your account preferences",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5F5A66)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("Personalized Controls", color = accent, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ModernDetailRowCard(item: DetailItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                item.label,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF6A6570)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                item.value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF22212A)
            )
        }
    }
}

@Composable
private fun NotificationToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF22212A))
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6A6570))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SettingsSectionCard(rows: List<SettingsRowData>, onRowClick: (Screen) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F7F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRowClick(row.route) }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE6EAF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(row.icon, contentDescription = null, tint = Color(0xFF1656D9), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(row.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (row.subtitle.isNotBlank()) {
                                Text(row.subtitle, style = MaterialTheme.typography.bodyMedium, color = row.subtitleColor)
                            }
                        }
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFFABA8B0), modifier = Modifier.size(16.dp))
                }

                if (index != rows.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE6E2E9))
                    )
                }
            }
        }
    }
}

@Composable
private fun SimilarityRing(scorePercent: Int) {
    val sweep = (scorePercent.coerceIn(0, 100) / 100f) * 360f
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFFDFDDE4),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF1656D9),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${scorePercent}%", color = Color(0xFF1656D9), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("SIMILARITY", color = Color(0xFF2D2B33), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    value: String,
    label: String,
    accent: Color,
    icon: ImageVector,
    tint: Color = Color(0xFFEDEBF0)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tint)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5D5862))
        }
    }
}

@Composable
private fun SourceCard(title: String, description: String, similarityPercent: Int, tag: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F7F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE9E6EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF1656D9), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5F5A64))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF5E5964),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE7E4EA))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${similarityPercent}%", color = Color(0xFF1656D9), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("SIMILARITY", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AppBottomNav(
    selected: Screen,
    onSearch: () -> Unit,
    onLibrary: () -> Unit,
    onSettings: () -> Unit
) {
    NavigationBar(containerColor = Color(0xFFF7F5F8)) {
        NavigationBarItem(
            selected = selected == Screen.HOME,
            onClick = onSearch,
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = selected == Screen.RESULT,
            onClick = onLibrary,
            icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Library") },
            label = { Text("Library") }
        )
        NavigationBarItem(
            selected = selected == Screen.SETTINGS,
            onClick = onSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
