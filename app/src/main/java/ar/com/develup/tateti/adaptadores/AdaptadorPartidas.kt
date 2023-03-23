package ar.com.develup.tateti.adaptadores

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ar.com.develup.tateti.R
import ar.com.develup.tateti.actividades.ActividadPartida
import ar.com.develup.tateti.actividades.ActividadPartidas
import ar.com.develup.tateti.databinding.ItemPartidaBinding
import ar.com.develup.tateti.modelo.Constantes
import ar.com.develup.tateti.modelo.Partida
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AdaptadorPartidas(
        private val actividad: ActividadPartidas
) : RecyclerView.Adapter<AdaptadorPartidas.Holder>() {

    private val partidas: MutableList<Partida> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemLayoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_partida, null)
        return Holder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val partida = partidas[position]
        holder.partida = partida
        holder.idPartida.text = "id: " + partida.id
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener { it ->
            for (document in it) {
                if (document.id == partida.retador) {
                    holder.retador.text = "Retador: " + document.data["email"].toString()
                }
            }
        }
        holder.estado.text = partida.calcularEstado()
    }

    override fun getItemCount(): Int {
        return partidas.size
    }

    fun agregarPartida(partida: Partida) {
        if (!partidas.contains(partida)) {
            partidas.add(partida)
            notifyItemInserted(partidas.size - 1)
        }
    }

    fun partidaCambio(partida: Partida) {
        if (partidas.contains(partida)) {
            val posicion = partidas.indexOf(partida)
            partidas[posicion] = partida
            notifyItemChanged(posicion)
        }
    }

    fun remover(partida: Partida?) {
        if (partidas.contains(partida)) {
            val posicion = partidas.indexOf(partida)
            partidas.remove(partida)
            notifyItemRemoved(posicion)
        }
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var   binding  : ItemPartidaBinding
        var partida: Partida? = null
        var idPartida: TextView
        var estado: TextView
        var retador: TextView

        private val clickPartidaListener = View.OnClickListener {
            val intent = Intent(actividad, ActividadPartida::class.java)
            intent.putExtra(Constantes.EXTRA_PARTIDA, partida)
            actividad.startActivity(intent)
        }

        init {
            //TODO se debe agregar el id del correo en el item_partida
            binding = ItemPartidaBinding.bind(itemView)

            binding.itemPartida.setOnClickListener(clickPartidaListener)
            idPartida = binding.idPartida
            estado =  binding.estado
            retador = binding.retador

        }
    }

}