package ar.com.develup.tateti.modelo

import android.content.Context
import android.content.SharedPreferences


//TODO deberia ser un objeto y el contexto sacar de aplicacioncontext
class SPManager(val context: Context) {

    val PREFS_NAME = "register.preferences"

    companion object {
        val USER_NAME = "USER_NAME"
        val USER_EMAIL= "USER_EMAIL"
        val USER_PASSWORD = "USER_PASSWORD"
        val USER_PATH_FILE = "USER_PATH_FILE"
        val INIT = "INIT"
    }


    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)

    fun getSp(): SharedPreferences {
        return prefs
    }

    /**
     * Método para agregar un dato en las SharedPreferencies
     *
     * @param key   Nombre con el que se guarda el dato en las SharedPreferencies
     * @param value Objeto a almacenar
     */
    fun add(key: String?, value: Any?) {
        val editor: SharedPreferences.Editor = getSp().edit()
        if (value is String) {
            editor.putString(key, value as String?)
        }
        if (value is Long) {
            editor.putLong(key, value)
        }
        if (value is Int) {
            editor.putInt(key, value)
        }
        if (value is Double || value is Float) {
            editor.putFloat(key, value as Float)
        }
        if (value is Boolean) {
            editor.putBoolean(key, value)
        }

        editor.apply()
    }

    /**
     * Método que devuelve un String de las SharedPreferencies
     *
     * @param key           Nombre del dato a buscar
     * @param value_default Volor por default que devuelve si no encuentra la key
     * @return String con el valor del dato.
     */
    fun getString(key: String?, value_default: String?): String? {
        return getSp().getString(key, value_default)
    }

    /**
     * Método que devuelve un Long
     *
     * @param key           Nombre del dato a buscar
     * @param value_default Volor por default que devuelve si no encuentra la key
     * @return Long con el valor del dato.
     */
    fun getLong(key: String?, value_default: Long): Long? {
        return getSp().getLong(key, value_default)
    }

    /**
     * Método que devuelve un Int
     *
     * @param key           Nombre del dato a buscar
     * @param value_default Volor por default que devuelve si no encuentra la key
     * @return Int con el valor del dato.
     */
    fun getInt(key: String?, value_default: Int): Int {
        return getSp().getInt(key, value_default)
    }

    /**
     * Método que devuelve un float
     *
     * @param key           Nombre del dato a buscar
     * @param value_default Volor por default que devuelve si no encuentra la key
     * @return float con el valor del dato.
     */
    fun getFloat(key: String?, value_default: Float): Float {
        return getSp().getFloat(key, value_default)
    }

    /**
     * Método que devuelve un Boolean
     *
     * @param key           Nombre del dato a buscar
     * @param value_default Volor por default que devuelve si no encuentra la key
     * @return boolean con el valor del dato.
     */
    fun getBoolean(key: String?, value_default: Boolean): Boolean {
        return getSp().getBoolean(key, value_default)
    }

    /**
     * Método que limpia las SharedPreferencies
     */
    fun clean() {
        getSp().edit().clear().apply()
    }

    /**
     * Devuelve una clase a partir de un string
     *
     * @param class_name_string
     * @param default_class
     * @return
     */
    fun getStoredClass(class_name_string: String?, default_class: Class<*>?): Class<*>? {
        var class_name_string = class_name_string
        class_name_string = getString(class_name_string, null)
        if (class_name_string != null) {
            try {
                return Class.forName(class_name_string.replace("class ", ""))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return default_class
    }


    /**
     * Para obtener un valor guardado de una path_file(Se guarda en el CameraActivity)
     *
     * @return
     */
    fun imageStoredPath(): String? {
        return SPManager(context).getString("path_file", "")
    }

    fun saveImagePathFile(absolutePath: String) {
        SPManager(context).add("path_file", absolutePath)
    }
    /**
     * Remueve de las SP una key
     *
     * @param key
     */
    fun remove(key: String?) {
        getSp().edit().remove(key).apply()
    }
}