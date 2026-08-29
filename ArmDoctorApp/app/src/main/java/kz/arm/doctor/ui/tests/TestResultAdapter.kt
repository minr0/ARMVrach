// ui/tests/TestResultAdapter.kt
package kz.arm.doctor.ui.tests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kz.arm.doctor.data.models.TestResult
import kz.arm.doctor.databinding.ItemTestResultBinding

class TestResultAdapter : ListAdapter<TestResult, TestResultAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTestResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemTestResultBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: TestResult) {
            b.tvIndicator.text = item.indicator
            b.valueChip.text = "${item.value} ${item.unit}".trim()
            b.tvNorm.text = if (item.norm.isNotBlank()) "Норма: ${item.norm}" else "Норма не указана"
            b.tvDate.text = formatDate(item.date)

            // Color indicator: if value is numeric, compare with norm (simplified)
            val isNormal = isValueInNorm(item.value, item.norm)
            val colorRes = when {
                item.norm.isBlank() -> kz.arm.doctor.R.color.status_neutral
                isNormal == true -> kz.arm.doctor.R.color.status_ok
                isNormal == false -> kz.arm.doctor.R.color.status_bad
                else -> kz.arm.doctor.R.color.status_neutral
            }
            b.statusDot.setBackgroundResource(colorRes)
            b.valueChip.setChipBackgroundColorResource(colorRes)
        }

        /** Tries to parse "3.9–6.1" or "120/80" norm and check if value falls in range. */
        private fun isValueInNorm(value: String, norm: String): Boolean? {
            if (norm.isBlank()) return null
            return try {
                val v = value.replace(",", ".").toDoubleOrNull() ?: return null
                // Handle range like "3.9–6.1" or "3.9-6.1"
                val parts = norm.replace("–", "-").replace(",", ".").split("-")
                if (parts.size == 2) {
                    val lo = parts[0].trim().toDoubleOrNull() ?: return null
                    val hi = parts[1].trim().toDoubleOrNull() ?: return null
                    v in lo..hi
                } else null
            } catch (e: Exception) {
                null
            }
        }

        private fun formatDate(date: String): String {
            // "2025-02-19" → "19.02.2025"
            return try {
                val parts = date.split("-")
                if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else date
            } catch (e: Exception) { date }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TestResult>() {
        override fun areItemsTheSame(a: TestResult, b: TestResult) = a.id == b.id
        override fun areContentsTheSame(a: TestResult, b: TestResult) = a == b
    }
}
