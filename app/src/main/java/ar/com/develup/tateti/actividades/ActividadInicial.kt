package ar.com.develup.tateti.actividades

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import ar.com.develup.tateti.databinding.ActividadInicialBinding
import ar.com.develup.tateti.modelo.SPManager
import ar.com.develup.tateti.modelo.ValidForm
import ar.com.develup.tateti.servicios.AuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.shobhitpuri.custombuttons.GoogleSignInButton
import dmax.dialog.SpotsDialog
import java.util.regex.Matcher
import java.util.regex.Pattern


class ActividadInicial : AppCompatActivity() {

    lateinit var mAlertDialog: AlertDialog
    lateinit var mAuthProvider: AuthProvider
    lateinit var mBtnLoginGoogle: GoogleSignInButton
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var mFirebaseFirestore: FirebaseFirestore
    lateinit var mRemoteConfig: FirebaseRemoteConfig
    val GOOGLE_SIGN_IN: Int = 100

    private lateinit var binding: ActividadInicialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadInicialBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mAlertDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Cargando ...")
            .setCancelable(false).build()
        mBtnLoginGoogle = binding.btnLoginGoogle
        mAuthProvider = AuthProvider()
        mFirebaseFirestore = FirebaseFirestore.getInstance()
        //indico que la app ya se abrio una vez y no es la primera vez
        SPManager(applicationContext).add(SPManager.INIT, false)
        SPManager(applicationContext).add(SPManager.TUTORIAL, false)


        mRemoteConfig = FirebaseRemoteConfig.getInstance()


        binding.iniciarSesion.setOnClickListener { iniciarSesion() }
        binding.registrate.setOnClickListener { registrate() }
        binding.olvideMiContrasena.setOnClickListener { olvideMiContrasena() }
        iniciarSesionGoogle()


      //  if (usuarioEstaLogueado()) {
        //    verPartidas()
         //   finish()
       // }
        actualizarRemoteConfig()
    }


    private fun iniciarSesionGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        mBtnLoginGoogle.setOnClickListener {
            signInGoogle()
        }

    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(ContentValues.TAG, "Google sign in failed", e)
            }
        }

    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        mAuthProvider.googleLogin(account).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val id = mAuthProvider.getUid()

                if (id != null) {
                    checkUserExist(id)
                }
                // Sign in success, update UI with the signed-in user's information
                Log.d(ContentValues.TAG, "signInWithCredential:success")

            } else {
                Snackbar.make(
                    binding.root,
                    "No se pudo iniciar sesion con Google",
                    Snackbar.LENGTH_SHORT
                ).show()
                // If sign in fails, display a message to the user.
                Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
            }
        }

    }

    private fun checkUserExist(id: String) {
        mFirebaseFirestore.collection("Users").document(id).get().addOnSuccessListener { it ->
            mAlertDialog.show()
            if (it.exists()) {
                mAlertDialog.dismiss()
                verPartidas()
            } else {
                usuarioNuevoGoogle(id)
            }
        }
    }

    private fun usuarioNuevoGoogle(id: String) {
        val email = mAuthProvider.getEmail()

        val map: MutableMap<String, String> = mutableMapOf()
        if (email != null) {
            map["email"] = email

        }
        mFirebaseFirestore.collection("Users").document(id).set(map).addOnCompleteListener {
            if (it.isSuccessful) {
                mAlertDialog.dismiss()

                intent = Intent(this, ActividadPartidas::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "No se pudo almacenar la informacion del usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun usuarioEstaLogueado(): Boolean {
        // TODO-05-AUTHENTICATION
        if (mAuthProvider.getEmail() == null || mAuthProvider.getEmail().equals("")) {
            return false
        }
        return true
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
        mRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 6
        }
        mRemoteConfig.setConfigSettingsAsync(configSettings)
        configurarDefaultsRemoteConfig()
        configurarOlvideMiContrasena()
    }

    private fun configurarDefaultsRemoteConfig() {
        // TODO-04-REMOTECONFIG

        mRemoteConfig.setDefaultsAsync(R.xml.firebase_config_defaults);


    }

    private fun configurarOlvideMiContrasena() {
        // TODO-04-REMOTECONFIG
        // Obtener el valor de la configuracion para saber si mostrar
        // o no el boton de olvide mi contraseña

        mRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    val showPasswordBotton = mRemoteConfig.getBoolean("show_password_botton")

                    if (showPasswordBotton) {
                        binding.olvideMiContrasena.visibility = View.VISIBLE

                    } else {
                        binding.olvideMiContrasena.visibility = View.GONE

                    }
                    Log.d(TAG, "Config params updated: $updated")
                    Toast.makeText(
                        this, "La opción olvidar contraseña se actualizó",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this, "Fetch failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }


    }

    private fun olvideMiContrasena() {
        // Obtengo el mail
        val email = binding.email.text.toString()

        // Si no completo el email, muestro mensaje de error
        if (email.isEmpty()) {
            Snackbar.make(binding.rootView!!, "Completa el email", Snackbar.LENGTH_SHORT).show()
        } else {
            // TODO-05-AUTHENTICATION
            // Si completo el mail debo enviar un mail de reset
            // Para ello, utilizamos sendPasswordResetEmail con el email como parametro
            // Agregar el siguiente fragmento de codigo como CompleteListener, que notifica al usuario
            // el resultado de la operacion

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                      Snackbar.make(binding.rootView, "Email enviado", Snackbar.LENGTH_SHORT).show()
                  } else {
                    Log.e("Error enviar recupero", task.exception.toString())
                     Snackbar.make(binding.rootView, "Error al enviar el email, verifique si el correo esta registrado", Snackbar.LENGTH_SHORT).show()
                  }
              }
        }
    }

    private fun iniciarSesion() {

        val email = binding.email.text.toString();
        val password = binding.password.text.toString();
        Log.d("Campo", "email:$email")
        Log.d("Campo", "password:$password")

        mAlertDialog.show()
        //TODO se debe validar el password
        if (ValidForm.validEmail(email) && password.isNotEmpty()) {

            mAuthProvider.login(email, password).addOnCompleteListener {
                mAlertDialog.dismiss()
                if (it.isSuccessful) {
                    Snackbar.make(
                        binding.rootView!!,"Bienvenido!", Snackbar.LENGTH_LONG).show()
                    verPartidas()
                } else {

                    if (it.exception is FirebaseAuthInvalidUserException) {
                        Snackbar.make(
                            binding.rootView!!,
                            "El email o contraseña no son correctas ",
                            Snackbar.LENGTH_LONG
                        ).show()
                        FirebaseCrashlytics.getInstance().setUserId(email)
                        FirebaseCrashlytics.getInstance()
                            .setCustomKey("TIPO_ERROR", "IDENTIFICACION")
                        FirebaseCrashlytics.getInstance().log("Identificacion erronea")
                    }
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Error Critico ", Toast.LENGTH_LONG).show()
                        FirebaseCrashlytics.getInstance().setUserId(email)
                        FirebaseCrashlytics.getInstance()
                            .setCustomKey("TIPO_ERROR", "CREDENCIALES")
                        FirebaseCrashlytics.getInstance().log("error credencial")


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

        mAuthProvider.mAuth.signOut()
        SPManager(applicationContext).add(SPManager.INIT, false)

        // FirebaseAuth.getInstance().signOut()


    }

    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }
}