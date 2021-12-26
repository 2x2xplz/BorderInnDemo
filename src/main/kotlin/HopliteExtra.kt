import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.io.File


class ExternalFilePropertySource(
    // for external files not on the classpath
    private val filepath: String,
    private val optional: Boolean = false
) : PropertySource {

    // first we validate file exists
    override fun node(context: PropertySourceContext): ConfigResult<Node> {
        return if (!File(filepath).exists()) {
            if (optional) Undefined.valid()
                else ConfigFailure.UnknownSource(filepath).invalid()
        } else {
            File(filepath).let { configFile ->
                context.parsers.locate(configFile.extension).map {
                    it.load(configFile.inputStream(), filepath)
                }
            }
        }
    }
}