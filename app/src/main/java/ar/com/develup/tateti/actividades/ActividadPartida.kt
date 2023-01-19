package ar.com.develup.tateti.actividades

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ar.com.develup.tateti.databinding.ActividadPartidaBinding
import ar.com.develup.tateti.modelo.Constantes
import ar.com.develup.tateti.modelo.Movimiento
import ar.com.develup.tateti.modelo.Partida
import ar.com.develup.tateti.servicios.AuthProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
/*Hecho por victor orue 2022*/


class ActividadPartida : AppCompatActivity() {

    private var partida: Partida? = null
    companion object {
        private const val TAG = "ActividadPartida"
    }

    private lateinit var binding: ActividadPartidaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constantes.EXTRA_PARTIDA)) {
            partida = intent.getSerializableExtra(Constantes.EXTRA_PARTIDA) as Partida

            // Si esta partida creada no tiene oponente, entonces me sumo yo como oponente
            if (partida?.oponente == null) {
                sumarmeComoOponente()
            }
            suscribirseACambiosEnLaPartida()
        }
        val botones: MutableList<Button> = LinkedList()
        botones.add(binding.position1)
        botones.add(binding.position2)
        botones.add(binding.position3)
        botones.add(binding.position4)
        botones.add(binding.position5)
        botones.add(binding.position6)
        botones.add(binding.position7)
        botones.add(binding.position8)
        botones.add(binding.position9)
        for (boton in botones) {
            boton.setOnClickListener { jugar(boton) }
        }
    }

    private fun suscribirseACambiosEnLaPartida() {
        // TODO-06-DATABASE
        // 1 - Obtener una referencia a Constantes.TABLA_PARTIDAS
        // 2 - Obtener el child de la partida, a partir de partida.id
        // 3 - Agregar como valueEventListener el listener partidaCambio definido mas abajo
        val jugador = FirebaseAuth.getInstance().currentUser?.uid
        partida?.oponente = jugador
        val database = FirebaseDatabase.getInstance().reference
        val referenciaPartidas = database.child(Constantes.TABLA_PARTIDAS)
        val referenciaPartida = referenciaPartidas.child(partida?.id!!)
        referenciaPartida.child("oponente").setValue(jugador)

        referenciaPartida.addValueEventListener(partidaCambio)

    }

    override fun onPause() {
        // Ahora nos tenemos que desuscribir a los cambios en la base de datos.
        // 1 - Obtener una referencia a Constantes.TABLA_PARTIDAS
        // 2 - Obtener el child de la partida, a partir de partida.id
        // 3 - REMOVER el valueEventListener el listener partidaCambio
        super.onPause()
        // TODO-06-DATABASE
        val database = obtenerReferenciaALaBaseDeDatos();
        val referenciaPartidas = database.child(Constantes.TABLA_PARTIDAS);
        val referenciaPartida = partida?.id?.let { referenciaPartidas.child(it) };
        referenciaPartida?.removeEventListener(partidaCambio);


    }


    private val partidaCambio: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val partida =
                dataSnapshot.getValue(Partida::class.java)// Obtener la partida a partir del dataSnapshot
            if (partida != null) {
                partida.id = dataSnapshot.key // Asignar el valor del campo "key" del dataSnapshot
                this@ActividadPartida.partida = partida
                cargarVistasPartidaIniciada()
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    }


    private fun cargarVistasPartidaIniciada() {
        for ((position, player) in partida?.movimientos!!) {
            val boton = binding.table.findViewWithTag<View>(position.toString()) as Button
            if (player == partida?.retador) {
                boton.text = "X"
            } else {
                boton.text = "O"
            }
            boton.isEnabled = false
        }
        comprobarGanador()
    }

    private fun comprobarGanador() {
        if (hayTaTeTi()) {
            val player = obtenerIdDeUsuario()
            //TODO Mejorar el alert Dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Partida finalizada")
            builder.setMessage(if (partida?.ganador == player) "GANASTE!" else "PERDISTE :(")
            try {
                builder.show()
            } catch (ignored: Exception) {
                Log.e(TAG, "")
            }
        } else if (finalizo()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Partida finalizada")
            builder.setMessage("Es un empate")
            try {
                builder.show()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun finalizo(): Boolean {
        return partida?.movimientos?.size == 9
    }

    private fun hayTaTeTi(): Boolean {
        return (sonIguales(1, 2, 3) || sonIguales(4, 5, 6) || sonIguales(7, 8, 9) //Horizontal
                || sonIguales(1, 4, 7) || sonIguales(2, 5, 8) || sonIguales(3, 6, 9) //Vertical
                || sonIguales(1, 5, 9) || sonIguales(3, 5, 7)) //Diagonal
    }

    private fun sonIguales(vararg casilleros: Int): Boolean {
        var sonIguales = true
        var valor: String? = null
        var i = 0
        while (i < casilleros.size && sonIguales) {
            val boton = binding.table.findViewWithTag<View>(casilleros[i].toString()) as Button
            val simbolo = boton.text.toString()
            if (valor == null) {
                valor = simbolo
            } else {
                sonIguales = !simbolo.isEmpty() && valor == simbolo
            }
            i++
        }
        if (sonIguales) {
            for (casillero in casilleros) {
                val boton = binding.table.findViewWithTag<View>(casillero.toString()) as Button
                boton.setTextColor(
                    ContextCompat.getColor(applicationContext, android.R.color.holo_green_dark)
                )
            }
            if ("X".equals(valor, ignoreCase = true)) {
                establecerGanador(partida?.retador)
            } else if ("O".equals(valor, ignoreCase = true)) {
                establecerGanador(partida?.oponente)
            }
        }
        return sonIguales
    }

    private fun establecerGanador(ganador: String?) {
        partida?.ganador = ganador
        val database = obtenerReferenciaALaBaseDeDatos()
        val referenciaPartidas = database.child(Constantes.TABLA_PARTIDAS);
        val referenciaPartida = partida?.id?.let { referenciaPartidas.child(it) };
        // TODO-06-DATABASE Descomentar la siguiente linea una vez obtenidos los dos datos anteriores
        referenciaPartida?.child("ganador")?.setValue(ganador)
    }

    fun jugar(button: Button) {
        if (esMiTurno()) {
            val jugador = obtenerIdDeUsuario()
            val posicion = button.tag as String
            val numeroPosicion = Integer.valueOf(posicion)
            if (partida == null) {
                button.text = "X"
                crearPartida(numeroPosicion)
            } else if (partida?.ganador == null) {
                if (partida?.retador == jugador) {
                    button.text = "X"
                } else if (partida?.oponente == jugador) {
                    button.text = "O"
                }
                actualizarPartida(numeroPosicion)
            }
        } else if (partida?.ganador == null) {
            Snackbar.make(binding.rootView, "Es el turno de tu oponente", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun esMiTurno(): Boolean {
        val jugador = obtenerIdDeUsuario()
        return (partida == null
                ||
                partida!!.movimientos[partida!!.movimientos.size - 1].jugador != jugador)
    }

    private fun actualizarPartida(posicion: Int) {
        val jugador = FirebaseAuth.getInstance().currentUser!!.uid
        partida?.movimientos?.add(Movimiento(posicion, jugador))

        val database = FirebaseDatabase.getInstance().reference
        val referenciaPartidas = database.child(Constantes.TABLA_PARTIDAS)
        val referenciaPartida = referenciaPartidas.child(partida?.id!!)
        referenciaPartida.child("movimientos").setValue(partida?.movimientos)
        // TODO-06-DATABASE Descomentar la siguiente linea una vez obtenidos los dos datos anteriores

        referenciaPartida.child("movimientos").setValue(partida?.movimientos)
    }

    private fun crearPartida(posicion: Int) {
        val jugador = obtenerIdDeUsuario()
        partida = Partida()
        partida?.retador = jugador
        partida?.movimientos?.add(Movimiento(posicion, jugador))
        val database = FirebaseDatabase.getInstance().reference
        val referenciaPartidas = database.child(Constantes.TABLA_PARTIDAS)
        val referenciaPartida = referenciaPartidas.push()
        referenciaPartida.setValue(partida)
        partida?.id = referenciaPartida.key

        suscribirseACambiosEnLaPartida()
    }

    private fun sumarmeComoOponente() {
        val jugador = FirebaseAuth.getInstance().currentUser?.uid
        partida?.oponente = jugador
        val database = FirebaseDatabase.getInstance().reference
        val referenciaPartidas = database.child(Constantes.TABLA_PARTIDAS)
        val referenciaPartida = referenciaPartidas.child(partida?.id!!)
        referenciaPartida.child("oponente").setValue(jugador)

    }

    private fun obtenerIdDeUsuario(): String {
        // TODO-05-AUTHENTICATION
        val mAuthProvider = AuthProvider()
        return mAuthProvider.getUid()!!
        // Obtener el uid del currentUser y retornarlo
    }

    private fun obtenerReferenciaALaBaseDeDatos(): DatabaseReference {
        // TODO-06-DATABASE
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        return database.getReference(Constantes.TABLA_PARTIDAS)

    }
}


