package combustiblelemons.obsqr

/**
 * Created by hiv on 2/26/17.
 */
data class WifiInfo(
        val ssid: String,
        val pass: String,
        val encryption: WifiEncryption,
        val hidden: Boolean
)