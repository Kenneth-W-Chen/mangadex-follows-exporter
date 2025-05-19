import MangadexApi.Data.MangaInfoResponse
import MangadexApi.Data.SimplifiedMangaInfo
import Utilities.ExportOptions
import Utilities.Links
import Utilities.SettingsManager
import Utilities.exportMangaList
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.util.EnumSet
import java.util.concurrent.CompletableFuture
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import kotlin.text.toInt

class ExporterUI : JFrame("Mangadex Follows Exporter") {
    val windowMinWidth: Int = 600
    val windowMinHeight: Int = 800

    val mainPanel = JPanel()
    var usernameField: JTextField = JTextField()
    var passwordField: JTextField = JTextField()
    var apiClientIdField: JTextField = JTextField()
    var apiClientSecretField: JTextField = JTextField()
    var exportOptionCheckboxes: Array<JCheckBox> = arrayOf(
        JCheckBox("txt"),
        JCheckBox("csv"),
        JCheckBox("MangaUpdates")
    )
    val exportAllCheckBox = JCheckBox("All")

    var linksOptionCheckboxes: Array<JCheckBox> = Links.entries.map({ l -> JCheckBox(l.canonicalName) }).toTypedArray()
    val linksAllCheckBox = JCheckBox("All")

    var locales: DefaultListModel<String> = DefaultListModel<String>()
    val initialOffsetField: JTextField = JTextField()
    val fetchLimitField: JTextField = JTextField()

    val logArea: JTextPane = object: JTextPane(){
        override fun getScrollableTracksViewportWidth(): Boolean {
            return true
        }
    }

    val settings: SettingsManager = SettingsManager()
    lateinit var runWorker: MangadexApiClientWorker

    init {
        initializeFrame()
        addMDUserCredsSection()
        addSettingsSection()
        addRunSection()
        add(mainPanel)
        isVisible = true
    }

    private fun initializeFrame() {
        setSize(800, 600)
        minimumSize = Dimension(windowMinWidth, windowMinHeight)
        defaultCloseOperation = EXIT_ON_CLOSE
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

    }

    private fun addMDUserCredsSection() {
        val userCredsPanel = JPanel()
        userCredsPanel.layout = GridLayout(2, 2, 20, 0)

        // Section header
        val userCredsHeader = JLabel("MangaDex Log-In Information")
        userCredsHeader.labelFor = userCredsPanel
        userCredsHeader.font = Font("Sans Serif", Font.BOLD, 20)
        userCredsHeader.alignmentX = CENTER_ALIGNMENT
        mainPanel.add(userCredsHeader)

        // Text Fields
        userCredsPanel.add(getFieldLayoutPanel("Username", usernameField))
        userCredsPanel.add(getFieldLayoutPanel("Password", passwordField))
        userCredsPanel.add(getFieldLayoutPanel("API Client ID", apiClientIdField))
        userCredsPanel.add(getFieldLayoutPanel("API Client Secret", apiClientSecretField))
        userCredsPanel.maximumSize = Dimension(400, 100)
        userCredsPanel.alignmentX = CENTER_ALIGNMENT
        mainPanel.add(userCredsPanel)


        // Save Load Buttons Panel
        val saveLoadPanel = JPanel()
        saveLoadPanel.layout = BoxLayout(saveLoadPanel, BoxLayout.X_AXIS)
        saveLoadPanel.alignmentY = CENTER_ALIGNMENT
        saveLoadPanel.alignmentX = CENTER_ALIGNMENT

        val saveButton = JButton("Save to File")
        saveButton.alignmentX = CENTER_ALIGNMENT
        saveButton.alignmentY = CENTER_ALIGNMENT
        saveButton.addActionListener {
            logArea.append("Saving MangaDex user credentials....\n")
            settings.saveMDUserCredentials(
                mapOf(
                    SettingsManager.SecretsKeys.MD_USERNAME to usernameField.text,
                    SettingsManager.SecretsKeys.MD_PASSWORD to passwordField.text,
                    SettingsManager.SecretsKeys.MD_API_CLIENT_ID to apiClientIdField.text,
                    SettingsManager.SecretsKeys.MD_API_CLIENT_SECRET to apiClientSecretField.text,
                )
            )
            logArea.append("Saved.\n")
        }

        val loadButton = JButton("Load from File")
        loadButton.alignmentX = CENTER_ALIGNMENT
        loadButton.alignmentY = CENTER_ALIGNMENT
        loadButton.addActionListener {
            logArea.append("Loading MangaDex user credentials....\n")
            settings.loadUserCredentials()
            usernameField.text = settings.secrets[SettingsManager.SecretsKeys.MD_USERNAME.name].toString()
            passwordField.text = settings.secrets[SettingsManager.SecretsKeys.MD_PASSWORD.name].toString()
            apiClientIdField.text = settings.secrets[SettingsManager.SecretsKeys.MD_API_CLIENT_ID.name].toString()
            apiClientSecretField.text = settings.secrets[SettingsManager.SecretsKeys.MD_API_CLIENT_SECRET.name].toString()
            logArea.append("Loaded.\n")
        }

        val filler = Box.Filler(
            Dimension(20, 100),
            Dimension(50, 100),
            Dimension(100, 100)
        )
        filler.alignmentX = CENTER_ALIGNMENT
        filler.alignmentY = CENTER_ALIGNMENT
        saveLoadPanel.add(saveButton)
        saveLoadPanel.add(filler)
        saveLoadPanel.add(loadButton)

        mainPanel.add(saveLoadPanel)
    }

