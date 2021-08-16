package me.mementomorri.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.mementomorri.model.main_classes.Objective
import me.mementomorri.model.main_classes.objectiveTable
import model.items.Coffee
import model.items.GreenTea
import model.items.HealingPotion
import model.items.Item
import model.main_classes.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Repo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Application.objectivesRest(
    repo: Repo<Objective> = objectivesRepo,
    path:String= "/objective",
    objectiveSerializer: KSerializer<Objective> = Objective.serializer(),
) {
    routing {
        route(path) {
            get {
                call.respond(repo.read())
                HttpStatusCode.OK
            }
            post {
                call.respond(
                    parseObjectiveBody(objectiveSerializer)?.let { elem ->
                        if (repo.create(elem))
                            HttpStatusCode.Created
                        else
                            HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/byadventurer/{adventurerId}") {
            get {
                call.respond(
                    parseAdventurerId()?.let { id ->
                        getObjectivesByCharacterID(id)
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{objectiveId}") {
            get {
                call.respond(
                    parseObjectiveId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            elem
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                    parseObjectiveId()?.let { i: Int ->
                        if (repo.delete(i))
                            HttpStatusCode.OK
                        else
                            HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{objectiveId}/{adventurerId}/complete"){
            get{
                call.respond(
                    parseObjectiveId()?.let { obj_id ->
                        parseAdventurerId()?.let { adv_id ->
                            if (completeObjective(adv_id, obj_id))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.Conflict
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.NotFound
                )
            }
        }
    }
}


fun PipelineContext<Unit, ApplicationCall>.parseAdventurerId(id: String = "adventurerId") =
    call.parameters[id]?.toIntOrNull()

fun PipelineContext<Unit, ApplicationCall>.parseObjectiveId(id: String = "objectiveId") =
    call.parameters[id]?.toIntOrNull()

suspend fun PipelineContext<Unit, ApplicationCall>.parseObjectiveBody(
    serializer: KSerializer<Objective>
) =
    try {
        Json.decodeFromString(
            serializer,
            call.receive()
        )
    } catch (e: Throwable) {
        null
    }

fun getObjectivesByCharacterID(ID: Int): List<Objective> {
    val result = mutableListOf<Objective>()
    transaction {
        objectiveTable.selectAll().mapNotNull { objectiveTable.readResult(it) }
    }.forEach { objective ->
        if (objective.adventurerId == ID) result
            .add(Objective(objective.id,objective.name, objective.description, objective.type,
                objective.adventurerId, objective.difficulty, objective.deadline,objective.startDate,objective.completionCount))
    }
    return result.toList()
}

fun completeObjective(adventurerID: Int, objectiveID: Int):Boolean {
    //completes task depends on its type, raise completion count of habit, removes quest on complete and gives reward
    val adventurer = adventurersRepo.read(adventurerID)
    val objective = objectivesRepo.read(objectiveID)
    if (adventurer == null || objective == null) {
        return false
    } else {
        if (adventurer.buffs.isNotEmpty()) transaction {
            buffTable.deleteWhere { buffTable.duration less LocalDate.now() }
        }
        val isGreedy = adventurer.buffs.firstOrNull { it.name == "Greedy" }
        when (ObjectiveType.valueOf(objective.type)) {
            ObjectiveType.HABIT ->{
                try {
                    val updateCount = objective.completionCount?.plus(1)
                    objective.completionCount?.plus(1)
                    transaction {
                        objectiveTable.update({
                            (objectiveTable.adventurerId eq adventurer.id) and (objectiveTable.name eq objective.name)
                        }) {
                            fill(
                                it,
                                Objective(
                                    objective.id,
                                    objective.name,
                                    objective.description,
                                    objective.type,
                                    objective.adventurerId,
                                    objective.difficulty,
                                    objective.deadline,
                                    objective.startDate,
                                    updateCount
                                )
                            )
                        } > 0
                    }
                    return if (isGreedy == null) {
                        Reward(
                            2 * objectiveDifficultyToInt(objective),
                            2 * objectiveDifficultyToInt(objective),
                            null
                        ).getReward(adventurer)
                        adventurersRepo.update(adventurerID, adventurer)
                        true
                    } else {
                        Reward(
                            2 * objectiveDifficultyToInt(objective),
                            2 * objectiveDifficultyToInt(objective),
                            null
                        ).getGreedyReward(adventurer)
                        adventurersRepo.update(adventurerID, adventurer)
                        true
                    }
                } catch (e: Exception) {
                    throw Exception("can't get Habit with taskName:${objective.name} while trying to complete it", e)
                }
            }
//            ObjectiveType.DAILY ->{
//                try {
//                    if (objective.checkDeadline()) {
//                        objective.completionCount = objective.completionCount?.plus(1)
//                        if (isGreedy == null) {
//                            Reward(
//                                2 * objectiveDifficultyToInt(objective),
//                                2 * objectiveDifficultyToInt(objective),
//                                null
//                            ).getReward(adventurer)
//                        } else {
//                            Reward(
//                                2 * objectiveDifficultyToInt(objective),
//                                2 * objectiveDifficultyToInt(objective),
//                                null
//                            ).getGreedyReward(adventurer)
//                        }
//                        LocalDate.parse(objective.deadline.toString(), DateTimeFormatter.ISO_DATE)?.plusDays(1)
//                        transaction {
//                            objectiveTable.update({
//                                (objectiveTable.adventurerId eq adventurer.id) and (objectiveTable.name eq objective.name)
//                            }) {
//                                fill(it, objective)
//                            } > 0
//                        }
//                        adventurersRepo.update(adventurerID, adventurer)
//                        return true
//                    } else {
//                        if (adventurer.buffs.firstOrNull {
//                                it.name == "Friendly protection"
//                                        || it.name == "Shield protection"
//                            } == null) {
//                            adventurer.healthPoints.minus(objectiveDifficultyToInt(objective) * 3)
//                            if (objectiveDifficultyToInt(objective) == 4
//                                || objectiveDifficultyToInt(objective) == 5
//                            ) adventurer.experiencePoints.minus(objectiveDifficultyToInt(objective))
//                        }
//                        adventurersRepo.update(adventurerID, adventurer)
//                        return true
//                    }
//                } catch (e: Exception) {
//                    throw Exception("can't get Daily with taskName:${objective.name} while trying to complete it", e)
//                }
//            }
            ObjectiveType.TODO ->{
                try {
                    if (objective.checkDeadline()) {
                        if (isGreedy == null) {
                            Reward(
                                (2.5 * objectiveDifficultyToInt(objective)).toInt(),
                                (2.5 * objectiveDifficultyToInt(objective)).toInt(),
                                null
                            ).getReward(adventurer)
                        } else {
                            Reward(
                                (2.5 * objectiveDifficultyToInt(objective)).toInt(),
                                (2.5 * objectiveDifficultyToInt(objective)).toInt(),
                                null
                            ).getGreedyReward(adventurer)
                        }
                        objectivesRepo.delete(objective.id)
                        adventurersRepo.update(adventurerID, adventurer)
                        return true
                    } else {
                        if (adventurer.buffs.firstOrNull {
                                it.name == "Friendly protection"
                                        || it.name == "Shield protection"
                            } == null) {
                            adventurer.healthPoints.minus((objectiveDifficultyToInt(objective) * 3.5).toInt())
                            if (objectiveDifficultyToInt(objective) == 4
                                || objectiveDifficultyToInt(objective) == 5
                            ) adventurer.experiencePoints.minus(objectiveDifficultyToInt(objective))
                            objectivesRepo.delete(objective.id)
                        }
                        adventurersRepo.update(adventurerID, adventurer)
                        return true
                    }
                } catch (e: Exception) {
                    throw Exception("can't get ToDo with taskName:${objective.name} while trying to complete it", e)
                }
            }
            ObjectiveType.QUEST -> {
                try {
                    val reward = when (ObjectiveDifficulty.valueOf(objective.difficulty)) {
                        ObjectiveDifficulty.MEDIUM -> listOf<Item>(HealingPotion(2))
                        ObjectiveDifficulty.HARD -> listOf(HealingPotion(2), GreenTea(2))
                        ObjectiveDifficulty.VERYHARD -> listOf(HealingPotion(2), GreenTea(2), Coffee(2))
                        else -> listOf(GreenTea(1))
                    }
                    if (isGreedy == null) {
                        Reward(
                            3 * objectiveDifficultyToInt(objective),
                            3 * objectiveDifficultyToInt(objective),
                            reward
                        ).getReward(adventurer)
                    } else {
                        Reward(
                            3 * objectiveDifficultyToInt(objective),
                            3 * objectiveDifficultyToInt(objective),
                            reward
                        ).getGreedyReward(adventurer)
                    }
                    objectivesRepo.delete(objective.id)
                    adventurersRepo.update(adventurerID, adventurer)
                    return true
                } catch (e: Exception) {
                    throw Exception("can't get Quest with taskName:${objective.name} while trying to complete it", e)
                }
            }
        }
    }
}

private fun objectiveDifficultyToInt(objective: Objective): Int {
    return when (ObjectiveDifficulty.valueOf(objective.difficulty)) {
        ObjectiveDifficulty.VERYEASY -> 1
        ObjectiveDifficulty.EASY -> 2
        ObjectiveDifficulty.MEDIUM -> 3
        ObjectiveDifficulty.HARD -> 4
        ObjectiveDifficulty.VERYHARD -> 5
    }
}