package model.challenges

import kotlinx.serialization.Serializable
import me.mementomorri.model.main_classes.Adventurer
import model.main_classes.Reward
import model.main_classes.adventurersRepo
import model.main_classes.objectivesRepo
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder

@Serializable
open class Challenge (
    val name: String,
    val description: String
) {
    open val rewards: Reward = Reward(0, 20, null)

    open fun checkChallengeCondition(adventurer: Adventurer): Boolean=true //true if condition passed, false otherwise

    open fun getReward(adventurer: Adventurer){
        if (checkChallengeCondition(adventurer)) {
            rewards.getReward(adventurer)
        }
    }
}

fun Challenge.checkShowingTheAttitudeCond(adventurerID: Int) :Boolean{
    val character= adventurersRepo.read(adventurerID)
    return if (character == null) false else {
        objectivesRepo.read().filter {it.adventurerId == adventurerID && it.type=="DAILY" }.firstOrNull { it.completionCount!! >= 5 } != null
    }
}

class ChallengeTable: Table(){
    val name = varchar("name", 50)
    val description= varchar("description", 255)
    fun fill(builder: UpdateBuilder<Int>, item: Challenge) {
        builder[name] = item.name
        builder[description] = item.description
    }

     fun readResult(result: ResultRow): Challenge? =
             Challenge(
                    result[name],
                    result[description]
            )
}

val challenges= ChallengeTable()