    private fun getFieldLayoutPanel(label: String, textField: JTextField): JPanel {
        val layoutPanel = JPanel()
        layoutPanel.layout = BoxLayout(layoutPanel, BoxLayout.Y_AXIS)
        layoutPanel.maximumSize = Dimension(300, 50)
        val label = JLabel(label)
        label.labelFor = textField
        label.maximumSize = Dimension(100, 50)
        textField.maximumSize = Dimension(400, 20)
        layoutPanel.add(label)
        layoutPanel.add(textField)

        return layoutPanel
    }

    private fun addSettingsSection() {
        val settingsContentPanel = JPanel()
        settingsContentPanel.layout = BoxLayout(settingsContentPanel, BoxLayout.X_AXIS)
        settingsContentPanel.alignmentX = CENTER_ALIGNMENT
        settingsContentPanel.alignmentY = TOP_ALIGNMENT
        val filler = Box.Filler(
            Dimension(20,20),
            Dimension(20,20),
            Dimension(20,20)
        )
        filler.alignmentX = CENTER_ALIGNMENT
        filler.alignmentY = CENTER_ALIGNMENT

        settingsContentPanel.add(getExportOptionsPanel())
        settingsContentPanel.add(filler.copy())
        settingsContentPanel.add(getLinkOptionsPanel())
        settingsContentPanel.add(filler.copy())
        settingsContentPanel.add(getLocaleOptionsPanel())
        settingsContentPanel.add(filler.copy())
        settingsContentPanel.add(getAPIOptionsPanel())

        val settingsMainPanel = JPanel()
        settingsMainPanel.layout = BoxLayout(settingsMainPanel, BoxLayout.Y_AXIS)
        settingsMainPanel.alignmentX = CENTER_ALIGNMENT
        settingsMainPanel.alignmentY = TOP_ALIGNMENT
        settingsMainPanel.add(settingsContentPanel)
        settingsMainPanel.add(filler.copy())
        // settings save/load buttons
        val settingsSubcontentPanel = JPanel()
        settingsSubcontentPanel.layout = BoxLayout(settingsSubcontentPanel, BoxLayout.X_AXIS)
        settingsSubcontentPanel.alignmentX = CENTER_ALIGNMENT
        settingsSubcontentPanel.alignmentY = TOP_ALIGNMENT
        settingsMainPanel.add(settingsSubcontentPanel)

        val saveButton = JButton("Save Settings")
        saveButton.alignmentX = CENTER_ALIGNMENT
        saveButton.alignmentY = CENTER_ALIGNMENT
        saveButton.addActionListener {
            logArea.append("Saving settings....\n")
            var exportSetting: String = ""
            for (exportOptionCheckbox in exportOptionCheckboxes) {
                if (exportOptionCheckbox.isSelected)
                    exportSetting += exportOptionCheckbox.text + ","
            }
            exportSetting = exportSetting.removeSuffix(",")
            var linksSetting: String = ""
            for (linkCheckbox in linksOptionCheckboxes) {
                if (linkCheckbox.isSelected)
                    linksSetting += linkCheckbox.text + ","
            }
            linksSetting = linksSetting.removeSuffix(",")

            var localePreferenceSetting: String = ""
            for (i in 0..locales.size() - 1) {
                localePreferenceSetting += locales.get(i) + ","
            }
            localePreferenceSetting = localePreferenceSetting.removeSuffix(",")

            settings.saveSettings(
                mapOf(
                    SettingsManager.SettingsKeys.EXPORT to exportSetting,
                    SettingsManager.SettingsKeys.LINKS to linksSetting,
                    SettingsManager.SettingsKeys.LOCALE_PREFERENCE to localePreferenceSetting,
                    SettingsManager.SettingsKeys.INITIAL_OFFSET to initialOffsetField.text,
                    SettingsManager.SettingsKeys.FETCH_LIMIT to fetchLimitField.text,
                )
            )
            logArea.append("Saved.\n")
        }
        settingsSubcontentPanel.add(saveButton)
        settingsSubcontentPanel.add(filler.copy())
        val loadButton = JButton("Load Settings")
        loadButton.alignmentX = CENTER_ALIGNMENT
        loadButton.alignmentY = CENTER_ALIGNMENT
        loadButton.addActionListener {
            logArea.append("Loading settings....\n")
            settings.loadSettings()
            val exportOptions = settings.config.get(SettingsManager.SettingsKeys.EXPORT.name).toString().split(",")
            if (exportOptions.isNotEmpty()) {
                exportOptionCheckboxes.forEach { it.isSelected = exportOptions.contains(it.text) }
                if (exportOptionCheckboxes.any({ !it.isSelected })) {
                    exportAllCheckBox.isSelected = false

                } else {
                    exportAllCheckBox.isSelected = true
                }
            }

            val linksOptions = settings.config.get(SettingsManager.SettingsKeys.LINKS.name).toString().split(",")
            if (linksOptions.isNotEmpty()) {
                linksOptionCheckboxes.forEach { it.isSelected = linksOptions.contains(it.text) }
                if (linksOptionCheckboxes.any({ !it.isSelected })) {
                    linksAllCheckBox.isSelected = false
                } else {
                    linksAllCheckBox.isSelected = true
                }
            }
            val localePreferenceOption = settings.config.get(SettingsManager.SettingsKeys.LOCALE_PREFERENCE.name).toString().split(",")
            if (localePreferenceOption.isNotEmpty()) {
                if (locales.size() > localePreferenceOption.size) {
                    locales.removeRange(localePreferenceOption.size, localePreferenceOption.size)
                }
                for (i in 0..locales.size() - 1) {
                    locales[i] = localePreferenceOption[i]
                }
                if (locales.size() < localePreferenceOption.size) {
                    locales.addAll(localePreferenceOption.subList(locales.size, localePreferenceOption.size))
                }
            }

            initialOffsetField.text = settings.config.get(SettingsManager.SettingsKeys.INITIAL_OFFSET.name).toString()
            fetchLimitField.text = settings.config.get(SettingsManager.SettingsKeys.FETCH_LIMIT.name).toString()
            logArea.append("Loaded.\n")
        }
        settingsSubcontentPanel.add(loadButton)
        mainPanel.add(settingsMainPanel)
    }

