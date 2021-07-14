package ar.com.develup.tateti.servicios

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthProvider {

    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String): Task<AuthResult> {
        return mAuth.signInWithEmailAndPassword(email, password)
    }

    fun googleLogin(account: GoogleSignInAccount): Task<AuthResult> {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        return mAuth.signInWithCredential(credential)

    }

    fun getUid(): String? {

        return if (mAuth.currentUser != null)
            mAuth.currentUser?.uid
        else
            null
    }

    fun getEmail(): String? {

        if (mAuth.currentUser != null)
            return mAuth.currentUser?.email
        else
            return null

    }
}