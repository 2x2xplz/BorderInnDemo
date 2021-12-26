
import com.sksamuel.hoplite.ConfigFilePropertySource
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.json.JsonPropertySource
import java.io.File


// Hoplite setup
data class SampleConfig(val portNumber: Int, val platform: String)


val myConfig: SampleConfig = ConfigLoader.Builder()
    // next line was copied from https://github.com/sksamuel/hoplite/issues/161#issuecomment-677468140 but doesn't compile
    //.addPropertySource(ConfigFilePropertySource(ConfigSource.FileSource("/home/sam/foo")))
    // next line fixes non-compilation, but throws error
    .addPropertySource(ConfigFilePropertySource(ConfigSource.FileSource(File("xyz.yaml")), optional = true))
    //.addSource(PropertySource.file(File("e:/config_test.yaml"), optional = true))

    .addSource(JsonPropertySource(""" { "portNumber": 80, "platform": "dev-default" } """))
    .build()
    .loadConfigOrThrow()



fun main() {
    println("app has started on port ${myConfig.portNumber} at ${myConfig.platform}")
}