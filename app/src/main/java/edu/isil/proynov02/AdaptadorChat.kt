package edu.isil.proynov02

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.isil.proynov02.R
import java.io.IOException


// Define la data class ChatMessage para representar un mensaje de chat con posibles atributos como texto, timestamp, URL de imagen y URL de audio
data class ChatMessage(
    var message: String = "",
    var timestamp: String = "",
    var imageUrl: String? = null,
    var audioUrl: String? = null
)

// Define la clase AdaptadorChat que hereda de RecyclerView.Adapter y maneja los mensajes de chat
class AdaptadorChat(private val chatList: MutableList<ChatMessage>) : RecyclerView.Adapter<AdaptadorChat.ViewHolder>() {

    // Crea y devuelve un ViewHolder cuando el RecyclerView lo necesita
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Infla el layout de item_chat para cada elemento de la lista
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    // Vincula los datos del ChatMessage a las vistas del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Obtiene el mensaje de chat de la posición actual
        val chatMessage = chatList[position]
        // Asigna el texto del mensaje al TextView correspondiente
        holder.messageText.text = chatMessage.message
        // Asigna el timestamp del mensaje al TextView correspondiente
        holder.messageTime.text = chatMessage.timestamp

        // Si hay una URL de imagen, carga la imagen usando Glide y la muestra
        if (chatMessage.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(chatMessage.imageUrl)
                .into(holder.messageImage)
            holder.messageImage.visibility = View.VISIBLE
        } else {
            // Si no hay URL de imagen, oculta el ImageView
            holder.messageImage.visibility = View.GONE
        }

        // Si hay una URL de audio, muestra el icono del reproductor de audio y configura el MediaPlayer
        if (chatMessage.audioUrl != null) {
            holder.audioPlayer.visibility = View.VISIBLE
            holder.audioPlayer.setOnClickListener {
                val mediaPlayer = MediaPlayer()
                try {
                    mediaPlayer.setDataSource(chatMessage.audioUrl)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: IOException) {
                    Log.e("AudioPlayer", "prepare() failed", e)
                }
            }
        } else {
            // Si no hay URL de audio, oculta el ImageView del reproductor de audio
            holder.audioPlayer.visibility = View.GONE
        }
    }

    // Devuelve el número total de elementos en la lista de mensajes de chat
    override fun getItemCount(): Int {
        return chatList.size
    }

    // Define el ViewHolder que contiene las vistas para cada elemento de la lista de chat
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.message_text) // TextView para mostrar el texto del mensaje
        val messageTime: TextView = itemView.findViewById(R.id.message_time) // TextView para mostrar el timestamp del mensaje
        val messageImage: ImageView = itemView.findViewById(R.id.message_image) // ImageView para mostrar la imagen del mensaje
        val audioPlayer: ImageView = itemView.findViewById(R.id.audio_player) // ImageView para mostrar el icono del reproductor de audio
    }
}