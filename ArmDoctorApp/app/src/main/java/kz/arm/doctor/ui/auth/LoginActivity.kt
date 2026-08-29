// ui/auth/LoginActivity.kt
package kz.arm.doctor.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kz.arm.doctor.data.local.SessionManager
import kz.arm.doctor.data.network.RetrofitClient
import kz.arm.doctor.data.repository.AppRepository
import kz.arm.doctor.databinding.ActivityLoginBinding
import kz.arm.doctor.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        repository = AppRepository(RetrofitClient.create(sessionManager))

        // Already logged in → skip login screen
        if (sessionManager.isLoggedIn()) {
            goToMain()
            return
        }

        binding.btnLogin.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        val iin = binding.etIin.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validation
        if (iin.length != 12 || !iin.all { it.isDigit() }) {
            binding.tilIin.error = "ИИН должен содержать 12 цифр"
            return
        }
        binding.tilIin.error = null

        if (password.isBlank()) {
            binding.tilPassword.error = "Введите пароль"
            return
        }
        binding.tilPassword.error = null

        setLoading(true)

        lifecycleScope.launch {
            repository.login(iin, password)
                .onSuccess { response ->
                    sessionManager.saveSession(
                        token = response.token,
                        userId = response.userId,
                        fullName = response.fullName,
                        iin = response.iin
                    )
                    goToMain()
                }
                .onFailure { error ->
                    setLoading(false)
                    val message = when {
                        error.message?.contains("404") == true -> "Пациент с таким ИИН не найден"
                        error.message?.contains("401") == true -> "Неверный пароль"
                        error.message?.contains("Unable to resolve") == true -> "Нет подключения к интернету"
                        else -> error.message ?: "Произошла ошибка"
                    }
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (loading) "" else "Войти"
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
