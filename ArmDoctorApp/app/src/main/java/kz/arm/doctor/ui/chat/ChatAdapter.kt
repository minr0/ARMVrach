// ui/chat/ChatAdapter.kt
package kz.arm.doctor.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kz.arm.doctor.data.models.ChatMessage
import kz.arm.doctor.databinding.ItemChatDoctorBinding
import kz.arm.doctor.databinding.ItemChatPatientBinding

class ChatAdapter(private val myUserId: Int) :
    ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_PATIENT = 0
        private const val TYPE_DOCTOR = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sender == myUserId) TYPE_PATIENT else TYPE_DOCTOR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_PATIENT) {
            PatientViewHolder(ItemChatPatientBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            DoctorViewHolder(ItemChatDoctorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is PatientViewHolder -> holder.bind(msg)
            is DoctorViewHolder -> holder.bind(msg)
        }
    }

    inner class PatientViewHolder(private val b: ItemChatPatientBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: ChatMessage) {
            b.tvMessage.text = msg.text
            b.tvTime.text = formatTime(msg.createdAt)
            b.ivRead.visibility = if (msg.isRead) View.VISIBLE else View.INVISIBLE
        }
    }

    inner class DoctorViewHolder(private val b: ItemChatDoctorBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: ChatMessage) {
            b.tvMessage.text = msg.text
            b.tvTime.text = formatTime(msg.createdAt)
            b.tvSenderName.text = msg.senderName
        }
    }

    /** "2025-02-20T14:35:00.123456Z" → "14:35" */
    private fun formatTime(dt: String): String {
        return try {
            val t = dt.replace("T", " ").substring(11, 16)
            t
        } catch (e: Exception) { "" }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.id == b.id
        override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
    }
}
