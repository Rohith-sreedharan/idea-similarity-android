package com.example.ideasimilarityapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class IdeaRequest(val text: String)

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val token: String,
    val user_id: String,
    val name: String,
    val email: String
)

data class SignupResponse(
    val status: String,
    val token: String,
    val user_id: String,
    val name: String,
    val email: String,
    val message: String
)

data class SourceBreakdownItem(
    val title: String,
    val description: String,
    val similarity_percent: Int,
    val tag: String
)

data class IdeaResponse(
    val status: String,
    val similarity_score: Double,
    val most_similar_idea: String?,
    val word_count: Int? = null,
    val citations_found: Int? = null,
    val confidence_label: String? = null,
    val analysis_note: String? = null,
    val source_breakdown: List<SourceBreakdownItem>? = null
)

data class SettingsSummaryResponse(
    val status: String,
    val user_id: String,
    val name: String,
    val email: String,
    val profile_image_url: String,
    val subscription_plan: String,
    val notification_status: String,
    val support_status: String
)

data class AccountSettingsResponse(
    val status: String,
    val full_name: String,
    val email: String,
    val department: String,
    val institution: String,
    val profile_image_url: String
)

data class ProfileIconOption(
    val id: String,
    val label: String,
    val image_url: String
)

data class ProfileIconsResponse(
    val status: String,
    val options: List<ProfileIconOption>
)

data class UpdateProfileImageRequest(
    val profile_image_url: String
)

data class UpdateProfileImageResponse(
    val status: String,
    val message: String,
    val profile_image_url: String
)

data class SubscriptionSettingsResponse(
    val status: String,
    val plan_name: String,
    val renewal_date: String,
    val billing_cycle: String,
    val features: List<String>
)

data class NotificationSettingsResponse(
    val status: String,
    val email_notifications: Boolean,
    val push_notifications: Boolean,
    val weekly_digest: Boolean
)

data class HelpSettingsResponse(
    val status: String,
    val support_email: String,
    val help_center_url: String,
    val faq_topics: List<String>
)

interface ApiService {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/signup")
    fun signup(@Body request: SignupRequest): Call<SignupResponse>

    @POST("check")
    fun checkIdea(@Body request: IdeaRequest): Call<IdeaResponse>

    @GET("settings/summary/{userId}")
    fun getSettingsSummary(@Path("userId") userId: String): Call<SettingsSummaryResponse>

    @GET("settings/account/{userId}")
    fun getAccountSettings(@Path("userId") userId: String): Call<AccountSettingsResponse>

    @GET("settings/profile-icons")
    fun getProfileIcons(): Call<ProfileIconsResponse>

    @PUT("settings/account/profile-image/{userId}")
    fun updateProfileImage(
        @Path("userId") userId: String,
        @Body request: UpdateProfileImageRequest
    ): Call<UpdateProfileImageResponse>

    @GET("settings/subscription/{userId}")
    fun getSubscriptionSettings(@Path("userId") userId: String): Call<SubscriptionSettingsResponse>

    @GET("settings/notifications/{userId}")
    fun getNotificationSettings(@Path("userId") userId: String): Call<NotificationSettingsResponse>

    @GET("settings/help/{userId}")
    fun getHelpSettings(@Path("userId") userId: String): Call<HelpSettingsResponse>
}
