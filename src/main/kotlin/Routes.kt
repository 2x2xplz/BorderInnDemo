import org.http4k.core.*
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.string
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.format.KotlinxSerialization.auto


// all the http4k Lenses for extracting search parameters and de-serialization of API responses
val pathPlace = Path.string().of("start")
val queryPlace = Query.required("start")
val queryDaysBack = Query.optional("days")
val geocodeLens : BiDiBodyLens<GeocodeResponse> = Body.auto<GeocodeResponse>().toLens()
val directionsLens : BiDiBodyLens<DirectionsResponse> = Body.auto<DirectionsResponse>().toLens()


val rootMessage : String = """
    <p>App root served on port ${config.portNumber}</p>
    <p>Usage: <code>[rootURL]/from/[city]</code> or <code>[rootURL]/from?start=[city]</code></p>
    <p>This is a demo app for learning purposes. It relies upon turn-by-turn directions provided by <a href="https://api.openrouteservice.org">api.openrouteservice.org</a>, which is a free service and is often offline or overloaded. Please be patient and try again later if your query is not working. Thanks to openrouteservice for providing this API.</p>
    <p>The Border Inn, Baker, NV<br />
        <img src="https://borderinncasino.com/wp-content/uploads/2018/06/the-border-inn-4.jpg">
    </p>
""".trimIndent()

val appRoutes : RoutingHttpHandler = routes(

    // app will accept the user's starting point either as a path segment or as a query param
    "/from/{start}" bind Method.GET to { request -> pathPlace(request).let { Response(Status.OK).body(directionsHandler(it)) } },
    "/from" bind Method.GET to { request -> queryPlace(request).let { Response(Status.OK).body(directionsHandler(it)) } },

    "/report" bind Method.GET to { request ->
        val queryResult = queryDaysBack(request).let { if (it.isNullOrBlank()) getMostCommon() else getMostCommon(it.toLong()) }
        Response(Status.OK).body(queryResult)
    },

    "/" bind Method.GET to { Response(Status.OK).body(rootMessage) }
)

val serverApp = appRoutes.asServer(Undertow(config.portNumber))


