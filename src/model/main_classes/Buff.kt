package model.main_classes

import kotlinx.serialization.Serializable
import me.mementomorri.model.main_classes.adventurerTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
open class Buff(
        val name:String,
//        @kotlinx.serialization.ExperimentalSerializationApi
//        @Serializable(with=DateSerializer::class)
        val duration: String = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE),
        val adventurer_id: Int
)

class BuffTable: Table(){
        val name = varchar("name", 50)
        val duration= varchar("duration", 10)
        val adventurer_id= reference("adventurer_id", adventurerTable)
        fun fill(builder: UpdateBuilder<Int>, item: Buff) {
                builder[name] = item.name
                builder[duration] = item.duration
                builder[adventurer_id] = item.adventurer_id
        }

        fun readResult(result: ResultRow): Buff =
                Buff(
                        result[name],
                        result[duration],
                        result[adventurer_id].value
                )
}

val buffTable= BuffTable()
