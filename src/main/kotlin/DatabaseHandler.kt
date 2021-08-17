import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.time.Instant
import java.time.temporal.ChronoUnit


object StartingPoints: Table<Nothing>("startingpoints") {
    val postalCode : Column<String> = varchar("postalcode")
    val stateAbbr : Column<String> = varchar("state")
    val searchtime : Column<Instant> = timestamp("searchtime")
}

/*
    create table startingpoints (
        postalcode varchar(20),
        state varchar(20)
        searchtime timestamp with time zone
    )
*/

fun getMostCommon(daysBack: Long = 7) : String =
        pg.from(StartingPoints)
            .select(StartingPoints.stateAbbr, count(StartingPoints.searchtime).aliased("searches"))
            .groupBy(StartingPoints.stateAbbr)
            .where(StartingPoints.searchtime greater Instant.now().minus(daysBack, ChronoUnit.DAYS))
            .orderBy(count(StartingPoints.searchtime).desc())
            .map { row -> "state: ${row[StartingPoints.stateAbbr]}, searches: ${row.getInt(2)}" }
            .joinToString("\n")
