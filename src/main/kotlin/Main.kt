import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.json.JsonPropertySource
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.ktorm.database.Database
import java.io.File


// Hoplite configuration setup
data class GeoAPI(val apiKey: String, val host: String = "https://api.openrouteservice.org")
data class DBConfig(val url: String = "jdbc:h2:mem:", val username: String, val password: String)
data class Destination(val name: String, val lat: Float, val lon: Float, val timeZone: String)
data class AppConfig(val portNumber: Int = System.getenv("PORT")?.toInt() ?: 0,
                     val platform: String = "dev",
                     val destination: Destination, val db: DBConfig, val geo: GeoAPI)


val config: AppConfig = ConfigLoader.Builder()
    // try loading a config JSON string directly
    .addSource(JsonPropertySource(System.getenv("HOPLITE_JSON") ?: System.getProperty("HOPLITE_JSON") ?: "{}"))
    // try a config file
    .addSource(PropertySource.file(File(System.getenv("HOPLITE_FILENAME") ?: System.getProperty("HOPLITE_FILENAME") ?: ""), optional = true))
    // fallback to dev config (should not load in production)
    .addSource(PropertySource.resource("/config_dev.yaml", optional = true))
    .build() //.also { println("HOPLITE_JSON : ${System.getProperty("HOPLITE_JSON")}, HOPLITE_FILENAME : ${System.getProperty("HOPLITE_FILENAME")}") }
    .loadConfigOrThrow()


// normally would use Hikari conn pool...
//   but this is a demo project, with limited resources and low connection limits!
val pg: Database = Database.connect(
    url = config.db.url,
    driver = "org.postgresql.Driver",
    user = config.db.username,
    password = config.db.password
)


// provided by http4k - will be used by AWS Lambda
@Suppress("unused")
class GatewayListener : ApiGatewayV2LambdaFunction(appRoutes)


fun main() {
    println("starting app...")
    serverApp.start()
    println("app has started on port ${config.portNumber} at ${config.destination.name}")
}