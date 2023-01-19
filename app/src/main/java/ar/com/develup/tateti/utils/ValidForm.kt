package ar.com.develup.tateti.utils


import java.util.regex.Matcher
import java.util.regex.Pattern

enum class Email {
    EMPTY_EMAIL, SHORT_EMAIL, ERROR_EMAIL, CORRECT_EMAIL
}

enum class Password {
    EMPTY_PASSWORD, SHORT_PASSWORD, ERROR_PASSWORD, CORRECT_PASSWORD,
}

object ValidForm {
    const val  MIN_EMAIL : Int  = 4
    const val  MIN_PASSWORD : Int  = 6


    fun validEmail(email: String): Email {
        if (email.isEmpty()) return Email.EMPTY_EMAIL
        if (email.length < MIN_EMAIL) return Email.SHORT_EMAIL
        val patrons: Pattern =
            Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val comparator: Matcher = patrons.matcher(email)
        return if (comparator.find()) {
            Email.CORRECT_EMAIL
        } else {
            Email.ERROR_EMAIL
        }
    }

    fun validPassword(password: String): Password {
        if (password.isEmpty()) return Password.EMPTY_PASSWORD
        if (password.length < MIN_PASSWORD) return Password.SHORT_PASSWORD
        val patrons: Pattern =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        val comparator: Matcher = patrons.matcher(password)
        return if (comparator.find()) {
            Password.CORRECT_PASSWORD
        } else {
            Password.ERROR_PASSWORD
        }
    }

    fun validName(name: String): Boolean {
        if (name.isEmpty()) return false
        if (name.length < 2) return false

        val pat = Pattern.compile("^[a-zA-Z0-9]{2,30}$")
        val mat = pat.matcher(name)
        return mat.matches()
    }

    fun validPhone(phone: String): Boolean {
        val pat = Pattern.compile("^\\d{8,11}\$")
        val mat = pat.matcher(phone)
        return mat.matches()

    }

    fun removeLastChar(str: String?): String? {
        return if (str == null || str.isEmpty()) {
            str
        } else str.substring(0, str.length - 1)
    }


}