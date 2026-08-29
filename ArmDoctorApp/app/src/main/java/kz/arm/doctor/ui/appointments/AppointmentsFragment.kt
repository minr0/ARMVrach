// ui/appointments/AppointmentsFragment.kt
package kz.arm.doctor.ui.appointments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kz.arm.doctor.databinding.DialogCreateAppointmentBinding
import kz.arm.doctor.databinding.FragmentAppointmentsBinding
import kz.arm.doctor.ui.main.MainActivity
import java.util.Calendar

class AppointmentsFragment : Fragment() {

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AppointmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppointmentAdapter()
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setColorSchemeResources(kz.arm.doctor.R.color.primary_green)
        binding.swipeRefresh.setOnRefreshListener { loadData() }

        binding.fabCreate.setOnClickListener { showCreateDialog() }

        loadData()
    }

    private fun loadData() {
        val repo = (requireActivity() as MainActivity).repository
        binding.swipeRefresh.isRefreshing = true
        binding.emptyView.visibility = View.GONE

        lifecycleScope.launch {
            repo.getAppointments()
                .onSuccess { list ->
                    binding.swipeRefresh.isRefreshing = false
                    if (list.isEmpty()) binding.emptyView.visibility = View.VISIBLE
                    else adapter.submitList(list.sortedByDescending { it.datetime })
                }
                .onFailure {
                    binding.swipeRefresh.isRefreshing = false
                    Snackbar.make(binding.root, it.message ?: "Ошибка", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun showCreateDialog() {
        val dialogBinding = DialogCreateAppointmentBinding.inflate(layoutInflater)
        var selectedDateTime = ""

        dialogBinding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(),
                { _, year, month, day ->
                    TimePickerDialog(requireContext(), { _, hour, minute ->
                        selectedDateTime = "%04d-%02d-%02dT%02d:%02d:00".format(year, month + 1, day, hour, minute)
                        dialogBinding.tvSelectedDateTime.text =
                            "%02d.%02d.%04d %02d:%02d".format(day, month + 1, year, hour, minute)
                        dialogBinding.tvSelectedDateTime.visibility = View.VISIBLE
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).also { it.datePicker.minDate = System.currentTimeMillis() }.show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Записаться на приём")
            .setView(dialogBinding.root)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Записаться") { _, _ ->
                val complaint = dialogBinding.etComplaint.text.toString().trim()
                if (complaint.isBlank() || selectedDateTime.isBlank()) {
                    Snackbar.make(binding.root, "Заполните все поля", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                createAppointment(selectedDateTime, complaint)
            }
            .show()
    }

    private fun createAppointment(datetime: String, complaint: String) {
        val repo = (requireActivity() as MainActivity).repository
        lifecycleScope.launch {
            repo.createAppointment(datetime, complaint)
                .onSuccess {
                    Snackbar.make(binding.root, "Запись создана!", Snackbar.LENGTH_SHORT).show()
                    loadData()
                }
                .onFailure {
                    Snackbar.make(binding.root, it.message ?: "Ошибка", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
