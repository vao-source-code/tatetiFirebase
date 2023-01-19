package ar.com.develup.tateti.actividades

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import ar.com.develup.tateti.databinding.ActividadInicialBinding
import ar.com.develup.tateti.servicios.AuthProvider
import ar.com.develup.tateti.utils.Email
import ar.com.develup.tateti.utils.Password
import ar.com.develup.tateti.utils.ValidForm
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dmax.dialog.SpotsDialog


class ActividadInicial : AppCompatActivity() {

    companion object {
        private const val TAG = "ActividadInicial"
    }

    private lateinit var binding: ActividadInicialBinding

    lateinit var mAlertDialog: AlertDialog
    lateinit var mAuthProvider: AuthProvider
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var mFirebaseFirestore: FirebaseFirestore
    lateinit var mRemoteConfig: FirebaseRemoteConfig
    val GOOGLE_SIGN_IN: Int = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadInicialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAlertDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Cargando ...")
            .setCancelable(false).build()


        mAuthProvider = AuthProvider()
        mFirebaseFirestore = FirebaseFirestore.getInstance()


        mRemoteConfig = FirebaseRemoteConfig.getInstance()


        binding.btnInitSession.setOnClickListener { iniciarSesion() }
        binding.btnRegister.setOnClickListener { register() }
        binding.btnForgotPassword.setOnClickListener { forgotPassword() }
        iniciarSesionGoogle()


        if (isUserLogged()) {
            listGames()
            finish()
        }
        updateRemoteConfig()
    }



    private fun iniciarSesionGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("BLkBPbMqEyWGkW1_RI3VbwBE")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.btnLoginGoogle.setOnClickListener {
            signInGoogle()
        }

    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        //actionGoogleSingApi.launch(signInIntent)
         startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private val actionGoogleSingApi =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
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
                listGames()
            } else {
                userNewGoogle(id)

            }
        }
    }

    private fun userNewGoogle(id: String) {
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
                FirebaseCrashlytics.getInstance()
                    .log(TAG + "No se pudo almacenar la informacion del usuario")
                Toast.makeText(
                    this,
                    "No se pudo almacenar la informacion del usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun isUserLogged(): Boolean {
        // TODO-05-AUTHENTICATION
        return (mAuthProvider.getEmail() != null)
    }

    private fun listGames() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private fun register() {
        val intent = Intent(this, ActividadRegistracion::class.java)
        startActivity(intent)
    }

    private fun updateRemoteConfig() {
        mRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 6
        }
        mRemoteConfig.setConfigSettingsAsync(configSettings)
        configDefaultsRemoteConfig()
        configForgotPassword()
    }

    private fun configDefaultsRemoteConfig() {
        // TODO-04-REMOTECONFIG
        mRemoteConfig.setDefaultsAsync(R.xml.firebase_config_defaults);

    }

    private fun configForgotPassword() {
        // TODO-04-REMOTECONFIG
        // Obtener el valor de la configuracion para saber si mostrar
        // o no el boton de olvide mi contraseña

        mRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    val showPasswordBotton = mRemoteConfig.getBoolean("show_password_botton")

                    if (showPasswordBotton) {
                        binding.btnForgotPassword.visibility = View.VISIBLE

                    } else {
                        binding.btnForgotPassword.visibility = View.GONE

                    }
                    Log.d(TAG, "Config params updated: $updated")
                    Toast.makeText(
                        this, "Se activo la funcion recuperar contraseña",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(TAG, task.exception.toString())
                    FirebaseCrashlytics.getInstance().log(TAG + task.exception.toString())

                    Toast.makeText(
                        this, "fallo actualizacion",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }


    }

    private fun forgotPassword() {
        // Obtengo el mail
        val email = binding.editTextEmail.text.toString()
        // TODO-05-AUTHENTICATION

        // Si no completo el email, muestro mensaje de error

        if (ValidForm.validEmail(email) == Email.CORRECT_EMAIL) {
            resetPassword(email)
        } else {
            errorEmail(email)
        }
    }

    private fun errorEmail(email: String) {
        when (ValidForm.validEmail(email)) {


            Email.SHORT_EMAIL -> {
                Snackbar.make(
                    binding.rootView!!,
                    "Revisa el email, debe contener al menos  $ValidForm.MIN_EMAIL",
                    Snackbar.LENGTH_SHORT
                ).show()

            }

            Email.EMPTY_EMAIL -> {
                Snackbar.make(
                    binding.rootView!!,
                    "El email no puede ser vacio",
                    Snackbar.LENGTH_SHORT
                ).show()

            }

            Email.ERROR_EMAIL -> {
                Snackbar.make(
                    binding.rootView!!,
                    "El email no puede ser vacio",
                    Snackbar.LENGTH_SHORT
                ).show()

            }

            else -> {
                Log.e(TAG, "error al querer verificar el email ")

            }
        }
    }

    private fun errorPassword(
        password: String,
    ) {
        when (ValidForm.validPassword(password)) {

            Password.SHORT_PASSWORD -> {
                Snackbar.make(
                    binding.rootView!!,
                    "Revisa la contraseña, debe contener al menos  ${ValidForm.MIN_PASSWORD}",
                    Snackbar.LENGTH_SHORT
                ).show()

            }

            Password.EMPTY_PASSWORD -> {
                Snackbar.make(
                    binding.rootView!!,
                    "La contraseña no puede estar vacia",
                    Snackbar.LENGTH_SHORT
                ).show()

            }

            Password.ERROR_PASSWORD -> {
                Snackbar.make(
                    binding.rootView!!,
                    "Error en la contraseña",
                    Snackbar.LENGTH_SHORT
                ).show()

            }

            else -> {
                Log.e(TAG, "error al querer verificar el email ")

            }
        }

    }

        private fun resetPassword(email: String) {
            mAuthProvider.mAuth.setLanguageCode("es")
            mAuthProvider.mAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                mAlertDialog.show()
                if (it.isSuccessful) {

                    Snackbar.make(
                        binding.rootView!!,
                        "Se pudo enviar el correo",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()

                } else {

                    Snackbar.make(
                        binding.rootView!!,
                        "No se pudo enviar el correo",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
                mAlertDialog.dismiss()
            }
        }

        private fun iniciarSesion() {

            val email = binding.editTextEmail.text.toString();
            val password = binding.editTextPassword.text.toString();
            Log.d(TAG, "Campo email:$email")
            Log.d(TAG, "Campo password:$password")

            mAlertDialog.show()
            //TODO poner validForm


            if (ValidForm.validEmail(email) == Email.CORRECT_EMAIL) {
                if (ValidForm.validPassword(password) == Password.CORRECT_PASSWORD) {
                    authenticationFirebase(email, password)
                }else{
                    errorPassword(password)
                }
            } else {
                errorEmail(email)
            }



            mAlertDialog.dismiss()


        }


        private fun authenticationFirebase(email: String, password: String) {
            mAuthProvider.login(email, password).addOnCompleteListener {
                mAlertDialog.dismiss()
                if (it.isSuccessful) {
                    listGames()
                } else {

                    if (it.exception is FirebaseAuthInvalidUserException) {
                        Snackbar.make(
                            binding.rootView!!,
                            "El email o contraseña no son correctas ",
                            Snackbar.LENGTH_SHORT

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

                        Log.e(TAG, "error credencial $it")


                    }
                }

            }
        }


        private fun desloguearse() {
            // TODO-05-AUTHENTICATION
            // Hacer signOut de Firebase
        }


    }