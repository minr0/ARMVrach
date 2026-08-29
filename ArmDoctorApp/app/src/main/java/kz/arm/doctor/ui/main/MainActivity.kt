// ui/main/MainActivity.kt
package kz.arm.doctor.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kz.arm.doctor.R
import kz.arm.doctor.data.local.SessionManager
import kz.arm.doctor.data.network.RetrofitClient
import kz.arm.doctor.data.repository.AppRepository
import kz.arm.doctor.databinding.ActivityMainBinding
import kz.arm.doctor.ui.appointments.AppointmentsFragment
import kz.arm.doctor.ui.auth.LoginActivity
import kz.arm.doctor.ui.chat.ChatFragment
import kz.arm.doctor.ui.tests.TestResultsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var sessionManager: SessionManager
    lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        repository = AppRepository(RetrofitClient.create(sessionManager))

        setupToolbar()
        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(TestResultsFragment(), R.id.nav_tests)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = "АРМ Врача"
        binding.toolbar.subtitle = sessionManager.getFullName()

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                showLogoutDialog()
                true
            } else false
        }
        binding.toolbar.inflateMenu(R.menu.menu_main)
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_tests -> TestResultsFragment()
                R.id.nav_appointments -> AppointmentsFragment()
                R.id.nav_chat -> ChatFragment()
                else -> return@setOnItemSelectedListener false
            }
            loadFragment(fragment, item.itemId)
            true
        }
    }

    private fun loadFragment(fragment: Fragment, itemId: Int) {
        // Update toolbar title based on section
        binding.toolbar.title = when (itemId) {
            R.id.nav_tests -> "Мои анализы"
            R.id.nav_appointments -> "Записи"
            R.id.nav_chat -> "Чат с врачом"
            else -> "АРМ Врача"
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Выйти из аккаунта?")
            .setMessage("Вы будете перенаправлены на экран входа.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Выйти") { _, _ ->
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .show()
    }
}
