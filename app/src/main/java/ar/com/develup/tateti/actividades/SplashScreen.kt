package ar.com.develup.tateti.actividades

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import ar.com.develup.tateti.actividades.ActividadPartidas
import ar.com.develup.tateti.databinding.ActivitySplashScreenBinding
import ar.com.develup.tateti.modelo.SPManager
import ar.com.develup.tateti.modelo.SPManager.Companion.INIT
import com.squareup.okhttp.internal.Version

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private var binding: ActivitySplashScreenBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Binding
        binding = ActivitySplashScreenBinding.inflate(
            layoutInflater
        )

        // Layout
        setContentView(binding!!.root)

        // Escondo la barra de arriba
        //  getSupportActionBar().hide();

        // Seteo la imagen del cliente
        setCLientLogo()

        // Abro la próxima activity luego de una cantidad de milisegundos
        if(VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            goToNextActivityAfterMilliseconds(10)
        } else {
            goToNextActivityAfterMilliseconds(200)
        }
    }

    /**
     * Se setea el trademark del cliente en base al dialect
     */
    private fun setCLientLogo() {
        binding!!.ivInfoilLogo.setImageResource(R.drawable.logo_principal)
    }

    /**
     * Opens the next activity after the designated milliseconds
     *
     * @param milliseconds
     */
    private fun goToNextActivityAfterMilliseconds(milliseconds: Int) {
        Log.d("Splash Screen", "Starting next activity in: $milliseconds")
        Handler(Looper.getMainLooper()).postDelayed({
            val i = Intent(this@SplashScreen, nextActivity())
            startActivity(i)
            finish()
        }, milliseconds.toLong())

    }

    /**
     * Determina la proxima activity luego del splash
     */
    private fun nextActivity(): Class<*> {
        return if (SPManager(applicationContext).getBoolean(
                INIT,
                false
            )
        ) ActividadPartidas::class.java else ActividadInicial::class.java
    }
}