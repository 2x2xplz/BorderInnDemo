import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


// these data classes represent the JSON data responses we will get back from the OpenRouteService API
//   docs at https://openrouteservice.org/dev/#/api-docs


@Serializable
data class GeoProps (val postalcode: String = "00000", val region_a: String = "XX") // add some defaults, since the API doesn't provide every detail for every location
@Serializable
data class Geometry (val type: String, val coordinates: List<Float>)
@Serializable
data class GeoPlace (val type: String, val geometry: Geometry, val properties: GeoProps, val bbox: List<Float> = listOf())
@Serializable
data class GeocodeResponse (val geocoding: JsonObject, val type: String, val features: List<GeoPlace>, val bbox: List<Float>)


@Serializable
data class DirStep(val distance: Float, val duration: Float, val type: Int, val instruction: String, val name: String, val way_points: List<Int>)
@Serializable
data class DirSegments(val distance: Float, val duration: Float, val steps: List<DirStep>)
@Serializable
data class DirSummary(val distance: Float, val duration: Float)
@Serializable
data class DirProperties(val segments: List<DirSegments>, val summary: DirSummary, val way_points: List<Int>)
@Serializable
data class DirFeatures (val bbox: List<Float> = listOf(), val type: String, val properties: DirProperties, val geometry: JsonObject)
@Serializable
data class DirectionsResponse (val type: String, val features: List<DirFeatures>, val bbox: List<Float>, val metadata: JsonObject)

