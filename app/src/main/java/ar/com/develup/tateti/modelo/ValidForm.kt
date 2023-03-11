package ar.com.develup.tateti.modelo

import java.util.regex.Matcher
import java.util.regex.Pattern

object ValidForm {

    fun validEmail(email: String): Boolean {
        if (email.isEmpty()) return false
        val patrons: Pattern =
            Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val comparator: Matcher = patrons.matcher(email)
        return comparator.find()
    }

    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }


}