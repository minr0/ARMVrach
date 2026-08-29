// ui/chat/ChatFragment.kt
package kz.arm.doctor.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.arm.doctor.databinding.FragmentChatBinding
import kz.arm.doctor.ui.main.MainActivity

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatAdapter
    private var pollJob: Job? = null
    private var myUserId: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        myUserId = activity.sessionManager.getUserId()

        adapter = ChatAdapter(myUserId)
        binding.recyclerView.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }

        loadMessages()
        startPolling()
    }

    private fun loadMessages(scrollToBottom: Boolean = true) {
        val repo = (requireActivity() as MainActivity).repository
        lifecycleScope.launch {
            repo.getChatMessages()
                .onSuccess { messages ->
                    adapter.submitList(messages)
                    if (scrollToBottom && messages.isNotEmpty()) {
                        binding.recyclerView.post {
                            binding.recyclerView.smoothScrollToPosition(messages.size - 1)
                        }
                    }
                    binding.emptyView.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
                }
                .onFailure { /* silent refresh — already shown at first load */ }
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isBlank()) return

        binding.btnSend.isEnabled = false
        val repo = (requireActivity() as MainActivity).repository

        lifecycleScope.launch {
            repo.sendMessage(text)
                .onSuccess {
                    binding.etMessage.setText("")
                    binding.btnSend.isEnabled = true
                    loadMessages()
                }
                .onFailure {
                    binding.btnSend.isEnabled = true
                    Snackbar.make(binding.root, it.message ?: "Не удалось отправить", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    /** Poll every 10 seconds for new messages (simple polling, no WebSocket needed). */
    private fun startPolling() {
        pollJob = lifecycleScope.launch {
            while (isActive) {
                delay(10_000)
                if (_binding != null) loadMessages(scrollToBottom = false)
            }
        }
    }

    override fun onDestroyView() {
        pollJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
