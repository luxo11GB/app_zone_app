package com.example.app_s10

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GamesListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val gameList = mutableListOf<Game>()
    private val filteredList = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        recyclerView = findViewById(R.id.recyclerViewGames)
        recyclerView.layoutManager = LinearLayoutManager(this)

        gameAdapter = GameAdapter(
            games = filteredList,
            onDeleteClick = { deleteGame(it) },
            onEditClick = { editGame(it) }
        )
        recyclerView.adapter = gameAdapter

        val searchTextId = resources.getIdentifier("android:id/search_src_text", null, null)
        val searchText = findViewById<EditText>(searchTextId)
        searchText?.setTextColor(Color.WHITE)
        searchText?.setHintTextColor(Color.WHITE)


        setupSearchView()
        loadGames()
    }

    private fun setupSearchView() {
        val searchView = findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterGamesByGenre(newText ?: "")
                return true
            }
        })
    }

    private fun filterGamesByGenre(query: String) {
        val lowerQuery = query.trim().lowercase()
        filteredList.clear()

        if (lowerQuery.isEmpty()) {
            filteredList.addAll(gameList)
        } else {
            filteredList.addAll(gameList.filter {
                it.genre.lowercase().contains(lowerQuery)
            })
        }

        gameAdapter.updateGames(filteredList)
    }

    private fun loadGames() {
        val userId = auth.currentUser?.uid ?: return
        val gamesRef = database.child("games").child(userId)

        gamesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gameList.clear()
                for (gameSnapshot in snapshot.children) {
                    val game = gameSnapshot.getValue(Game::class.java)
                    if (game != null) {
                        gameList.add(game)
                    }
                }
                filterGamesByGenre("") // Mostrar todos por defecto
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GamesListActivity, "Error al cargar juegos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteGame(game: Game) {
        val userId = auth.currentUser?.uid ?: return
        database.child("games").child(userId).child(game.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Juego eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar juego", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editGame(game: Game) {
        val intent = Intent(this, EditGameActivity::class.java)
        intent.putExtra("GAME_ID", game.id)
        startActivity(intent)
    }
}
