package edu.isil.proynov02

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import edu.isil.proynov02.R

// Declara la clase LoginActivity que hereda de AppCompatActivity, proporcionando compatibilidad con versiones antiguas de Android
class LoginActivity : AppCompatActivity() {

    // Declaración de variables para FirebaseAuth y los elementos de la interfaz de usuario
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    // Método que se ejecuta cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout de la actividad desde un archivo XML
        setContentView(R.layout.activity_login)

        // Inicializa la instancia de FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Asocia los elementos de la interfaz de usuario con las variables declaradas
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Configura un listener para el botón de inicio de sesión
        loginButton.setOnClickListener {
            // Obtiene el texto introducido en los campos de correo electrónico y contraseña
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            // Llama a la función signIn para autenticar al usuario
            signIn(email, password)
        }
    }

    // Función para autenticar al usuario con correo electrónico y contraseña
    private fun signIn(email: String, password: String) {
        // Utiliza FirebaseAuth para iniciar sesión con el correo electrónico y la contraseña proporcionados
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Si el inicio de sesión es exitoso, se registra un mensaje en el log
                    Log.d("LoginActivity", "signInWithEmail:success")
                    // Obtiene el usuario autenticado
                    val user = auth.currentUser
                    // Inicia la actividad principal y finaliza la actividad de inicio de sesión
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Si el inicio de sesión falla, se registra un mensaje de error en el log
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    // Aquí puedes actualizar la UI con un mensaje de error para el usuario
                }
            }
    }
}