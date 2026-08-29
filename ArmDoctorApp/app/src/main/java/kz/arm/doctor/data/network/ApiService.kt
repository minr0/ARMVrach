// data/network/ApiService.kt
package kz.arm.doctor.data.network

import kz.arm.doctor.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("api/auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ── Appointments ──────────────────────────────────────────────────────────

    @GET("api/appointments/")
    suspend fun getAppointments(): Response<List<Appointment>>

    @POST("api/appointments/")
    suspend fun createAppointment(@Body request: AppointmentCreateRequest): Response<Appointment>

    // ── Test Results ──────────────────────────────────────────────────────────

    @GET("api/test-results/")
    suspend fun getTestResults(): Response<List<TestResult>>

    // ── Health Logs ───────────────────────────────────────────────────────────

    @GET("api/health-logs/")
    suspend fun getHealthLogs(): Response<List<HealthLog>>

    @POST("api/health-logs/")
    suspend fun createHealthLog(@Body request: HealthLog): Response<HealthLog>

    // ── Chat ──────────────────────────────────────────────────────────────────

    @GET("api/chat/")
    suspend fun getChatMessages(): Response<List<ChatMessage>>

    @POST("api/chat/")
    suspend fun sendMessage(@Body request: ChatMessageCreateRequest): Response<ChatMessage>
}