    private fun getExportOptionsPanel(): JPanel {
        // Export options
        val exportPanel = JPanel()
        exportPanel.layout = BoxLayout(exportPanel, BoxLayout.Y_AXIS)
        exportPanel.alignmentY = TOP_ALIGNMENT

        val exportLabel = JLabel("Export to")
        exportLabel.labelFor = exportPanel
        exportPanel.add(exportLabel)
        for (checkbox in exportOptionCheckboxes) {
            exportPanel.add(checkbox)
            checkbox.addActionListener {
                if (!checkbox.isSelected) {
                    exportAllCheckBox.isSelected = false
                } else {
                    exportAllCheckBox.isSelected = exportOptionCheckboxes.all({ it.isSelected })
                }
            }
        }
        exportAllCheckBox.addActionListener {
            for (checkbox in exportOptionCheckboxes) {
                checkbox.isSelected = exportAllCheckBox.isSelected
            }
        }
        exportPanel.add(exportAllCheckBox)

        return exportPanel
    }

    private fun getLinkOptionsPanel(): JPanel {

        val linksPanel = JPanel()
        linksPanel.layout = BoxLayout(linksPanel, BoxLayout.Y_AXIS)
        linksPanel.alignmentY = TOP_ALIGNMENT

        val linksLabel = JLabel("Links to Save")
        linksLabel.labelFor = linksPanel
        linksPanel.add(linksLabel)

        val linksCheckBoxPanel = JPanel()
        linksCheckBoxPanel.layout = BoxLayout(linksCheckBoxPanel, BoxLayout.Y_AXIS)
        linksAllCheckBox.toolTipText = "Select to save all of the links. Deselect to save none of the links."
        linksAllCheckBox.addActionListener {
            linksOptionCheckboxes.forEach { checkbox -> checkbox.isSelected = linksAllCheckBox.isSelected }
        }
        linksCheckBoxPanel.add(linksAllCheckBox)
        for (linkCheckbox in linksOptionCheckboxes) {
            linksCheckBoxPanel.add(linkCheckbox)
        }
        val linksScrollPane: JScrollPane = JScrollPane(linksCheckBoxPanel)
        linksScrollPane.minimumSize = Dimension(150, 130)
        linksScrollPane.maximumSize = Dimension(150, 170)
        linksScrollPane.verticalScrollBar.unitIncrement = 10
        linksScrollPane.alignmentX = LEFT_ALIGNMENT
        linksPanel.add(linksScrollPane)
        return linksPanel
    }

