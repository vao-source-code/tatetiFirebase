package ar.com.develup.tateti.actividades

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import ar.com.develup.tateti.servicios.AuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.actividad_inicial.*
import dmax.dialog.SpotsDialog
import java.util.regex.Matcher
import java.util.regex.Pattern


class ActividadInicial : AppCompatActivity() {

    lateinit var mAlertDialog: AlertDialog
    lateinit var mAuthProvider: AuthProvider
    lateinit var mBtnLoginGoogle: SignInButton
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var mFirebaseFirestore: FirebaseFirestore
    lateinit var mRemoteConfig : FirebaseRemoteConfig
    val  GOOGLE_SIGN_IN : Int  = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_inicial)

        mAlertDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Cargando ...")
            .setCancelable(false).build()


        mBtnLoginGoogle = findViewById(R.id.btnLoginGoogle)
        mAuthProvider = AuthProvider()
        mFirebaseFirestore = FirebaseFirestore.getInstance()


        mRemoteConfig = FirebaseRemoteConfig.getInstance()


        iniciarSesion.setOnClickListener { iniciarSesion() }
        registrate.setOnClickListener { registrate() }
        olvideMiContrasena.setOnClickListener { olvideMiContrasena() }
        iniciarSesionGoogle()


        if (!usuarioEstaLogueado()) {
            verPartidas()
            finish()
        }
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

    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount) {
        mAuthProvider.googleLogin(account).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val id = mAuthProvider.getUid()

                if (id != null) {
                    checkUserExist(id)
                }
                // Sign in success, update UI with the signed-in user's information
                Log.d(ContentValues.TAG, "signInWithCredential:success")

            } else {

                // If sign in fails, display a message to the user.
                Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
            }
        }

    }

    private fun checkUserExist(id: String) {
        mFirebaseFirestore.collection("Users").document(id).get().addOnSuccessListener { it ->
mAlertDialog.show()
            if(it.exists()){
                mAlertDialog.dismiss()
                verPartidas()
            }else{
                usuarioNuevoGoogle(id)

            }
        }
    }

    private fun usuarioNuevoGoogle(id : String) {
        val email = mAuthProvider.getEmail()

        val map : MutableMap<String,String> = mutableMapOf()
        if(email !=null){
            map["email"] = email

        }
        mFirebaseFirestore.collection("Users").document(id).set(map).addOnCompleteListener {
            if(it.isSuccessful){
                mAlertDialog.dismiss()

                intent = Intent(this, ActividadPartidas::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this, "No se pudo almacenar la informacion del usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun usuarioEstaLogueado(): Boolean {
        // TODO-05-AUTHENTICATION
        if ( mAuthProvider.getEmail() == null)
        return false
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
                        olvideMiContrasena.visibility = View.VISIBLE

                    } else {
                        olvideMiContrasena.visibility = View.GONE

                    }
                    Log.d(TAG, "Config params updated: $updated")
                    Toast.makeText(this, "Fetch and activate succeeded",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Fetch failed",
                        Toast.LENGTH_SHORT).show()
                }

            }



    }

    private fun olvideMiContrasena() {
        // Obtengo el mail
        val email = email.text.toString()

        // Si no completo el email, muestro mensaje de error
        if (email.isEmpty()) {
            Snackbar.make(rootView!!, "Completa el email", Snackbar.LENGTH_SHORT).show()
        } else {

            resetPassword(email)
            // TODO-05-AUTHENTICATION

        }
    }

    private fun resetPassword(email: String) {
mAuthProvider.mAuth.setLanguageCode("es")
        mAuthProvider.mAuth.sendPasswordResetEmail(email).addOnCompleteListener {
            mAlertDialog.show()
            if(it.isSuccessful){

                Snackbar.make(rootView!!, "Se pudo enviar el correo", Snackbar.LENGTH_SHORT).show()


            }else{
                Snackbar.make(rootView!!, "No se pudo enviar el correo", Snackbar.LENGTH_SHORT).show()
            }
            mAlertDialog.dismiss()
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
                            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()


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