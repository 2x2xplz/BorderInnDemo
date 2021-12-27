import org.http4k.client.OkHttp
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.ktorm.dsl.insert
import java.time.ZoneId
import java.time.ZonedDateTime


val ok : HttpHandler = OkHttp() // http4k client with OkHttp implementation

// for each search, we need to make 2 API calls -- to convert the place name to coordinates, then to get directions based on the coordinates
fun directionsHandler(startingPoint: String) : String {

    /* this is a demo project... no, we aren't doing any error checking or validation!  */

    // the Directions API only accepts coordinates, not place names, so we need to convert free text into coordinates using the Geocode API
    val geoResponse : Response = Request(Method.GET, "${config.geo.host}/geocode/search")
        .query("api_key", config.geo.apiKey)
        .query("text", startingPoint)
        .let { request -> ok(request) } //.also { println(it.bodyString()) }
    // the Lens (an http4k feature) automatically de-serializes the JSON response into the data classes we defined
    val geoData : GeocodeResponse = geocodeLens(geoResponse)
    val startingCoordinates : List<Float> = geoData.features[0].geometry.coordinates // .also { println(it) }

    val dirResponse : Response = Request(Method.GET, "${config.geo.host}/v2/directions/driving-car")
        .query("api_key", config.geo.apiKey)
        .query("start", startingCoordinates.joinToString(","))
        .query("end", "${config.destination.lon},${config.destination.lat}")
        .let { request -> ok(request) } //.also { println(request) }  //.also { println(it.bodyString()) }

    // again, we use the Lens to automatically de-serialize the response into data classes
    directionsLens(dirResponse).features[0].properties.segments[0].let { segment ->
        val distance : Double = segment.distance.times(0.62137).div(1000.0) // convert meters to miles
        val duration : Double = segment.duration.div(60.0) // convert seconds to minutes
        val steps : List<String> = segment.steps.map { step -> "${step.instruction} (${String.format("%.1f", step.distance.times(0.62137).div(1000.0))} mi.)" }
        return listOf<String>(
            "You are ${distance.toInt()} miles away from ${config.destination.name}!",
            "Follow the directions below and you'll be here in ${if (duration > 60) "${duration.toInt().div(60)} hours and " else ""}${duration.mod(60.0).toInt() } minutes.",
            "",
            "DIRECTIONS TO ${config.destination.name.uppercase()}:",
            *(steps.toTypedArray()) // spread operator '*' converts an array into its elements
        ).joinToString("\n").also {

            // let's save the search query in the database
            pg.insert(StartingPoints) { columns ->
                set(columns.postalCode, geoData.features[0].properties.postalcode)
                set(columns.stateAbbr, geoData.features[0].properties.region_a)
                set(columns.searchtime, ZonedDateTime.now(ZoneId.of(config.destination.timeZone)).toInstant())
            }

        }
    }
}