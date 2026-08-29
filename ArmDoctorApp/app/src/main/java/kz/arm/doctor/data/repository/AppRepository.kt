// data/repository/AppRepository.kt
package kz.arm.doctor.data.repository

import kz.arm.doctor.data.models.*
import kz.arm.doctor.data.network.ApiService

/**
 * Repository wraps all API calls and returns sealed Results.
 * ViewModels call these suspend functions from their coroutine scope.
 */
class AppRepository(private val api: ApiService) {

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun login(iin: String, password: String): Result<LoginResponse> = runCatching {
        val response = api.login(LoginRequest(iin, password))
        response.body() ?: error("Ошибка сервера: ${response.code()}")
    }

    // ── Appointments ──────────────────────────────────────────────────────────

    suspend fun getAppointments(): Result<List<Appointment>> = runCatching {
        val response = api.getAppointments()
        response.body() ?: error("Ошибка: ${response.code()}")
    }

    suspend fun createAppointment(datetime: String, complaint: String): Result<Appointment> = runCatching {
        val response = api.createAppointment(AppointmentCreateRequest(datetime, complaint))
        response.body() ?: error("Ошибка: ${response.code()}")
    }

    // ── Test Results ──────────────────────────────────────────────────────────

    suspend fun getTestResults(): Result<List<TestResult>> = runCatching {
        val response = api.getTestResults()
        response.body() ?: error("Ошибка: ${response.code()}")
    }

    // ── Chat ──────────────────────────────────────────────────────────────────

    suspend fun getChatMessages(): Result<List<ChatMessage>> = runCatching {
        val response = api.getChatMessages()
        response.body() ?: error("Ошибка: ${response.code()}")
    }

    suspend fun sendMessage(text: String): Result<ChatMessage> = runCatching {
        val response = api.sendMessage(ChatMessageCreateRequest(text))
        response.body() ?: error("Ошибка: ${response.code()}")
    }
}
