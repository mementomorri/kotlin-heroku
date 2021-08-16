package me.mementomorri.model.main_classes

import kotlinx.serialization.Serializable
import model.main_classes.ObjectiveDifficulty
import model.main_classes.ObjectiveType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.DefaultIdTable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
class Objective(
    val id: Int,
    var name: String,
    var description: String,
    val type: String,
    val adventurerId: Int,
    var difficulty: String = "MEDIUM",
    var deadline: String?= null,
    val startDate: String?= null,
    var completionCount: Int?= 0,
){

    fun checkDeadline():Boolean{ //true if there is time left, false otherwise
        return LocalDate.now()<LocalDate.parse(deadline.toString(), DateTimeFormatter.ISO_DATE)
    }


    override fun toString(): String {
        return "Objective with: name=${name}, difficulty=${difficulty},type=${type}, characterId=${adventurerId}, completionCount=${completionCount}"
    }
}

fun Objective.getDifficulty(): ObjectiveDifficulty {
    return ObjectiveDifficulty.valueOf(this.difficulty.uppercase())
}

fun Objective.getType(): ObjectiveType {
    return ObjectiveType.valueOf(this.type.uppercase())
}

class ObjectiveTable(): DefaultIdTable<Objective>(){
    val name= varchar("name", 50)
    val description= varchar("description", 510)
    val difficulty= varchar("difficulty", 10)
    val type= varchar("type", 50)
    val adventurerId= reference("adventurerId", adventurerTable)
    val deadline= varchar("deadline",10).nullable()
    val startDate= varchar("startDate",10).nullable()
    val completionCount= integer("completionCount").nullable()

    override fun fill (builder: UpdateBuilder<Int>, item: Objective) {
        builder[name] = item.name
        builder[description] = item.description
        builder[difficulty] = item.difficulty
        builder[type] = item.type
        builder[adventurerId] = item.adventurerId
        builder[deadline] = item.deadline
        builder[startDate] = item.startDate
        builder[completionCount] = item.completionCount
    }

    override fun readResult(result: ResultRow): Objective =
        Objective(
            result[id].value,
            result[name],
            result[description],
            result[type],
            result[adventurerId].value,
            result[difficulty],
            result[deadline],
            result[startDate],
            result[completionCount]
        )
}

val objectiveTable= ObjectiveTable()