    private fun getLocaleOptionsPanel(): JPanel {
        val titleLocaleMainPanel = JPanel()
        titleLocaleMainPanel.layout = BoxLayout(titleLocaleMainPanel, BoxLayout.Y_AXIS)
        titleLocaleMainPanel.alignmentY = TOP_ALIGNMENT

        // panel header
        val titleLabel = JLabel("Title Locale Preference")
        titleLabel.labelFor = titleLocaleMainPanel
        titleLocaleMainPanel.add(titleLabel)

        // list of locales
        for (l in arrayOf("ja", "ja-ro", "ko", "ko-ro", "zh","zh-hk","zh-ro","en")) {
            locales.addElement(l)
        }

        val titleLocaleContentPanel = JPanel()
        titleLocaleContentPanel.layout = GridBagLayout()
        titleLocaleContentPanel.alignmentX = LEFT_ALIGNMENT
        titleLocaleContentPanel.border = BorderFactory.createLineBorder(Color.DARK_GRAY)
        titleLocaleContentPanel.maximumSize = Dimension(140, 130)

        val localeJList = JList(locales)
        localeJList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val localeScrollPane: JScrollPane = JScrollPane(localeJList)
        localeScrollPane.minimumSize = Dimension(70, 130)
        localeScrollPane.maximumSize = Dimension(70, 130)
        localeScrollPane.verticalScrollBar.unitIncrement = 10

        titleLocaleContentPanel.add(localeScrollPane, GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            gridheight = 2
            weightx = 0.5
            weighty = 1.0
        })

