package model.main_classes

import me.mementomorri.model.main_classes.adventurerTable
import me.mementomorri.model.main_classes.objectiveTable
import model.abilities.abilityTable
import model.items.itemTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import repo.AppRepo

val shopRepo= AppRepo(itemTable)
val abilitiesRepo= AppRepo(abilityTable)
val adventurersRepo= AppRepo(adventurerTable)
val objectivesRepo= AppRepo(objectiveTable)

class UserCharacterFiller(
        val userId: Int,
        val characterId: Int,
        val id:Int = -1
)

class UserCharacterTable: IntIdTable(){
    val userId= reference("userId", userTable)
    val adventurerId= reference("adventurerId", adventurerTable)

    fun fill (builder: UpdateBuilder<Int>, item: UserCharacterFiller){
        builder[userId] = item.userId
        builder[adventurerId] = item.characterId
    }

    fun readResult(result: ResultRow): UserCharacterFiller? =
            UserCharacterFiller(
                    result[userId].value,
                    result[adventurerId].value,
                    result[id].value
            )
    fun addPossession(userId: Int, characterId: Int)= transaction{
        userAdventurerTable.insertAndGetId { userAdventurerTable.fill(it, UserCharacterFiller(userId, characterId)) }.value
        true
    }
}

val userAdventurerTable= UserCharacterTable()