package ar.com.develup.tateti.actividades

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import ar.com.develup.tateti.servicios.AuthProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.actividad_inicial.*
import dmax.dialog.SpotsDialog
import java.util.regex.Matcher
import java.util.regex.Pattern


class ActividadInicial : AppCompatActivity() {

    lateinit var mAlertDialog: AlertDialog
    lateinit var mAuthProvider: AuthProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_inicial)

        mAlertDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Cargando ...")
            .setCancelable(false).build()
        mAuthProvider = AuthProvider()

        iniciarSesion.setOnClickListener { iniciarSesion() }
        registrate.setOnClickListener { registrate() }
        olvideMiContrasena.setOnClickListener { olvideMiContrasena() }

        if (usuarioEstaLogueado()) {
            // Si el usuario esta logueado, se redirige a la pantalla
            // de partidas
            verPartidas()
            finish()
        }
        actualizarRemoteConfig()
    }

    private fun usuarioEstaLogueado(): Boolean {
        // TODO-05-AUTHENTICATION
        // Validar que currentUser sea != null
        return false
    }

    private fun verPartidas() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private fun registrate() {
        val intent = Intent(this, ActividadRegistracion::class.java)
        startActivity(intent)
    }

    private fun actualizarRemoteConfig() {
        configurarDefaultsRemoteConfig()
        configurarOlvideMiContrasena()
    }

    private fun configurarDefaultsRemoteConfig() {
        // TODO-04-REMOTECONFIG
        // Configurar los valores por default para remote config,
        // ya sea por codigo o por XML
    }

    private fun configurarOlvideMiContrasena() {
        // TODO-04-REMOTECONFIG
        // Obtener el valor de la configuracion para saber si mostrar
        // o no el boton de olvide mi contraseña
        val botonOlvideHabilitado = false
        if (botonOlvideHabilitado) {
            olvideMiContrasena.visibility = View.VISIBLE
        } else {
            olvideMiContrasena.visibility = View.GONE
        }

        olvideMiContrasena.visibility = View.VISIBLE

    }

    private fun olvideMiContrasena() {
        // Obtengo el mail
        val email = email.text.toString()

        // Si no completo el email, muestro mensaje de error
        if (email.isEmpty()) {
            Snackbar.make(rootView!!, "Completa el email", Snackbar.LENGTH_SHORT).show()
        } else {
            // TODO-05-AUTHENTICATION
            // Si completo el mail debo enviar un mail de reset
            // Para ello, utilizamos sendPasswordResetEmail con el email como parametro
            // Agregar el siguiente fragmento de codigo como CompleteListener, que notifica al usuario
            // el resultado de la operacion

            //  .addOnCompleteListener { task ->
            //      if (task.isSuccessful) {
            //          Snackbar.make(rootView, "Email enviado", Snackbar.LENGTH_SHORT).show()
            //      } else {
            //          Snackbar.make(rootView, "Error " + task.exception, Snackbar.LENGTH_SHORT).show()
            //      }
            //  }
        }
    }

    private fun iniciarSesion() {

        val email = email.text.toString();
        val password = password.text.toString();
        Log.d("Campo", "email:$email")
        Log.d("Campo", "password:$password")

        mAlertDialog.show()
        if (email.isNotEmpty() && password.isNotEmpty()) {

            if (isEmailValid(email)) {
                mAuthProvider.login(email,password).addOnCompleteListener {
                    mAlertDialog.dismiss()
                    if (it.isSuccessful) {
                        Toast.makeText(this, "pase por aca ", Toast.LENGTH_LONG).show()
                        verPartidas()
                    } else {

                        if(it.exception is FirebaseAuthInvalidUserException ){
                            Toast.makeText(this, "El email o contraseña no son correctas ", Toast.LENGTH_LONG).show()
                            FirebaseCrashlytics.getInstance().setUserId(email)
                            FirebaseCrashlytics.getInstance().setCustomKey("TIPO_ERROR","IDENTIFICACION")
                            FirebaseCrashlytics.getInstance().log("Identificacion erronea")
                        }
                        if(it.exception is FirebaseAuthInvalidCredentialsException){
                            Toast.makeText(this, "Error Critico ", Toast.LENGTH_LONG).show()
                            FirebaseCrashlytics.getInstance().setUserId(email)
                            FirebaseCrashlytics.getInstance().setCustomKey("TIPO_ERROR","CREDENCIALES")
                            FirebaseCrashlytics.getInstance().log("error credencial")


                        }
                    }

                }
            }
        } else {
            Toast.makeText(this, "Error al procesar los campos, estan vacios", Toast.LENGTH_LONG)
                .show()
        }
        mAlertDialog.dismiss()


    }

        // TODO-05-AUTHENTICATION
        // IMPORTANTE: Eliminar  la siguiente linea cuando se implemente authentication


        // TODO-05-AUTHENTICATION
        // hacer signInWithEmailAndPassword con los valores ingresados de email y password
        // Agregar en addOnCompleteListener el campo authenticationListener definido mas abajo


    //    private val authenticationListener: OnCompleteListener<AuthResult?> = OnCompleteListener<AuthResult?> { task ->
    //        if (task.isSuccessful) {
    //            if (usuarioVerificoEmail()) {
    //                verPartidas()
    //            } else {
    //                desloguearse()
    //                Snackbar.make(rootView!!, "Verifica tu email para continuar", Snackbar.LENGTH_SHORT).show()
    //            }
    //        } else {
    //            if (task.exception is FirebaseAuthInvalidUserException) {
    //                Snackbar.make(rootView!!, "El usuario no existe", Snackbar.LENGTH_SHORT).show()
    //            } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
    //                Snackbar.make(rootView!!, "Credenciales inválidas", Snackbar.LENGTH_SHORT).show()
    //            }
    //        }
    //    }

    private fun usuarioVerificoEmail(email: String): Boolean {
        // TODO-05-AUTHENTICATION
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun desloguearse() {
        // TODO-05-AUTHENTICATION
        // Hacer signOut de Firebase
    }

    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }
}