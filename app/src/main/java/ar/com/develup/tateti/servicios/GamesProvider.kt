package ar.com.develup.tateti.servicios
import ar.com.develup.tateti.modelo.Constantes
import ar.com.develup.tateti.modelo.Partida
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class GamesProvider {

    var mCollection: CollectionReference =
        FirebaseFirestore.getInstance().collection(Constantes.TABLA_PARTIDAS)


    public fun getUser(id: String): Task<DocumentSnapshot> {
        return mCollection.document(id).get()
    }

    public fun create(partida : Partida): Task<Void> {
        return mCollection.document(partida.id!!).set(partida)
    }

}