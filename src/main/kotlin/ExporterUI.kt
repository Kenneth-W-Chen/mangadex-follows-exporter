import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import javax.swing.*

class ExporterUI : JFrame("Mangadex Follows Exporter") {
//    val button: JButton = JButton("test")
    val mainPanel = JPanel()
    var usernameField: JTextField = JTextField()
    var passwordField: JPasswordField = JPasswordField()
    var apiClientIdField: JTextField = JTextField()
    var apiClientSecretField: JPasswordField = JPasswordField()

    init {
        initializeFrame()
        addUserCredsSection()
//        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
//        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
//        button.bounds = Rectangle(15, 50, 50, 50)
//        button.addActionListener { println("test button clicked") }
//        panel.add(button)
//        add(panel, GridBagConstraints().apply {gridx = 0; gridy = 1; gridwidth = 4 })

        add(mainPanel)
        isVisible = true
    }

    private fun initializeFrame(){
        setSize(600, 600)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

    }

    private fun addUserCredsSection(){
        val userCredsPanel = JPanel()
        userCredsPanel.layout = GridLayout(2, 2,20,0)

        // Section header
        val userCredsHeader = JLabel("User Info")
        userCredsHeader.labelFor = userCredsPanel
        userCredsHeader.font = Font("Sans Serif", Font.BOLD, 20)
        userCredsHeader.alignmentX = Component.CENTER_ALIGNMENT
        mainPanel.add(userCredsHeader)

        // Text Fields
        userCredsPanel.add(getFieldLayoutPanel("Username",usernameField))
        userCredsPanel.add(getFieldLayoutPanel("Password",passwordField))
        userCredsPanel.add(getFieldLayoutPanel("API Client ID",apiClientIdField))
        userCredsPanel.add(getFieldLayoutPanel("API Client Secret",apiClientSecretField))
        userCredsPanel.maximumSize = Dimension(400, 100)
        userCredsPanel.alignmentX = Component.CENTER_ALIGNMENT
        mainPanel.add(userCredsPanel)



        // Save Load Buttons Panel
        val saveLoadPanel = JPanel()
        saveLoadPanel.layout = BoxLayout(saveLoadPanel, BoxLayout.X_AXIS)
        saveLoadPanel.alignmentY = Component.CENTER_ALIGNMENT
        saveLoadPanel.alignmentX = Component.CENTER_ALIGNMENT
        //todo button logic
        val saveButton = JButton("Save to File")
        saveButton.alignmentX = Component.CENTER_ALIGNMENT
        saveButton.alignmentY = Component.CENTER_ALIGNMENT
        val loadButton = JButton("Load from File")
        loadButton.alignmentX = Component.CENTER_ALIGNMENT
        loadButton.alignmentY = Component.CENTER_ALIGNMENT
        val filler = Box.Filler(
            Dimension(20,100),
            Dimension(50,100),
            Dimension(100,100))
        filler.alignmentX = Component.CENTER_ALIGNMENT
        filler.alignmentY = Component.CENTER_ALIGNMENT
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

    private fun addSettingsSection(){
        val settingsPanel = JPanel()
        settingsPanel.layout = BoxLayout(settingsPanel, BoxLayout.X_AXIS)
        settingsPanel.alignmentX = Component.CENTER_ALIGNMENT
        settingsPanel.alignmentY = Component.TOP_ALIGNMENT

        mainPanel.add(settingsPanel)
    }
}
