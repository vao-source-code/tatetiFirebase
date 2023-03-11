package ar.com.develup.tateti.actividades.tutorials

import ar.com.develup.tateti.R
import ar.com.develup.tateti.modelo.SPManager

class StartupTutorialsActivity : AbstractTutorialsActivity() {
    override fun initializeFragments() {
        this.addFragment(
            R.drawable.icon_next,
            "Bienvenido",
            "Gracias por instalar la aplicación móvil de InfoProd Zafiro.\nEsta aplicación fué creada por InfOil S.A."
        )
        this.addFragment( //"#F1F1F1",
            R.drawable.icon_next,
            "Zafiro Android",
            "Esta aplicación permite realizar carga de datos a Zafiro desde un dispositivo móvil. \nLa aplicación funciona de forma offline, permitiendo registrar los datos en ubicaciones sin conectividad. Estos datos son luego enviados a Zafiro cuando el dispositivo obtiene conexión."
        )
        this.addFragment( //"#F1F1F1",
            R.drawable.icon_next,
            "Habilitación del Dispositivo Móvil",
            "\nAntes de comenzar hay que realizar 3 pasos: \n1) Ingresar el código de seguridad (única vez). \n2) Ingresar la dirección del servidor Zafiro. \n3) Solicitar la habilitación del movil en Zafiro. \n\nAnte cualquier duda contacte a un administrador de su empresa."
        )
    }

    override fun handleLastFragmentClosed() {
        SPManager(applicationContext).add(SHOULD_SHOW_SECURITY_PIN_TUTORIAL, false)
    }

    companion object {
        var SHOULD_SHOW_SECURITY_PIN_TUTORIAL = "SHOULD_SHOW_SECURITY_PIN_TUTORIAL"
    }
}