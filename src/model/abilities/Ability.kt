package model.abilities

import kotlinx.serialization.Serializable
import me.mementomorri.model.main_classes.adventurerTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.DefaultIdTable

@Serializable
open class Ability(
    val name: String,
    val adventurerClass: String,
    val description: String,
    val energyRequired: Int,
    val levelRequired: Int,
    val id: Int = -1
){
    override fun toString(): String {
        return "name=${name}, adventurerClass=${adventurerClass}, energyRequired=${energyRequired}, levelRequired=${levelRequired}, id=${id}"
    }
}

class AbilityTable: DefaultIdTable<Ability>(){
    val name= varchar("name", 50)
    val adventurerClass=  varchar("adventurerClass",50)
    val description= varchar("description", 255)
    val energyRequired= integer("energyRequired")
    val levelRequired= integer("levelRequired")
    override fun fill(builder: UpdateBuilder<Int>, item: Ability) {
        builder[name]= item.name
        builder[adventurerClass]= item.adventurerClass
        builder[description]= item.description
        builder[energyRequired]= item.energyRequired
        builder[levelRequired]= item.levelRequired
    }

    override fun readResult(result: ResultRow): Ability?=
            Ability(
                    result[name],
                    result[adventurerClass],
                    result[description],
                    result[energyRequired],
                    result[levelRequired],
                    result[id].value
            )
}

val abilityTable= AbilityTable()

class AdventurerAbilityFiller(
    val ability_id: Int,
    val adventurer_id: Int,
    val id: Int= -1
){
    override fun toString(): String {
        return "id=${id}, ability_id=${ability_id}, character_id=${adventurer_id}"
    }
}

class AdventurerAbilityTable: DefaultIdTable<AdventurerAbilityFiller>(){
    val ability_id= reference("ability_id", abilityTable)
    val adventurer_id= reference("adventurer_id", adventurerTable)

    override fun fill(builder: UpdateBuilder<Int>, item: AdventurerAbilityFiller) {
        builder[ability_id]= item.ability_id
        builder[adventurer_id]= item.adventurer_id
    }

    override fun readResult(result: ResultRow)=
            AdventurerAbilityFiller(
                    result[ability_id].value,
                    result[adventurer_id].value,
                    result[id].value
            )
}

val adventurerAbilityTable= AdventurerAbilityTable()