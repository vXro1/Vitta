package com.vitta.app.ui.common

/**
 * Reglas de validación compartidas (Informe de pruebas, bloque 1: HAL-02,
 * HAL-03, HAL-05, HAL-11). Se definen una sola vez y las usan el registro,
 * la cuenta, los formularios de hábito y la nota del día.
 *
 * Cada función devuelve null si el valor es válido o el mensaje que se
 * muestra debajo del campo.
 */
object VittaValidation {

    private const val LETRAS = "A-Za-zÁÉÍÓÚÜáéíóúüÑñ"

    /** HAL-02: al menos una letra; letras (con tildes y ñ), espacios y números; 2 a 50. */
    private val NOMBRE_RE = Regex("^(?=.*[$LETRAS])[${LETRAS}0-9 ]{2,50}$")

    /** HAL-03: formato con dominio y extensión de al menos 2 letras. */
    private val CORREO_RE = Regex("^[^\\s@]+@[^\\s@]+\\.[A-Za-z]{2,}$")

    /** HAL-05: el título del hábito solo acepta letras (con tildes y ñ) y espacios, 3 a 40. */
    private val TITULO_RE = Regex("^[$LETRAS ]{3,40}$")

    /** HAL-11: mínimo de caracteres de la nota del día. */
    const val NOTA_MIN = 5

    fun nombre(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ingresa tu nombre"
            v.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            v.length > 50 -> "El nombre puede tener máximo 50 caracteres"
            !NOMBRE_RE.matches(v) && v.none { it.isLetter() } -> "El nombre debe tener al menos una letra"
            !NOMBRE_RE.matches(v) -> "Usa solo letras, números y espacios"
            else -> null
        }
    }

    fun correo(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ingresa tu correo"
            !v.contains("@") -> "Falta la arroba, ej: nombre@correo.com"
            !CORREO_RE.matches(v) -> "Escribe un correo con dominio y extensión, ej: nombre@correo.com"
            else -> null
        }
    }

    fun tituloHabito(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ponle un nombre a tu hábito"
            v.length < 3 -> "El nombre debe tener al menos 3 letras"
            v.length > 40 -> "El nombre puede tener máximo 40 caracteres"
            !TITULO_RE.matches(v) -> "Usa solo letras y espacios (sin números ni símbolos)"
            else -> null
        }
    }

    /**
     * Contraseña (registro y cambio de contraseña): letras, al menos una
     * mayúscula y al menos un número. El mínimo de 8 caracteres lo exige
     * también el backend.
     */
    fun contrasena(valor: String): String? = when {
        valor.isBlank() -> "Ingresa una contraseña"
        valor.length < 8 -> "Mínimo 8 caracteres"
        valor.none { it.isLetter() } -> "Debe tener letras"
        valor.none { it.isUpperCase() } -> "Debe tener al menos una mayúscula"
        valor.none { it.isDigit() } -> "Debe tener al menos un número"
        else -> null
    }

    /** HAL-11: mínimo [NOTA_MIN] caracteres y al menos una letra o número. */
    fun nota(valor: String): String? {
        val v = valor.trim()
        return when {
            v.length < NOTA_MIN -> "Escribe al menos $NOTA_MIN caracteres"
            v.none { it.isLetterOrDigit() } -> "La nota debe tener letras o números"
            else -> null
        }
    }
}
