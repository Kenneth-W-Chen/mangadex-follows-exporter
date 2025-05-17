import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.swing.*

class ExporterUI : JFrame("Mangadex Follows Exporter") {
    //    val button: JButton = JButton("test")
    val mainPanel = JPanel()
    var usernameField: JTextField = JTextField()
    var passwordField: JTextField = JTextField()
    var apiClientIdField: JTextField = JTextField()
    var apiClientSecretField: JTextField = JTextField()
    var exportOptions: Array<JCheckBox> = arrayOf(
        JCheckBox("txt"),
        JCheckBox("csv"),
        JCheckBox("MangaUpdates")
    )
    var linksOptions: Array<JCheckBox> = arrayOf(
        JCheckBox("Amazon"),
        JCheckBox("AniList"),
        JCheckBox("Anime-Planet"),
        JCheckBox("Book Walker"),
        JCheckBox("CDJapan"),
        JCheckBox("eBookJapan"),
        JCheckBox("Kitsu"),
        JCheckBox("MangaUpdates"),
        JCheckBox("MyAnimeList"),
        JCheckBox("NovelUpdates"),
        JCheckBox("Official English"),
        JCheckBox("Raws"),
    )

    var languages: DefaultListModel<String> = DefaultListModel<String>()

    val settings: SettingsManager = SettingsManager()

    init {
        initializeFrame()
        addMDUserCredsSection()
        addSettingsSection()
//        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
//        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
//        button.bounds = Rectangle(15, 50, 50, 50)
//        button.addActionListener { println("test button clicked") }
//        panel.add(button)
//        add(panel, GridBagConstraints().apply {gridx = 0; gridy = 1; gridwidth = 4 })

        add(mainPanel)
        isVisible = true
    }

    private fun initializeFrame() {
        setSize(600, 600)
        minimumSize = Dimension(600, 800)
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
            settings.saveMDUserCredentials(
                mapOf(
                    SettingsManager.SecretsKeys.MD_USERNAME to usernameField.text,
                    SettingsManager.SecretsKeys.MD_PASSWORD to passwordField.text,
                    SettingsManager.SecretsKeys.MD_API_CLIENT_ID to apiClientIdField.text,
                    SettingsManager.SecretsKeys.MD_API_CLIENT_SECRET to apiClientSecretField.text,
                )
            )
            //todo put a message into the log text box
        }

        val loadButton = JButton("Load from File")
        loadButton.alignmentX = CENTER_ALIGNMENT
        loadButton.alignmentY = CENTER_ALIGNMENT
        loadButton.addActionListener {
            settings.loadUserCredentials()
            usernameField.text = settings.secrets[SettingsManager.SecretsKeys.MD_USERNAME.name].toString()
            passwordField.text = settings.secrets[SettingsManager.SecretsKeys.MD_PASSWORD.name].toString()
            apiClientIdField.text = settings.secrets[SettingsManager.SecretsKeys.MD_API_CLIENT_ID.name].toString()
            apiClientSecretField.text =
                settings.secrets[SettingsManager.SecretsKeys.MD_API_CLIENT_SECRET.name].toString()
            //todo put a message into the log text box

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
        val settingsPanel = JPanel()
        settingsPanel.layout = BoxLayout(settingsPanel, BoxLayout.X_AXIS)
        settingsPanel.alignmentX = CENTER_ALIGNMENT
        settingsPanel.alignmentY = TOP_ALIGNMENT

        // Export options
        val exportPanel = JPanel()
        exportPanel.layout = BoxLayout(exportPanel, BoxLayout.Y_AXIS)
        exportPanel.alignmentY = TOP_ALIGNMENT

        var exportLabel = JLabel("Export to")
        exportLabel.labelFor = exportPanel
        exportPanel.add(exportLabel)
        for (checkbox in exportOptions) {
            exportPanel.add(checkbox)
        }
        settingsPanel.add(exportPanel)

        // Links options
        var linksPanel = JPanel()
        linksPanel.layout = BoxLayout(linksPanel, BoxLayout.Y_AXIS)
        linksPanel.alignmentY = TOP_ALIGNMENT

        var linksLabel = JLabel("Links to Save")
        linksLabel.labelFor = linksPanel
        linksPanel.add(linksLabel)

        var linksCheckBoxPanel = JPanel()
        linksCheckBoxPanel.layout = BoxLayout(linksCheckBoxPanel, BoxLayout.Y_AXIS)
        for (linkCheckbox in linksOptions) {
            linksCheckBoxPanel.add(linkCheckbox)
        }
        val linksScrollPane: JScrollPane = JScrollPane(linksCheckBoxPanel)
        linksScrollPane.minimumSize = Dimension(150, 130)
        linksScrollPane.maximumSize = Dimension(150, 130)
        linksScrollPane.verticalScrollBar.unitIncrement = 10
        linksScrollPane.alignmentX = LEFT_ALIGNMENT
        linksPanel.add(linksScrollPane)
        settingsPanel.add(linksPanel)

        // Title locale options
        var titleLocaleMainPanel = JPanel()
        titleLocaleMainPanel.layout = BoxLayout(titleLocaleMainPanel, BoxLayout.Y_AXIS)
        titleLocaleMainPanel.alignmentY = TOP_ALIGNMENT

        var titleLabel = JLabel("Title Locale Preference")
        titleLabel.labelFor = titleLocaleMainPanel
        titleLocaleMainPanel.add(titleLabel)
        for (l in arrayOf("en", "ja-ro", "ja", "ko-ro", "ko", "zh-ro")) {
            languages.addElement(l)
        }

        var titleLocaleContentPanel = JPanel()
        titleLocaleContentPanel.layout = GridBagLayout()
        titleLocaleContentPanel.alignmentX = LEFT_ALIGNMENT
        titleLocaleContentPanel.border = BorderFactory.createLineBorder(Color.DARK_GRAY)
        titleLocaleContentPanel.maximumSize = Dimension(140, 130)

        var localeList = JList(languages)
        localeList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        var localeScrollPane: JScrollPane = JScrollPane(localeList)
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

        var upButton = JButton("UP")
        upButton.addActionListener {
            if(localeList.selectedIndex != 0){
                languages.swap(localeList.selectedIndex, localeList.selectedIndex-1)
                localeList.selectedIndex--
            }
        }

        var downButton = JButton("DOWN")
        downButton.addActionListener {
            if(localeList.selectedIndex != languages.size - 1){
                languages.swap(localeList.selectedIndex, localeList.selectedIndex+1)
                localeList.selectedIndex++
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
        settingsPanel.add(titleLocaleMainPanel)



        mainPanel.add(settingsPanel)
    }


}


fun<T> DefaultListModel<T>.swap(index1: Int, index2: Int) {
    set(index1, set(index2, elementAt(index1)))
}
