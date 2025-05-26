package Utilities

import java.util.Properties
import kotlin.collections.iterator
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class SettingsManager {
    val secrets: Properties = Properties()
    val config: Properties = Properties()
    /*    init{
            secrets.load(FileInputStream("secrets.properties"))
            config.load(FileInputStream("config.properties"))
        }*/

    fun saveUserCredentials(credentials: Map<SecretsKeys, String>) {
        loadUserCredentials()
        for ((key, value) in credentials) {
            secrets.setProperty(key.name, value)
        }
        Path(System.getProperty("user.home"), "md_exporter_secrets.properties").outputStream().use{
            secrets.store(it, null)
        }
    }

    fun loadUserCredentials() {
        Path(System.getProperty("user.home"), "md_exporter_secrets.properties").inputStream().use{
            secrets.load(it)
        }
    }

    fun saveSettings(settings: Map<SettingsKeys, String>) {
        for((key, value) in settings) {
            config.setProperty(key.name, value)
        }
        Path(System.getProperty("user.home"), "md_exporter_config.properties").outputStream().use{
            config.store(it, null)
        }
    }

    fun loadSettings() {
        Path(System.getProperty("user.home"), "md_exporter_config.properties").inputStream().use{
            config.load(it)
        }
    }

    enum class SecretsKeys{
        MD_USERNAME,
        MD_PASSWORD,
        MD_API_CLIENT_ID,
        MD_API_CLIENT_SECRET,
        MU_USERNAME,
        MU_PASSWORD
    }

    enum class SettingsKeys{
        EXPORT,
        LINKS,
        LOCALE_PREFERENCE,
        INITIAL_OFFSET,
        FETCH_LIMIT
    }
}