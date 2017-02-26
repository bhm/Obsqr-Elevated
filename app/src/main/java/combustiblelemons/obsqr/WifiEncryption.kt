package combustiblelemons.obsqr

/**
 * Created by hiv on 2/26/17.
 */
enum class WifiEncryption {
    WEP, WPA, NO_PASS;

    fun matches(name: String): Boolean {
        return this.name.equals(name, true)
    }

    companion object {

        fun fromName(name: String): WifiEncryption {
            return WifiEncryption.values().firstOrNull { it.name == name } ?: NO_PASS
        }
    }
}