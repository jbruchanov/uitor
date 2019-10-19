import com.scurab.uitor.web.ServerApi
import com.scurab.uitor.web.inspector.LayoutInspectorPage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    GlobalScope.launch {
        val config = ServerApi().loadClientConfiguration()
        LayoutInspectorPage(config).onStart()
    }
}