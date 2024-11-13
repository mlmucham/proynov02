package edu.isil.proynov02


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import edu.isil.proynov02.AdaptadorChat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

import android.Manifest
import android.media.MediaPlayer
import com.google.firebase.FirebaseApp
import edu.isil.proynov02.R


class MainActivity : AppCompatActivity() {

    // Declaración de variables para las vistas y Firebase
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var attachButton: ImageButton
    private lateinit var recordButton: ImageButton
    private lateinit var recyclerView: RecyclerView

    private lateinit var chatAdapter: AdaptadorChat
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var chatList: MutableList<ChatMessage>

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private val PICK_IMAGE_REQUEST = 1
    private val PICK_AUDIO_REQUEST = 2
    private var audioUri: Uri? = null
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String = ""

    // Método que se ejecuta cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Inicializar vistas
        profileImageView = findViewById(R.id.imagen_perfil_chat)
        usernameTextView = findViewById(R.id.N_usuario_chat)
        messageEditText = findViewById(R.id.Et_mensaje)
        sendButton = findViewById(R.id.IB_Enviar)
        attachButton = findViewById(R.id.IB_Adjuntar)
        recordButton = findViewById(R.id.IB_Grabar)
        recyclerView = findViewById(R.id.RV_chats)

        // Configurar RecyclerView
        chatList = mutableListOf()
        chatAdapter = AdaptadorChat(chatList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        // Inicializar Firebase Database y Storage
        database = FirebaseDatabase.getInstance().reference.child("chats")
        storageReference = FirebaseStorage.getInstance().reference

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario está autenticado
        currentUser = auth.currentUser
        if (currentUser == null) {
            // Si no hay usuario autenticado, redirigir a la actividad de inicio de sesión
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // Configurar el perfil del usuario y escuchar mensajes entrantes
            setupUserProfile()
            listenForMessages()
        }

        // Manejar eventos de los botones
        sendButton.setOnClickListener {
            sendMessage()
        }

        attachButton.setOnClickListener {
            openImageChooser()
        }

        recordButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                startRecording()
            }
        }
    }

    // Configurar el perfil del usuario
    private fun setupUserProfile() {
        currentUser?.let { user ->
            // Puedes configurar la información del perfil del usuario aquí
            // profileImageView.setImageURI(user.photoUrl) // Si tienes un URI de la imagen
            usernameTextView.text = user.displayName ?: "Nombre del usuario"
        }
    }

    // Enviar mensaje de texto
    private fun sendMessage() {
        val message = messageEditText.text.toString().trim()
        if (message.isNotEmpty()) {
            val currentTimestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val chatMessage = ChatMessage(message, currentTimestamp)
            database.push().setValue(chatMessage)
            messageEditText.text.clear()
        }
    }

    // Escuchar mensajes entrantes
    private fun listenForMessages() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    chatList.add(chatMessage)
                    chatAdapter.notifyItemInserted(chatList.size - 1)
                    recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Habilitar modo de pantalla completa
    private fun enableEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Abrir selector de imágenes
    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    // Abrir selector de audio
    private fun openAudioChooser() {
        val intent = Intent()
        intent.type = "audio/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), PICK_AUDIO_REQUEST)
    }

    // Manejar resultados de actividades
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val imageUri = data.data
                    Log.d("MainActivity", "Image URI: $imageUri")
                    uploadImageToFirebase(imageUri)
                }
                PICK_AUDIO_REQUEST -> {
                    audioUri = data.data
                    Log.d("MainActivity", "Audio URI: $audioUri")
                    uploadAudioToFirebase(audioUri)
                }
            }
        }
    }

    // Subir imagen a Firebase Storage
    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val fileReference = storageReference.child("chat_images/" + System.currentTimeMillis().toString() + "." + getFileExtension(imageUri))

            fileReference.putFile(imageUri)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val currentTimestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                        val chatMessage = ChatMessage(imageUrl = uri.toString(), timestamp = currentTimestamp)
                        database.push().setValue(chatMessage)
                    }
                }
                .addOnFailureListener {
                    // Manejar errores
                    Log.e("StorageUtil", "Error uploading image", it)
                }
        }
    }

    // Subir audio a Firebase Storage
    private fun uploadAudioToFirebase(audioUri: Uri?) {
        if (audioUri != null) {
            val fileReference = storageReference.child("chat_audio/" + System.currentTimeMillis().toString() + "." + getFileExtension(audioUri))

            fileReference.putFile(audioUri)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val currentTimestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                        val chatMessage = ChatMessage(audioUrl = uri.toString(), timestamp = currentTimestamp)
                        database.push().setValue(chatMessage)
                    }
                }
                .addOnFailureListener {
                    // Manejar errores
                    Log.e("StorageUtil", "Error uploading audio", it)
                }
        }
    }

    // Obtener la extensión del archivo
    private fun getFileExtension(uri: Uri): String? {
        val mimeType = contentResolver.getType(uri)
        Log.d("MainActivity", "MIME Type: $mimeType")
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "3gp"
    }

    // Iniciar grabación de audio
    private fun startRecording() {
        audioFilePath = "${externalCacheDir?.absolutePath}/audiorecord.3gp"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)

            try {
                prepare()
                start()
                Toast.makeText(this@MainActivity, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Log.e("AudioRecord", "prepare() failed", e)
            }
        }

        recordButton.setOnClickListener {
            stopRecording()
        }
    }

    // Detener grabación de audio
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()

        // Reproducir el archivo grabado para verificar que es válido
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepare()
            start()
        }

        uploadAudioToFirebase(Uri.fromFile(File(audioFilePath)))
    }

    // Manejar permisos de grabación de audio
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}