        // list interaction buttons
        val upButton = JButton("UP")
        upButton.addActionListener {
            if (localeJList.selectedIndex >= 0) {
                locales.swap(localeJList.selectedIndex, localeJList.selectedIndex - 1)
                localeJList.selectedIndex--
            }
        }
        titleLocaleContentPanel.add(upButton, GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
            gridheight = 1
            weightx = 0.5
            weighty = 0.5
            fill = GridBagConstraints.BOTH
            ipadx = 3
        })

        val downButton = JButton("DOWN")
        downButton.addActionListener {
            if (localeJList.selectedIndex != locales.size - 1 && localeJList.selectedIndex >= 0) {
                locales.swap(localeJList.selectedIndex, localeJList.selectedIndex + 1)
                localeJList.selectedIndex++
            }
        }

        titleLocaleContentPanel.add(downButton, GridBagConstraints().apply {
            gridx = 1
            gridy = 1
            gridwidth = 1
            gridheight = 1
            weightx = 0.5
            weighty = 0.5
            ipadx = 3
            fill = GridBagConstraints.BOTH
        })
        titleLocaleMainPanel.add(titleLocaleContentPanel)

        // isnertion panel
        val titleLocaleSubContentPanel = JPanel()
        titleLocaleSubContentPanel.layout = GridBagLayout()
        titleLocaleSubContentPanel.maximumSize = Dimension(140, 40)
        titleLocaleSubContentPanel.alignmentX = LEFT_ALIGNMENT

        // insertion panel header
        val addLabel = JLabel("Add Locale")
        titleLocaleSubContentPanel.add(addLabel, GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.NONE
            weightx = 0.75
            weighty = 0.5
            anchor = GridBagConstraints.LAST_LINE_START
        })

        // insertion text field
        val addLocaleField = JTextField()
        addLocaleField.maximumSize = Dimension(140, 30)
        addLabel.labelFor = addLocaleField
        titleLocaleSubContentPanel.add(addLocaleField, GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 1
            gridheight = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 0.75
            weighty = 0.5
            anchor = GridBagConstraints.FIRST_LINE_START
        })
        val insert: Action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                if (addLocaleField.text.isNotEmpty() && !locales.contains(addLocaleField.text)) {
                    var index = localeJList.selectedIndex
                    if (index < 0) {
                        index = 0
                    }
                    locales.insertElementAt(addLocaleField.text, index)
                }
            }
        }
        addLocaleField.addActionListener(insert)

        // insert button
        val addLocaleButton = JButton("+")
        addLocaleButton.addActionListener(insert)

        titleLocaleSubContentPanel.add(addLocaleButton, GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
            gridheight = 2
            fill = GridBagConstraints.BOTH
            weightx = 0.25
            weighty = 1.0
            anchor = GridBagConstraints.LINE_END
        })

        titleLocaleMainPanel.add(titleLocaleSubContentPanel)
        return titleLocaleMainPanel
    }

    private fun getAPIOptionsPanel(): JPanel {
        val apiOptionsPanel = JPanel()
        apiOptionsPanel.layout = BoxLayout(apiOptionsPanel, BoxLayout.Y_AXIS)
        apiOptionsPanel.alignmentX = CENTER_ALIGNMENT
        apiOptionsPanel.alignmentY = TOP_ALIGNMENT
        // panel header
        val panelLabel = JLabel("API Options")
        panelLabel.labelFor = apiOptionsPanel
        apiOptionsPanel.add(panelLabel)

        val apiOptionsContentPanel = JPanel()
        apiOptionsContentPanel.layout = BoxLayout(apiOptionsContentPanel, BoxLayout.Y_AXIS)
        apiOptionsContentPanel.border = EmptyBorder(0, 10, 0, 0)

        // offset setting
        val offsetLabel = JLabel("Initial Offset")
        offsetLabel.labelFor = initialOffsetField
        apiOptionsContentPanel.add(offsetLabel)
        initialOffsetField.toolTipText =
            "Sets the starting point for the fetching process. Setting it to 0 fetches everything."
        initialOffsetField.maximumSize = Dimension(100, 30)
        initialOffsetField.margin = Insets(0, 10, 0, 0)
        initialOffsetField.alignmentX = LEFT_ALIGNMENT
        apiOptionsContentPanel.add(initialOffsetField)

        // fetch limit setting
        val fetchLimitLabel = JLabel("Fetch Limit")
        fetchLimitLabel.labelFor = fetchLimitField

        fetchLimitField.toolTipText =
            "Sets a limit on how many manga are fetched per API call. Minimum is 1, and the maximum is 100."
        fetchLimitField.maximumSize = Dimension(100, 30)
        fetchLimitField.alignmentX = LEFT_ALIGNMENT
        apiOptionsContentPanel.add(fetchLimitLabel)
        apiOptionsContentPanel.add(fetchLimitField)

        apiOptionsPanel.add(apiOptionsContentPanel)
        return apiOptionsPanel
    }

    private fun addRunSection() {
        mainPanel.add(Box.Filler(Dimension(20,20),Dimension(20,20),Dimension(20,20)))
        val logPanel = JPanel()
        logPanel.layout = BoxLayout(logPanel, BoxLayout.Y_AXIS)
        logPanel.border = EmptyBorder(10, 10, 10, 10)
        logPanel.minimumSize = Dimension(600, 400)
        mainPanel.add(logPanel)

        val runButton = JButton("Run")
        runButton.alignmentX = CENTER_ALIGNMENT
        runButton.maximumSize = Dimension(1170, 30)
        runButton.addActionListener {
            if(this::runWorker.isInitialized && runWorker.running) {
                logArea.append("Already running.\n", LogType.WARNING)
                return@addActionListener
            }
            if(usernameField.text.isEmpty()) {
                logArea.append("Username is empty.\n", LogType.ERROR)
                return@addActionListener
            }
            if(passwordField.text.isEmpty()) {
                logArea.append("Password is empty.\n", LogType.ERROR)
                return@addActionListener
            }
            if(apiClientIdField.text.isEmpty()) {
                logArea.append("API client id is empty.\n", LogType.ERROR)
                return@addActionListener
            }
            if(apiClientSecretField.text.isEmpty()) {
                logArea.append("API client secret is empty.\n", LogType.ERROR)
                return@addActionListener
            }
            if(exportOptionCheckboxes.none({it.isSelected})){
                logArea.append("No export option selected.\n", LogType.ERROR)
                return@addActionListener
            }
            var fetchLimit = fetchLimitField.text.toIntOrNull()
            if(fetchLimit == null){
                if(fetchLimitField.text.isNotEmpty()){
                    logArea.append("Fetch limit wasn't set to a number.\n", LogType.ERROR)
                    return@addActionListener
                }
                fetchLimit = 100
            }
            var initialOffset = initialOffsetField.text.toIntOrNull()
            if(initialOffset == null){
                if(initialOffsetField.text.isNotEmpty()){
                    logArea.append("Initial offset wasn't set to a number.\n", LogType.ERROR)
                    return@addActionListener
                }
                initialOffset = 0
            }
            val exportOptions = EnumSet.noneOf(ExportOptions::class.java)
            for(checkbox in exportOptionCheckboxes){
                if(checkbox.isSelected)
                    exportOptions.add(ExportOptions.entries.find { it.name == checkbox.text.uppercase() })
            }
            println(exportOptions)
            val saveLinks = EnumSet.noneOf(Links::class.java)
            for(checkbox in linksOptionCheckboxes){
                if(checkbox.isSelected)
                    saveLinks.add(Links.entries.find{it.canonicalName == checkbox.text})
            }
            println(saveLinks)
            runWorker = MangadexApiClientWorker(
                MangadexApi.Client(usernameField.text, passwordField.text, apiClientIdField.text, apiClientSecretField.text),
                logArea,
                fetchLimit,
                initialOffset,
                locales.toStringArray(),
                "My_MangaDex_Follows",
                exportOptions,
                saveLinks
            )
            runWorker.execute()
        }
        logPanel.add(runButton)

        logPanel.add(Box.Filler(Dimension(10,10),Dimension(10,10),Dimension(10,10)))

        logArea.isEditable = false
        logArea.border = CompoundBorder(LineBorder(Color.LIGHT_GRAY, 1), EmptyBorder(5, 5, 5, 5))

        val scrollPane = JScrollPane(logArea)
        scrollPane.maximumSize = Dimension(1200, 200)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.border = null
        logPanel.add(scrollPane)
    }
}

