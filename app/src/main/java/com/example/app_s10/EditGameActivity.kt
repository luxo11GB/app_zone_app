package com.example.app_s10

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditGameActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etGenre: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var btnUpdate: Button

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var gameId: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_game)

        etTitle = findViewById(R.id.etGameTitle)
        etGenre = findViewById(R.id.etGameGenre)
        ratingBar = findViewById(R.id.ratingBar)
        btnUpdate = findViewById(R.id.btnSaveGame)

        btnUpdate.text = "✏️ ACTUALIZAR JUEGO"

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: return

        gameId = intent.getStringExtra("GAME_ID") ?: return

        loadGameData()

        btnUpdate.setOnClickListener {
            updateGame()
        }
    }

    private fun loadGameData() {
        val gameRef = database.child("games").child(userId).child(gameId)
        gameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(Game::class.java)
                if (game != null) {
                    etTitle.setText(game.title)
                    etGenre.setText(game.genre)
                    ratingBar.rating = game.rating
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditGameActivity, "Error al cargar juego", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateGame() {
        val title = etTitle.text.toString().trim()
        val genre = etGenre.text.toString().trim()
        val rating = ratingBar.rating

        if (title.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedGame = Game(
            id = gameId,
            title = title,
            genre = genre,
            rating = rating,
            userId = userId
        )

        database.child("games").child(userId).child(gameId).setValue(updatedGame)
            .addOnSuccessListener {
                Toast.makeText(this, "Juego actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }
}
