package Utilities

import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import kotlin.collections.iterator

class SettingsManager {
    val secrets: Properties = Properties()
    val config: Properties = Properties()
    /*    init{
            secrets.load(FileInputStream("secrets.properties"))
            config.load(FileInputStream("config.properties"))
        }*/

    fun saveMDUserCredentials(credentials: Map<SecretsKeys, String>) {
        for ((key, value) in credentials) {
            secrets.setProperty(key.name, value)
        }
        FileOutputStream("secrets.properties", false).use { s ->
            secrets.store(s, null)
        }
    }

    fun loadUserCredentials() {
        FileInputStream("secrets.properties").use { s ->
            secrets.load(s)
        }
    }

    fun saveSettings(settings: Map<SettingsKeys, String>) {
        for((key, value) in settings) {
            config.setProperty(key.name, value)
        }
        FileOutputStream("config.properties", false).use { s ->
            config.store(s, null)
        }
    }

    fun loadSettings() {
        FileInputStream("config.properties").use { s ->
            config.load(s)
        }
    }

    enum class SecretsKeys{
        MD_USERNAME,
        MD_PASSWORD,
        MD_API_CLIENT_ID,
        MD_API_CLIENT_SECRET,
    }

    enum class SettingsKeys{
        EXPORT,
        LINKS,
        LOCALE_PREFERENCE,
        INITIAL_OFFSET,
        FETCH_LIMIT
    }
}