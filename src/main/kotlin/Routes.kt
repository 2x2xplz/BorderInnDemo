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


val appRoutes : RoutingHttpHandler = routes(

    // app will accept the user's starting point either as a path segment or as a query param
    "/from/{start}" bind Method.GET to { request -> pathPlace(request).let { Response(Status.OK).body(directionsHandler(it)) } },
    "/from" bind Method.GET to { request -> queryPlace(request).let { Response(Status.OK).body(directionsHandler(it)) } },

    "/report" bind Method.GET to { request ->
        val queryResult = queryDaysBack(request).let { if (it.isNullOrBlank()) getMostCommon() else getMostCommon(it.toLong()) }
        Response(Status.OK).body(queryResult)
    },

    "/" bind Method.GET to { Response(Status.OK).body("app root served on port ${config.portNumber}") }
)


val serverApp = appRoutes.asServer(Undertow(config.portNumber))

