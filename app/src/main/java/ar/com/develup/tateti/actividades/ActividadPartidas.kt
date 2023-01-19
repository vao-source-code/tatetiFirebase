package ar.com.develup.tateti.actividades

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ar.com.develup.tateti.adaptadores.AdaptadorPartidas
import ar.com.develup.tateti.databinding.ActividadPartidasBinding
import ar.com.develup.tateti.modelo.Constantes
import ar.com.develup.tateti.modelo.Partida
import com.google.firebase.database.*


class ActividadPartidas : AppCompatActivity() {

    companion object {
        private const val TAG = "ActividadPartidas"
    }

    private lateinit var binding: ActividadPartidasBinding


    private lateinit var adaptadorPartidas: AdaptadorPartidas
    private lateinit var myRef: DatabaseReference

    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadPartidasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        database = FirebaseDatabase.getInstance()

        binding.newGame.setOnClickListener { nuevaPartida() }
    }

    private fun initRecyclerView() {
        adaptadorPartidas = AdaptadorPartidas(this)
        binding.listGames.layoutManager = LinearLayoutManager(this)
        binding.listGames.adapter = adaptadorPartidas

    }

    override fun onResume() {
        super.onResume()
        // TODO-06-DATABASE
        // Obtener una referencia a la base de datos, suscribirse a los cambios en Constantes.TABLA_PARTIDAS
        myRef = database.getReference(Constantes.TABLA_PARTIDAS)
        // y agregar como ChildEventListener el listenerTablaPartidas definido mas abajo
        myRef.addChildEventListener(listenerTableGames)

    }

    fun nuevaPartida() {
        val intent = Intent(this, ActividadPartida::class.java)
        startActivity(intent)
    }

    private val listenerTableGames: ChildEventListener = object : ChildEventListener {

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            Log.i(TAG, "onChildAdded: $dataSnapshot")
            val partida =
                dataSnapshot.getValue(Partida::class.java)!! // Obtener el valor del dataSnapshot
            partida.id = dataSnapshot.key // Asignar el valor del campo "key" del dataSnapshot
            adaptadorPartidas.agregarPartida(partida)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            Log.i(TAG, "onChildChanged: $s")
            val partida =
                dataSnapshot.getValue(Partida::class.java)!! // Obtener el valor del dataSnapshot
            partida.id = dataSnapshot.key // Asignar el valor del campo "key" del dataSnapshot
            adaptadorPartidas.partidaCambio(partida)
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            Log.i(TAG, "onChildRemoved: ")
            val partida =
                dataSnapshot.getValue(Partida::class.java)!! // Obtener el valor del dataSnapshot
            partida.id = dataSnapshot.key // Asignar el valor del campo "key" del dataSnapshot
            adaptadorPartidas.remover(partida)
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
            Log.i(TAG, "onChildMoved: $s")
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.i(TAG, "onCancelled: ")
        }
    }


}