class MangadexApiClientWorker(
    private val client: MangadexApi.Client,
    private val logger:JTextPane,
    private val fetchLimit: Int,
    private val initialOffset: Int,
    private val localePreference: Array<String>,
    private val fileName: String,
    private val exportOptions: EnumSet<ExportOptions>,
    private val saveLinks: EnumSet<Links>
): SwingWorker<Boolean, Pair<String, LogType>>(){
    public var running: Boolean = true

    override fun doInBackground(): Boolean {
        running = true
        val list = fetchTitles().get()
        publish(Pair("Exporting list...\n", LogType.STANDARD))
        exportMangaList(list, fileName, saveLinks, exportOptions)
        return true
    }

    override fun process(chunks: List<Pair<String, LogType>?>?) {
        if(chunks.isNullOrEmpty()) return
        for(pair in chunks){
            if(pair == null) continue
            logger.append(pair.first,pair.second)
        }
    }

    override fun done() {
        running = false
        publish(Pair("Done running", LogType.STANDARD))
        super.done()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun fetchTitles(): CompletableFuture<MutableList<SimplifiedMangaInfo>> = GlobalScope.future {
        client.fetchTokens()
        delay(1000)
        var currentOffset: Int = initialOffset
        var expectedTotal: Int = 99
        var emptyDataReturned: Boolean = false
        var stepCount: Int = 0  // purely statistical number... non-essential
        var apiCalls: Int = 0 // stat
        var mangaList: MutableList<SimplifiedMangaInfo> = mutableListOf()
        do {
            for (i in 1..5) {
                publish(Pair("Current index: $currentOffset\n",LogType.STANDARD))
                var response: HttpResponse = client.getFollowedMangaList(fetchLimit, currentOffset)
                apiCalls++
                if (response.status == HttpStatusCode.TooManyRequests) {
                    publish(Pair("Reached ratelimit for API call $apiCalls. Response headers: \n" + response.headers.toString() + "\n",LogType.WARNING))
                    try {
                        var currentPeriodEnd: Int = (response.headers.get("RateLimit-Retry-After") ?: response.headers.get("X-RateLimit-Retry-After") ?: "60000").toInt()
                        val waitTime = System.currentTimeMillis() - currentPeriodEnd + 1
                        publish(Pair("Waiting $waitTime milliseconds.\n",LogType.STANDARD))
                        delay(waitTime)
                    } catch (e: NumberFormatException) {
                        delay(60000)
                        publish(Pair("Waiting 60000 milliseconds.\n",LogType.STANDARD))
                    }
                } else if (response.status.isSuccess()) {
                    val responseBody = response.body<MangaInfoResponse>()
                    emptyDataReturned = responseBody.data.isEmpty()
                    expectedTotal = responseBody.total
                    for(manga in responseBody.data){
                        mangaList.add(manga.toSimplifiedMangaInfo(localePreference))
                    }
                    publish(Pair("Successful response ($stepCount): Received " + responseBody.data.size + " titles.\n",LogType.STANDARD))
                    currentOffset += responseBody.data.size
                    stepCount++

                } else {
                    publish(Pair("Unexpected HTTP Response: ${response.status}",LogType.ERROR))
                }

                if(currentOffset >= expectedTotal) {
                    break
                }
            }

            delay(1000)
        } while (currentOffset <= expectedTotal && !emptyDataReturned)
        publish(Pair("Finished fetching titles. Stats:\n\tExpected total: $expectedTotal\n\tReceived: ${mangaList.size}\n\tNumber of API calls:$apiCalls\n\tNumber of successful API calls: $stepCount",LogType.STANDARD))
        mangaList
    }

}


fun <T> DefaultListModel<T>.swap(index1: Int, index2: Int) {
    set(index1, set(index2, elementAt(index1)))
}

fun Box.Filler.copy(): Box.Filler{
    val copy = Box.Filler(this.minimumSize, this.preferredSize, this.maximumSize)
    copy.alignmentX = this.alignmentX
    copy.alignmentY = this.alignmentY
    return copy
}

fun JTextPane.append(string: String, logType: LogType = LogType.STANDARD){
    val style: Style?
    when(logType){
        LogType.STANDARD -> {
            style = null
        }
        LogType.WARNING -> {
            style = addStyle("Color Style", null)
            StyleConstants.setForeground(style, Color.YELLOW)
        }
        LogType.ERROR -> {
            style = addStyle("Color Style", null)
            StyleConstants.setForeground(style, Color.RED)
        }
    }
    styledDocument.insertString(styledDocument.length, string,style)
}

fun DefaultListModel<String>.toStringArray(): Array<String> {
    return Array<String>(this.size, {i -> "" + this[i]})
}

enum class LogType{
    STANDARD,
    WARNING,
    ERROR,
}