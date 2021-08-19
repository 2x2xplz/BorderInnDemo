import com.sksamuel.hoplite.ConfigFilePropertySource
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.json.JsonPropertySource
import com.zaxxer.hikari.HikariDataSource
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.io.File


// Hoplite setup
data class GeoAPI(val apiKey: String, val host: String = "https://api.openrouteservice.org")
data class DBConfig(val url: String = "jdbc:h2:mem:", val username: String, val password: String)
data class Destination(val name: String, val lat: Float, val lon: Float, val timeZone: String)
data class AppConfig(val portNumber: Int = System.getenv("PORT")?.toInt() ?: 0,
                     val platform: String = "dev",
                     val destination: Destination, val db: DBConfig, val geo: GeoAPI)
val config: AppConfig = ConfigLoader.Builder()
    .addSource(JsonPropertySource(System.getenv("HOPLITE_JSON") ?: System.getProperty("HOPLITE_JSON") ?: "{}"))
    .apply {
        System.getenv("HOPLITE_FILENAME") ?: System.getProperty("HOPLITE_FILENAME")?.let { configFile ->
            if (!File(configFile).exists())
                throw Exception("specified config file ($configFile) doesn't exist")
            this.addPropertySource(
                ConfigFilePropertySource(
                    ConfigSource.FileSource(File(configFile)), optional = true))
        }
    }
    .addSource(PropertySource.resource("/config_dev.yaml", optional = true))
    .build()
    .loadConfigOrThrow()


// Hikari is our database connection pool
object connHikari : HikariDataSource() {
    init {
        this.jdbcUrl = config.db.url
        this.username = config.db.username
        this.password = config.db.password
        this.maximumPoolSize = 4
        this.driverClassName = "org.h2.Driver" // pg driver is automatically registered
    }
}


// 'Database' is a ktorm object
val pg: Database = Database.connect(connHikari, dialect = PostgreSqlDialect())


// provided by http4k - will be used by AWS Lambda
@Suppress("unused")
class GatewayListener : ApiGatewayV2LambdaFunction(appRoutes)


fun main() {
    println("starting app...")
    serverApp.start()
    println("app has started on port ${config.portNumber} at ${config.destination.name}")
}