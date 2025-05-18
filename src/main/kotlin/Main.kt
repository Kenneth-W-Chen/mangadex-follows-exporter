import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import javax.swing.SwingUtilities

suspend fun main() {
    runBlocking(context = Dispatchers.Swing){
        SwingUtilities.invokeLater {
            val ui: ExporterUI = ExporterUI()
        }
    }
}
