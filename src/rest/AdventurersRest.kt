package me.mementomorri.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.mementomorri.model.main_classes.Adventurer
import me.mementomorri.model.main_classes.Objective
import model.abilities.Ability
import model.abilities.AdventurerAbilityFiller
import model.abilities.adventurerAbilityTable
import model.items.Item
import model.main_classes.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Repo
import rest.*

fun Application.adventurersRest(
        repo: Repo<Adventurer> = adventurersRepo,
        path:String= "/adventurers",
        adventurerSerializer: KSerializer<Adventurer> = Adventurer.serializer(),
        itemSerializer: KSerializer<Item> = Item.serializer(),
//         objectiveSerializer: KSerializer<Objective> = Objective.serializer(),
        abilitySerializer: KSerializer<Ability> = Ability.serializer(),
        buffSerializer: KSerializer<Buff> = Buff.serializer()
){
    routing {
        route(path){
            get{
                call.respond(repo.read())
                HttpStatusCode.OK
            }
            post {
                call.respond(
                    parseAdventurerBody(adventurerSerializer)?.let { elem ->
                        if (repo.create(elem))
                            HttpStatusCode.Created
                        else
                            HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{adventurerId}"){
            get {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            elem
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            put {
                call.respond(
                    parseAdventurerBody(adventurerSerializer)?.let { elem ->
                        parseAdventurerId()?.let { id ->
                            if(repo.update(id, elem))
                                HttpStatusCode.Accepted
                            else
                                HttpStatusCode.NotFound
                        }
                    }?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                    parseAdventurerId()?.let { i: Int ->
                        if (repo.delete(i))
                            HttpStatusCode.OK
                        else
                            HttpStatusCode.NotFound
                    }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{adventurerId}/inventory"){
            get {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        repo.read(id)?.let { adventurer ->
                            adventurer.inventory
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            post {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        repo.read(id)?.let { adventurer ->
                            parseItemBody(itemSerializer)?.let { item ->
                                if (adventurer.addItemToInventory(item))
                                    HttpStatusCode.Created
                                else
                                    HttpStatusCode.NotFound
                            }?: HttpStatusCode.BadRequest
                        }?: HttpStatusCode.NotFound
                    }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{adventurerId}/inventory/{itemId}"){
            delete {
                call.respond(
                    parseAdventurerId()?.let { adventurerId ->
                        repo.read(adventurerId)?.let { adventurer ->
                            parseItemId()?.let { itemId ->
                                if (adventurer.removeItemFromInventory(itemId))
                                    HttpStatusCode.OK
                                else
                                    HttpStatusCode.NotFound
                            }?: HttpStatusCode.BadRequest
                        }?: HttpStatusCode.NotFound
                    }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{adventurerId}/abilities"){
            get {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        repo.read(id)?.let { adventurer ->
                            adventurer.abilities
                        }?: HttpStatusCode.NotFound
                    }?: HttpStatusCode.BadRequest
                )
            }
            post {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        parseAbilityBody(abilitySerializer)?.let{ ability ->
                            if (addAbility(id, ability))
                                HttpStatusCode.Created
                            else
                                HttpStatusCode.NotFound
                        }?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{adventurerId}/buffs"){
            get {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        repo.read(id)?.let { adventurer ->
                            adventurer.buffs
                        }?: HttpStatusCode.NotFound
                    }?: HttpStatusCode.BadRequest
                )
            }
            post {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        parseBuffBody(buffSerializer)?.let { buff ->
                            if (addBuff(id, buff))
                                HttpStatusCode.Created
                            else
                                HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{adventurerId}/buffs/{buffName}"){
            delete {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        parseBuffName()?.let { buffName ->
                            if (removeBuff(id, buffName))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        }?: HttpStatusCode.BadRequest
                    }?: HttpStatusCode.BadRequest
                )
            }
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.parseBuffName(id: String = "buffName") =
    call.parameters[id]

suspend fun PipelineContext<Unit, ApplicationCall>.parseAdventurerBody(
    serializer: KSerializer<Adventurer>
) =
    try {
        Json.decodeFromString(
            serializer,
            call.receive()
        )
    } catch (e: Throwable) {
        null
    }

suspend fun PipelineContext<Unit, ApplicationCall>.parseBuffBody(
    serializer: KSerializer<Buff>
) =
    try {
        Json.decodeFromString(
            serializer,
            call.receive()
        )
    } catch (e: Throwable) {
        null
    }

private fun addAbility(adventurerID: Int, ability: Ability): Boolean{
    val abId= abilitiesRepo.read().firstOrNull{ it.name == ability.name }
    return if (abId != null){
        transaction {
            adventurerAbilityTable.insert { fill(it, AdventurerAbilityFiller(abId.id, adventurerID)) }
            true
        }
    } else {
        false
    }
}

private fun addBuff(adventurerID: Int, buff: Buff): Boolean{
    transaction {
        buffTable.insert { fill(it, buff) }
    }
    return adventurersRepo.read(adventurerID)?.buffs?.firstOrNull { it.name == buff.name } != null
}

private fun removeBuff(characterId: Int, buffName:String): Boolean{
    return transaction {
        buffTable.deleteWhere { (buffTable.adventurer_id eq characterId) and(buffTable.name eq buffName) } > 0
    }
}
