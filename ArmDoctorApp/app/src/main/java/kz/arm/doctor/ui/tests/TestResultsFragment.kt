// ui/tests/TestResultsFragment.kt
package kz.arm.doctor.ui.tests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kz.arm.doctor.databinding.FragmentTestResultsBinding
import kz.arm.doctor.ui.main.MainActivity

class TestResultsFragment : Fragment() {

    private var _binding: FragmentTestResultsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TestResultAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTestResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TestResultAdapter()
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setColorSchemeResources(kz.arm.doctor.R.color.primary_green)
        binding.swipeRefresh.setOnRefreshListener { loadData() }

        loadData()
    }

    private fun loadData() {
        val repository = (requireActivity() as MainActivity).repository
        binding.swipeRefresh.isRefreshing = true
        binding.emptyView.visibility = View.GONE

        lifecycleScope.launch {
            repository.getTestResults()
                .onSuccess { results ->
                    binding.swipeRefresh.isRefreshing = false
                    if (results.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                    } else {
                        adapter.submitList(results)
                    }
                }
                .onFailure { error ->
                    binding.swipeRefresh.isRefreshing = false
                    Snackbar.make(binding.root, error.message ?: "Ошибка загрузки", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
