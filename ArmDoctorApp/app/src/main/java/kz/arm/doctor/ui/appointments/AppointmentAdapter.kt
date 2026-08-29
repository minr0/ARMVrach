// ui/appointments/AppointmentAdapter.kt
package kz.arm.doctor.ui.appointments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kz.arm.doctor.R
import kz.arm.doctor.data.models.Appointment
import kz.arm.doctor.databinding.ItemAppointmentBinding

class AppointmentAdapter : ListAdapter<Appointment, AppointmentAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemAppointmentBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Appointment) {
            b.tvDateTime.text = formatDateTime(item.datetime)
            b.tvComplaint.text = item.complaint

            val (statusText, colorRes) = when (item.status) {
                "waiting" -> "Ожидание" to R.color.status_waiting
                "accepted" -> "Принят" to R.color.status_ok
                "done" -> "Завершён" to R.color.status_neutral
                "cancelled" -> "Отменён" to R.color.status_bad
                else -> item.status to R.color.status_neutral
            }
            b.chipStatus.text = statusText
            b.chipStatus.setChipBackgroundColorResource(colorRes)

            if (item.doctorNote.isNotBlank()) {
                b.tvDoctorNote.visibility = android.view.View.VISIBLE
                b.tvDoctorNote.text = "Заметка врача: ${item.doctorNote}"
            } else {
                b.tvDoctorNote.visibility = android.view.View.GONE
            }
        }

        /** "2025-02-20T10:00:00" → "20.02.2025, 10:00" */
        private fun formatDateTime(dt: String): String {
            return try {
                val dateTime = dt.replace("T", " ").substring(0, 16)
                val (date, time) = dateTime.split(" ")
                val (y, m, d) = date.split("-")
                "$d.$m.$y, $time"
            } catch (e: Exception) { dt }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Appointment>() {
        override fun areItemsTheSame(a: Appointment, b: Appointment) = a.id == b.id
        override fun areContentsTheSame(a: Appointment, b: Appointment) = a == b
    }
}
