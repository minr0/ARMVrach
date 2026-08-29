// data/models/Models.kt
package kz.arm.doctor.data.models

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val iin: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    val iin: String
)

// ── Appointment ───────────────────────────────────────────────────────────────

data class Appointment(
    val id: Int,
    val patient: Int,
    @SerializedName("patient_name") val patientName: String,
    val datetime: String,       // ISO 8601: "2025-02-20T10:00:00"
    val status: String,         // waiting | accepted | done | cancelled
    val complaint: String,
    @SerializedName("doctor_note") val doctorNote: String,
    @SerializedName("created_at") val createdAt: String
)

data class AppointmentCreateRequest(
    val datetime: String,
    val complaint: String
)

// ── TestResult ────────────────────────────────────────────────────────────────

data class TestResult(
    val id: Int,
    val patient: Int,
    val indicator: String,
    val value: String,
    val unit: String,
    val norm: String,
    val date: String,           // "2025-02-19"
    @SerializedName("added_by") val addedBy: Int?
)

// ── HealthLog ─────────────────────────────────────────────────────────────────

data class HealthLog(
    val id: Int,
    val patient: Int,
    val date: String,
    val weight: Double?,
    val systolic: Int?,
    val diastolic: Int?,
    @SerializedName("blood_sugar") val bloodSugar: Double?,
    val note: String,
    @SerializedName("created_at") val createdAt: String
)

// ── ChatMessage ───────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: Int,
    val patient: Int,
    val sender: Int,
    @SerializedName("sender_name") val senderName: String,
    val text: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("is_from_doctor") val isFromDoctor: Boolean
)

data class ChatMessageCreateRequest(
    val text: String
)
