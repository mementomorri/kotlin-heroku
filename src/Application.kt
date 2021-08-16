package me.mementomorri

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import com.google.gson.Gson
import io.ktor.jackson.*
import me.mementomorri.model.main_classes.Adventurer
import me.mementomorri.model.main_classes.Objective
import me.mementomorri.model.main_classes.adventurerTable
import me.mementomorri.model.main_classes.objectiveTable
import me.mementomorri.rest.adventurersRest
import me.mementomorri.rest.objectivesRest
import model.abilities.*
import model.challenges.challenges
import model.challenges.showingTheAttitude
import model.challenges.solidHabit
import model.items.*
import model.main_classes.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import rest.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        header(HttpHeaders.Origin)
        header(HttpHeaders.ContentType)
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        anyHost()
    }

    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    Database.connect(
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    initDB()
    abilityRest()
    challengeRest()
    shopRest()
    userRest()
    adventurersRest()
    objectivesRest()

    routing {
        get("/") {
            val resp= Gson().toJson("HELLO WORLD!")
            call.respondText(resp, contentType = ContentType.Application.Json)
            call.response.header("Access-Control-Allow-Origin", "*")
        }
        post("/") {
            val text:String= call.receive()
            if (text.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, "Text received")
                println(text)
            }else call.respond(HttpStatusCode.BadRequest)
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

private fun initDB() {
    transaction {
        SchemaUtils.create(userTable, userAdventurerTable, adventurerTable,
            buffTable, adventurerItemTable, adventurerAbilityTable, itemTable, abilityTable, challenges, objectiveTable)
    }
    userTable.addUser(User("Alice", "4l1c3"))
    userTable.addUser(User("Bob", "b0bb1e"))
    userTable.addUser(User("Charlie", "ch4rl13"))

    adventurersRepo.create(Adventurer("Alice", "MAGICIAN", 1))
    adventurersRepo.create(Adventurer("Bob", "WARRIOR", 2))
    adventurersRepo.create(Adventurer("Charlie", "ARCHER", 3))

    userAdventurerTable.addPossession(1, 1)
    userAdventurerTable.addPossession(2, 2)
    userAdventurerTable.addPossession(3, 3)

    shopRepo.create(HealingPotion(20))
    shopRepo.create(GreenTea(20))
    shopRepo.create(Coffee(20))
    shopRepo.create(DustRabbitsScroll(20))
    shopRepo.create(DishDisasterScroll(20))

    abilitiesRepo.create(burstsOfFlame)
    abilitiesRepo.create(cheeringWords)
    abilitiesRepo.create(frostChill)
    abilitiesRepo.create(friendlyProtection)
    abilitiesRepo.create(greedyProfit)
    abilitiesRepo.create(huntersFocus)
    abilitiesRepo.create(swordplayPractice)

    objectivesRepo.create(Objective(1, "Touch the beauty", "Touch Mosi's tail","HABIT", 1, "HARD",null,null,0))

    adventurersRepo.read().forEach { adventurer ->
        objectivesRepo.create(Objective(nextObjectiveId(),"Brush the teeth", "Brush the teeth every morning",
            "HABIT",adventurer.id,"VERYEASY",null,LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
        objectivesRepo.create(Objective(nextObjectiveId(),"Stretch at the morning", "Stretch at least 5 minutes a day", "HABIT",adventurer.id,"EASY",
            LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE),LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
        objectivesRepo.create(Objective(nextObjectiveId(),"Pet the cats", "Pet Mosi and Simi", "TODO", adventurer.id, "MEDIUM",
        LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ISO_DATE),LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
    }

    transaction {
        challenges.insert {
            fill(it, solidHabit)
        }
    }
    transaction {
        challenges.insert {
            fill(it, showingTheAttitude)
        }
    }
    println("Database default state initialized")
}

fun nextObjectiveId():Int{
    return objectivesRepo.read().size.plus(1)
}