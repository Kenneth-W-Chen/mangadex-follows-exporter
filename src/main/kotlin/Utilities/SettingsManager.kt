package Utilities

import java.util.Properties
import kotlin.collections.iterator
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

/**
 * Class to manage application settings. Uses "~/md_exporter_secrets.properties" and "~/md_exporter_config.properties" for settings.
 * The secrets file has user credentials and log-in information for various services.
 * The config file has other, non-sensitive settings.
 */
class SettingsManager {
    /**
     * Object holding user credentials and log-in information.
     */
    val secrets: Properties = Properties()

    /**
     * Object holding non-sensitive settings.
     */
    val config: Properties = Properties()

    /**
     * Saves user credentials to the secrets file.
     * @param credentials A map with the corresponding secret mapped to [SecretsKeys].
     */
    fun saveUserCredentials(credentials: Map<SecretsKeys, String>) {
        loadUserCredentials()
        for ((key, value) in credentials) {
            secrets.setProperty(key.name, value)
        }
        Path(System.getProperty("user.home"), "md_exporter_secrets.properties").outputStream().use{
            secrets.store(it, null)
        }
    }

    /**
     * Loads user credentials from the disk into [secrets].
     */
    fun loadUserCredentials() {
        Path(System.getProperty("user.home"), "md_exporter_secrets.properties").inputStream().use{
            secrets.load(it)
        }
    }

    /**
     * Saves settings to the config file.
     * @param settings A map with the corresponding secret mapped to [SettingsKeys].
     */
    fun saveSettings(settings: Map<SettingsKeys, String>) {
        for((key, value) in settings) {
            config.setProperty(key.name, value)
        }
        Path(System.getProperty("user.home"), "md_exporter_config.properties").outputStream().use{
            config.store(it, null)
        }
    }

    /**
     * Loads settings from the disk into [config]
     */
    fun loadSettings() {
        Path(System.getProperty("user.home"), "md_exporter_config.properties").inputStream().use{
            config.load(it)
        }
    }

    /**
     * Enums representing the keys stored in the secrets.properties file.
     */
    enum class SecretsKeys{
        /**
         * MangaDex username.
         */
        MD_USERNAME,

        /**
         * MangaDex password.
         */
        MD_PASSWORD,

        /**
         * MangaDex API client ID.
         */
        MD_API_CLIENT_ID,

        /**
         * MangaDex API client secret.
         */
        MD_API_CLIENT_SECRET,

        /**
         * MangaUpdates username.
         */
        MU_USERNAME,

        /**
         * MangaUpdates password.
         */
        MU_PASSWORD
    }

    /**
     * Enums representing the keys stored in the config.properties file.
     */
    enum class SettingsKeys{
        /**
         * Where to export the manga list. See [ExportOptions].
         */
        EXPORT,

        /**
         * What links to save. See [Links].
         */
        LINKS,

        /**
         * The preference for which title to save for each manga. See [MangadexApi.Data.MangaInfo.toSimplifiedMangaInfo].
         */
        LOCALE_PREFERENCE,

        /**
         * The initial index to start fetching manga from. See [MangadexApi.Client.getAllFollowedManga].
         */
        INITIAL_OFFSET,

        /**
         * The number of titles to fetch per MangaDex API call. See [MangadexApi.Client.getAllFollowedManga].
         */
        FETCH_LIMIT,

        /**
         * How titles are added to a MangaUpdates list. See [MangaUpdatesAPI.Client.addTitlesToListById] and [MangaUpdatesAPI.Client.addTitlesToListByTitle].
         */
        MANGAUPDATES_IMPORT
    }
}