package ar.com.develup.tateti.actividades

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.databinding.ActividadRegistracionBinding
import ar.com.develup.tateti.utils.Email
import ar.com.develup.tateti.utils.Password
import ar.com.develup.tateti.utils.ValidForm
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dmax.dialog.SpotsDialog
import java.util.regex.Matcher
import java.util.regex.Pattern

class ActividadRegistracion : AppCompatActivity() {

    companion object {
        private const val TAG = "ActividadRegistracion"
    }

    private lateinit var binding: ActividadRegistracionBinding


    lateinit var rAuth: FirebaseAuth

    lateinit var rFirebaseFirestore: FirebaseFirestore
    lateinit var mAlertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActividadRegistracionBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnRegister.setOnClickListener { registrarse() }

        mAlertDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Registrando ...")
            .setCancelable(false).build()
        rAuth = FirebaseAuth.getInstance()
        rFirebaseFirestore = Firebase.firestore

    }

    fun registrarse() {
        val passwordIngresada = binding.editTextPassword.text.toString()
        val confirmarPasswordIngresada = binding.editTextConfirmPassword.text.toString()
        val email = binding.editTextEmail.text.toString()


        when (ValidForm.validEmail(email)) {

            Email.EMPTY_EMAIL, Email.SHORT_EMAIL, Email.ERROR_EMAIL -> {
                Snackbar.make(
                    binding.rootView,
                    "Debe ingresar un email correcto",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            Email.CORRECT_EMAIL -> {
                if (isPasswordValid(passwordIngresada, confirmarPasswordIngresada)) {
                    registrarUsuarioEnFirebase(email, passwordIngresada)
                } else {
                    Snackbar.make(
                        binding.rootView,
                        "Las contraseñas no son validas",
                        Snackbar.LENGTH_SHORT
                    ).show()

                }
            }
            else -> {
                Log.e(TAG, "error al tomar datos")
            }
        }

    }

    private fun registrarUsuarioEnFirebase(email: String, password: String) {
        // TODO-05-AUTHENTICATION
        createUser(email, password)
    }

    private fun createUser(email: String, password: String) {

        mAlertDialog.show()


        rAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {

                val id: String = rAuth.currentUser?.uid ?: ""
                val map: MutableMap<String, String> = mutableMapOf()
                map["email"] = email
                createFirebaseBBDD(map, id)

            } else {
                mAlertDialog.dismiss()
                Toast.makeText(this, "No se pudo registar el usuario", Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun createFirebaseBBDD(map: MutableMap<String, String>, id: String) {
        rFirebaseFirestore.collection("Users").document(id).set(map).addOnCompleteListener {
            if (it.isSuccessful) {
                mAlertDialog.dismiss()

                Toast.makeText(
                    this,
                    "El usuario se almacenó en la base de datos",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, ActividadInicial::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else if (it.exception is FirebaseAuthUserCollisionException) {
                //Si el usuario ya existe, mostramos error
                FirebaseCrashlytics.getInstance().log("Intento de duplicacion de datos")
                Toast.makeText(
                    this,
                    "El usuario  ya se encuentra en la base de datos ",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "El usuario  no se almacenó en la base de datos",
                    Toast.LENGTH_LONG
                ).show()

                FirebaseCrashlytics.getInstance().log("Error de datos")

            }

        }
    }

    private val registracionCompletaListener: OnCompleteListener<AuthResult?> =
        OnCompleteListener { task ->
            if (task.isSuccessful) {
                // Si se registro OK, muestro mensaje y envio mail de verificacion
                Snackbar.make(binding.rootView, "Registro exitoso", Snackbar.LENGTH_SHORT).show()
                //enviarEmailDeVerificacion()

            } else if (task.exception is FirebaseAuthUserCollisionException) {
                // Si el usuario ya existe, mostramos error
                Snackbar.make(binding.rootView, "El usuario ya existe", Snackbar.LENGTH_SHORT)
                    .show()

            } else {
                // Por cualquier otro error, mostramos un mensaje de error
                Snackbar.make(
                    binding.rootView,
                    "El registro fallo: " + task.exception,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }


    //ValidarPassword
    private fun isPasswordValid(password: String, confirmPassword: String): Boolean {
        if (password.equals(confirmPassword) && ValidForm.validPassword(password) == Password.CORRECT_PASSWORD) {
            return true
        }
        return false
    }

    //valida email si posee los caracteres necesarios
    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

}