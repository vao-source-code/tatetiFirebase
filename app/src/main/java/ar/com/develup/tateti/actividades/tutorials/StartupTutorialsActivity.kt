package ar.com.develup.tateti.actividades.tutorials

import android.content.Intent
import ar.com.develup.tateti.R
import ar.com.develup.tateti.actividades.ActividadInicial
import ar.com.develup.tateti.modelo.SPManager
import ar.com.develup.tateti.modelo.SPManager.Companion.TUTORIAL

class StartupTutorialsActivity : AbstractTutorialsActivity() {
    override fun initializeFragments() {
        this.addFragment(
            R.drawable.logo_principal,
            "Bienvenido",
            "Gracias por instalar la aplicación móvil de Tateti.\nEsta aplicación fué creada por Victor Orue"
        )
        this.addFragment( //"#F1F1F1",
            R.drawable.tutorial_1,
            "Tateti",
            "Esta aplicación permite jugar a Tateti, también conocido como \"tres en raya\" o \"gato\", es un juego de mesa para dos jugadores que se juega en un tablero de 3x3 casillas."
        )
        this.addFragment( //"#F1F1F1",
            R.drawable.tutorial_3,
            "¿Como jugar?",
            "\n1) El Tateti es un juego para dos jugadores que se juega en un tablero de 3x3 casillas.\n" +
                    "2) Cada jugador utiliza un símbolo, generalmente una \"X\" o un círculo \"O\".\n" +
                    "3) El objetivo es conseguir colocar tres de tus símbolos en línea recta antes que el otro jugador lo haga.\n" +
                    "4) Los jugadores se turnan para colocar sus símbolos en una casilla vacía del tablero.\n" +
                    "5) El juego termina cuando un jugador consigue colocar tres de sus símbolos en línea recta o cuando se llenan todos los espacios del tablero sin que ningún jugador haya ganado."
        )
    }

    override fun handleLastFragmentClosed() {
        SPManager(applicationContext).add(TUTORIAL, false)
        val i = Intent(this@StartupTutorialsActivity, ActividadInicial::class.java)
        startActivity(i)
    }

}