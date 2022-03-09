import java.util.*

object OSValidator {
    private val OS = System.getProperty("os.name").lowercase(Locale.getDefault())
    private var IS_WINDOWS = OS.indexOf("win") >= 0
    private var IS_MAC = OS.indexOf("mac") >= 0
    private var IS_UNIX = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0

    fun checkOS() : String {
        return if (IS_WINDOWS) {
            "Windows"
        } else if (IS_MAC) {
            "Mac"
        } else if (IS_UNIX) {
            "Linux"
        } else {
            "NOT SUPPORTED"
        }
    }
}